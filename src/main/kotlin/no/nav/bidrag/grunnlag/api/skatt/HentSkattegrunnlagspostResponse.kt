package no.nav.bidrag.grunnlag.api.skatt

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate

data class HentSkattegrunnlagspostResponse(

  @Schema(description = "Type skattegrunnlag, ordinær eller Svalbard")
  val skattegrunnlagType: String = "",

  @Schema(description = "Type inntekt, Lonnsinntekt, Naeringsinntekt, Pensjon eller trygd, Ytelse fra offentlig")
  val inntektType: String = "",

  @Schema(description = "Belop")
  val belop: BigDecimal = BigDecimal.ZERO
)