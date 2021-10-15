package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.grunnlag.api.HentGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.api.OppdaterGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.api.OppdaterGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.api.OpprettGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.api.OpprettGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.api.OpprettInntektAinntektRequest
import no.nav.bidrag.grunnlag.api.OpprettInntektspostAinntektRequest
import no.nav.bidrag.grunnlag.api.toInntektAinntektDto
import no.nav.bidrag.grunnlag.api.toInntektspostAinntektDto
import no.nav.bidrag.grunnlag.dto.GrunnlagspakkeDto
import no.nav.bidrag.grunnlag.dto.InntektAinntektDto
import no.nav.bidrag.grunnlag.dto.InntektspostAinntektDto
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


  private fun opprettInntektAinntekt(opprettInntektAinntektRequest: OpprettInntektAinntektRequest, grunnlagspakkeId: Int): InntektAinntektDto {
    return persistenceService.opprettInntektAinntekt(opprettInntektAinntektRequest.toInntektAinntektDto(grunnlagspakkeId))
  }

  private fun opprettInntektspostAinntekt(opprettInntektspostAinntektRequest: OpprettInntektspostAinntektRequest, inntektId: Int): InntektspostAinntektDto {
    return persistenceService.opprettInntektspostAinntekt(opprettInntektspostAinntektRequest.toInntektspostAinntektDto(inntektId))
  }


  fun hentGrunnlagspakke(grunnlagspakkeId: Int): HentGrunnlagspakkeResponse {
    return persistenceService.hentGrunnlagspakke(grunnlagspakkeId)

  }


}