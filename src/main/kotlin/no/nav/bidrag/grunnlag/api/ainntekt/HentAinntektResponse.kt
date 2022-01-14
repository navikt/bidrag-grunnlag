package no.nav.bidrag.grunnlag.api.ainntekt

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime

data class HentAinntektResponse(

  @Schema(description = "Id til personen inntekten er rapportert for")
  val personId: String = "",

  @Schema(description = "Periode fra-dato")
  val periodeFra: LocalDate = LocalDate.now(),

  @Schema(description = "Periode til-dato")
  val periodeTil: LocalDate = LocalDate.now(),

  @Schema(description = "Angir om en inntektsopplysning er aktiv")
  val aktiv: Boolean = true,

  @Schema(description = "Tidspunkt inntekten taes i bruk")
  val brukFra: LocalDateTime = LocalDateTime.now(),

  @Schema(description = "Tidspunkt inntekten ikke lenger er aktiv. Null betyr at inntekten er aktiv")
  val brukTil: LocalDateTime? = null,

  @Schema(description = "Hentet tidspunkt")
  val hentetTidspunkt: LocalDateTime = LocalDateTime.now(),

  @Schema(description = "Liste over poster for innhentede inntektsposter")
  val ainntektspostListe: List<HentAinntektspostResponse> = emptyList()
  )