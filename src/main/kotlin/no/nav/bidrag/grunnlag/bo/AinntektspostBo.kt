package no.nav.bidrag.grunnlag.bo

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.grunnlag.persistence.entity.Ainntektspost

import java.math.BigDecimal
import java.time.LocalDate
import kotlin.reflect.full.memberProperties

data class AinntektspostBo(

  @Schema(description = "Inntekt-id")
  val inntektId: kotlin.Int = 0,

  @Schema(description = "Perioden inntektsposten er utbetalt YYYYMM")
  val utbetalingsperiode: kotlin.String?,

  @Schema(description = "Fra-dato for opptjening")
  val opptjeningsperiodeFra: java.time.LocalDate?,

  @Schema(description = "Til-dato for opptjening")
  val opptjeningsperiodeTil: java.time.LocalDate?,

  @Schema(description = "Id til de som rapporterer inn inntekten")
  val opplysningspliktigId: kotlin.String?,

  @Schema(description = "Id til virksomheten som rapporterer inn inntekten")
  val virksomhetId: kotlin.String?,

  @Schema(description = "Type inntekt, Lonnsinntekt, Naeringsinntekt, Pensjon eller trygd, Ytelse fra offentlig")
  val inntektType: kotlin.String,

  @Schema(description = "Type fordel, Kontantytelse, Naturalytelse, Utgiftsgodtgjorelse")
  val fordelType: kotlin.String?,

  @Schema(description = "Beskrivelse av inntekt")
  val beskrivelse: kotlin.String?,

  @Schema(description = "Belop")
  val belop: java.math.BigDecimal,

  @Schema(description = "Fra-dato etterbetaling")
  val etterbetalingsperiodeFra: java.time.LocalDate?,

  @Schema(description = "Til-dato etterbetaling")
  val etterbetalingsperiodeTil: java.time.LocalDate?
)

fun AinntektspostBo.toAinntektspostEntity() = with(::Ainntektspost) {
  val propertiesByName = AinntektspostBo::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      Ainntektspost::inntektspostId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toAinntektspostEntity)
    }
  })

}