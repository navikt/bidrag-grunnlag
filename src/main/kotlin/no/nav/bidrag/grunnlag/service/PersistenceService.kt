package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.grunnlag.api.HentInntektResponse
import no.nav.bidrag.grunnlag.api.HentGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.api.HentInntektspostResponse
import no.nav.bidrag.grunnlag.api.OppdaterGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.dto.GrunnlagspakkeDto
import no.nav.bidrag.grunnlag.dto.InntektDto
import no.nav.bidrag.grunnlag.dto.InntektspostDto
import no.nav.bidrag.grunnlag.dto.toGrunnlagspakkeEntity
import no.nav.bidrag.grunnlag.dto.toInntektEntity
import no.nav.bidrag.grunnlag.dto.toInntektspostEntity
import no.nav.bidrag.grunnlag.persistence.entity.toGrunnlagspakkeDto
import no.nav.bidrag.grunnlag.persistence.entity.toInntektDto
import no.nav.bidrag.grunnlag.persistence.entity.toInntektspostDto
import no.nav.bidrag.grunnlag.persistence.repository.GrunnlagspakkeRepository
import no.nav.bidrag.grunnlag.persistence.repository.InntektRepository
import no.nav.bidrag.grunnlag.persistence.repository.InntektspostRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PersistenceService(
  val grunnlagspakkeRepository: GrunnlagspakkeRepository,
  val inntektRepository: InntektRepository,
  val inntektspostRepository: InntektspostRepository
) {

  private val LOGGER = LoggerFactory.getLogger(PersistenceService::class.java)

  fun opprettNyGrunnlagspakke(grunnlagspakkeDto: GrunnlagspakkeDto): GrunnlagspakkeDto {
    val nyGrunnlagspakke = grunnlagspakkeDto.toGrunnlagspakkeEntity()
    val grunnlagspakke = grunnlagspakkeRepository.save(nyGrunnlagspakke)
    return grunnlagspakke.toGrunnlagspakkeDto()
  }

  // Henter inn grunnlag fra eksterne kilder og returnerer
  fun oppdaterGrunnlagspakke(oppdaterGrunnlagspakkeRequest: OppdaterGrunnlagspakkeRequest) {

    // bygg opp inntektsgrunnlag og lagre


  }

  fun opprettInntekt(inntektDto: InntektDto): InntektDto {
    val nyInntekt = inntektDto.toInntektEntity()
    val inntekt = inntektRepository.save(nyInntekt)
    return inntekt.toInntektDto()
  }

  fun opprettInntektspost(inntektspostDto: InntektspostDto): InntektspostDto {
    val nyInntektspost = inntektspostDto.toInntektspostEntity()
    val inntektspost = inntektspostRepository.save(nyInntektspost)
    return inntektspost.toInntektspostDto()
  }


  // Returnerer lagret, komplett grunnlagspakke
  fun hentGrunnlagspakke(grunnlagspakkeId: Int): HentGrunnlagspakkeResponse {
    val grunnlagspakke = HentGrunnlagspakkeResponse(
      grunnlagspakkeId, hentAlleInntekter(grunnlagspakkeId)

    )

    return grunnlagspakke
  }

  fun hentAlleInntekter(grunnlagspakkeId: Int): List<HentInntektResponse> {
    val hentInntektResponseListe = mutableListOf<HentInntektResponse>()
    inntektRepository.hentInntekter(grunnlagspakkeId)
      .forEach { inntekt ->
        val hentInntektspostListe = mutableListOf<HentInntektspostResponse>()
        inntektspostRepository.hentInntektsposter(inntekt.inntektId)
          .forEach { inntektspost ->
            hentInntektspostListe.add(
              HentInntektspostResponse(
                inntektspost.utbetalingsperiode,
                inntektspost.opptjeningsperiodeFra,
                inntektspost.opptjeningsperiodeTil,
                inntektspost.opplysningspliktigId,
                inntektspost.inntektType,
                inntektspost.fordelType,
                inntektspost.beskrivelse,
                inntektspost.belop
              )
            )
          }
        hentInntektResponseListe.add(
          HentInntektResponse(
            inntekt.personId,
            inntekt.type,
            hentInntektspostListe
          )
        )
      }

    return hentInntektResponseListe

  }


}