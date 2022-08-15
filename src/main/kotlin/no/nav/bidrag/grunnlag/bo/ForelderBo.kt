package no.nav.bidrag.grunnlag.bo

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.grunnlag.persistence.entity.Forelder
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.reflect.full.memberProperties

data class ForelderBo(

  @Schema(description = "Grunnlagspakke-id")
  val grunnlagspakkeId: Int = 0,

  @Schema(description = "Person-id til angitt person")
  val personId: String,

  @Schema(description = "Personens navn")
  val navn: String?,

  @Schema(description = "Personens fødselsdato")
  val foedselsdato: LocalDate?,

  @Schema(description = "Personens eventuelle dødsdato")
  val doedsdato: LocalDate?,

  @Schema(description = "Angir om en personopplysning er aktiv")
  val aktiv: Boolean = true,

  @Schema(description = "Tidspunkt personopplysning taes i bruk")
  val brukFra: LocalDateTime = LocalDateTime.now(),

  @Schema(description = "Tidspunkt personopplysningen ikke lenger er aktiv. Null betyr at personopplysningen er aktiv")
  val brukTil: LocalDateTime? = null,

  @Schema(description = "Manuelt opprettet av")
  val opprettetAv: String?,

  @Schema(description = "Opprettet tidspunkt")
  val hentetTidspunkt: LocalDateTime = LocalDateTime.now()

)

fun ForelderBo.toPersonEntity() = with(::Forelder) {
  val propertiesByName = ForelderBo::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      Forelder::forelderId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toPersonEntity)
    }
  })

}