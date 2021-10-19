/*
package no.nav.bidrag.grunnlag.api.ainntekt

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.grunnlag.dto.InntektAinntektDto
import java.time.LocalDate
import kotlin.reflect.full.memberProperties

@Schema(description ="Egenskaper ved en inntekt fra a-inntekt")
data class OpprettInntektAinntektRequest(

  @Schema(description = "Id til personen inntekten er rapport på")
  val personId: String = "",

  @Schema(description = "Type/kilde til inntektsopplysninger")
  val type: String = "",

  @Schema(description = "Gyldig fra-dato")
  val gyldigFra: LocalDate = LocalDate.now(),

  @Schema(description = "Gyldig til-dato")
  val gyldigTil: LocalDate = LocalDate.now(),

  @Schema(description = "Angir om en inntektsopplysning er aktiv")
  val aktiv: Boolean = true,

  @Schema(description = "Liste over alle inntektsposter som inngår i inntekten")
  val inntektspostListe: List<OpprettInntektspostAinntektRequest> = emptyList()
)

fun OpprettInntektAinntektRequest.toInntektAinntektDto(grunnlagspakkeId: Int) = with(::InntektAinntektDto) {
  val propertiesByName = OpprettInntektAinntektRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      InntektAinntektDto::grunnlagspakkeId.name -> grunnlagspakkeId
      else -> propertiesByName[parameter.name]?.get(this@toInntektAinntektDto)
    }
  })
}

fun OpprettInntektAinntektRequest.toInntektAinntektDto() = with(::InntektAinntektDto) {
  val propertiesByName = OpprettInntektAinntektRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      InntektAinntektDto::inntektId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toInntektAinntektDto)
    }
  })
}*/
