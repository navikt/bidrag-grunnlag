package no.nav.bidrag.grunnlag.api.ainntekt

import io.swagger.v3.oas.annotations.media.Schema

data class HentInntektAinntektResponse(

  @Schema(description = "Id til personen inntekten er rapport for")
  val personId: String = "",

  @Schema(description = "Liste over poster for innhentede inntekter")
  val inntektspostAinntektListe: List<HentInntektspostAinntektResponse> = emptyList(),

  )