package no.nav.bidrag.grunnlag.api

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Respons ved opprettelse av ny grunnlagspakke")
data class NyGrunnlagspakkeResponse(

  @Schema(description = "Id til opprettet grunnlagspakke")
  val grunnlagspakkeId: Int = 0

)
