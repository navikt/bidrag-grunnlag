package no.nav.bidrag.grunnlag.bo

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.grunnlag.persistence.entity.Husstandsmedlem
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.reflect.full.memberProperties

data class HusstandsmedlemBo(

  @Schema(description = "Generert Id til husstanden")
  var husstandId: String?,

  @Schema(description = "Identen til husstandsmedlemmet")
  var personId: String?,

  @Schema(description = "Navn på husstandsmedlemmet, format <Fornavn, mellomnavn, Etternavn")
  var navn: String?,

  @Schema(description = "Husstandsmedlemmet bor i husstanden fra- og med måned")
  val periodeFra: LocalDate,

  @Schema(description = "Husstandsmedlemmet bor i husstanden til- og med måned")
  val periodeTil: LocalDate?,

  @Schema(description = "Manuelt opprettet av")
  val opprettetAv: String?,

  @Schema(description = "Opprettet tidspunkt")
  val opprettetTidspunkt: LocalDateTime
)

fun HusstandsmedlemBo.toHusstandsmedlemEntity() = with(::Husstandsmedlem) {
  val propertiesByName = HusstandsmedlemBo::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      Husstandsmedlem::husstandsmedlemId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toHusstandsmedlemEntity)
    }
  })

}