package no.nav.bidrag.grunnlag.api

import io.swagger.v3.oas.annotations.media.Schema

data class HentGrunnlagspakkeResponse(

  @Schema(description = "grunnlagspakke-id")
  val grunnlagspakkeId: Int = 0,

  @Schema(description = "Liste over innhentede inntekter og underliggende poster")
  val inntektListe: List<HentInntektResponse> = emptyList(),

  @Schema(description = "Liste over innhentede stønader")
  val stonadListe: List<HentStonadResponse> = emptyList()

)