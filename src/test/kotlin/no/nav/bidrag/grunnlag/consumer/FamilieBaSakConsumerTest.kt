package no.nav.bidrag.grunnlag.consumer

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.grunnlag.TestUtil
import no.nav.bidrag.grunnlag.consumer.familiebasak.FamilieBaSakConsumer
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.BisysStønadstype
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.FamilieBaSakResponse
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
import java.time.YearMonth

@ExtendWith(MockitoExtension::class)
@DisplayName("FamilieBaSakConsumerTest")
internal class FamilieBaSakConsumerTest {

    @InjectMocks
    private val familieBaSakConsumer: FamilieBaSakConsumer? = null

    @Mock
    private val restTemplateMock: HttpHeaderRestTemplate? = null

    @Test
    fun `Sjekk at ok respons fra FamilieBaSak endepunkt mappes korrekt`() {
        val request = TestUtil.byggFamilieBaSakRequest()

        Mockito.`when`(
            restTemplateMock?.exchange(
                eq(FAMILIEBASAK_CONTEXT),
                eq(HttpMethod.POST),
                eq(initHttpEntity(request)),
                any<Class<FamilieBaSakResponse>>()
            )
        )
            .thenReturn(ResponseEntity(TestUtil.byggFamilieBaSakResponse(), HttpStatus.OK))

        when (val restResponseFamilieBaSak = familieBaSakConsumer!!.hentFamilieBaSak(request)) {
            is RestResponse.Success -> {
                val hentFamilieBaSakResponse = restResponseFamilieBaSak.body
                assertAll(
                    Executable { assertThat(hentFamilieBaSakResponse).isNotNull },
                    Executable { assertThat(hentFamilieBaSakResponse.perioder.size).isEqualTo(2) },
                    Executable { assertThat(hentFamilieBaSakResponse.perioder[0].stønadstype).isEqualTo(BisysStønadstype.UTVIDET) },
                    Executable { assertThat(hentFamilieBaSakResponse.perioder[0].fomMåned).isEqualTo(YearMonth.parse("2021-01")) },
                    Executable { assertThat(hentFamilieBaSakResponse.perioder[0].tomMåned).isEqualTo(YearMonth.parse("2021-12")) },
                    Executable { assertThat(hentFamilieBaSakResponse.perioder[0].beløp).isEqualTo(1000.11) },
                    Executable { assertThat(hentFamilieBaSakResponse.perioder[0].manueltBeregnet).isFalse() },
                    Executable { assertThat(hentFamilieBaSakResponse.perioder[0].deltBosted).isFalse() },
                    Executable { assertThat(hentFamilieBaSakResponse.perioder[1].stønadstype).isEqualTo(BisysStønadstype.UTVIDET) },
                    Executable { assertThat(hentFamilieBaSakResponse.perioder[1].fomMåned).isEqualTo(YearMonth.parse("2022-01")) },
                    Executable { assertThat(hentFamilieBaSakResponse.perioder[1].tomMåned).isEqualTo(YearMonth.parse("2022-12")) },
                    Executable { assertThat(hentFamilieBaSakResponse.perioder[1].beløp).isEqualTo(2000.22) },
                    Executable { assertThat(hentFamilieBaSakResponse.perioder[1].manueltBeregnet).isFalse() },
                    Executable { assertThat(hentFamilieBaSakResponse.perioder[1].deltBosted).isFalse() }
                )
            }
            else -> {
                fail("Test returnerte med RestResponse.Failure, som ikke var forventet")
            }
        }
    }

    @Test
    fun `Sjekk at exception fra FamilieBaSak endepunkt håndteres korrekt`() {
        val request = TestUtil.byggFamilieBaSakRequest()

        Mockito.`when`(
            restTemplateMock?.exchange(
                eq(FAMILIEBASAK_CONTEXT),
                eq(HttpMethod.POST),
                eq(initHttpEntity(request)),
                any<Class<FamilieBaSakResponse>>()
            )
        )
            .thenThrow(HttpClientErrorException(HttpStatus.BAD_REQUEST))

        when (val restResponseFamilieBaSak = familieBaSakConsumer!!.hentFamilieBaSak(request)) {
            is RestResponse.Failure -> {
                assertAll(
                    Executable { assertThat(restResponseFamilieBaSak.statusCode).isEqualTo(HttpStatus.BAD_REQUEST) },
                    Executable { assertThat(restResponseFamilieBaSak.restClientException).isInstanceOf(HttpClientErrorException::class.java) }
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
        private const val FAMILIEBASAK_CONTEXT = "/api/bisys/hent-utvidet-barnetrygd"
    }
}
