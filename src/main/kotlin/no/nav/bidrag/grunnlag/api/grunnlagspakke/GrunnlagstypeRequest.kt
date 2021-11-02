package no.nav.bidrag.grunnlag.api.grunnlagspakke

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime

data class GrunnlagstypeRequest(

  @Schema(description = "Hvilken type grunnlag skal hentes")
  val grunnlagstype: String = "",

  @Schema(description = "Liste over hvilke personId'er og periode grunnlag skal hentes for")
  val personIdOgPeriodeRequestListe: List<PersonIdOgPeriodeRequest> = emptyList()

)