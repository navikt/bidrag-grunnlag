package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.grunnlag.api.FinnGrunnlagResponse
import no.nav.bidrag.grunnlag.api.NyGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.api.NyGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.dto.GrunnlagspakkeDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class GrunnlagspakkeService (val persistenceService: PersistenceService){

  fun opprettGrunnlagspakke (nyGrunnlagspakkeRequest: NyGrunnlagspakkeRequest): NyGrunnlagspakkeResponse {
    val grunnlagspakkeDto = GrunnlagspakkeDto (
      opprettetAv = nyGrunnlagspakkeRequest.opprettetAv
    )
    val opprettetGrunnlagspakke = persistenceService.opprettNyGrunnlagspakke(grunnlagspakkeDto)
    return NyGrunnlagspakkeResponse(opprettetGrunnlagspakke.grunnlagspakkeId)

  }

  fun finnGrunnlag(grunnlagspakkeId: Int): FinnGrunnlagResponse {
    val grunnlagspakkeDto = persistenceService.finnGrunnlagspakke(grunnlagspakkeId)
//    if (grunnlagspakkeDto != null) {
//    val inntektListe = persistenceService.finnInntekterForGrunnlagspakke(grunnlagspakkeId)
      return FinnGrunnlagResponse(
        grunnlagspakkeDto.grunnlagspakkeId, grunnlagspakkeDto.opprettetAv, grunnlagspakkeDto.opprettetTimestamp,
        grunnlagspakkeDto.endretTimestamp)
//      } else return null
    }


}