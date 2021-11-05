package no.nav.bidrag.grunnlag.api.grunnlagspakke

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime

data class SettGyldigTilDatoForGrunnlagspakkeRequest(

  @Schema(description = "Grunnlagspakke-id")
  val grunnlagspakkeId: Int = 0,

  @Schema(description = "GyldigTil-dato som skal settes for angitt grunnlagspakke")
  val gyldigTil: String = ""

)