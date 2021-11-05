package no.nav.bidrag.grunnlag.api.grunnlagspakke

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Respons ved oppdatering av  grunnlagspakke")
data class OppdaterGrunnlagspakkeResponse(

  @Schema(description = "Grunnlagspakke-id")
  val grunnlagspakkeId: Int = 0,

  @Schema(description = "Liste over hvilke typer grunnlag som skal hentes inn. På nivået under er personId og status angitt")
  val grunnlagtypeResponsListe: List<GrunnlagstypeResponse> = emptyList()

)
