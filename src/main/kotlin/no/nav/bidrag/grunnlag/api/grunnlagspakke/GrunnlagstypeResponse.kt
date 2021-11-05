package no.nav.bidrag.grunnlag.api.grunnlagspakke

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime

data class GrunnlagstypeResponse(

  @Schema(description = "Hvilken type grunnlag som er hentet")
  val grunnlagstype: String = "",

  @Schema(description = "Liste over resultatet av alle restkall. Inneholder personId og status på hver innhenting.")
  val restkallResponseListe: List<RestkallResponse> = emptyList()

)