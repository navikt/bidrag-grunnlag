package no.nav.bidrag.grunnlag.consumer

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.grunnlag.TestUtil
import no.nav.bidrag.grunnlag.consumer.familiekssak.FamilieKsSakConsumer
import no.nav.bidrag.grunnlag.consumer.familiekssak.api.BisysResponsDto
import no.nav.bidrag.grunnlag.exception.RestResponse
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
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
import java.time.YearMonth

@ExtendWith(MockitoExtension::class)
@DisplayName("FamilieKsSakConsumerTest")
internal class FamilieKsSakConsumerTest {

    @InjectMocks
    private val familieKsConsumer: FamilieKsSakConsumer? = null

    @Mock
    private val restTemplateMock: HttpHeaderRestTemplate? = null

    @Test
    fun `Sjekk at ok respons fra Kontantstotte-endepunkt mappes korrekt`() {
        val request = TestUtil.byggKontantstotteRequest()

        Mockito.`when`(
            restTemplateMock?.exchange(
                eq(FAMILIEKSSAK_CONTEXT),
                eq(HttpMethod.POST),
                eq(initHttpEntity(request)),
                any<Class<BisysResponsDto>>()
            )
        )
            .thenReturn(ResponseEntity(TestUtil.byggKontantstotteResponse(), HttpStatus.OK))

        when (val restResponseKontantstotte = familieKsConsumer!!.hentKontantstotte(request)) {
            is RestResponse.Success -> {
                val hentKontantstotteResponse = restResponseKontantstotte.body
                assertAll(
                    { assertThat(hentKontantstotteResponse).isNotNull },
                    { assertThat(hentKontantstotteResponse.infotrygdPerioder.size).isEqualTo(1) },
                    { assertThat(hentKontantstotteResponse.ksSakPerioder.size).isEqualTo(1) },
                    { assertThat(hentKontantstotteResponse.infotrygdPerioder[0].beløp).isEqualTo(15001) },
                    { assertThat(hentKontantstotteResponse.infotrygdPerioder[0].fomMåned).isEqualTo(YearMonth.parse("2022-01")) },
                    { assertThat(hentKontantstotteResponse.infotrygdPerioder[0].tomMåned).isEqualTo(YearMonth.parse("2022-12")) },
                    { assertThat(hentKontantstotteResponse.infotrygdPerioder[0].beløp).isEqualTo(15001) },
                    { assertThat(hentKontantstotteResponse.infotrygdPerioder[0].barna[0]).isEqualTo("11223344551") },
                    { assertThat(hentKontantstotteResponse.infotrygdPerioder[0].barna[1]).isEqualTo("15544332211") },

                    { assertThat(hentKontantstotteResponse.ksSakPerioder[0].fomMåned).isEqualTo(YearMonth.parse("2023-01")) },
                    { assertThat(hentKontantstotteResponse.ksSakPerioder[0].tomMåned).isEqualTo(YearMonth.parse("2023-06")) },
                    { assertThat(hentKontantstotteResponse.ksSakPerioder[0].barn.ident).isEqualTo("11223344551") },
                    { assertThat(hentKontantstotteResponse.ksSakPerioder[0].barn.beløp).isEqualTo(5000) }
                )
            }
            else -> {
                fail("Test returnerte med RestResponse.Failure, som ikke var forventet")
            }
        }
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `Sjekk at exception fra Kontantstotte-endepunkt håndteres korrekt`() {
        val request = TestUtil.byggKontantstotteRequest()

        Mockito.`when`(
            restTemplateMock?.exchange(
                eq(FAMILIEKSSAK_CONTEXT),
                eq(HttpMethod.POST),
                eq(initHttpEntity(request)),
                any<Class<BisysResponsDto>>()
            )
        )
            .thenThrow(HttpClientErrorException(HttpStatus.BAD_REQUEST))

        when (val restResponseKontantstotte = familieKsConsumer!!.hentKontantstotte(request)) {
            is RestResponse.Failure -> {
                assertAll(
                    { assertThat(restResponseKontantstotte.statusCode).isEqualTo(HttpStatus.BAD_REQUEST) },
                    { assertThat(restResponseKontantstotte.restClientException).isInstanceOf(HttpClientErrorException::class.java) }
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
        private const val FAMILIEKSSAK_CONTEXT = "/api/bisys/hent-utbetalingsinfo"
    }
}
