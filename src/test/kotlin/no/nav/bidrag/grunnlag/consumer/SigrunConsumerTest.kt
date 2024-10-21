package no.nav.bidrag.grunnlag.consumer

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.grunnlag.TestUtil
import no.nav.bidrag.grunnlag.consumer.skattegrunnlag.SigrunConsumer
import no.nav.bidrag.grunnlag.consumer.skattegrunnlag.api.HentSummertSkattegrunnlagRequest
import no.nav.bidrag.grunnlag.consumer.skattegrunnlag.api.HentSummertSkattegrunnlagResponse
import no.nav.bidrag.grunnlag.exception.RestResponse
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.function.Executable
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.util.UriComponentsBuilder
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
@DisplayName("SigrunConsumerTest")
internal class SigrunConsumerTest {

    @InjectMocks
    private val sigrunConsumer: SigrunConsumer? = null

    @Mock
    private val restTemplateMock: HttpHeaderRestTemplate? = null

    @Test
    fun `Sjekk at ok respons fra skattegrunnlag-endepunkt mappes korrekt`() {
        val request = TestUtil.byggHentSkattegrunnlagRequest()

        Mockito.`when`(
            restTemplateMock?.exchange(
                eq(uriBuilder(request)),
                eq(HttpMethod.GET),
                any(),
                eq(HentSummertSkattegrunnlagResponse::class.java),
            ),
        )
            .thenReturn(ResponseEntity(TestUtil.byggHentSkattegrunnlagResponse(), HttpStatus.OK))

        when (val restResponseSkattegrunnlag = sigrunConsumer!!.hentSummertSkattegrunnlag(request)) {
            is RestResponse.Success -> {
                val hentSkattegrunnlagResponse = restResponseSkattegrunnlag.body
                assertAll(
                    Executable { assertThat(hentSkattegrunnlagResponse).isNotNull },
                    Executable { assertThat(hentSkattegrunnlagResponse.grunnlag!!.size).isEqualTo(1) },
                    Executable { assertThat(hentSkattegrunnlagResponse.grunnlag!![0].beloep).isEqualTo("100000") },
                    Executable { assertThat(hentSkattegrunnlagResponse.grunnlag!![0].tekniskNavn).isEqualTo("tekniskNavn") },
                    Executable { assertThat(hentSkattegrunnlagResponse.svalbardGrunnlag!!.size).isEqualTo(1) },
                    Executable { assertThat(hentSkattegrunnlagResponse.svalbardGrunnlag!![0].beloep).isEqualTo("100000") },
                    Executable { assertThat(hentSkattegrunnlagResponse.svalbardGrunnlag!![0].tekniskNavn).isEqualTo("tekniskNavn") },
                    Executable { assertThat(hentSkattegrunnlagResponse.skatteoppgjoersdato).isEqualTo(LocalDate.now().toString()) },
                )
            }

            else -> {
                fail("Test returnerte med RestResponse.Failure, som ikke var forventet")
            }
        }
    }

    @Test
    fun `Sjekk at exception fra skattegrunnlag-endepunkt håndteres korrekt`() {
        val request = TestUtil.byggHentSkattegrunnlagRequest()

        Mockito.`when`(
            restTemplateMock?.exchange(
                eq(uriBuilder(request)),
                eq(HttpMethod.GET),
                any(),
                eq(HentSummertSkattegrunnlagResponse::class.java),
            ),
        )
            .thenThrow(HttpClientErrorException(HttpStatus.BAD_REQUEST))

        when (val restResponseSkattegrunnlag = sigrunConsumer!!.hentSummertSkattegrunnlag(request)) {
            is RestResponse.Failure -> {
                assertAll(
                    Executable { assertThat(restResponseSkattegrunnlag.statusCode).isEqualTo(HttpStatus.BAD_REQUEST) },
                    Executable { assertThat(restResponseSkattegrunnlag.restClientException).isInstanceOf(HttpClientErrorException::class.java) },
                )
            }

            else -> {
                fail("Test returnerte med RestResponse.Success, som ikke var forventet")
            }
        }
    }

    private fun uriBuilder(request: HentSummertSkattegrunnlagRequest) = UriComponentsBuilder.fromPath(SIGRUN_CONTEXT)
        .queryParam("rettighetspakke", "navBidrag")
        .queryParam("inntektsaar", request.inntektsAar)
        .queryParam("stadie", "oppgjoer")
        .build()
        .toUriString()

    companion object {
        private const val SIGRUN_CONTEXT = "/api/v2/summertskattegrunnlag"
    }
}
