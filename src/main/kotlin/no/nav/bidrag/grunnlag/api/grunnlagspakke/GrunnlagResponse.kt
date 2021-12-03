package no.nav.bidrag.grunnlag.api.grunnlagspakke

import io.swagger.v3.oas.annotations.media.Schema

data class GrunnlagResponse(

  @Schema(description = "Hvilken type grunnlag som er hentet")
  val grunnlagstype: String = "",

  @Schema(description = "Liste over resultatet av alle kall for å hente grunnlag. Inneholder personId og status på hver innhenting.")
  val hentGrunnlagkallResponseListe: List<HentGrunnlagkallResponse> = emptyList()
)