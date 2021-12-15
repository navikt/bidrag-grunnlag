package no.nav.bidrag.grunnlag.api.ainntekt

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate

data class HentAinntektspostResponse (

  @Schema(description = "Perioden innteksposten er utbetalt YYYYMM")
  val utbetalingsperiode: String? = "",

  @Schema(description = "Fra-dato for opptjening")
  val opptjeningsperiodeFra: LocalDate? = LocalDate.now(),

  @Schema(description = "Til-dato for opptjening")
  val opptjeningsperiodeTil: LocalDate? = LocalDate.now(),

  @Schema(description = "Id til de som rapporterer inn inntekten")
  val opplysningspliktigId: String? = "",

  @Schema(description = "Id til virksomheten som rapporterer inn inntekten")
  val virksomhetId: String? = "",

  @Schema(description = "Type inntekt, Lonnsinntekt, Naeringsinntekt, Pensjon eller trygd, Ytelse fra offentlig")
  val inntektType: String = "",

  @Schema(description = "Type fordel, Kontantytelse, Naturalytelse, Utgiftsgodtgjorelse")
  val fordelType: String? = "",

  @Schema(description = "Beskrivelse av inntekt")
  val beskrivelse: String? = "",

  @Schema(description = "Belop")
  val belop: BigDecimal = BigDecimal.ZERO,

  @Schema(description = "Fra-dato etterbetaling")
  val etterbetalingsperiodeFra: LocalDate?,

  @Schema(description = "Til-dato etterbetaling")
  val etterbetalingsperiodeTil: LocalDate?
)