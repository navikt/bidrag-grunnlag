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

import no.nav.bidrag.grunnlag.consumer.FamilieBaSakConsumer
import no.nav.bidrag.grunnlag.consumer.familiebasak.FamilieBaSakRequest

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional
class GrunnlagspakkeService(private val persistenceService: PersistenceService, private val familieBaSakConsumer: FamilieBaSakConsumer) {

  companion object {

    @JvmStatic
    private val LOGGER = LoggerFactory.getLogger(GrunnlagspakkeService::class.java)
  }

  fun opprettGrunnlagspakke(opprettGrunnlagspakkeRequest: OpprettGrunnlagspakkeRequest): OpprettGrunnlagspakkeResponse {
    val grunnlagspakkeDto = GrunnlagspakkeDto(
      opprettetAv = opprettGrunnlagspakkeRequest.opprettetAv
    )
    val opprettetGrunnlagspakke = persistenceService.opprettNyGrunnlagspakke(grunnlagspakkeDto)
    return OpprettGrunnlagspakkeResponse(opprettetGrunnlagspakke.grunnlagspakkeId)

  }

  fun oppdaterGrunnlagspakke(oppdaterGrunnlagspakkeRequest: OppdaterGrunnlagspakkeRequest): OppdaterGrunnlagspakkeResponse {

    val familieBaSakRequest = FamilieBaSakRequest(
      personIdent = oppdaterGrunnlagspakkeRequest.identListe[0],
      fraDato = LocalDate.parse(oppdaterGrunnlagspakkeRequest.periodeFom + "-01")
    )
    LOGGER.info("Kaller familie-ba-sak med personIdent ********${familieBaSakRequest.personIdent.substring(IntRange(8, 10))} og fraDato " +
        "${familieBaSakRequest.fraDato}")
    val familieBaSakResponse = familieBaSakConsumer.hentFamilieBaSak(familieBaSakRequest)
    LOGGER.info("familie-ba-sak ga følgende respons: $familieBaSakResponse")
    return if (familieBaSakResponse.perioder.isNotEmpty()) {
      OppdaterGrunnlagspakkeResponse(familieBaSakResponse.perioder.get(0).stønadstype.toString())
    } else {
      OppdaterGrunnlagspakkeResponse("ingen data")
    }
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
