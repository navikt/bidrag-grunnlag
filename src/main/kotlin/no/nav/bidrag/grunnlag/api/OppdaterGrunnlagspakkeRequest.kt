package no.nav.bidrag.grunnlag.api

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime

data class OppdaterGrunnlagspakkeRequest(

  @Schema(description = "Grunnlagspakke-id")
  val grunnlagspakkeId: Int = 0,

  @Schema(description = "Behandling-type")
  val behandlingType: String = "",

  @Schema(description = "Liste over id'er det skal hentes ut grunnlag for (typisk BP, BM, BB)")
  val identListe: List<String> = emptyList(),

  @Schema(description = "Første periode det skal hentes ut grunnlag for (på formatet YYYYMM)")
  val periodeFom: String = "",

  @Schema(description = "Siste periode det skal hentes ut grunnlag for (på formatet YYYYMM)")
  val periodeTom: String = "",

  @Schema(description = "Opplysningene som hentes er gyldige til-og-med denne perioden (på formatet YYYYMM")
  val gyldigTom: String = ""

)