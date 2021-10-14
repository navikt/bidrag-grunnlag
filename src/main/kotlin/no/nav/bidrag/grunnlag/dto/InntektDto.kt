package no.nav.bidrag.grunnlag.dto

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.grunnlag.persistence.entity.Inntekt
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.reflect.full.memberProperties

data class InntektDto (

  @Schema(description = "Inntekt-id")
  val inntektId: Int = 0,

  @Schema(description = "Grunnlagspakke-id")
  val grunnlagspakkeId: Int = 0,

  @Schema(description = "Id til personen inntekten er rapport for")
  val personId: Int = 0,

  @Schema(description = "Type/kilde til inntektsopplysninger")
  val type: String = "",

  @Schema(description = "Gyldig fra-dato")
  val gyldigFra: LocalDate = LocalDate.now(),

  @Schema(description = "Gyldig til-dato")
  val gyldigTil: LocalDate = LocalDate.now(),

  @Schema(description = "Angir om en inntektsopplysning er aktiv")
  val aktiv: Boolean = true,

  @Schema(description = "Hentet tidspunkt")
  val hentetTidspunkt: LocalDateTime = LocalDateTime.now(),

  @Schema(description = "Tidspunkt inntekten taes i bruk")
  val brukFra: LocalDateTime = LocalDateTime.now(),

  @Schema(description = "Tidspunkt inntekten ikke lenger aktiv. Null betyr at inntekten er aktiv")
  val brukTil: LocalDateTime? = null

)

fun InntektDto.toInntektEntity() = with(::Inntekt) {
  val propertiesByName = InntektDto::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      else -> propertiesByName[parameter.name]?.get(this@toInntektEntity)
    }
  })

}