package no.nav.bidrag.grunnlag.api.ubst

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class HentUtvidetBarnetrygdOgSmaabarnstilleggResponse(

  @Schema(description = "Id til personen ubst er rapport for")
  val personId: String = "",

  @Schema(description = "Type stønad, utvidet barnetrygd eller småbarnstillegg")
  val type: String = "",

  @Schema(description = "Periode fra- og med måned")
  val periodeFra: LocalDate = LocalDate.now(),

  @Schema(description = "Periode til- og med måned")
  val periodeTil: LocalDate? = LocalDate.now(),

  @Schema(description = "Angir om en inntektsopplysning er aktiv")
  val aktiv: Boolean = true,

  @Schema(description = "Tidspunkt inntekten taes i bruk")
  val brukFra: LocalDateTime = LocalDateTime.now(),

  @Schema(description = "Tidspunkt inntekten ikke lenger aktiv. Null betyr at inntekten er aktiv")
  val brukTil: LocalDateTime? = null,

  @Schema(description = "Beløp")
  val belop: BigDecimal = BigDecimal.ZERO,

  @Schema(description = "Angir om stønaden er manuelt beregnet")
  val manueltBeregnet: Boolean = false,

  @Schema(description = "Hentet tidspunkt")
  val hentetTidspunkt: LocalDateTime = LocalDateTime.now()
)
