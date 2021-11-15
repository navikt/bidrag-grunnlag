package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.grunnlag.api.ainntekt.HentAinntektResponse
import no.nav.bidrag.grunnlag.api.grunnlagspakke.HentKomplettGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.api.skatt.HentSkattegrunnlagResponse
import no.nav.bidrag.grunnlag.api.ainntekt.HentAinntektspostResponse
import no.nav.bidrag.grunnlag.api.skatt.HentSkattegrunnlagspostResponse
import no.nav.bidrag.grunnlag.api.ubst.HentUtvidetBarnetrygdOgSmaabarnstilleggResponse
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OppdaterGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.dto.GrunnlagspakkeDto
import no.nav.bidrag.grunnlag.dto.AinntektDto
import no.nav.bidrag.grunnlag.dto.SkattegrunnlagDto
import no.nav.bidrag.grunnlag.dto.AinntektspostDto
import no.nav.bidrag.grunnlag.dto.SkattegrunnlagspostDto
import no.nav.bidrag.grunnlag.dto.UtvidetBarnetrygdOgSmaabarnstilleggDto
import no.nav.bidrag.grunnlag.dto.toGrunnlagspakkeEntity
import no.nav.bidrag.grunnlag.dto.toAinntektEntity
import no.nav.bidrag.grunnlag.dto.toSkattegrunnlagEntity
import no.nav.bidrag.grunnlag.dto.toAinntektspostEntity
import no.nav.bidrag.grunnlag.dto.toSkattegrunnlagspostEntity
import no.nav.bidrag.grunnlag.dto.toUtvidetBarnetrygdOgSmaabarnstilleggEntity
import no.nav.bidrag.grunnlag.exception.custom.InvalidGrunnlagspakkeIdException
import no.nav.bidrag.grunnlag.persistence.entity.toGrunnlagspakkeDto
import no.nav.bidrag.grunnlag.persistence.entity.toAinntektDto
import no.nav.bidrag.grunnlag.persistence.entity.toSkattegrunnlagDto
import no.nav.bidrag.grunnlag.persistence.entity.toAinntektspostDto
import no.nav.bidrag.grunnlag.persistence.entity.toSkattegrunnlagspostDto
import no.nav.bidrag.grunnlag.persistence.entity.toUtvidetBarnetrygdOgSmaabarnstilleggDto
import no.nav.bidrag.grunnlag.persistence.repository.GrunnlagspakkeRepository
import no.nav.bidrag.grunnlag.persistence.repository.AinntektRepository
import no.nav.bidrag.grunnlag.persistence.repository.SkattegrunnlagRepository
import no.nav.bidrag.grunnlag.persistence.repository.AinntektspostRepository
import no.nav.bidrag.grunnlag.persistence.repository.SkattegrunnlagspostRepository
import no.nav.bidrag.grunnlag.persistence.repository.UtvidetBarnetrygdOgSmaabarnstilleggRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PersistenceService(
  val grunnlagspakkeRepository: GrunnlagspakkeRepository,
  val ainntektRepository: AinntektRepository,
  val ainntektspostRepository: AinntektspostRepository,
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

  fun opprettAinntekt(ainntektDto: AinntektDto): AinntektDto {
    val nyInntekt = ainntektDto.toAinntektEntity()
    val inntekt = ainntektRepository.save(nyInntekt)
    return inntekt.toAinntektDto()
  }

  fun opprettAinntektspost(ainntektspostDto: AinntektspostDto): AinntektspostDto {
    val nyInntektspost = ainntektspostDto.toAinntektspostEntity()
    val inntektspost = ainntektspostRepository.save(nyInntektspost)
    return inntektspost.toAinntektspostDto()
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
      grunnlagspakkeId, hentAinntekt(grunnlagspakkeId), hentSkattegrunnlag(grunnlagspakkeId),
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


  fun hentAinntekt(grunnlagspakkeId: Int): List<HentAinntektResponse> {
    val hentAinntektResponseListe = mutableListOf<HentAinntektResponse>()
    ainntektRepository.hentAinntekter(grunnlagspakkeId)
      .forEach { inntekt ->
        val hentAinntektspostListe = mutableListOf<HentAinntektspostResponse>()
        ainntektspostRepository.hentInntektsposter(inntekt.inntektId)
          .forEach { inntektspost ->
            hentAinntektspostListe.add(
              HentAinntektspostResponse(
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
        hentAinntektResponseListe.add(
          HentAinntektResponse(
            personId = inntekt.personId,
            periodeFra = inntekt.periodeFra,
            periodeTil = inntekt.periodeTil,
            aktiv = inntekt.aktiv,
            brukFra = inntekt.brukFra,
            brukTil = inntekt.brukTil,
            hentetTidspunkt = inntekt.hentetTidspunkt,
            ainntektspostListe = hentAinntektspostListe
          )
        )
      }

    return hentAinntektResponseListe

  }

  fun hentSkattegrunnlag(grunnlagspakkeId: Int): List<HentSkattegrunnlagResponse> {
    val hentSkattegrunnlagResponseListe = mutableListOf<HentSkattegrunnlagResponse>()
    skattegrunnlagRepository.hentSkattegrunnlag(grunnlagspakkeId)
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
    utvidetBarnetrygdOgSmaabarnstilleggRepository.hentUbst(grunnlagspakkeId)
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