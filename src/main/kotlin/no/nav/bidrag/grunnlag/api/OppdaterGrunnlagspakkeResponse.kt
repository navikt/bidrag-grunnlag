package no.nav.bidrag.grunnlag.api

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Respons ved oppdatering av  grunnlagspakke")
data class OppdaterGrunnlagspakkeResponse(

  @Schema(description = "Status")
  val status: String = ""

)
