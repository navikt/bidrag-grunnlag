package no.nav.bidrag.grunnlag.consumer

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.grunnlag.TestUtil
import no.nav.bidrag.grunnlag.consumer.inntektskomponenten.InntektskomponentenConsumer
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.tjenester.aordningen.inntektsinformasjon.inntekt.InntektType
import no.nav.tjenester.aordningen.inntektsinformasjon.response.HentInntektListeResponse
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
import java.math.BigDecimal
import java.time.YearMonth
import java.util.*

@ExtendWith(MockitoExtension::class)
@DisplayName("InntektskomponentenConsumerTest")
internal class InntektskomponentenConsumerTest {

    @InjectMocks
    private val inntektskomponentenConsumer: InntektskomponentenConsumer? = null

    @Mock
    private val restTemplateMock: HttpHeaderRestTemplate? = null

    @Test
    fun `Sjekk at ok respons mappes korrekt`() {
        val request = TestUtil.byggHentInntektListeRequest()

        Mockito.`when`(
            restTemplateMock?.exchange(
                eq(INNTEKT_LISTE_CONTEXT),
                eq(HttpMethod.POST),
                any(),
                any<Class<HentInntektListeResponse>>()
            )
        )
            .thenReturn(ResponseEntity(TestUtil.byggHentInntektListeResponse(), HttpStatus.OK))

        when (val restResponseInntekt = inntektskomponentenConsumer!!.hentInntekter(request, false)) {
            is RestResponse.Success -> {
                val hentInntektListeResponse = restResponseInntekt.body
                assertAll(
                    Executable { assertThat(hentInntektListeResponse).isNotNull },
                    Executable { assertThat(hentInntektListeResponse.arbeidsInntektMaaned!!.size).isEqualTo(1) },
                    Executable { assertThat(hentInntektListeResponse.arbeidsInntektMaaned!![0].aarMaaned).isEqualTo(YearMonth.parse("2021-01")) },
                    Executable { assertThat(hentInntektListeResponse.arbeidsInntektMaaned!![0].arbeidsInntektInformasjon).isNotNull },
                    Executable { assertThat(hentInntektListeResponse.arbeidsInntektMaaned!![0].arbeidsInntektInformasjon.inntektListe).isNotNull },
                    Executable {
                        assertThat(hentInntektListeResponse.arbeidsInntektMaaned!![0].arbeidsInntektInformasjon.inntektListe!![0].inntektType)
                            .isEqualTo(InntektType.LOENNSINNTEKT)
                    },
                    Executable {
                        assertThat(hentInntektListeResponse.arbeidsInntektMaaned!![0].arbeidsInntektInformasjon.inntektListe!![0].beloep)
                            .isEqualTo(BigDecimal.valueOf(10000))
                    }
                )
            }

            else -> {
                fail("Test returnerte med RestResponse.Failure, som ikke var forventet")
            }
        }
    }

    @Test
    fun `Sjekk at exception håndteres korrekt`() {
        val request = TestUtil.byggHentInntektListeRequest()

        Mockito.`when`(
            restTemplateMock?.exchange(
                eq(INNTEKT_LISTE_CONTEXT),
                eq(HttpMethod.POST),
                any(),
                any<Class<HentInntektListeResponse>>()
            )
        )
            .thenThrow(HttpClientErrorException(HttpStatus.BAD_REQUEST))

        when (val restResponseInntekt = inntektskomponentenConsumer!!.hentInntekter(request, false)) {
            is RestResponse.Failure -> {
                assertAll(
                    Executable { assertThat(restResponseInntekt.statusCode).isEqualTo(HttpStatus.BAD_REQUEST) },
                    Executable { assertThat(restResponseInntekt.restClientException).isInstanceOf(HttpClientErrorException::class.java) }
                )
            }

            else -> {
                fail("Test returnerte med RestResponse.Success, som ikke var forventet")
            }
        }
    }

    companion object {
        private const val INNTEKT_LISTE_CONTEXT = "/rs/api/v1/hentinntektliste"
    }
}
