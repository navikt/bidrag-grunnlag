package no.nav.bidrag.grunnlag.consumer

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.timelimiter.TimeLimiterConfig
import io.github.resilience4j.timelimiter.TimeLimiterRegistry
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import no.nav.bidrag.grunnlag.TestUtil
import no.nav.bidrag.grunnlag.consumer.inntektskomponenten.InntektskomponentenConsumer
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.tjenester.aordningen.inntektsinformasjon.response.HentInntektListeResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestTemplate
import java.net.URI
import java.time.Duration
import java.util.concurrent.TimeoutException
import java.util.function.Function
import java.util.function.Supplier

@ExtendWith(MockKExtension::class)
@DisplayName("InntektskomponentenConsumerTest")
internal class InntektskomponentenConsumerTest {
    @MockK
    private lateinit var restTemplateMock: RestTemplate

    @MockK
    private lateinit var grunnlagConsumerMock: GrunnlagConsumer

    @MockK
    private lateinit var circuitBreakerFactoryMock: CircuitBreakerFactory<*, *>

    @MockK
    private lateinit var timeLimiterRegistry: TimeLimiterRegistry

    private lateinit var inntektskomponentenConsumer: InntektskomponentenConsumer
    private lateinit var realCircuitBreaker: CircuitBreaker
    private lateinit var circuitBreakerRegistry: CircuitBreakerRegistry

    @BeforeEach
    fun setup() {
        // Konfig for test av CircuitBreaker
        val circuitBreakerConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(50f)
            .slidingWindowSize(5)
            .minimumNumberOfCalls(2)
            .waitDurationInOpenState(Duration.ofMillis(1000))
            .build()

        // Mock Springs CircuitBreakerFactory slik at vi bruker Resilience4j internt
        every { circuitBreakerFactoryMock.create(any()) } returns
            object : org.springframework.cloud.client.circuitbreaker.CircuitBreaker {
                override fun <T : Any?> run(toRun: Supplier<T>, fallback: Function<Throwable, T>?): T = try {
                    realCircuitBreaker.executeSupplier(toRun)
                } catch (ex: Throwable) {
                    fallback?.apply(ex) ?: throw ex
                }

                override fun <T : Any?> run(toRun: Supplier<T>): T = realCircuitBreaker.executeSupplier(toRun)
            }

        circuitBreakerRegistry = CircuitBreakerRegistry.of(circuitBreakerConfig)
        realCircuitBreaker = circuitBreakerRegistry.circuitBreaker("inntektskomponenten-test")

        // Time limiter config
        val timeLimiterConfig = TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(1))
            .build()

        timeLimiterRegistry = TimeLimiterRegistry.of(timeLimiterConfig)

        inntektskomponentenConsumer = InntektskomponentenConsumer(
            URI("http://localhost"),
            restTemplateMock,
            grunnlagConsumerMock,
            circuitBreakerFactoryMock,
            timeLimiterRegistry,
        )
    }

    @Test
    fun `circuit breaker skal håndtere timeouts`() {
        val request = TestUtil.byggHentInntektListeRequest()
        val httpEntity = HttpEntity(request)

        every { grunnlagConsumerMock.initHttpEntityInntektskomponenten(request) } returns httpEntity
        every { grunnlagConsumerMock.logResponse(any(), any<RestResponse<HentInntektListeResponse>>()) } just runs
        every {
            restTemplateMock.exchange(
                any<String>(),
                HttpMethod.POST,
                httpEntity,
                HentInntektListeResponse::class.java,
            )
        } answers {
            Thread.sleep(2000)
            ResponseEntity(TestUtil.byggHentInntektListeResponse(), HttpStatus.OK)
        }

        val resultat = inntektskomponentenConsumer.hentInntekter(request, false)

        resultat.shouldBeInstanceOf<RestResponse.Failure<HentInntektListeResponse>>()
        resultat.message shouldContain "timeout"
        resultat.statusCode shouldBe HttpStatus.SERVICE_UNAVAILABLE
    }

    @Test
    fun `circuit breaker skal starte etter flere påfølgende feil`() {
        val request = TestUtil.byggHentInntektListeRequest()
        val httpEntity = HttpEntity(request)

        every { grunnlagConsumerMock.initHttpEntityInntektskomponenten(request) } returns httpEntity
        every { grunnlagConsumerMock.logResponse(any(), any<RestResponse<HentInntektListeResponse>>()) } just runs
        every {
            restTemplateMock.exchange(
                any<String>(),
                HttpMethod.POST,
                httpEntity,
                HentInntektListeResponse::class.java,
            )
        } throws ResourceAccessException("Connection failed")

        repeat(10) {
            inntektskomponentenConsumer.hentInntekter(request, false)
        }

        // Open betyr at circuitBreaker er påslått pga gjentagende feil
        realCircuitBreaker.state shouldBe CircuitBreaker.State.OPEN

        val resultat = inntektskomponentenConsumer.hentInntekter(request, false)
        resultat.shouldBeInstanceOf<RestResponse.Failure<HentInntektListeResponse>>()
        resultat.statusCode shouldBe HttpStatus.SERVICE_UNAVAILABLE
    }

    @Test
    fun `fallback skal kalles ved timeout exception`() {
        val request = TestUtil.byggHentInntektListeRequest()
        val httpEntity = HttpEntity(request)

        every { grunnlagConsumerMock.initHttpEntityInntektskomponenten(request) } returns httpEntity
        every { grunnlagConsumerMock.logResponse(any(), any<RestResponse<HentInntektListeResponse>>()) } just runs
        every {
            restTemplateMock.exchange(
                any<String>(),
                HttpMethod.POST,
                httpEntity,
                HentInntektListeResponse::class.java,
            )
        } throws TimeoutException("Kall timeout")

        val result = inntektskomponentenConsumer.hentInntekter(request, false)

        result.shouldBeInstanceOf<RestResponse.Failure<HentInntektListeResponse>>()
        result.message shouldContain "timeout"
        result.statusCode shouldBe HttpStatus.SERVICE_UNAVAILABLE
    }

    @Test
    fun `hentInntekter skal returnere ok respons`() {
        val request = TestUtil.byggHentInntektListeRequest()
        val response = TestUtil.byggHentInntektListeResponse()
        val httpEntity = HttpEntity(request)
        val responseEntity = ResponseEntity(response, HttpStatus.OK)

        every { grunnlagConsumerMock.initHttpEntityInntektskomponenten(request) } returns httpEntity
        every { grunnlagConsumerMock.logResponse(any(), any<RestResponse<HentInntektListeResponse>>()) } just runs
        every {
            restTemplateMock.exchange(
                "http://localhost/rs/api/v1/hentinntektliste",
                HttpMethod.POST,
                httpEntity,
                HentInntektListeResponse::class.java,
            )
        } returns responseEntity

        val restResponse = inntektskomponentenConsumer.hentInntekter(request = request, abonnerteInntekterRequest = false)

        restResponse.shouldBeInstanceOf<RestResponse.Success<HentInntektListeResponse>>()
        (restResponse as RestResponse.Success).body shouldBe response
    }

    @Test
    fun `hentInntekter skal håndtere exception`() {
        val request = TestUtil.byggHentInntektListeRequest()
        val httpEntity = HttpEntity(request)

        every { grunnlagConsumerMock.initHttpEntityInntektskomponenten(request) } returns httpEntity
        every { grunnlagConsumerMock.logResponse(any(), any<RestResponse<HentInntektListeResponse>>()) } just runs
        every {
            restTemplateMock.exchange(
                "http://localhost/rs/api/v1/hentinntektliste",
                HttpMethod.POST,
                httpEntity,
                HentInntektListeResponse::class.java,
            )
        } throws HttpClientErrorException(HttpStatus.BAD_REQUEST)

        val restResponse = inntektskomponentenConsumer.hentInntekter(request = request, abonnerteInntekterRequest = false)

        restResponse.shouldBeInstanceOf<RestResponse.Failure<HentInntektListeResponse>>()
        (restResponse as RestResponse.Failure).statusCode shouldBe HttpStatus.BAD_REQUEST
    }

/*
    @Test
    fun `test at TimeLimiter timer ut`() {
        // Simulate a long-running task
        val longRunningTask = CompletableFuture.supplyAsync {
            Thread.sleep(2000) // Sover to sekunder for å trigge timeout
            "resultat"
        }

        // Tester at TimeoutException kastes
        assertThrows(TimeoutException::class.java) {
            TimeLimiter.decorateFutureSupplier(timeLimiter) { longRunningTask }.call()[0]
        }
    }

    @Test
    fun `hentInntekter skal returnere ok respons`() {
        val request = TestUtil.byggHentInntektListeRequest()
        val response = TestUtil.byggHentInntektListeResponse()
        val httpEntity = HttpEntity(request)
        val responseEntity = ResponseEntity(response, HttpStatus.OK)

        every { grunnlagConsumerMock.initHttpEntityInntektskomponenten(request) } returns httpEntity

        every {
            grunnlagConsumerMock.logResponse(any(), any<RestResponse<HentInntektListeResponse>>())
        } just runs

        every {
            restTemplateMock.exchange(
                "http://localhost/rs/api/v1/hentinntektliste",
                HttpMethod.POST,
                httpEntity,
                HentInntektListeResponse::class.java,
            )
        } returns responseEntity

        // Consumer-kall
        val restResponse = inntektskomponentenConsumer.hentInntekter(request = request, abonnerteInntekterRequest = false)

        // Assertions
        restResponse is RestResponse.Success
        (restResponse as RestResponse.Success).body shouldBe response
    }

    @Test
    fun `hentInntekter skal håndtere exception`() {
        val request = TestUtil.byggHentInntektListeRequest()
        val httpEntity = HttpEntity(request)

        every { grunnlagConsumerMock.initHttpEntityInntektskomponenten(request) } returns httpEntity

        every {
            grunnlagConsumerMock.logResponse(any(), any<RestResponse<HentInntektListeResponse>>())
        } just runs

        every {
            restTemplateMock.exchange(
                "http://localhost/rs/api/v1/hentinntektliste",
                HttpMethod.POST,
                httpEntity,
                HentInntektListeResponse::class.java,
            )
        } throws HttpClientErrorException(HttpStatus.BAD_REQUEST)

        // Consumer-kall
        val restResponse = inntektskomponentenConsumer.hentInntekter(request = request, abonnerteInntekterRequest = false)

        // Assertions
        restResponse is RestResponse.Failure
        (restResponse as RestResponse.Failure).statusCode shouldBe HttpStatus.BAD_REQUEST
    }
*/
}
