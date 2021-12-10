package no.nav.bidrag.grunnlag.api.grunnlagspakke

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Respons ved oppdatering av  grunnlagspakke")
data class OppdaterGrunnlagspakkeResponse(

  @Schema(description = "Grunnlagspakke-id")
  val grunnlagspakkeId: Int = 0,

  @Schema(description = "Liste over grunnlagene som er hentet inn med person-id og status")
  val grunnlagtypeResponsListe: List<HentGrunnlagResponse> = emptyList()
)
