package no.nav.bidrag.grunnlag.api.grunnlagspakke

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.grunnlag.service.GrunnlagType
import no.nav.bidrag.grunnlag.service.GrunnlagsRequestStatus

data class HentGrunnlagResponse(

  @Schema(description = "Hvilken type grunnlag som er hentet")
  val grunnlagType: GrunnlagType,

  @Schema(description = "Angir personId som grunnlag er hentet for")
  val personId: String,

  @Schema(description = "Status for utført kall")
  val status: GrunnlagsRequestStatus,

  @Schema(description = "Statusmelding for utført kall")
  val statusMelding: String,

  )