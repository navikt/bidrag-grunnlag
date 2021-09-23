package no.nav.bidrag.grunnlag

import no.nav.bidrag.grunnlag.api.NyGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.dto.GrunnlagspakkeDto
import java.time.LocalDateTime

class TestUtil {

  companion object {

    fun byggNyGrunnlagspakkeRequest() = NyGrunnlagspakkeRequest(
      opprettetAv = "RTV9999"
    )

    fun byggGrunnlagspakkeDto() = GrunnlagspakkeDto(
      grunnlagspakkeId = (1..100).random(),
      opprettetAv = "RTV9999",
      opprettetTimestamp = LocalDateTime.now(),
      endretTimestamp = LocalDateTime.now()
    )
  }
}