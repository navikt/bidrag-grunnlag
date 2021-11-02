package no.nav.bidrag.grunnlag.api.grunnlagspakke

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime

data class RestkallResponse(

  @Schema(description = "Angir personId som grunnlag skal hentes for")
  val personId: String = "",

  @Schema(description = "Status på utført restkall")
  val status: String = ""

)