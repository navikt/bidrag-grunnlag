package no.nav.bidrag.grunnlag.api

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.grunnlag.dto.InntektSkattDto
import java.time.LocalDate

import kotlin.reflect.full.memberProperties

@Schema(description ="Egenskaper ved en inntekt")
data class OpprettInntektRequest(

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
  val inntektspostListe: List<OpprettInntektspostRequest> = emptyList()
)

fun OpprettInntektRequest.toInntektDto(grunnlagspakkeId: Int) = with(::InntektSkattDto) {
  val propertiesByName = OpprettInntektRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      InntektSkattDto::grunnlagspakkeId.name -> grunnlagspakkeId
      else -> propertiesByName[parameter.name]?.get(this@toInntektDto)
    }
  })
}

fun OpprettInntektRequest.toInntektDto() = with(::InntektSkattDto) {
  val propertiesByName = OpprettInntektRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      InntektSkattDto::inntektId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toInntektDto)
    }
  })
}