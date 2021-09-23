package no.nav.bidrag.grunnlag.api

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class HentGrunnlagResponse (

  @Schema(description = "grunnlagspakke-id")
  val grunnlagspakkeId: Int = 0,

  @Schema(description = "opprettet av")
  val opprettetAv: String = "",

  @Schema(description = "opprettet timestamp")
  val opprettetTimestamp: LocalDateTime = LocalDateTime.now(),

  @Schema(description = "Endret timestamp")
  val endretTimestamp: LocalDateTime? = null


    )