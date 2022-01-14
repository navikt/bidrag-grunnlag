package no.nav.bidrag.grunnlag.api.barnetillegg

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class HentBarnetilleggResponse(

  @Schema(description = "Id til personen barnetillegg er rapportert for")
  val partPersonId: String = "",

  @Schema(description = "Type barnetillegg")
  val barnetilleggType: String = "",

  @Schema(description = "Periode fra- og med måned")
  val periodeFra: LocalDate = LocalDate.now(),

  @Schema(description = "Periode til- og med måned")
  val periodeTil: LocalDate? = LocalDate.now(),

  @Schema(description = "Angir om en stønad er aktiv")
  val aktiv: Boolean = true,

  @Schema(description = "Tidspunkt stønaden taes i bruk")
  val brukFra: LocalDateTime = LocalDateTime.now(),

  @Schema(description = "Tidspunkt stønaden ikke lenger er aktiv. Null betyr at stønaden er aktiv")
  val brukTil: LocalDateTime? = null,

  @Schema(description = "Id til barnet barnetillegget er rapportert for")
  val barnPersonId: String = "",

  @Schema(description = "Bruttobeløp")
  val belopBrutto: BigDecimal = BigDecimal.ZERO,

  @Schema(description = "Angir om barnet er felles- eller særkullsbarn")
  val barnType: String = "",

  @Schema(description = "Hentet tidspunkt")
  val hentetTidspunkt: LocalDateTime = LocalDateTime.now()
)
