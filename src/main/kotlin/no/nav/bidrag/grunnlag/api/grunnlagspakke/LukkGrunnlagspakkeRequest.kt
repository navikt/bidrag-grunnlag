package no.nav.bidrag.grunnlag.api.grunnlagspakke

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime

data class LukkGrunnlagspakkeRequest(

  @Schema(description = "Grunnlagspakke-id. GyldigTil-dato settes lik dagens dato for angitt gunnlagspakke")
  val grunnlagspakkeId: Int = 0

)