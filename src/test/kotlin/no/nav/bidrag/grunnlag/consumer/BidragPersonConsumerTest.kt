package no.nav.bidrag.grunnlag.consumer

import no.nav.bidrag.behandling.felles.enums.SivilstandKode
import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.grunnlag.TestUtil
import no.nav.bidrag.grunnlag.consumer.bidragperson.BidragPersonConsumer
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.HusstandsmedlemmerResponseDto
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
  fun `Sjekk at ok respons fra Bidrag-person-endepunkt for husstandsmedlemmer mappes korrekt`() {
    val request = TestUtil.byggHusstandsmedlemmerRequest()

    Mockito.`when`(
      restTemplateMock?.exchange(
        eq(BIDRAGPERSON_HUSSTANDSMEDLEMMER_CONTEXT),
        eq(HttpMethod.POST),
        eq(initHttpEntity(request)),
        any<Class<HusstandsmedlemmerResponseDto>>()
      )
    )
      .thenReturn(ResponseEntity(TestUtil.byggHentHusstandsmedlemmerResponse(), HttpStatus.OK))

    when (val restResponseHusstandsmedlemmer = bidragPersonConsumer!!.hentHusstandsmedlemmer(request)) {
      is RestResponse.Success -> {
        val hentHusstandsmedlemmerResponse = restResponseHusstandsmedlemmer.body
        assertAll(
          Executable { assertThat(hentHusstandsmedlemmerResponse).isNotNull },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.size).isEqualTo(2) },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(0)?.gyldigFraOgMed).isEqualTo(LocalDate.parse("2011-01-01")) },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(0)?.gyldigTilOgMed).isEqualTo(LocalDate.parse("2011-10-01")) },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(0)?.adressenavn).isEqualTo("adressenavn1") },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(0)?.husnummer).isEqualTo("husnummer1") },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(0)?.husbokstav).isEqualTo("husbokstav1") },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(0)?.bruksenhetsnummer).isEqualTo("bruksenhetsnummer1") },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(0)?.postnummer).isEqualTo("postnr1") },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(0)?.bydelsnummer).isEqualTo("bydelsnummer1") },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(0)?.kommunenummer).isEqualTo("kommunenummer1") },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(0)?.matrikkelId).isEqualTo(12345) },

          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(0)?.husstandsmedlemmerResponseListe?.get(0)?.personId).isEqualTo("123") },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(0)?.husstandsmedlemmerResponseListe?.get(0)?.fornavn).isEqualTo("fornavn1") },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(0)?.husstandsmedlemmerResponseListe?.get(0)?.mellomnavn).isEqualTo("mellomnavn1") },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(0)?.husstandsmedlemmerResponseListe?.get(0)?.etternavn).isEqualTo("etternavn1") },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(0)?.husstandsmedlemmerResponseListe?.get(0)?.gyldigFraOgMed).isEqualTo(LocalDate.parse("2011-01-01")) },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(0)?.husstandsmedlemmerResponseListe?.get(0)?.gyldigTilOgMed).isEqualTo(LocalDate.parse("2011-02-01")) },

          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(0)?.husstandsmedlemmerResponseListe?.get(1)?.personId).isEqualTo("234") },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(0)?.husstandsmedlemmerResponseListe?.get(1)?.fornavn).isEqualTo("fornavn2") },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(0)?.husstandsmedlemmerResponseListe?.get(1)?.mellomnavn).isEqualTo("mellomnavn2") },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(0)?.husstandsmedlemmerResponseListe?.get(1)?.etternavn).isEqualTo("etternavn2") },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(0)?.husstandsmedlemmerResponseListe?.get(1)?.gyldigFraOgMed).isEqualTo(LocalDate.parse("2011-01-01")) },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(0)?.husstandsmedlemmerResponseListe?.get(1)?.gyldigTilOgMed).isEqualTo(LocalDate.parse("2011-12-01")) },

          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(0)?.husstandsmedlemmerResponseListe?.get(2)?.personId).isEqualTo("345") },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(0)?.husstandsmedlemmerResponseListe?.get(2)?.fornavn).isEqualTo("fornavn3") },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(0)?.husstandsmedlemmerResponseListe?.get(2)?.mellomnavn).isEqualTo("mellomnavn3") },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(0)?.husstandsmedlemmerResponseListe?.get(2)?.etternavn).isEqualTo("etternavn3") },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(0)?.husstandsmedlemmerResponseListe?.get(2)?.gyldigFraOgMed).isEqualTo(LocalDate.parse("2011-05-01")) },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(0)?.husstandsmedlemmerResponseListe?.get(2)?.gyldigTilOgMed).isEqualTo(LocalDate.parse("2011-06-01")) },


          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(1)?.gyldigFraOgMed).isEqualTo(LocalDate.parse("2011-10-01")) },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(1)?.gyldigTilOgMed).isNull() },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(1)?.adressenavn).isEqualTo("adressenavn2") },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(1)?.husnummer).isEqualTo("husnummer2") },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(1)?.husbokstav).isEqualTo("husbokstav2") },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(1)?.bruksenhetsnummer).isEqualTo("bruksenhetsnummer2") },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(1)?.postnummer).isEqualTo("postnr2") },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(1)?.bydelsnummer).isEqualTo("bydelsnummer2") },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(1)?.kommunenummer).isEqualTo("kommunenummer2") },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(1)?.matrikkelId).isEqualTo(54321) },

          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(1)?.husstandsmedlemmerResponseListe?.get(0)?.personId).isEqualTo("987") },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(1)?.husstandsmedlemmerResponseListe?.get(0)?.fornavn).isEqualTo("fornavn1bosted2") },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(1)?.husstandsmedlemmerResponseListe?.get(0)?.mellomnavn).isEqualTo("mellomnavn1bosted2") },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(1)?.husstandsmedlemmerResponseListe?.get(0)?.etternavn).isEqualTo("etternavn1bosted2") },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(1)?.husstandsmedlemmerResponseListe?.get(0)?.gyldigFraOgMed).isEqualTo(LocalDate.parse("2017-01-01")) },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(1)?.husstandsmedlemmerResponseListe?.get(0)?.gyldigTilOgMed).isEqualTo(LocalDate.parse("2018-02-01")) },

          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(1)?.husstandsmedlemmerResponseListe?.get(1)?.personId).isEqualTo("234") },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(1)?.husstandsmedlemmerResponseListe?.get(1)?.fornavn).isEqualTo("fornavn2bosted2") },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(1)?.husstandsmedlemmerResponseListe?.get(1)?.mellomnavn).isEqualTo("mellomnavn2bosted2") },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(1)?.husstandsmedlemmerResponseListe?.get(1)?.etternavn).isEqualTo("etternavn2bosted2") },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(1)?.husstandsmedlemmerResponseListe?.get(1)?.gyldigFraOgMed).isEqualTo(LocalDate.parse("2020-01-01")) },
          Executable { assertThat(hentHusstandsmedlemmerResponse.husstandResponseListe?.get(1)?.husstandsmedlemmerResponseListe?.get(1)?.gyldigTilOgMed).isNull() }

          )
      }
      else -> {
        fail("Test returnerte med RestResponse.Failure, som ikke var forventet")
      }
    }
  }

  @Test
  fun `Sjekk at exception fra Bidrag-person-husstandsmedlemmer-endepunkt håndteres korrekt`() {
    val request = TestUtil.byggHusstandsmedlemmerRequest()

    Mockito.`when`(
      restTemplateMock?.exchange(
        eq(BIDRAGPERSON_HUSSTANDSMEDLEMMER_CONTEXT),
        eq(HttpMethod.POST),
        eq(initHttpEntity(request)),
        any<Class<HusstandsmedlemmerResponseDto>>()
      )
    )
      .thenThrow(HttpClientErrorException(HttpStatus.BAD_REQUEST))

    when (val restResponseHusstandsmedlemmer = bidragPersonConsumer!!.hentHusstandsmedlemmer(request)) {
      is RestResponse.Failure -> {
        assertAll(
          Executable { assertThat(restResponseHusstandsmedlemmer.statusCode).isEqualTo(HttpStatus.BAD_REQUEST) },
          Executable { assertThat(restResponseHusstandsmedlemmer.restClientException).isInstanceOf(HttpClientErrorException::class.java) }
        )
      }
      else -> {
        fail("Test returnerte med RestResponse.Success, som ikke var forventet")
      }
    }
  }



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
  fun `Sjekk at exception fra Bidrag-person-sivilstand-endepunkt håndteres korrekt`() {
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
    private const val BIDRAGPERSON_FORELDER_BARN_RELASJON_CONTEXT = "/bidrag-person/forelderbarnrelasjon"
    private const val BIDRAGPERSON_FOEDSEL_DOED_CONTEXT = "/bidrag-person/foedselogdoed"
    private const val BIDRAGPERSON_HUSSTANDSMEDLEMMER_CONTEXT = "/bidrag-person/husstandsmedlemmer"
    private const val BIDRAGPERSON_SIVILSTAND_CONTEXT = "/bidrag-person/sivilstand"
    private const val BIDRAGPERSON_PERSON_INFO_CONTEXT = "/bidrag-person/informasjon"
  }
}