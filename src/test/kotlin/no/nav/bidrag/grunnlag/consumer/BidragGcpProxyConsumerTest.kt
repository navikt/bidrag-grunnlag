package no.nav.bidrag.grunnlag.consumer

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.grunnlag.TestUtil
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.BidragGcpProxyConsumer
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.ainntekt.HentInntektListeResponseIntern
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.barnetillegg.HentBarnetilleggPensjonResponse
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.skatt.HentSkattegrunnlagResponse
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
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

@ExtendWith(MockitoExtension::class)
@DisplayName("BidragGrunnlagConsumerTest")
internal class BidragGcpProxyConsumerTest {

  @InjectMocks
  private val bidragGcpProxyConsumer: BidragGcpProxyConsumer? = null

  @Mock
  private val restTemplateMock: HttpHeaderRestTemplate? = null

  @Test
  fun `Sjekk at ok respons fra BidragGcpProxy inntekt endepunkt mappes korrekt`() {
    val request = TestUtil.byggHentInntektRequest()

    Mockito.`when`(
      restTemplateMock?.exchange(
        eq(BIDRAGGCPPROXY_INNTEKT_CONTEXT),
        eq(HttpMethod.POST),
        eq(initHttpEntity(request)),
        any<Class<HentInntektListeResponse>>()
      )
    )
      .thenReturn(ResponseEntity(TestUtil.byggHentInntektListeResponse(), HttpStatus.OK))

    when (val restResponseInntekt = bidragGcpProxyConsumer!!.hentAinntekt(request)) {
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
  fun `Sjekk at exception fra BidragGcpProxy inntekt endepunkt håndteres korrekt`() {
    val request = TestUtil.byggHentInntektRequest()

    Mockito.`when`(
      restTemplateMock?.exchange(
        eq(BIDRAGGCPPROXY_INNTEKT_CONTEXT),
        eq(HttpMethod.POST),
        eq(initHttpEntity(request)),
        any<Class<HentInntektListeResponseIntern>>()
      )
    )
      .thenThrow(HttpClientErrorException(HttpStatus.BAD_REQUEST))

    when (val restResponseInntekt = bidragGcpProxyConsumer!!.hentAinntekt(request)) {
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

  @Test
  fun `Sjekk at ok respons fra BidragGcpProxy skattegrunnlag endepunkt mappes korrekt`() {
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
  fun `Sjekk at exception fra BidragGcpProxy skattegrunnlag endepunkt håndteres korrekt`() {
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
  fun `Sjekk at ok respons fra BidragGcpProxy barnetillegg pensjon endepunkt mappes korrekt`() {
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
  fun `Sjekk at exception fra BidragGcpProxy barnetillegg pensjon endepunkt håndteres korrekt`() {
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
    private const val BIDRAGGCPPROXY_INNTEKT_CONTEXT = "/inntekt/hent"
    private const val BIDRAGGCPPROXY_SKATTEGRUNNLAG_CONTEXT = "/skattegrunnlag/hent"
    private const val BIDRAGGCPPROXY_BARNETILLEGG_PENSJON_CONTEXT = "/barnetillegg/pensjon/hent"
  }
}