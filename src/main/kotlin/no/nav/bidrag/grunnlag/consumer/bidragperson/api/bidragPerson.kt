package no.nav.bidrag.grunnlag.consumer.bidragperson.api

import java.time.LocalDate

// Request til bidrag-person

data class ForelderBarnRequest (
  val personId: String,
  val periodeFra: LocalDate
)

data class HusstandsmedlemmerRequest(
  val personId: String,
  val periodeFra: LocalDate
)

data class SivilstandRequest(
  val personId: String,
  val periodeFra: LocalDate
)


// Response fra bidrag-person

data class NavnFoedselDoedResponseDto(
// Gir navn, fødselsdato og fødselsår for angitt person. Fødselsår finnes for alle i PDL, mens noen ikke har utfyllt fødselsdato
  var navn: String?,
  val foedselsdato: LocalDate?,
  val foedselsaar: Int,
// Eventuell dødsdato til personen
  val doedsdato: LocalDate?
)

data class ForelderBarnRelasjonResponseDto(
// Liste over alle hentede forekomster av foreldre-barnrelasjoner
  val forelderBarnRelasjonResponse: List<ForelderBarnRelasjonResponse>?
)

data class ForelderBarnRelasjonResponse(
  val relatertPersonsIdent: String,
  val relatertPersonsRolle: ForelderBarnRelasjonRolle,
// Hvilken rolle personen i requesten har til personen i responsen
  val minRolleForPerson: ForelderBarnRelasjonRolle?
)

enum class ForelderBarnRelasjonRolle {
  BARN, FAR, MEDMOR, MOR
}


data class HusstandsmedlemmerResponseDto(
// Periodisert liste over husstander for personen i requesten og husstandens medlemmer i perioden
  val husstandResponseListe: List<HusstandResponse>?
)

data class HusstandResponse(
  val gyldigFraOgMed: LocalDate? = null,
  val gyldigTilOgMed: LocalDate? = null,
  val adressenavn: String? = null,
  val husnummer: String? = null,
  val husbokstav: String? = null,
  val bruksenhetsnummer: String? = null,
  val postnummer: String? = null,
  val bydelsnummer: String? = null,
  val kommunenummer: String? = null,
  val matrikkelId: Long? = null,
  val husstandsmedlemmerResponseListe: List<HusstandsmedlemmerResponse>
)

data class HusstandsmedlemmerResponse(
  val gyldigFraOgMed: LocalDate? = null,
  val gyldigTilOgMed: LocalDate? = null,
  val personId: String? = null,
  val fornavn: String? = null,
  val mellomnavn: String? = null,
  val etternavn: String? = null,
  var foedselsdato: LocalDate? = null,
  var doedsdato: LocalDate? = null,
)

data class PersonResponseDto(
  val ident: String = "",
  val navn: String? = null,
  val doedsdato: LocalDate? = null,
  val diskresjonskode: String? = null,
  val aktoerId: String? = null

)


data class SivilstandResponseDto(
//  Liste over alle hentede forekomster av sivilstand for personen i requesten
  val sivilstand: List<SivilstandResponse>?
)

data class SivilstandResponse(
  val type: String,
  val gyldigFraOgMed: LocalDate?,
  val bekreftelsesdato: LocalDate?
)

