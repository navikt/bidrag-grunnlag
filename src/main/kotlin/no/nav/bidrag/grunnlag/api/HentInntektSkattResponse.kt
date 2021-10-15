package no.nav.bidrag.grunnlag.api

import io.swagger.v3.oas.annotations.media.Schema

data class HentInntektSkattResponse(

  @Schema(description = "Id til personen inntekten er rapport for")
  val personId: String = "",

/*  @Schema(description = "Type/kilde til inntektsopplysninger")
  val type: String = "",*/

  @Schema(description = "Liste over poster for innhentede inntekter")
  val inntektspostSkattListe: List<HentInntektspostSkattResponse> = emptyList(),

  )