package no.nav.bidrag.grunnlag.api

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.grunnlag.dto.InntektspostAinntektDto
import java.time.LocalDate
import java.time.LocalDateTime

data class HentKomplettInntektResponse(

  @Schema(description = "Inntekt-id")
  val inntektId: Int = 0,

  @Schema(description = "Grunnlagspakke-id")
  val grunnlagspakkeId: Int = 0,

  @Schema(description = "Id til personen inntekten er rapport for")
  val personId: Int = 0,

  @Schema(description = "Type/kilde til inntektsopplysninger")
  val type: String = "",

  @Schema(description = "Gyldig fra-dato")
  val gyldigFra: LocalDate = LocalDate.now(),

  @Schema(description = "Gyldig til-dato")
  val gyldigTil: LocalDate = LocalDate.now(),

  @Schema(description = "Angir om en inntektsopplysning er aktiv")
  val aktiv: Boolean = true,

  @Schema(description = "Hentet tidspunkt")
  val hentetTidspunkt: LocalDateTime = LocalDateTime.now(),

  @Schema(description = "Tidspunkt inntekten taes i bruk")
  val brukFra: LocalDateTime = LocalDateTime.now(),

  @Schema(description = "Tidspunkt inntekten ikke lenger aktiv. Null betyr at inntekten er aktiv")
  val brukTil: LocalDateTime? = null,

  @Schema(description = "Liste over poster for innhentede inntekter")
  val inntektspostListe: List<InntektspostAinntektDto> = emptyList(),

  )