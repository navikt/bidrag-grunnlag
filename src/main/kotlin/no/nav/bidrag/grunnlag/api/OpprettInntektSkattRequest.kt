package no.nav.bidrag.grunnlag.api

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.grunnlag.dto.InntektAinntektDto
import no.nav.bidrag.grunnlag.dto.InntektSkattDto
import java.time.LocalDate

import kotlin.reflect.full.memberProperties

@Schema(description ="Egenskaper ved en inntekt fra skatt")
data class OpprettInntektSkattRequest(

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
  val inntektspostListe: List<OpprettInntektspostSkattRequest> = emptyList()
)

fun OpprettInntektSkattRequest.toInntektSkattDto(grunnlagspakkeId: Int) = with(::InntektSkattDto) {
  val propertiesByName = OpprettInntektSkattRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      InntektSkattDto::grunnlagspakkeId.name -> grunnlagspakkeId
      else -> propertiesByName[parameter.name]?.get(this@toInntektSkattDto)
    }
  })
}

fun OpprettInntektSkattRequest.toInntektSkattDto() = with(::InntektSkattDto) {
  val propertiesByName = OpprettInntektSkattRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      InntektSkattDto::inntektId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toInntektSkattDto)
    }
  })
}