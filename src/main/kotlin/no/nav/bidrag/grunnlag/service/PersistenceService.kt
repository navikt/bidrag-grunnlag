package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.grunnlag.api.ainntekt.HentInntektAinntektResponse
import no.nav.bidrag.grunnlag.api.grunnlagspakke.HentKomplettGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.api.skatt.HentSkattegrunnlagResponse
import no.nav.bidrag.grunnlag.api.ainntekt.HentInntektspostAinntektResponse
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
import no.nav.bidrag.grunnlag.exception.custom.InvalidGrunnlagspakkeIdException
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
    return HentKomplettGrunnlagspakkeResponse(
      grunnlagspakkeId, hentInntekterAinntekt(grunnlagspakkeId), hentSkattegrunnlag(grunnlagspakkeId),
      hentUtvidetBarnetrygdOgSmaabarnstillegg(grunnlagspakkeId)
    )
  }

  // Returnerer formaal som er angitt for grunnlagspakken
  fun hentFormaalGrunnlagspakke(grunnlagspakkeId: Int): String {
    return grunnlagspakkeRepository.hentFormaalGrunnlagspakke(grunnlagspakkeId)
  }


  // Valider at grunnlagspakke eksisterer
  fun validerGrunnlagspakke(grunnlagspakkeId: Int) {
    if (!grunnlagspakkeRepository.existsById(grunnlagspakkeId))
      throw InvalidGrunnlagspakkeIdException("Grunnlagspakke med id ${grunnlagspakkeId} finnes ikke")
  }

  // Setter gyldig til-dato = dagens dato for angitt grunnlagspakke
  fun lukkGrunnlagspakke(grunnlagspakkeId: Int): Int {
    grunnlagspakkeRepository.lukkGrunnlagspakke(grunnlagspakkeId)
    return grunnlagspakkeId

  }


  fun hentInntekterAinntekt(grunnlagspakkeId: Int): List<HentInntektAinntektResponse> {
    val hentInntektAinntektResponseListe = mutableListOf<HentInntektAinntektResponse>()
    inntektAinntektRepository.hentAktiveAinntekter(grunnlagspakkeId)
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
                inntektspost.inntektType,
                inntektspost.fordelType,
                inntektspost.beskrivelse,
                inntektspost.belop
              )
            )
          }
        hentInntektAinntektResponseListe.add(
          HentInntektAinntektResponse(
            personId = inntekt.personId,
            periodeFra = inntekt.periodeFra,
            periodeTil = inntekt.periodeTil,
            aktiv = inntekt.aktiv,
            brukFra = inntekt.brukFra,
            brukTil = inntekt.brukTil,
            hentetTidspunkt = inntekt.hentetTidspunkt,
            inntektspostAinntektListe = hentInntektspostListe
          )
        )
      }

    return hentInntektAinntektResponseListe

  }

  fun hentSkattegrunnlag(grunnlagspakkeId: Int): List<HentSkattegrunnlagResponse> {
    val hentSkattegrunnlagResponseListe = mutableListOf<HentSkattegrunnlagResponse>()
    skattegrunnlagRepository.hentAktivtSkattegrunnlag(grunnlagspakkeId)
      .forEach { inntekt ->
        val hentSkattegrunnlagspostListe = mutableListOf<HentSkattegrunnlagspostResponse>()
        skattegrunnlagspostRepository.hentSkattegrunnlagsposter(inntekt.skattegrunnlagId)
          .forEach { inntektspost ->
            hentSkattegrunnlagspostListe.add(
              HentSkattegrunnlagspostResponse(
                skattegrunnlagType = inntektspost.skattegrunnlagType,
                inntektType = inntektspost.inntektType,
                belop = inntektspost.belop
              )
            )
          }
        hentSkattegrunnlagResponseListe.add(
          HentSkattegrunnlagResponse(
            personId = inntekt.personId,
            periodeFra = inntekt.periodeFra,
            periodeTil = inntekt.periodeTil,
            aktiv = inntekt.aktiv,
            brukFra = inntekt.brukFra,
            brukTil = inntekt.brukTil,
            hentetTidspunkt = inntekt.hentetTidspunkt,
            hentSkattegrunnlagspostListe
          )
        )
      }

    return hentSkattegrunnlagResponseListe

  }

  fun hentUtvidetBarnetrygdOgSmaabarnstillegg(grunnlagspakkeId: Int): List<HentUtvidetBarnetrygdOgSmaabarnstilleggResponse> {
    val hentUtvidetBarnetrygdOgSmaabarnstilleggResponseListe = mutableListOf<HentUtvidetBarnetrygdOgSmaabarnstilleggResponse>()
    utvidetBarnetrygdOgSmaabarnstilleggRepository.hentAktiveUbst(grunnlagspakkeId)
      .forEach { ubst ->
        hentUtvidetBarnetrygdOgSmaabarnstilleggResponseListe.add(
          HentUtvidetBarnetrygdOgSmaabarnstilleggResponse(
            personId = ubst.personId,
            type = ubst.type,
            periodeFra = ubst.periodeFra,
            periodeTil = ubst.periodeTil,
            belop = ubst.belop,
            manueltBeregnet = ubst.manueltBeregnet
          )
        )
      }
    return hentUtvidetBarnetrygdOgSmaabarnstilleggResponseListe
  }
}