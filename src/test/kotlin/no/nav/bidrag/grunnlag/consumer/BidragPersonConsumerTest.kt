package no.nav.bidrag.grunnlag.consumer

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.domain.enums.Sivilstandstype
import no.nav.bidrag.domain.ident.PersonIdent
import no.nav.bidrag.domain.number.MatrikkelId
import no.nav.bidrag.domain.string.Adressenavn
import no.nav.bidrag.domain.string.Bruksenhetsnummer
import no.nav.bidrag.domain.string.Bydelsnummer
import no.nav.bidrag.domain.string.FulltNavn
import no.nav.bidrag.domain.string.Husbokstav
import no.nav.bidrag.domain.string.Husnummer
import no.nav.bidrag.domain.string.Kommunenummer
import no.nav.bidrag.domain.string.Postnummer
import no.nav.bidrag.domain.tid.Bekreftelsesdato
import no.nav.bidrag.domain.tid.FomDato
import no.nav.bidrag.domain.tid.TomDato
import no.nav.bidrag.grunnlag.TestUtil
import no.nav.bidrag.grunnlag.consumer.bidragperson.BidragPersonConsumer
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.transport.person.HusstandsmedlemmerDto
import no.nav.bidrag.transport.person.Sivilstandshistorikk
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
                any<Class<HusstandsmedlemmerDto>>()
            )
        )
            .thenReturn(ResponseEntity(TestUtil.byggHentHusstandsmedlemmerResponse(), HttpStatus.OK))

        when (val restResponseHusstandsmedlemmer = bidragPersonConsumer!!.hentHusstandsmedlemmer(request.ident)) {
            is RestResponse.Success -> {
                val hentHusstandsmedlemmerResponse = restResponseHusstandsmedlemmer.body
                assertAll(
                    Executable { assertThat(hentHusstandsmedlemmerResponse).isNotNull },
                    Executable { assertThat(hentHusstandsmedlemmerResponse.husstandListe.size).isEqualTo(2) },
                    Executable { assertThat(hentHusstandsmedlemmerResponse.husstandListe[0].gyldigFraOgMed).isEqualTo(FomDato(LocalDate.parse("2011-01-01"))) },
                    Executable { assertThat(hentHusstandsmedlemmerResponse.husstandListe[0].gyldigTilOgMed).isEqualTo(TomDato(LocalDate.parse("2011-10-01"))) },
                    Executable { assertThat(hentHusstandsmedlemmerResponse.husstandListe[0].adressenavn).isEqualTo(Adressenavn("adressenavn1")) },
                    Executable { assertThat(hentHusstandsmedlemmerResponse.husstandListe[0].husnummer).isEqualTo(Husnummer("husnummer1")) },
                    Executable { assertThat(hentHusstandsmedlemmerResponse.husstandListe[0].husbokstav).isEqualTo(Husbokstav("husbokstav1")) },
                    Executable { assertThat(hentHusstandsmedlemmerResponse.husstandListe[0].bruksenhetsnummer).isEqualTo(Bruksenhetsnummer("bruksenhetsnummer1")) },
                    Executable { assertThat(hentHusstandsmedlemmerResponse.husstandListe[0].postnummer).isEqualTo(Postnummer("postnr1")) },
                    Executable { assertThat(hentHusstandsmedlemmerResponse.husstandListe[0].bydelsnummer).isEqualTo(Bydelsnummer("bydelsnummer1")) },
                    Executable { assertThat(hentHusstandsmedlemmerResponse.husstandListe[0].kommunenummer).isEqualTo(Kommunenummer("kommunenummer1")) },
                    Executable { assertThat(hentHusstandsmedlemmerResponse.husstandListe[0].matrikkelId).isEqualTo(MatrikkelId(12345)) },

                    Executable { assertThat(hentHusstandsmedlemmerResponse.husstandListe[0].husstandsmedlemListe[0].personId).isEqualTo(PersonIdent("111")) },
                    Executable { assertThat(hentHusstandsmedlemmerResponse.husstandListe[0].husstandsmedlemListe[0].navn).isEqualTo(FulltNavn("fornavn1 mellomnavn1 etternavn1")) },
                    Executable { assertThat(hentHusstandsmedlemmerResponse.husstandListe[0].husstandsmedlemListe[0].gyldigFraOgMed).isEqualTo(FomDato(LocalDate.parse("2011-01-01"))) },
                    Executable { assertThat(hentHusstandsmedlemmerResponse.husstandListe[0].husstandsmedlemListe[0].gyldigTilOgMed).isEqualTo(TomDato(LocalDate.parse("2011-02-01"))) },

                    Executable { assertThat(hentHusstandsmedlemmerResponse.husstandListe[0].husstandsmedlemListe[1].personId).isEqualTo(PersonIdent("111")) },
                    Executable { assertThat(hentHusstandsmedlemmerResponse.husstandListe[0].husstandsmedlemListe[1].navn).isEqualTo(FulltNavn("fornavn1 mellomnavn1 etternavn1")) },
                    Executable { assertThat(hentHusstandsmedlemmerResponse.husstandListe[0].husstandsmedlemListe[1].gyldigFraOgMed).isEqualTo(FomDato(LocalDate.parse("2011-05-17"))) },
                    Executable { assertThat(hentHusstandsmedlemmerResponse.husstandListe[0].husstandsmedlemListe[1].gyldigTilOgMed).isNull() },

                    Executable { assertThat(hentHusstandsmedlemmerResponse.husstandListe[0].husstandsmedlemListe[2].personId).isEqualTo(PersonIdent("333")) },
                    Executable { assertThat(hentHusstandsmedlemmerResponse.husstandListe[0].husstandsmedlemListe[2].navn).isEqualTo(FulltNavn("fornavn3 mellomnavn3 etternavn3")) },
                    Executable { assertThat(hentHusstandsmedlemmerResponse.husstandListe[0].husstandsmedlemListe[2].gyldigFraOgMed).isEqualTo(FomDato(LocalDate.parse("2011-01-01"))) },
                    Executable { assertThat(hentHusstandsmedlemmerResponse.husstandListe[0].husstandsmedlemListe[2].gyldigTilOgMed).isEqualTo(TomDato(LocalDate.parse("2011-12-01"))) },

                    Executable { assertThat(hentHusstandsmedlemmerResponse.husstandListe[0].husstandsmedlemListe[3].personId).isEqualTo(PersonIdent("444")) },
                    Executable { assertThat(hentHusstandsmedlemmerResponse.husstandListe[0].husstandsmedlemListe[3].navn).isEqualTo(FulltNavn("fornavn4 mellomnavn4 etternavn4")) },
                    Executable { assertThat(hentHusstandsmedlemmerResponse.husstandListe[0].husstandsmedlemListe[3].gyldigFraOgMed).isEqualTo(FomDato(LocalDate.parse("2011-05-01"))) },
                    Executable { assertThat(hentHusstandsmedlemmerResponse.husstandListe[0].husstandsmedlemListe[3].gyldigTilOgMed).isEqualTo(TomDato(LocalDate.parse("2011-06-01"))) },

                    Executable { assertThat(hentHusstandsmedlemmerResponse.husstandListe[1].husstandsmedlemListe[0].personId).isEqualTo(PersonIdent("111")) },
                    Executable { assertThat(hentHusstandsmedlemmerResponse.husstandListe[1].husstandsmedlemListe[0].navn).isEqualTo(FulltNavn("fornavn1 mellomnavn1 etternavn1")) },
                    Executable { assertThat(hentHusstandsmedlemmerResponse.husstandListe[1].husstandsmedlemListe[0].gyldigFraOgMed).isEqualTo(FomDato(LocalDate.parse("2018-01-01"))) },
                    Executable { assertThat(hentHusstandsmedlemmerResponse.husstandListe[1].husstandsmedlemListe[0].gyldigTilOgMed).isEqualTo(TomDato(LocalDate.parse("2018-02-01"))) },

                    Executable { assertThat(hentHusstandsmedlemmerResponse.husstandListe[1].husstandsmedlemListe[1].personId).isEqualTo(PersonIdent("555")) },
                    Executable { assertThat(hentHusstandsmedlemmerResponse.husstandListe[1].husstandsmedlemListe[1].navn).isEqualTo(FulltNavn("fornavn5 mellomnavn5 etternavn5")) },
                    Executable { assertThat(hentHusstandsmedlemmerResponse.husstandListe[1].husstandsmedlemListe[1].gyldigFraOgMed).isEqualTo(FomDato(LocalDate.parse("2020-01-01"))) },
                    Executable { assertThat(hentHusstandsmedlemmerResponse.husstandListe[1].husstandsmedlemListe[1].gyldigTilOgMed).isNull() }

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
                any<Class<HusstandsmedlemmerDto>>()
            )
        )
            .thenThrow(HttpClientErrorException(HttpStatus.BAD_REQUEST))

        when (val restResponseHusstandsmedlemmer = bidragPersonConsumer!!.hentHusstandsmedlemmer(request.ident)) {
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
                any<Class<Sivilstandshistorikk>>()
            )
        )
            .thenReturn(ResponseEntity(TestUtil.byggHentSivilstandResponse(), HttpStatus.OK))

        when (val restResponseSivilstand = bidragPersonConsumer!!.hentSivilstand(request.ident)) {
            is RestResponse.Success -> {
                val hentSivilstandResponse = restResponseSivilstand.body
                assertAll(
                    Executable { assertThat(hentSivilstandResponse).isNotNull },
                    Executable { assertThat(hentSivilstandResponse.sivilstand.size).isEqualTo(3) },
                    Executable { assertThat(hentSivilstandResponse.sivilstand[0].type).isEqualTo(Sivilstandstype.SEPARERT_PARTNER) },
                    Executable { assertThat(hentSivilstandResponse.sivilstand[0].gyldigFraOgMed).isNull() },
                    Executable { assertThat(hentSivilstandResponse.sivilstand[0].bekreftelsesdato).isNull() },

                    Executable { assertThat(hentSivilstandResponse.sivilstand[1].type).isEqualTo(Sivilstandstype.ENKE_ELLER_ENKEMANN) },
                    Executable { assertThat(hentSivilstandResponse.sivilstand[1].gyldigFraOgMed).isNull() },
                    Executable { assertThat(hentSivilstandResponse.sivilstand[1].bekreftelsesdato).isEqualTo(Bekreftelsesdato(LocalDate.parse("2021-01-01"))) },

                    Executable { assertThat(hentSivilstandResponse.sivilstand[2].type).isEqualTo(Sivilstandstype.GJENLEVENDE_PARTNER) },
                    Executable { assertThat(hentSivilstandResponse.sivilstand[2].gyldigFraOgMed).isEqualTo(FomDato(LocalDate.parse("2021-09-01"))) },
                    Executable { assertThat(hentSivilstandResponse.sivilstand[2].bekreftelsesdato).isNull() }

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
                any<Class<Sivilstandshistorikk>>()
            )
        )
            .thenThrow(HttpClientErrorException(HttpStatus.BAD_REQUEST))

        when (val restResponseSivilstand = bidragPersonConsumer!!.hentSivilstand(request.ident)) {
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
