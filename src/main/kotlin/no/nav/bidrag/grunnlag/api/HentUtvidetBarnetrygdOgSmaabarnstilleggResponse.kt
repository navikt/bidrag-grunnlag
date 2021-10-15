package no.nav.bidrag.grunnlag.api

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate

data class HentUtvidetBarnetrygdOgSmaabarnstilleggResponse(

  @Schema(description = "Id til personen ubst er rapport for")
  val personId: String = "",

  @Schema(description = "Type stønad, utvidet barnetrygd eller småbarnstillegg")
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
