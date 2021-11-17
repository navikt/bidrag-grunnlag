package no.nav.bidrag.grunnlag.api.grunnlagspakke

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime

data class PersonIdOgPeriodeRequest(

  @Schema(description = "Angir personId som grunnlag skal hentes for")
  val personId: String = "",

  @Schema(description = "Første periode det skal hentes ut grunnlag for (på formatet YYYY-MM-DD)")
  val periodeFra: LocalDate = LocalDate.now(),

  @Schema(description = "Grunnlag skal hentes TIL denne perioden, på formatet YYYY-MM-DD")
  val periodeTil: LocalDate = LocalDate.now()
  )