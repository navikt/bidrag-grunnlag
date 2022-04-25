package no.nav.bidrag.grunnlag.api.grunnlagspakke

import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.Valid
import javax.validation.constraints.NotEmpty

data class OppdaterGrunnlagspakkeRequestDto(

  @Schema(description = "Liste over hvilke typer grunnlag som skal hentes inn. På nivået under er personId og perioder angitt")
  @field:Valid
  @field:NotEmpty(message = "Listen kan ikke være null eller tom.")
  val grunnlagRequestDtoListe: List<GrunnlagRequestDto>
)