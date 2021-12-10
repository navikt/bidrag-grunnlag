package no.nav.bidrag.grunnlag.api.grunnlagspakke

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.grunnlag.service.GrunnlagsRequestStatus
import no.nav.bidrag.grunnlag.service.Grunnlagstype

data class HentGrunnlagResponse(

  @Schema(description = "Hvilken type grunnlag som er hentet")
  val grunnlagstype: Grunnlagstype,

  @Schema(description = "Angir personId som grunnlag er hentet for")
  val personId: String,

  @Schema(description = "Status for utført kall")
  val status: GrunnlagsRequestStatus,

  @Schema(description = "Statusmelding for utført kall")
  val statusMelding: String,

  )