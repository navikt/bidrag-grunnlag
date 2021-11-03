package no.nav.bidrag.grunnlag.api.grunnlagspakke

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime

data class OppdaterGrunnlagspakkeRequest(

  @Schema(description = "Grunnlagspakke-id")
  val grunnlagspakkeId: Int = 0,

  @Schema(description = "Til hvilket formål skal grunnlagspakken benyttes. Bidrag, Forskudd, Særtilskudd")
  val formaal: String = "",

  @Schema(description = "Opplysningene som hentes er gyldige til (men ikke med) denne datoen (YYYY-MM-DD")
  val gyldigTil: String? = "",

  @Schema(description = "Liste over hvilke typer grunnlag som skal hentes inn. På nivået under er personId og perioder angitt")
  val grunnlagtypeRequestListe: List<GrunnlagstypeRequest> = emptyList()

)