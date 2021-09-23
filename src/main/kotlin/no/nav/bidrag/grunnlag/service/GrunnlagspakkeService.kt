package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.grunnlag.api.HentGrunnlagResponse
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

  fun hentGrunnlag(grunnlagspakkeId: Int): HentGrunnlagResponse {
    val grunnlagspakkeDto = persistenceService.hentGrunnlagspakke(grunnlagspakkeId)
//    if (grunnlagspakkeDto != null) {
//    val inntektListe = persistenceService.finnInntekterForGrunnlagspakke(grunnlagspakkeId)
      return HentGrunnlagResponse(
        grunnlagspakkeDto.grunnlagspakkeId, grunnlagspakkeDto.opprettetAv, grunnlagspakkeDto.opprettetTimestamp,
        grunnlagspakkeDto.endretTimestamp)
//      } else return null
    }


}