package no.nav.bidrag.grunnlag.api.grunnlagspakke

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.grunnlag.service.GrunnlagType
import java.time.LocalDate
import javax.validation.constraints.Pattern

data class GrunnlagRequest(

  @Schema(description = "Hvilken type grunnlag skal hentes")
  val grunnlagType: GrunnlagType,

  @Schema(description = "Angir personId som grunnlag skal hentes for")
  @field:Pattern(regexp = "^[0-9]{11}\$", message = "Ugyldig format. Må inneholde eksakt 11 siffer.")
  val personId: String,

  @Schema(description = "Første periode det skal hentes ut grunnlag for (på formatet YYYY-MM-DD)")
  val periodeFra: LocalDate,

  @Schema(description = "Grunnlag skal hentes TIL denne perioden, på formatet YYYY-MM-DD")
  val periodeTil: LocalDate,

  // Denne er ikke i bruk lenger og kan egentlig fjernes (men må synkes med Bisys og bidrag-gcp-proxy)
  @Schema(description = "Hent historiske inntekter fra Inntektskomponenten på angitt dato, skal kun brukes til engangsinnhenting av historikk")
  val innsynHistoriskeInntekterDato: LocalDate? = null
)
