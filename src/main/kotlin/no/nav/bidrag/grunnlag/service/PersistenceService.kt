package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.grunnlag.api.ainntekt.HentInntektAinntektResponse
import no.nav.bidrag.grunnlag.api.grunnlagspakke.HentKomplettGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.api.skatt.HentSkattegrunnlagResponse
import no.nav.bidrag.grunnlag.api.ainntekt.HentInntektspostAinntektResponse
import no.nav.bidrag.grunnlag.api.grunnlagspakke.GrunnlagstypeResponse
import no.nav.bidrag.grunnlag.api.grunnlagspakke.HentGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.api.skatt.HentSkattegrunnlagspostResponse
import no.nav.bidrag.grunnlag.api.ubst.HentUtvidetBarnetrygdOgSmaabarnstilleggResponse
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OppdaterGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.dto.GrunnlagspakkeDto
import no.nav.bidrag.grunnlag.dto.InntektAinntektDto
import no.nav.bidrag.grunnlag.dto.SkattegrunnlagDto
import no.nav.bidrag.grunnlag.dto.InntektspostAinntektDto
import no.nav.bidrag.grunnlag.dto.SkattegrunnlagspostDto
import no.nav.bidrag.grunnlag.dto.UtvidetBarnetrygdOgSmaabarnstilleggDto
import no.nav.bidrag.grunnlag.dto.toGrunnlagspakkeEntity
import no.nav.bidrag.grunnlag.dto.toInntektAinntektEntity
import no.nav.bidrag.grunnlag.dto.toSkattegrunnlagEntity
import no.nav.bidrag.grunnlag.dto.toInntektspostAinntektEntity
import no.nav.bidrag.grunnlag.dto.toSkattegrunnlagspostEntity
import no.nav.bidrag.grunnlag.dto.toUtvidetBarnetrygdOgSmaabarnstilleggEntity
import no.nav.bidrag.grunnlag.persistence.entity.toGrunnlagspakkeDto
import no.nav.bidrag.grunnlag.persistence.entity.toInntektAinntektDto
import no.nav.bidrag.grunnlag.persistence.entity.toSkattegrunnlagDto
import no.nav.bidrag.grunnlag.persistence.entity.toInntektspostAinntektDto
import no.nav.bidrag.grunnlag.persistence.entity.toSkattegrunnlagspostDto
import no.nav.bidrag.grunnlag.persistence.entity.toUtvidetBarnetrygdOgSmaabarnstilleggDto
import no.nav.bidrag.grunnlag.persistence.repository.GrunnlagspakkeRepository
import no.nav.bidrag.grunnlag.persistence.repository.InntektAinntektRepository
import no.nav.bidrag.grunnlag.persistence.repository.SkattegrunnlagRepository
import no.nav.bidrag.grunnlag.persistence.repository.InntektspostAinntektRepository
import no.nav.bidrag.grunnlag.persistence.repository.SkattegrunnlagspostRepository
import no.nav.bidrag.grunnlag.persistence.repository.UtvidetBarnetrygdOgSmaabarnstilleggRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class PersistenceService(
    val grunnlagspakkeRepository: GrunnlagspakkeRepository,
    val inntektAinntektRepository: InntektAinntektRepository,
    val inntektspostAinntektRepository: InntektspostAinntektRepository,
    val skattegrunnlagRepository: SkattegrunnlagRepository,
    val skattegrunnlagspostRepository: SkattegrunnlagspostRepository,
    val utvidetBarnetrygdOgSmaabarnstilleggRepository: UtvidetBarnetrygdOgSmaabarnstilleggRepository
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

  fun opprettInntektAinntekt(inntektAinntektDto: InntektAinntektDto): InntektAinntektDto {
    val nyInntekt = inntektAinntektDto.toInntektAinntektEntity()
    val inntekt = inntektAinntektRepository.save(nyInntekt)
    return inntekt.toInntektAinntektDto()
  }

  fun opprettInntektspostAinntekt(inntektspostAinntektDto: InntektspostAinntektDto): InntektspostAinntektDto {
    val nyInntektspost = inntektspostAinntektDto.toInntektspostAinntektEntity()
    val inntektspost = inntektspostAinntektRepository.save(nyInntektspost)
    return inntektspost.toInntektspostAinntektDto()
  }

  fun opprettSkattegrunnlag(skattegrunnlagDto: SkattegrunnlagDto): SkattegrunnlagDto {
    val nyInntekt = skattegrunnlagDto.toSkattegrunnlagEntity()
    val inntekt = skattegrunnlagRepository.save(nyInntekt)
    return inntekt.toSkattegrunnlagDto()
  }

  fun opprettSkattegrunnlagspost(skattegrunnlagspostDto: SkattegrunnlagspostDto): SkattegrunnlagspostDto {
    val nyInntektspost = skattegrunnlagspostDto.toSkattegrunnlagspostEntity()
    val inntektspost = skattegrunnlagspostRepository.save(nyInntektspost)
    return inntektspost.toSkattegrunnlagspostDto()
  }


  fun opprettUtvidetBarnetrygdOgSmaabarnstillegg(utvidetBarnetrygdOgSmaabarnstilleggDto: UtvidetBarnetrygdOgSmaabarnstilleggDto): UtvidetBarnetrygdOgSmaabarnstilleggDto {
    val nyubst = utvidetBarnetrygdOgSmaabarnstilleggDto.toUtvidetBarnetrygdOgSmaabarnstilleggEntity()
    val utvidetBarnetrygdOgSmaabarnstillegg = utvidetBarnetrygdOgSmaabarnstilleggRepository.save(nyubst)
    return utvidetBarnetrygdOgSmaabarnstillegg.toUtvidetBarnetrygdOgSmaabarnstilleggDto()
  }


  // Returnerer lagret, komplett grunnlagspakke
  fun hentKomplettGrunnlagspakke(grunnlagspakkeId: Int): HentKomplettGrunnlagspakkeResponse {
    val grunnlagspakke = HentKomplettGrunnlagspakkeResponse(
      hentGrunnlagspakke(grunnlagspakkeId), hentInntekterAinntekt(grunnlagspakkeId), hentSkattegrunnlag(grunnlagspakkeId),
      hentUtvidetBarnetrygdOgSmaabarnstillegg(grunnlagspakkeId)

    )

    return grunnlagspakke
  }

  // Returnerer lagret, komplett grunnlagspakke
  fun hentGrunnlagspakke(grunnlagspakkeId: Int): HentGrunnlagspakkeResponse {
  return HentGrunnlagspakkeResponse(grunnlagspakkeRepository.hentGrunnlagspakke(grunnlagspakkeId).grunnlagspakkeId)
  }

  // Setter gyldig til-dato for en grunnlagspakke
  fun settGyldigTildatoGrunnlagspakke(grunnlagspakkeId: Int, gyldigTil: LocalDate): Int {
    grunnlagspakkeRepository.settGyldigTildatoGrunnlagspakke(grunnlagspakkeId, gyldigTil)
    return grunnlagspakkeId

  }


  fun hentInntekterAinntekt(grunnlagspakkeId: Int): List<HentInntektAinntektResponse> {
    val hentInntektAinntektResponseListe = mutableListOf<HentInntektAinntektResponse>()
    inntektAinntektRepository.hentInntekter(grunnlagspakkeId)
      .forEach { inntekt ->
        val hentInntektspostListe = mutableListOf<HentInntektspostAinntektResponse>()
        inntektspostAinntektRepository.hentInntektsposter(inntekt.inntektId)
          .forEach { inntektspost ->
            hentInntektspostListe.add(
              HentInntektspostAinntektResponse(
                inntektspost.utbetalingsperiode,
                inntektspost.opptjeningsperiodeFra,
                inntektspost.opptjeningsperiodeTil,
                inntektspost.opplysningspliktigId,
                inntektspost.type,
                inntektspost.fordelType,
                inntektspost.beskrivelse,
                inntektspost.belop
              )
            )
          }
        hentInntektAinntektResponseListe.add(
          HentInntektAinntektResponse(
            inntekt.personId,
//            inntekt.type,
            hentInntektspostListe
          )
        )
      }

    return hentInntektAinntektResponseListe

  }

  fun hentSkattegrunnlag(grunnlagspakkeId: Int): List<HentSkattegrunnlagResponse> {
    val hentSkattegrunnlagResponseListe = mutableListOf<HentSkattegrunnlagResponse>()
    skattegrunnlagRepository.hentSkattegrunnlag(grunnlagspakkeId)
      .forEach { inntekt ->
        val hentInntektspostSkattListe = mutableListOf<HentSkattegrunnlagspostResponse>()
        skattegrunnlagspostRepository.hentSkattegrunnlagsposter(inntekt.skattegrunnlagId)
          .forEach { inntektspost ->
            hentInntektspostSkattListe.add(
              HentSkattegrunnlagspostResponse(
                inntektspost.type,
                inntektspost.belop
              )
            )
          }
        hentSkattegrunnlagResponseListe.add(
          HentSkattegrunnlagResponse(
            inntekt.personId,
            hentInntektspostSkattListe
          )
        )
      }

    return hentSkattegrunnlagResponseListe

  }

  fun hentUtvidetBarnetrygdOgSmaabarnstillegg(grunnlagspakkeId: Int): List<HentUtvidetBarnetrygdOgSmaabarnstilleggResponse> {
    val hentUtvidetBarnetrygdOgSmaabarnstilleggResponseListe = mutableListOf<HentUtvidetBarnetrygdOgSmaabarnstilleggResponse>()
    utvidetBarnetrygdOgSmaabarnstilleggRepository.hentStonader(grunnlagspakkeId)
      .forEach { ubst ->
        hentUtvidetBarnetrygdOgSmaabarnstilleggResponseListe.add(
          HentUtvidetBarnetrygdOgSmaabarnstilleggResponse(
            ubst.personId,
            ubst.type,
            ubst.periodeFra,
            ubst.periodeTil,
            ubst.belop,
            ubst.manueltBeregnet
          )
        )
      }

    return hentUtvidetBarnetrygdOgSmaabarnstilleggResponseListe

  }



}