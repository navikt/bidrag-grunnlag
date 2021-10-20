package no.nav.bidrag.grunnlag.api.grunnlagspakke

import io.swagger.v3.oas.annotations.media.Schema

data class HentGrunnlagspakkeRequest(

  @Schema(description = "grunnlagspakke-id")
  val grunnlagspakkeId: Int = 0,
)