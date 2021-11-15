package no.nav.bidrag.grunnlag.api.grunnlagspakke

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.grunnlag.validation.DateValid
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class OppdaterGrunnlagspakkeRequest(

  @Schema(description = "Grunnlagspakke-id")
  @field:NotNull(message = "Kan ikke være null.")
  val grunnlagspakkeId: Int,

  @Schema(description = "Opplysningene som hentes er gyldige til (men ikke med) denne datoen (YYYY-MM-DD")
  @field:NotNull(message = "Kan ikke være null.")
  @field:DateTimeFormat(pattern = "yyyy-MM-dd")
  val gyldigTil: LocalDate,

  @Schema(description = "Liste over hvilke typer grunnlag som skal hentes inn. På nivået under er personId og perioder angitt")
  @field:NotEmpty(message = "Listen over forespurte grunnlag kan ikke være tom.")
  val grunnlagtypeRequestListe: List<GrunnlagstypeRequest>

)