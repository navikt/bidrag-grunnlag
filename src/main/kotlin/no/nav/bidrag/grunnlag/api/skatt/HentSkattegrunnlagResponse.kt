package no.nav.bidrag.grunnlag.api.skatt

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class HentSkattegrunnlagResponse(

  @Schema(description = "Id til personen inntekten er rapport for")
  val personId: String = "",

  @Schema(description = "Periode fra")
  val periodeFra: LocalDate = LocalDate.now(),

  @Schema(description = "Periode frem til")
  val periodeTil: LocalDate = LocalDate.now(),

  @Schema(description = "Liste over poster med skattegrunnlag")
  val skattegrunnlagListe: List<HentSkattegrunnlagspostResponse> = emptyList(),

  )