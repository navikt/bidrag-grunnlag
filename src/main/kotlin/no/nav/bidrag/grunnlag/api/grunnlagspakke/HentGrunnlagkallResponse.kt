package no.nav.bidrag.grunnlag.api.grunnlagspakke

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.http.HttpStatus
import java.time.LocalDate
import java.time.LocalDateTime

data class HentGrunnlagkallResponse(

  @Schema(description = "Angir personId som grunnlag er hentet for")
  val personId: String = "",

  @Schema(description = "Status på utført kall")
  val status: String = "",

  @Schema(description = "HttpStatus på utført kall")
  val statuskode: HttpStatus = HttpStatus.OK
)