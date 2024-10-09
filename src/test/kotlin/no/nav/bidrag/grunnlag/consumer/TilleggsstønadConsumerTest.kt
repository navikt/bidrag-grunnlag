package no.nav.bidrag.grunnlag.consumer

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.grunnlag.TestUtil
import no.nav.bidrag.grunnlag.consumer.familiebasak.TilleggsstønadConsumer
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.TilleggsstønadResponse
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

@ExtendWith(MockitoExtension::class)
internal class TilleggsstønadConsumerTest {

    companion object {
        private const val TILLEGGSSTØNAD_CONTEXT = "/api/ekstern/vedtak/tilsyn-barn"
    }

    @InjectMocks
    private lateinit var tilleggsstønadConsumer: TilleggsstønadConsumer

    @Mock
    private lateinit var restTemplateMock: HttpHeaderRestTemplate

    @Test
    fun `Sjekk at ok respons fra tilleggsstønad-endepunkt mappes korrekt`() {
        val request = TestUtil.byggTilleggsstønadRequest()

        Mockito.`when`(
            restTemplateMock.exchange(
                eq(TILLEGGSSTØNAD_CONTEXT),
                eq(HttpMethod.POST),
                eq(initHttpEntity(request)),
                ArgumentMatchers.any<Class<TilleggsstønadResponse>>(),
            ),
        )
            .thenReturn(ResponseEntity(TestUtil.byggTilleggsstønadResponse(), HttpStatus.OK))

        when (val restResponseTilleggsstønad = tilleggsstønadConsumer.hentTilleggsstønad(request)) {
            is RestResponse.Success -> {
                val hentTilleggsstønadResponse = restResponseTilleggsstønad.body
                assertAll(
                    { Assertions.assertThat(hentTilleggsstønadResponse).isNotNull },
                    { Assertions.assertThat(hentTilleggsstønadResponse.harInnvilgetVedtak).isEqualTo(true) },
                )
            }
            else -> {
                Assertions.fail("Test returnerte med RestResponse.Failure, som ikke var forventet")
            }
        }
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `Sjekk at exception fra tilleggsstønad-endepunkt håndteres korrekt`() {
        val request = TestUtil.byggTilleggsstønadRequest()

        Mockito.`when`(
            restTemplateMock.exchange(
                eq(TILLEGGSSTØNAD_CONTEXT),
                eq(HttpMethod.POST),
                eq(initHttpEntity(request)),
                ArgumentMatchers.any<Class<TilleggsstønadResponse>>(),
            ),
        )
            .thenThrow(HttpClientErrorException(HttpStatus.BAD_REQUEST))

        when (val restResponseTilleggsstønad = tilleggsstønadConsumer.hentTilleggsstønad(request)) {
            is RestResponse.Failure -> {
                assertAll(
                    { Assertions.assertThat(restResponseTilleggsstønad.statusCode).isEqualTo(HttpStatus.BAD_REQUEST) },
                    {
                        Assertions.assertThat(restResponseTilleggsstønad.restClientException)
                            .isInstanceOf(HttpClientErrorException::class.java)
                    },
                )
            }
            else -> {
                Assertions.fail("Test returnerte med RestResponse.Success, som ikke var forventet")
            }
        }
    }

    private fun <T> initHttpEntity(body: T): HttpEntity<T> {
        val httpHeaders = HttpHeaders()
        httpHeaders.contentType = MediaType.APPLICATION_JSON
        return HttpEntity(body, httpHeaders)
    }
}
