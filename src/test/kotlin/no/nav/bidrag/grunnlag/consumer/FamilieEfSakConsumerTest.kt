package no.nav.bidrag.grunnlag.consumer

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.grunnlag.TestUtil
import no.nav.bidrag.grunnlag.consumer.familieefsak.FamilieEfSakConsumer
import no.nav.bidrag.grunnlag.consumer.familieefsak.api.BarnetilsynResponse
import no.nav.bidrag.grunnlag.consumer.familieefsak.api.Ressurs
import no.nav.bidrag.grunnlag.exception.RestResponse
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
internal class FamilieEfSakConsumerTest {

    companion object {
        private const val BARNETILSYN_CONTEXT = "/api/ekstern/bisys/perioder-barnetilsyn"
        private const val OVERGANGSSTØNAD_CONTEXT = "/api/ekstern/perioder/overgangsstonad/med-belop"
    }

    @InjectMocks
    private lateinit var familieEfSakConsumer: FamilieEfSakConsumer

    @Mock
    private lateinit var restTemplateMock: HttpHeaderRestTemplate

    @Test
    fun `Sjekk at ok respons fra Barnetilsyn-endepunkt mappes korrekt`() {
        val request = TestUtil.byggBarnetilsynRequest()

        Mockito.`when`(
            restTemplateMock.exchange(
                eq(BARNETILSYN_CONTEXT),
                eq(HttpMethod.POST),
                eq(initHttpEntity(request)),
                ArgumentMatchers.any<Class<BarnetilsynResponse>>()
            )
        )
            .thenReturn(ResponseEntity(TestUtil.byggBarnetilsynResponse(), HttpStatus.OK))

        when (val restResponseBarnetilsyn = familieEfSakConsumer.hentBarnetilsyn(request)) {
            is RestResponse.Success -> {
                val hentBarnetilsynResponse = restResponseBarnetilsyn.body
                assertAll(
                    { Assertions.assertThat(hentBarnetilsynResponse).isNotNull },
                    { Assertions.assertThat(hentBarnetilsynResponse.barnetilsynBisysPerioder.size).isEqualTo(1) },
                    { Assertions.assertThat(hentBarnetilsynResponse.barnetilsynBisysPerioder[0].periode.fom).isEqualTo(LocalDate.parse("2021-01-01")) },
                    { Assertions.assertThat(hentBarnetilsynResponse.barnetilsynBisysPerioder[0].periode.tom).isEqualTo(LocalDate.parse("2021-07-31")) },
                    { Assertions.assertThat(hentBarnetilsynResponse.barnetilsynBisysPerioder[0].barnIdenter[0]).isEqualTo("01012212345") },
                    { Assertions.assertThat(hentBarnetilsynResponse.barnetilsynBisysPerioder[0].barnIdenter[1]).isEqualTo("01011034543") }
                )
            }
            else -> {
                Assertions.fail("Test returnerte med RestResponse.Failure, som ikke var forventet")
            }
        }
    }

    @Test
    fun `Sjekk at ok respons fra Overgangsstønad-endepunkt mappes korrekt`() {
        val request = TestUtil.byggOvergangsstønadRequest()

        Mockito.`when`(
            restTemplateMock.exchange(
                eq(OVERGANGSSTØNAD_CONTEXT),
                eq(HttpMethod.POST),
                eq(initHttpEntity(request)),
                ArgumentMatchers.any<Class<Ressurs>>()
            )
        )
            .thenReturn(ResponseEntity(TestUtil.byggOvergangsstønadResponse(), HttpStatus.OK))

        when (val restResponseOvergangsstønad = familieEfSakConsumer.hentOvergangsstønad(request)) {
            is RestResponse.Success -> {
                val hentOvergangsstønadResponse = restResponseOvergangsstønad.body
                assertAll(
                    { Assertions.assertThat(hentOvergangsstønadResponse).isNotNull },
                    { Assertions.assertThat(hentOvergangsstønadResponse.data.perioder[0].personIdent).isEqualTo("12345678910") },
                    { Assertions.assertThat(hentOvergangsstønadResponse.data.perioder[0].fomDato).isEqualTo(LocalDate.parse("2020-01-01")) },
                    { Assertions.assertThat(hentOvergangsstønadResponse.data.perioder[0].tomDato).isEqualTo(LocalDate.parse("2020-12-31")) },
                    { Assertions.assertThat(hentOvergangsstønadResponse.data.perioder[0].beløp).isEqualTo(111) },
                    { Assertions.assertThat(hentOvergangsstønadResponse.data.perioder[0].datakilde).isEqualTo("Infotrygd") }
                )
            }
            else -> {
                Assertions.fail("Test returnerte med RestResponse.Failure, som ikke var forventet")
            }
        }
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `Sjekk at exception fra Barnetilsyn-endepunkt håndteres korrekt`() {
        val request = TestUtil.byggBarnetilsynRequest()

        Mockito.`when`(
            restTemplateMock.exchange(
                eq(BARNETILSYN_CONTEXT),
                eq(HttpMethod.POST),
                eq(initHttpEntity(request)),
                ArgumentMatchers.any<Class<BarnetilsynResponse>>()
            )
        )
            .thenThrow(HttpClientErrorException(HttpStatus.BAD_REQUEST))

        when (val restResponseBarnetilsyn = familieEfSakConsumer.hentBarnetilsyn(request)) {
            is RestResponse.Failure -> {
                assertAll(
                    { Assertions.assertThat(restResponseBarnetilsyn.statusCode).isEqualTo(HttpStatus.BAD_REQUEST) },
                    {
                        Assertions.assertThat(restResponseBarnetilsyn.restClientException)
                            .isInstanceOf(HttpClientErrorException::class.java)
                    }
                )
            }
            else -> {
                Assertions.fail("Test returnerte med RestResponse.Success, som ikke var forventet")
            }
        }
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `Sjekk at exception fra Overgangsstønad-endepunkt håndteres korrekt`() {
        val request = TestUtil.byggOvergangsstønadRequest()

        Mockito.`when`(
            restTemplateMock.exchange(
                eq(OVERGANGSSTØNAD_CONTEXT),
                eq(HttpMethod.POST),
                eq(initHttpEntity(request)),
                ArgumentMatchers.any<Class<Ressurs>>()
            )
        )
            .thenThrow(HttpClientErrorException(HttpStatus.BAD_REQUEST))

        when (val restResponseOvergangsstønad = familieEfSakConsumer.hentOvergangsstønad(request)) {
            is RestResponse.Failure -> {
                assertAll(
                    { Assertions.assertThat(restResponseOvergangsstønad.statusCode).isEqualTo(HttpStatus.BAD_REQUEST) },
                    {
                        Assertions.assertThat(restResponseOvergangsstønad.restClientException)
                            .isInstanceOf(HttpClientErrorException::class.java)
                    }
                )
            }
            else -> {
                Assertions.fail("Test returnerte med RestResponse.Success, som ikke var forventet")
            }
        }
    }

    fun <T> initHttpEntity(body: T): HttpEntity<T> {
        val httpHeaders = HttpHeaders()
        httpHeaders.contentType = MediaType.APPLICATION_JSON
        return HttpEntity(body, httpHeaders)
    }
}
