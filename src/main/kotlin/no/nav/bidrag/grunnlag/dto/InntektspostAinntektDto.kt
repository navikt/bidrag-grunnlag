package no.nav.bidrag.grunnlag.dto

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.grunnlag.persistence.entity.InntektspostAinntekt
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.reflect.full.memberProperties

data class InntektspostAinntektDto (

  @Schema(description = "Inntektspost-id")
  val inntektspostId: Int = 0,

  @Schema(description = "Inntekt-id")
  val inntektId: Int = 0,

  @Schema(description = "Perioden innteksposten er utbetalt YYYYMM")
  val utbetalingsperiode: String = "",

  @Schema(description = "Fra-dato for opptjening")
  val opptjeningsperiodeFra: LocalDate = LocalDate.now(),

  @Schema(description = "Til-dato for opptjening")
  val opptjeningsperiodeTil: LocalDate = LocalDate.now(),

  @Schema(description = "Id til de som rapporterer inn inntekten")
  val opplysningspliktigId: String = "",

  @Schema(description = "Type inntekt, Lonnsinntekt, Naeringsinntekt, Pensjon eller trygd, Ytelse fra offentlig")
  val type: String = "",

  @Schema(description = "Type fordel, Kontantytelse, Naturalytelse, Utgiftsgodtgjorelse")
  val fordelType: String = "",

  @Schema(description = "Beskrivelse av inntekt")
  val beskrivelse: String = "",

  @Schema(description = "Belop")
  val belop: BigDecimal = BigDecimal.ZERO
)

fun InntektspostAinntektDto.toInntektspostAinntektEntity() = with(::InntektspostAinntekt) {
  val propertiesByName = InntektspostAinntektDto::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      else -> propertiesByName[parameter.name]?.get(this@toInntektspostAinntektEntity)
    }
  })

}