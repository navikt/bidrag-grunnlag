package no.nav.bidrag.grunnlag.consumer

import no.nav.bidrag.behandling.felles.enums.SivilstandKode
import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.grunnlag.TestUtil
import no.nav.bidrag.grunnlag.consumer.bidragperson.BidragPersonConsumer
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.SivilstandResponseDto
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
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
@DisplayName("BidragPersonConsumerTest")
internal class BidragPersonConsumerTest {

  @InjectMocks
  private val bidragPersonConsumer: BidragPersonConsumer? = null

  @Mock
  private val restTemplateMock: HttpHeaderRestTemplate? = null

  @Test
  fun `Sjekk at ok respons fra Bidrag-person-endepunkt for sivilstand mappes korrekt`() {
    val request = TestUtil.byggSivilstandRequest()

    Mockito.`when`(
      restTemplateMock?.exchange(
        eq(BIDRAGPERSON_SIVILSTAND_CONTEXT),
        eq(HttpMethod.POST),
        eq(initHttpEntity(request)),
        any<Class<SivilstandResponseDto>>()
      )
    )
      .thenReturn(ResponseEntity(TestUtil.byggHentSivilstandResponse(), HttpStatus.OK))

    when (val restResponseSivilstand = bidragPersonConsumer!!.hentSivilstand(request)) {
      is RestResponse.Success -> {
        val hentSivilstandResponse = restResponseSivilstand.body
        assertAll(
          Executable { assertThat(hentSivilstandResponse).isNotNull },
          Executable { assertThat(hentSivilstandResponse.sivilstand?.size).isEqualTo(3) },
          Executable { assertThat(hentSivilstandResponse.sivilstand?.get(0)?.type).isEqualTo(SivilstandKode.ENSLIG.toString()) },
          Executable { assertThat(hentSivilstandResponse.sivilstand?.get(0)?.gyldigFraOgMed).isNull() },
          Executable { assertThat(hentSivilstandResponse.sivilstand?.get(0)?.bekreftelsesdato).isNull()},

          Executable { assertThat(hentSivilstandResponse.sivilstand?.get(1)?.type).isEqualTo(SivilstandKode.SAMBOER.toString()) },
          Executable { assertThat(hentSivilstandResponse.sivilstand?.get(1)?.gyldigFraOgMed).isNull() },
          Executable { assertThat(hentSivilstandResponse.sivilstand?.get(1)?.bekreftelsesdato).isEqualTo(LocalDate.parse("2021-01-01")) },

          Executable { assertThat(hentSivilstandResponse.sivilstand?.get(2)?.type).isEqualTo(SivilstandKode.GIFT.toString()) },
          Executable { assertThat(hentSivilstandResponse.sivilstand?.get(2)?.gyldigFraOgMed).isEqualTo(LocalDate.parse("2021-09-01")) },
          Executable { assertThat(hentSivilstandResponse.sivilstand?.get(2)?.bekreftelsesdato).isNull() },

        )
      }
      else -> {
        fail("Test returnerte med RestResponse.Failure, som ikke var forventet")
      }
    }
  }

  @Test
  fun `Sjekk at exception fra Bidrag-person-endepunkt håndteres korrekt`() {
    val request = TestUtil.byggSivilstandRequest()

    Mockito.`when`(
      restTemplateMock?.exchange(
        eq(BIDRAGPERSON_SIVILSTAND_CONTEXT),
        eq(HttpMethod.POST),
        eq(initHttpEntity(request)),
        any<Class<SivilstandResponseDto>>()
      )
    )
      .thenThrow(HttpClientErrorException(HttpStatus.BAD_REQUEST))

    when (val restResponseSivilstand = bidragPersonConsumer!!.hentSivilstand(request)) {
      is RestResponse.Failure -> {
        assertAll(
          Executable { assertThat(restResponseSivilstand.statusCode).isEqualTo(HttpStatus.BAD_REQUEST) },
          Executable { assertThat(restResponseSivilstand.restClientException).isInstanceOf(HttpClientErrorException::class.java) }
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
    private const val BIDRAGPERSON_SIVILSTAND_CONTEXT = "/bidrag-person/sivilstand"
  }
}