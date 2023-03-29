package no.nav.bidrag.grunnlag.consumer.bidragperson.api

import java.time.LocalDate

// Response fra bidrag-person

data class NavnFoedselDoedResponseDto(
// Gir navn, fødselsdato og fødselsår for angitt person. Fødselsår finnes for alle i PDL, mens noen ikke har utfyllt fødselsdato
    var navn: String,
    val foedselsdato: LocalDate?,
    val foedselsaar: Int,
// Eventuell dødsdato til personen
    val doedsdato: LocalDate?
)

data class ForelderBarnRelasjonDto(
// Liste over alle hentede forekomster av foreldre-barnrelasjoner
    val forelderBarnRelasjon: List<ForelderBarnRelasjon>?
)

data class ForelderBarnRelasjon(
    val minRolleForPerson: ForelderBarnRelasjonRolle,
    val relatertPersonsIdent: String?,
// Hvilken rolle personen i requesten har til personen i responsen
    val relatertPersonsRolle: ForelderBarnRelasjonRolle
) {
    fun erRelatertPersonsRolleBarn(): Boolean = relatertPersonsRolle == ForelderBarnRelasjonRolle.BARN
}

enum class ForelderBarnRelasjonRolle {
    BARN, FAR, MEDMOR, MOR
}

data class HusstandsmedlemmerDto(
// Periodisert liste over husstander for personen i requesten og husstandens medlemmer i perioden
    val husstandListe: List<Husstand>?
)

data class Husstand(
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
    val husstandsmedlemmerListe: List<Husstandsmedlemmer>
)

data class Husstandsmedlemmer(
    val gyldigFraOgMed: LocalDate? = null,
    val gyldigTilOgMed: LocalDate? = null,
    val personId: String? = null,
    val fornavn: String? = null,
    val mellomnavn: String? = null,
    val etternavn: String? = null,
    var foedselsdato: LocalDate? = null,
    var doedsdato: LocalDate? = null
)

data class SivilstandDto(
//  Liste over alle hentede forekomster av sivilstand for personen i requesten
    val sivilstand: List<Sivilstand>?
)

data class Sivilstand(
    val type: String,
    val gyldigFraOgMed: LocalDate?,
    val bekreftelsesdato: LocalDate?
)

data class PersonRequest(
    var ident: String
)
