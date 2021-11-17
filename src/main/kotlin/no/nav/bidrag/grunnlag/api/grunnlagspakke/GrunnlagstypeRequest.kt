package no.nav.bidrag.grunnlag.api.grunnlagspakke

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.grunnlag.service.Grunnlagstype
import javax.validation.Valid
import javax.validation.constraints.NotEmpty

data class GrunnlagstypeRequest(

  @Schema(description = "Hvilken type grunnlag skal hentes")
  val grunnlagstype: Grunnlagstype,

  @Schema(description = "Liste over hvilke personId'er og periode grunnlag skal hentes for")
  @field:Valid
  @field:NotEmpty(message = "Listen kan ikke være null eller tom.")
  val personIdOgPeriodeRequestListe: List<PersonIdOgPeriodeRequest>
)