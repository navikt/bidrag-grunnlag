package no.nav.bidrag.grunnlag.dto

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.grunnlag.comparator.IComparable
import no.nav.bidrag.grunnlag.persistence.entity.Ainntekt

import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.reflect.full.memberProperties

data class AinntektDto(

  @Schema(description = "Inntekt-id")
  val inntektId: Int = 0,

  @Schema(description = "Grunnlagspakke-id")
  val grunnlagspakkeId: Int = 0,

  @Schema(description = "Id til personen inntekten er rapport for")
  val personId: String = "",

  @Schema(description = "Periode fra-dato")
  override val periodeFra: LocalDate = LocalDate.now(),

  @Schema(description = "Periode til-dato")
  override val periodeTil: LocalDate = LocalDate.now(),

  @Schema(description = "Angir om en inntektsopplysning er aktiv")
  val aktiv: Boolean = true,

  @Schema(description = "Tidspunkt inntekten taes i bruk")
  val brukFra: LocalDateTime = LocalDateTime.now(),

  @Schema(description = "Tidspunkt inntekten ikke lenger aktiv. Null betyr at inntekten er aktiv")
  val brukTil: LocalDateTime? = null,

  @Schema(description = "Hentet tidspunkt")
  val hentetTidspunkt: LocalDateTime = LocalDateTime.now()
  ): IComparable<Ainntekt> {
  override fun expire(brukTil: LocalDateTime): Ainntekt {
    return this.copy(brukTil = brukTil, aktiv = false).toAinntektEntity()
  }

  override fun update(hentetTidspunkt: LocalDateTime): Ainntekt {
    return this.copy(hentetTidspunkt = hentetTidspunkt).toAinntektEntity()
  }

  override fun create(): Ainntekt {
    return this.toAinntektEntity()
  }
}

fun AinntektDto.toAinntektEntity() = with(::Ainntekt) {
  val propertiesByName = AinntektDto::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      else -> propertiesByName[parameter.name]?.get(this@toAinntektEntity)
    }
  })

}