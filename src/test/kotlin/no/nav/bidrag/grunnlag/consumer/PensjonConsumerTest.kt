package no.nav.bidrag.grunnlag.consumer

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.grunnlag.TestUtil
import no.nav.bidrag.grunnlag.consumer.pensjon.PensjonConsumer
import no.nav.bidrag.grunnlag.consumer.pensjon.api.BarnetilleggPensjon
import no.nav.bidrag.grunnlag.exception.RestResponse
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
internal class PensjonConsumerTest {

    @InjectMocks
    private lateinit var pensjonConsumer: PensjonConsumer

    @Mock
    private lateinit var restTemplateMock: HttpHeaderRestTemplate

    @Test
    fun `Sjekk at ok respons fra pensjon barnetillegg-endepunkt mappes korrekt`() {
        val request = TestUtil.byggHentBarnetilleggPensjonRequest()

        Mockito.`when`(
            restTemplateMock.exchange(
                eq(BARNETILLEGG_CONTEXT),
                eq(HttpMethod.POST),
                eq(initHttpEntity(request)),
                any<ParameterizedTypeReference<List<BarnetilleggPensjon>>>(),
            ),
        )
            .thenReturn(ResponseEntity(TestUtil.byggHentBarnetilleggPensjonResponse(), HttpStatus.OK))

        when (val restResponseBarnetillegg = pensjonConsumer.hentBarnetilleggPensjon(request)) {
            is RestResponse.Success -> {
                val hentBarnetilleggPensjonResponse = restResponseBarnetillegg.body
                assertAll(
                    { assertThat(hentBarnetilleggPensjonResponse).isNotNull },
                    { assertThat(hentBarnetilleggPensjonResponse.size).isEqualTo(2) },
                    { assertThat(hentBarnetilleggPensjonResponse[0].barn).isEqualTo("barnIdent") },
                    { assertThat(hentBarnetilleggPensjonResponse[0].beloep).isEqualTo(BigDecimal.valueOf(1001.45)) },
                    { assertThat(hentBarnetilleggPensjonResponse[1].barn).isEqualTo("barnIdent") },
                    { assertThat(hentBarnetilleggPensjonResponse[1].beloep).isEqualTo(BigDecimal.valueOf(2002.50)) },
                )
            }

            else -> {
                Assertions.fail("Test returnerte med RestResponse.Failure, som ikke var forventet")
            }
        }
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `Sjekk at exception fra pensjon barnetillegg-endepunkt håndteres korrekt`() {
        val request = TestUtil.byggHentBarnetilleggPensjonRequest()

        Mockito.`when`(
            restTemplateMock.exchange(
                eq(BARNETILLEGG_CONTEXT),
                eq(HttpMethod.POST),
                eq(initHttpEntity(request)),
                any<ParameterizedTypeReference<List<BarnetilleggPensjon>>>(),
            ),
        )
            .thenThrow(HttpClientErrorException(HttpStatus.BAD_REQUEST))

        when (val restResponseBarnetilleggPensjon = pensjonConsumer.hentBarnetilleggPensjon(request)) {
            is RestResponse.Failure -> {
                assertAll(
                    { assertThat(restResponseBarnetilleggPensjon.statusCode).isEqualTo(HttpStatus.BAD_REQUEST) },
                    { assertThat(restResponseBarnetilleggPensjon.restClientException).isInstanceOf(HttpClientErrorException::class.java) },
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

    companion object {
        private const val BARNETILLEGG_CONTEXT = "/pen/api/barnetillegg/search"
    }
}
