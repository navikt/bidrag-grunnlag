package no.nav.bidrag.grunnlag.api

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.grunnlag.dto.InntektDto
import java.time.LocalDate

import kotlin.reflect.full.memberProperties

@Schema(description ="Egenskaper ved en inntekt")
data class NyInntektRequest(

  @Schema(description = "Id til personen inntekten er rapport på")
  val personId: Int = 0,

  @Schema(description = "Type/kilde til inntektsopplysninger")
  val type: String = "",

  @Schema(description = "Gyldig fra-dato")
  val gyldigFra: LocalDate = LocalDate.now(),

  @Schema(description = "Gyldig til-dato")
  val gyldigTil: LocalDate = LocalDate.now(),

  @Schema(description = "Angir om en inntektsopplysning er aktiv")
  val aktiv: Boolean = true,

  @Schema(description = "Liste over alle inntektsposter som inngår i inntekten")
  val inntektspostListe: List<NyInntektspostRequest> = emptyList()
)

fun NyInntektRequest.toInntektDto(grunnlagspakkeId: Int) = with(::InntektDto) {
  val propertiesByName = NyInntektRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      InntektDto::grunnlagspakkeId.name -> grunnlagspakkeId
      else -> propertiesByName[parameter.name]?.get(this@toInntektDto)
    }
  })
}

fun NyInntektRequest.toInntektDto() = with(::InntektDto) {
  val propertiesByName = NyInntektRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      InntektDto::inntektId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toInntektDto)
    }
  })
}