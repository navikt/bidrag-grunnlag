package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.grunnlag.api.OppdaterGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.api.HentGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.api.OppdaterGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.api.OpprettGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.api.OpprettGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.dto.GrunnlagspakkeDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class GrunnlagspakkeService(val persistenceService: PersistenceService) {

  fun opprettGrunnlagspakke(opprettGrunnlagspakkeRequest: OpprettGrunnlagspakkeRequest): OpprettGrunnlagspakkeResponse {
    val grunnlagspakkeDto = GrunnlagspakkeDto(
      opprettetAv = opprettGrunnlagspakkeRequest.opprettetAv
    )
    val opprettetGrunnlagspakke = persistenceService.opprettNyGrunnlagspakke(grunnlagspakkeDto)
    return OpprettGrunnlagspakkeResponse(opprettetGrunnlagspakke.grunnlagspakkeId)

  }

  fun oppdaterGrunnlagspakke(oppdaterGrunnlagspakkeRequest: OppdaterGrunnlagspakkeRequest): OppdaterGrunnlagspakkeResponse {

//    return persistenceService.oppdaterGrunnlagspakke(oppdaterGrunnlagspakkeRequest.grunnlagspakkeId)

    return OppdaterGrunnlagspakkeResponse("Statuskode etter oppdatering: ")
  }

  fun hentGrunnlagspakke(oppdaterGrunnlagspakkeRequest: OppdaterGrunnlagspakkeRequest): HentGrunnlagspakkeResponse {
    return persistenceService.hentGrunnlagspakke(oppdaterGrunnlagspakkeRequest)

  }


}