package no.nav.bidrag.grunnlag.api

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.grunnlag.persistence.entity.Grunnlagspakke
import no.nav.bidrag.grunnlag.persistence.entity.Inntekt
import no.nav.bidrag.grunnlag.persistence.entity.Inntektspost
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.reflect.full.memberProperties

data class HentInntektspostResponse (

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