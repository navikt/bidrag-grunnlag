package no.nav.bidrag.grunnlag.api.skatt

import io.swagger.v3.oas.annotations.media.Schema

data class HentSkattegrunnlagResponse(

  @Schema(description = "Id til personen inntekten er rapport for")
  val personId: String = "",

  @Schema(description = "Liste over poster for innhentede inntekter")
  val inntektspostSkattListe: List<HentSkattegrunnlagspostResponse> = emptyList(),

  )