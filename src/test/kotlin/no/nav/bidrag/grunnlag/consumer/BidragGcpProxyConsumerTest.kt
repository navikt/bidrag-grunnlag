package no.nav.bidrag.grunnlag.consumer

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.grunnlag.TestUtil
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.BidragGcpProxyConsumer
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.barnetillegg.HentBarnetilleggPensjonResponse
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.skatt.HentSkattegrunnlagResponse
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
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import java.math.BigDecimal
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
@DisplayName("BidragGrunnlagConsumerTest")
internal class BidragGcpProxyConsumerTest {

    @InjectMocks
    private val bidragGcpProxyConsumer: BidragGcpProxyConsumer? = null

    @Mock
    private val restTemplateMock: HttpHeaderRestTemplate? = null

    @Test
    fun `Sjekk at ok respons fra BidragGcpProxy skattegrunnlag-endepunkt mappes korrekt`() {
        val request = TestUtil.byggHentSkattegrunnlagRequest()

        Mockito.`when`(
            restTemplateMock?.exchange(
                eq(BIDRAGGCPPROXY_SKATTEGRUNNLAG_CONTEXT),
                eq(HttpMethod.POST),
                eq(initHttpEntity(request)),
                any<Class<HentSkattegrunnlagResponse>>()
            )
        )
            .thenReturn(ResponseEntity(TestUtil.byggHentSkattegrunnlagResponse(), HttpStatus.OK))

        when (val restResponseSkattegrunnlag = bidragGcpProxyConsumer!!.hentSkattegrunnlag(request)) {
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
                    Executable { assertThat(hentSkattegrunnlagResponse.skatteoppgjoersdato).isEqualTo(LocalDate.now().toString()) }
                )
            }
            else -> {
                fail("Test returnerte med RestResponse.Failure, som ikke var forventet")
            }
        }
    }

    @Test
    fun `Sjekk at exception fra BidragGcpProxy skattegrunnlag-endepunkt håndteres korrekt`() {
        val request = TestUtil.byggHentSkattegrunnlagRequest()

        Mockito.`when`(
            restTemplateMock?.exchange(
                eq(BIDRAGGCPPROXY_SKATTEGRUNNLAG_CONTEXT),
                eq(HttpMethod.POST),
                eq(initHttpEntity(request)),
                any<Class<HentSkattegrunnlagResponse>>()
            )
        )
            .thenThrow(HttpClientErrorException(HttpStatus.BAD_REQUEST))

        when (val restResponseSkattegrunnlag = bidragGcpProxyConsumer!!.hentSkattegrunnlag(request)) {
            is RestResponse.Failure -> {
                assertAll(
                    Executable { assertThat(restResponseSkattegrunnlag.statusCode).isEqualTo(HttpStatus.BAD_REQUEST) },
                    Executable { assertThat(restResponseSkattegrunnlag.restClientException).isInstanceOf(HttpClientErrorException::class.java) }
                )
            }
            else -> {
                fail("Test returnerte med RestResponse.Success, som ikke var forventet")
            }
        }
    }

    @Test
    fun `Sjekk at ok respons fra BidragGcpProxy barnetillegg-pensjon-endepunkt mappes korrekt`() {
        val request = TestUtil.byggHentBarnetilleggPensjonRequest()

        Mockito.`when`(
            restTemplateMock?.exchange(
                eq(BIDRAGGCPPROXY_BARNETILLEGG_PENSJON_CONTEXT),
                eq(HttpMethod.POST),
                eq(initHttpEntity(request)),
                any<Class<HentBarnetilleggPensjonResponse>>()
            )
        )
            .thenReturn(ResponseEntity(TestUtil.byggHentBarnetilleggPensjonResponse(), HttpStatus.OK))

        when (val restResponseBarnetilleggPensjon = bidragGcpProxyConsumer!!.hentBarnetilleggPensjon(request)) {
            is RestResponse.Success -> {
                val hentBarnetilleggPensjonResponse = restResponseBarnetilleggPensjon.body
                assertAll(
                    Executable { assertThat(hentBarnetilleggPensjonResponse).isNotNull },
                    Executable { assertThat(hentBarnetilleggPensjonResponse.barnetilleggPensjonListe!!.size).isEqualTo(2) },
                    Executable { assertThat(hentBarnetilleggPensjonResponse.barnetilleggPensjonListe!![0].barn).isEqualTo("barnIdent") },
                    Executable { assertThat(hentBarnetilleggPensjonResponse.barnetilleggPensjonListe!![0].beloep).isEqualTo(BigDecimal.valueOf(1000.11)) },
                    Executable { assertThat(hentBarnetilleggPensjonResponse.barnetilleggPensjonListe!![1].barn).isEqualTo("barnIdent") },
                    Executable { assertThat(hentBarnetilleggPensjonResponse.barnetilleggPensjonListe!![1].beloep).isEqualTo(BigDecimal.valueOf(2000.22)) }
                )
            }
            else -> {
                fail("Test returnerte med RestResponse.Failure, som ikke var forventet")
            }
        }
    }

    @Test
    fun `Sjekk at exception fra BidragGcpProxy barnetillegg-pensjon-endepunkt håndteres korrekt`() {
        val request = TestUtil.byggHentBarnetilleggPensjonRequest()

        Mockito.`when`(
            restTemplateMock?.exchange(
                eq(BIDRAGGCPPROXY_BARNETILLEGG_PENSJON_CONTEXT),
                eq(HttpMethod.POST),
                eq(initHttpEntity(request)),
                any<Class<HentBarnetilleggPensjonResponse>>()
            )
        )
            .thenThrow(HttpClientErrorException(HttpStatus.BAD_REQUEST))

        when (val restResponseBarnetilleggPensjon = bidragGcpProxyConsumer!!.hentBarnetilleggPensjon(request)) {
            is RestResponse.Failure -> {
                assertAll(
                    Executable { assertThat(restResponseBarnetilleggPensjon.statusCode).isEqualTo(HttpStatus.BAD_REQUEST) },
                    Executable { assertThat(restResponseBarnetilleggPensjon.restClientException).isInstanceOf(HttpClientErrorException::class.java) }
                )
            }
            else -> {
                fail("Test returnerte med RestResponse.Success, som ikke var forventet")
            }
        }
    }

    fun <T> initHttpEntity(body: T): HttpEntity<T> {
        val httpHeaders = HttpHeaders()
        httpHeaders.contentType = MediaType.APPLICATION_JSON
        return HttpEntity(body, httpHeaders)
    }

    companion object {
        private const val BIDRAGGCPPROXY_SKATTEGRUNNLAG_CONTEXT = "/skattegrunnlag/hent"
        private const val BIDRAGGCPPROXY_BARNETILLEGG_PENSJON_CONTEXT = "/barnetillegg/pensjon/hent"
    }
}
