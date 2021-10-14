package no.nav.bidrag.grunnlag.api

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate

data class HentStonadResponse(

  @Schema(description = "Id til personen inntekten er rapport for")
  val personId: Int = 0,

  @Schema(description = "Type stønad")
  val type: String = "",

  @Schema(description = "Periode fra- og med måned")
  val periodeFra: LocalDate = LocalDate.now(),

  @Schema(description = "Periode til- og med måned")
  val periodeTil: LocalDate = LocalDate.now(),

  @Schema(description = "Beløp")
  val belop: BigDecimal = BigDecimal.ZERO,

  @Schema(description = "Angir om stønaden er manuelt beregnet")
  val manueltBeregnet: Boolean = false

)
