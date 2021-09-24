package no.nav.bidrag.grunnlag.api

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.grunnlag.dto.InntektDto
import no.nav.bidrag.grunnlag.dto.InntektspostDto
import java.math.BigDecimal
import java.time.LocalDate

import kotlin.reflect.full.memberProperties

@Schema(description ="Egenskaper ved en inntektspost")
data class NyInntektspostRequest(

  @Schema(description = "Perioden innteksposten er utbetalt YYYYMM")
  val utbetalingsperiode: String = "",

  @Schema(description = "Fra-dato for opptjening")
  val opptjeningsperiodeFra: LocalDate = LocalDate.now(),

  @Schema(description = "Til-dato for opptjening")
  val opptjeningsperiodeTil: LocalDate = LocalDate.now(),

  @Schema(description = "Id til de som rapporterer inn inntekten")
  val opplysningspliktigId: String = "",

  @Schema(description = "Type inntekt, Lonnsinntekt, Naeringsinntekt, Pensjon eller trygd, Ytelse fra offentlig")
  val inntektType: String = "",

  @Schema(description = "Type fordel, Kontantytelse, Naturalytelse, Utgiftsgodtgjorelse")
  val fordelType: String = "",

  @Schema(description = "Beskrivelse av inntekt")
  val beskrivelse: String = "",

  @Schema(description = "Belop")
  val belop: BigDecimal = BigDecimal.ZERO
)

fun NyInntektspostRequest.toInntektspostDto(inntektId: Int) = with(::InntektspostDto) {
  val propertiesByName = NyInntektspostRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      InntektspostDto::inntektId.name -> inntektId
      else -> propertiesByName[parameter.name]?.get(this@toInntektspostDto)
    }
  })
}

fun NyInntektspostRequest.toInntektspostDto() = with(::InntektspostDto) {
  val propertiesByName = NyInntektspostRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      InntektspostDto::inntektspostId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toInntektspostDto)
    }
  })
}