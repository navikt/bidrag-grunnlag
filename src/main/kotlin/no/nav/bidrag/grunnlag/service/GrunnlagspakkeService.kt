package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.grunnlag.api.OppdaterGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.api.HentGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.api.OppdaterGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.api.OpprettGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.api.OpprettGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.api.OpprettInntektRequest
import no.nav.bidrag.grunnlag.api.OpprettInntektspostRequest
import no.nav.bidrag.grunnlag.api.toInntektDto
import no.nav.bidrag.grunnlag.api.toInntektspostDto
import no.nav.bidrag.grunnlag.dto.GrunnlagspakkeDto
import no.nav.bidrag.grunnlag.dto.InntektDto
import no.nav.bidrag.grunnlag.dto.InntektspostDto
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


    return OppdaterGrunnlagspakkeResponse("Statuskode etter oppdatering: ")
  }


  private fun opprettInntekt(opprettInntektRequest: OpprettInntektRequest, grunnlagspakkeId: Int): InntektDto {
    return persistenceService.opprettInntekt(opprettInntektRequest.toInntektDto(grunnlagspakkeId))
  }

  private fun opprettInntektspost(opprettInntektspostRequest: OpprettInntektspostRequest, inntektId: Int): InntektspostDto {
    return persistenceService.opprettInntektspost(opprettInntektspostRequest.toInntektspostDto(inntektId))
  }


  fun hentGrunnlagspakke(grunnlagspakkeId: Int): HentGrunnlagspakkeResponse {
    return persistenceService.hentGrunnlagspakke(grunnlagspakkeId)

  }


}