package no.nav.bidrag.grunnlag.consumer.bidragperson.api

import java.time.LocalDate

// Request til bidrag-person

data class HusstandsmedlemmerRequest(
  val personId: String,
  val periodeFra: LocalDate
)

data class SivilstandRequest(
  val personId: String,
  val periodeFra: LocalDate
)


// Response fra bidrag-person

data class FoedselOgDoedDto(
// Gir fødselsdato og fødselsår for angitt person. Fødselsår finnes for alle i PDL, mens noen ikke har utfyllt fødselsdato
  var foedselsdato: LocalDate?,
  var foedselsaar: Int,
// Eventuell dødsdato til personen
  var doedsdato: LocalDate?
)


data class ForelderBarnRelasjonDto(
// Liste over alle hentede forekomster av foreldre-barnrelasjoner
  var forelderBarnRelasjon: List<ForelderBarnRelasjon>?
)

data class ForelderBarnRelasjon(
  val relatertPersonsIdent: String,
  val relatertPersonsRolle: ForelderBarnRelasjonRolle,
// Hvilken rolle personen i requesten har til personen i responsen
  val minRolleForPerson: ForelderBarnRelasjonRolle?
)

enum class ForelderBarnRelasjonRolle {
  BARN, FAR, MEDMOR, MOR
}


data class HusstandsmedlemmerDto(
// Periodisert liste over husstander for personen i requesten og husstandens medlemmer i perioden
  var husstandListe: List<Husstand>?
)

data class Husstand(
  val gyldigFraOgMed: LocalDate?,
  val gyldigTilOgMed: LocalDate?,
  var adressenavn: String? = null,
  var husnummer: String? = null,
  var husbokstav: String? = null,
  var bruksenhetsnummer: String? = null,
  var postnummer: String? = null,
  var bydelsnummer: String? = null,
  var kommunenummer: String? = null,
  val husstandsmedlemmerListe: List<Husstandsmedlemmer>
)

data class Husstandsmedlemmer(
  var personId: String? = null,
  var fornavn: String? = null,
  var mellomnavn: String? = null,
  var etternavn: String? = null,
  val gyldigFraOgMed: LocalDate?,
  val gyldigTilOgMed: LocalDate?
)


data class SivilstandDto(
//  Liste over alle hentede forekomster av sivilstand for personen i requesten
  var sivilstand: List<Sivilstand>?
)

data class Sivilstand(
  val type: String?,
  val gyldigFraOgMed: LocalDate?,
  val bekreftelsesdato: LocalDate?
)

