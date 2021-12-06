package no.nav.bidrag.grunnlag.api.grunnlagspakke

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import javax.validation.Valid
import javax.validation.constraints.NotEmpty

data class OppdaterGrunnlagspakkeRequest(

  @Schema(description = "Opplysningene som hentes er gyldige til (men ikke med) denne datoen (YYYY-MM-DD")
  val gyldigTil: LocalDate? = null,

  @Schema(description = "Liste over hvilke typer grunnlag som skal hentes inn. På nivået under er personId og perioder angitt")
  @field:Valid
  @field:NotEmpty(message = "Listen kan ikke være null eller tom.")
  val grunnlagRequestListe: List<GrunnlagRequest>
)