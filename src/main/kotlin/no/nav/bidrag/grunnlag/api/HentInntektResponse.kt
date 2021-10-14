package no.nav.bidrag.grunnlag.api

import io.swagger.v3.oas.annotations.media.Schema

data class HentInntektResponse(

  @Schema(description = "Id til personen inntekten er rapport for")
  val personId: Int = 0,

  @Schema(description = "Type/kilde til inntektsopplysninger")
  val type: String = "",

  @Schema(description = "Liste over poster for innhentede inntekter")
  val inntektspostListe: List<HentInntektspostResponse> = emptyList(),

  )