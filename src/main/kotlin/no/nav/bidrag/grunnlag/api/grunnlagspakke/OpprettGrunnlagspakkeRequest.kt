package no.nav.bidrag.grunnlag.api.grunnlagspakke

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.grunnlag.service.Formaal
import javax.validation.constraints.NotBlank

@Schema(description ="Request for å opprette ny grunnlagspakke, uten annet innhold")
data class OpprettGrunnlagspakkeRequest (

  @Schema(description = "Til hvilket formål skal grunnlagspakken benyttes. BIDRAG, FORSKUDD eller SAERTILSKUDD")
  val formaal: Formaal,

  @Schema(description = "opprettet av")
  @field:NotBlank(message = "Kan ikke være null eller blank.")
  val opprettetAv: String
)