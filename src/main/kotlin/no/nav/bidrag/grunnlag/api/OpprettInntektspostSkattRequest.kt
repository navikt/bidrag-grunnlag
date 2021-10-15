package no.nav.bidrag.grunnlag.api

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.grunnlag.dto.InntektspostSkattDto
import java.math.BigDecimal
import kotlin.reflect.full.memberProperties

@Schema(description ="Egenskaper ved en inntektspost")
data class OpprettInntektspostSkattRequest(

  @Schema(description = "Type inntekt, Lonnsinntekt, Naeringsinntekt, Pensjon eller trygd, Ytelse fra offentlig")
  val inntektType: String = "",

  @Schema(description = "Belop")
  val belop: BigDecimal = BigDecimal.ZERO
)

fun OpprettInntektspostSkattRequest.toInntektspostSkattDto(inntektId: Int) = with(::InntektspostSkattDto) {
  val propertiesByName = OpprettInntektspostSkattRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      InntektspostSkattDto::inntektId.name -> inntektId
      else -> propertiesByName[parameter.name]?.get(this@toInntektspostSkattDto)
    }
  })
}

fun OpprettInntektspostSkattRequest.toInntektspostSkattDto() = with(::InntektspostSkattDto) {
  val propertiesByName = OpprettInntektspostSkattRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      InntektspostSkattDto::inntektspostId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toInntektspostSkattDto)
    }
  })
}