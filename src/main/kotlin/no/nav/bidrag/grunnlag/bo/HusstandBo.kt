package no.nav.bidrag.grunnlag.bo

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.grunnlag.persistence.entity.Husstand
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.reflect.full.memberProperties

data class HusstandBo(

  @Schema(description = "Person-id til personen husstandsinformasjonen er hentet for")
  val personId: Int = 0,

  @Schema(description = "Personen bor i husstanden fra om med periode")
  val periodeFra: LocalDate,

  @Schema(description = "Personen bor i husstanden til om med periode")
  val periodeTil: LocalDate?,

  @Schema(description = "Navnet på en gate e.l.")
  val adressenavn: String?,

  @Schema(description = "Nummeret som identifiserer et av flere hus i en gate")
  val husnummer: String?,

  @Schema(description = "Bokstav som identifiserer del av et bygg")
  val husbokstav: String?,

  @Schema(description = "En bokstav og fire siffer som identifiserer en boligenhet innenfor et bygg eller en bygningsdel")
  val bruksenhetsnummer: String?,

  @Schema(description = "Norsk postnummer")
  val postnr: String?,

  @Schema(description = "6 siffer, identifiserer bydel")
  val bydelsnummer: String?,

  @Schema(description = "Siffer som identifiserer hvilken kommune adressen ligger i")
  val kommunenummer: String?,

  @Schema(description = "Nøkkel til geografisk adresse registrert i Kartverkets matrikkel")
  val matrikkelId: String?,

  @Schema(description = "Angis hvis barnet er manuelt registrert")
  val opprettetAv: String? = "",

  @Schema(description = "Lagret tidspunkt")
  val lagretTidspunkt: LocalDateTime = LocalDateTime.now()

)

fun HusstandBo.toHusstandEntity() = with(::Husstand) {
  val propertiesByName = HusstandBo::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      Husstand::husstandId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toHusstandEntity)
    }
  })

}