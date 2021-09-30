package no.nav.bidrag.grunnlag.api

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.grunnlag.dto.InntektDto
import java.time.LocalDateTime

data class HentGrunnlagspakkeRequest(

  @Schema(description = "grunnlagspakke-id")
  val grunnlagspakkeId: Int = 0,

)