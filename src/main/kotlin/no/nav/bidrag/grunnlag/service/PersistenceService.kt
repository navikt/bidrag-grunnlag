package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.grunnlag.api.ainntekt.HentAinntektResponse
import no.nav.bidrag.grunnlag.api.grunnlagspakke.HentKomplettGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.api.skatt.HentSkattegrunnlagResponse
import no.nav.bidrag.grunnlag.api.ainntekt.HentAinntektspostResponse
import no.nav.bidrag.grunnlag.api.skatt.HentSkattegrunnlagspostResponse
import no.nav.bidrag.grunnlag.api.ubst.HentUtvidetBarnetrygdOgSmaabarnstilleggResponse
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OppdaterGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.comparator.AinntektPeriodComparator
import no.nav.bidrag.grunnlag.comparator.Period
import no.nav.bidrag.grunnlag.comparator.PeriodComparable
import no.nav.bidrag.grunnlag.comparator.SkattegrunnlagPeriodComparator
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
import java.time.LocalDate
import java.time.LocalDateTime

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

  // Oppdaterer endret timestamp på grunnlagspakke, kalles ved oppdatering av grunnlag
  fun oppdaterEndretTimestamp(grunnlagspakkeId: Int, timestampOppdatering: LocalDateTime): Int {
    grunnlagspakkeRepository.oppdaterEndretTimestamp(grunnlagspakkeId, timestampOppdatering)
    return grunnlagspakkeId
  }

  fun oppdaterAinntektForGrunnlagspakke(
    grunnlagspakkeId: Int,
    newAinntektForPersonId: List<PeriodComparable<AinntektDto, AinntektspostDto>>,
    periodeFra: LocalDate,
    periodeTil: LocalDate,
    personId: String
  ) {
    val existingAinntektForPersonId = hentAinntektForPersonIdToCompare(grunnlagspakkeId, personId)
    val ainntektPeriodComparator = AinntektPeriodComparator()

    // Finner ut hvilke inntekter som er oppdatert/nye siden sist, hvilke som ikke er endret og hvilke som er utløpt.
    val comparatorResult =
      ainntektPeriodComparator.comparePeriodEntities(Period(periodeFra, periodeTil), newAinntektForPersonId, existingAinntektForPersonId)

    val hentetTidspunkt = LocalDateTime.now()

    // Setter utløpte Ainntekter til utløpt.
    LOGGER.debug("Setter ${comparatorResult.expiredEntities.size} eksisterende Ainntekter til utløpt.")
    comparatorResult.expiredEntities.forEach() { expiredEntity ->
      val expiredAinntekt = expiredEntity.periodEntity.copy(brukTil = hentetTidspunkt, aktiv = false).toAinntektEntity()
      ainntektRepository.save(expiredAinntekt)
    }
    // Oppdaterer hentet tidspunkt for uendrede Ainntekter.
    LOGGER.debug("Oppdaterer ${comparatorResult.equalEntities.size} uendrede eksisterende Ainntekter med nytt hentet tidspunkt.")
    comparatorResult.equalEntities.forEach() { equalEntity ->
      val unchangedAinntekt = equalEntity.periodEntity.copy(hentetTidspunkt = hentetTidspunkt).toAinntektEntity()
      ainntektRepository.save(unchangedAinntekt)
    }
    // Lagrer nye Ainntekter og Ainntektsposter.
    LOGGER.debug("Oppretter ${comparatorResult.updatedEntities.size} nye Ainntekter med underliggende inntektsposter")
    comparatorResult.updatedEntities.forEach() { updatedEntity ->
      val ainntekt = ainntektRepository.save(updatedEntity.periodEntity.toAinntektEntity())
      updatedEntity.children?.forEach() { ainntektspostDto ->
        val updatedAinntekt = ainntektspostDto.copy(inntektId = ainntekt.inntektId).toAinntektspostEntity()
        ainntektspostRepository.save(updatedAinntekt)
      }
    }
  }

  fun oppdaterSkattegrunnlagForGrunnlagspakke(
    grunnlagspakkeId: Int,
    newSkattegrunnlagForPersonId: List<PeriodComparable<SkattegrunnlagDto, SkattegrunnlagspostDto>>,
    periodeFra: LocalDate,
    periodeTil: LocalDate,
    personId: String
  ) {
    val existingAinntektForPersonId = hentSkattegrunnlagForPersonIdToCompare(grunnlagspakkeId, personId)
    val ainntektPeriodComparator = SkattegrunnlagPeriodComparator()

    // Finner ut hvilke skattegrunnlag som er oppdatert/nye siden sist, hvilke som ikke er endret og hvilke som er utløpt.
    val comparatorResult =
      ainntektPeriodComparator.comparePeriodEntities(Period(periodeFra, periodeTil), newSkattegrunnlagForPersonId, existingAinntektForPersonId)

    val hentetTidspunkt = LocalDateTime.now()

    // Setter utløpte skattegrunnlag til utløpt.
    LOGGER.debug("Setter ${comparatorResult.expiredEntities.size} eksisterende skattegrunnlag til utløpt.")
    comparatorResult.expiredEntities.forEach() { expiredEntity ->
      val expiredSkattegrunnlag = expiredEntity.periodEntity.copy(aktiv = false, brukTil = hentetTidspunkt).toSkattegrunnlagEntity()
      skattegrunnlagRepository.save(expiredSkattegrunnlag)
    }
    // Oppdaterer hentet tidspunkt for uendrede skattegrunnlag.
    LOGGER.debug("Oppdaterer ${comparatorResult.equalEntities.size} uendrede eksisterende skattegrunnlag med nytt hentet tidspunkt.")
    comparatorResult.equalEntities.forEach() { equalEntity ->
      val unchangedSkattegrunnlag = equalEntity.periodEntity.copy(hentetTidspunkt = hentetTidspunkt).toSkattegrunnlagEntity()
      skattegrunnlagRepository.save(unchangedSkattegrunnlag)
    }
    // Lagrer nye skattegrunnlag og skattegrunnlagsposter.
    LOGGER.debug("Oppretter ${comparatorResult.updatedEntities.size} nye skattegrunnlag med underliggende skattegrunnlagsposter")
    comparatorResult.updatedEntities.forEach() { updatedEntity ->
      val updatedSkattegrunnlag = skattegrunnlagRepository.save(updatedEntity.periodEntity.toSkattegrunnlagEntity())
      updatedEntity.children?.forEach() { ainntektspostDto ->
        val skattegrunnlagspost = ainntektspostDto.copy(skattegrunnlagId = updatedSkattegrunnlag.skattegrunnlagId).toSkattegrunnlagspostEntity()
        skattegrunnlagspostRepository.save(skattegrunnlagspost)
      }
    }
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
                inntektspost.virksomhetId,
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

  fun hentAinntektForPersonIdToCompare(grunnlagspakkeId: Int, personId: String): List<PeriodComparable<AinntektDto, AinntektspostDto>> {
    val ainntektForPersonIdListe = mutableListOf<PeriodComparable<AinntektDto, AinntektspostDto>>()
    ainntektRepository.hentAinntekter(grunnlagspakkeId)
      .forEach { inntekt ->
        if (inntekt.personId == personId) {
          val ainntektspostListe = mutableListOf<AinntektspostDto>()
          ainntektspostRepository.hentInntektsposter(inntekt.inntektId)
            .forEach() { ainntektspost -> ainntektspostListe.add(ainntektspost.toAinntektspostDto()) }
          ainntektForPersonIdListe.add(
            PeriodComparable(inntekt.toAinntektDto(), ainntektspostListe)
          )
        }
      }

    return ainntektForPersonIdListe
  }

  fun hentSkattegrunnlagForPersonIdToCompare(
    grunnlagspakkeId: Int,
    personId: String
  ): List<PeriodComparable<SkattegrunnlagDto, SkattegrunnlagspostDto>> {
    val skattegrunnlagForPersonIdListe = mutableListOf<PeriodComparable<SkattegrunnlagDto, SkattegrunnlagspostDto>>()
    skattegrunnlagRepository.hentSkattegrunnlag(grunnlagspakkeId)
      .forEach { skattegrunnlag ->
        if (skattegrunnlag.personId == personId) {
          val skattegrunnlagpostListe = mutableListOf<SkattegrunnlagspostDto>()
          skattegrunnlagspostRepository.hentSkattegrunnlagsposter(skattegrunnlag.skattegrunnlagId)
            .forEach() { skattegrunnlagspost -> skattegrunnlagpostListe.add(skattegrunnlagspost.toSkattegrunnlagspostDto()) }
          skattegrunnlagForPersonIdListe.add(
            PeriodComparable(skattegrunnlag.toSkattegrunnlagDto(), skattegrunnlagpostListe)
          )
        }
      }
    return skattegrunnlagForPersonIdListe
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
            aktiv = ubst.aktiv,
            brukFra = ubst.brukFra,
            brukTil = ubst.brukTil,
            belop = ubst.belop,
            manueltBeregnet = ubst.manueltBeregnet,
            hentetTidspunkt = ubst.hentetTidspunkt
          )
        )
      }
    return hentUtvidetBarnetrygdOgSmaabarnstilleggResponseListe
  }
}