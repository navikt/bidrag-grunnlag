package no.nav.bidrag.grunnlag

import no.nav.bidrag.grunnlag.api.NyGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.api.NyInntektRequest
import no.nav.bidrag.grunnlag.dto.GrunnlagspakkeDto
import no.nav.bidrag.grunnlag.dto.InntektDto
import java.time.LocalDate
import java.time.LocalDateTime

class TestUtil {

  companion object {

    fun byggNyGrunnlagspakkeRequest() = NyGrunnlagspakkeRequest(
      opprettetAv = "RTV9999"
    )

    fun byggNyInntektRequest() = NyInntektRequest(
      personId = 1234567,
      type = "Lønnsinntekt",
      gyldigFra = LocalDate.now(),
      gyldigTil = LocalDate.now(),
      aktiv = true
    )

    fun byggGrunnlagspakkeDto() = GrunnlagspakkeDto(
      grunnlagspakkeId = (1..100).random(),
      opprettetAv = "RTV9999",
      opprettetTimestamp = LocalDateTime.now(),
      endretTimestamp = LocalDateTime.now()
    )

    fun byggInntektDto() = InntektDto(
      inntektId = (1..100).random(),
      grunnlagspakkeId = (1..100).random(),
      personId = 1234567,
      type = "Lønnsinntekt",
      gyldigFra = LocalDate.now(),
      gyldigTil = LocalDate.now(),
      aktiv = true,
      hentetTidspunkt = LocalDateTime.now(),
      brukFra = LocalDateTime.now(),
      brukTil = null
    )

  }
}