package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.grunnlag.api.ainntekt.HentAinntektResponse
import no.nav.bidrag.grunnlag.api.grunnlagspakke.HentKomplettGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.api.skatt.HentSkattegrunnlagResponse
import no.nav.bidrag.grunnlag.api.ainntekt.HentAinntektspostResponse
import no.nav.bidrag.grunnlag.api.skatt.HentSkattegrunnlagspostResponse
import no.nav.bidrag.grunnlag.api.ubst.HentUtvidetBarnetrygdOgSmaabarnstilleggResponse
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OppdaterGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.comparator.AbstractPeriodComparator
import no.nav.bidrag.grunnlag.comparator.AinntektPeriodComparator
import no.nav.bidrag.grunnlag.comparator.ComparatorResult
import no.nav.bidrag.grunnlag.comparator.Period
import no.nav.bidrag.grunnlag.comparator.PeriodComparable
import no.nav.bidrag.grunnlag.comparator.PeriodComparableWithChildren
import no.nav.bidrag.grunnlag.comparator.SkattegrunnlagPeriodComparator
import no.nav.bidrag.grunnlag.dto.GrunnlagspakkeDto
import no.nav.bidrag.grunnlag.dto.AinntektDto
import no.nav.bidrag.grunnlag.dto.SkattegrunnlagDto
import no.nav.bidrag.grunnlag.dto.AinntektspostDto
import no.nav.bidrag.grunnlag.dto.IComparable
import no.nav.bidrag.grunnlag.dto.IComparableChild
import no.nav.bidrag.grunnlag.dto.SkattegrunnlagspostDto
import no.nav.bidrag.grunnlag.dto.UtvidetBarnetrygdOgSmaabarnstilleggDto
import no.nav.bidrag.grunnlag.dto.toGrunnlagspakkeEntity
import no.nav.bidrag.grunnlag.dto.toAinntektEntity
import no.nav.bidrag.grunnlag.dto.toSkattegrunnlagEntity
import no.nav.bidrag.grunnlag.dto.toAinntektspostEntity
import no.nav.bidrag.grunnlag.dto.toSkattegrunnlagspostEntity
import no.nav.bidrag.grunnlag.dto.toUtvidetBarnetrygdOgSmaabarnstilleggEntity
import no.nav.bidrag.grunnlag.exception.custom.InvalidGrunnlagspakkeIdException
import no.nav.bidrag.grunnlag.persistence.entity.Skattegrunnlag
import no.nav.bidrag.grunnlag.persistence.entity.Skattegrunnlagspost
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
    newAinntektForPersonId: List<PeriodComparableWithChildren<AinntektDto, AinntektspostDto>>,
    periodeFra: LocalDate,
    periodeTil: LocalDate,
    personId: String
  ) {
    val existingAinntektForPersonId = hentAinntektForPersonIdToCompare(grunnlagspakkeId, personId)
    val ainntektPeriodComparator = AinntektPeriodComparator()

    // Finner ut hvilke inntekter som er oppdatert/nye siden sist, hvilke som ikke er endret og hvilke som er utløpt.
    val comparatorResult =
      ainntektPeriodComparator.comparePeriodEntities(Period(periodeFra, periodeTil), newAinntektForPersonId, existingAinntektForPersonId)

    val processedComparatorResult = processComparatorResult(comparatorResult)
    ainntektRepository.saveAll(processedComparatorResult.entities)
    if (processedComparatorResult.childEntities.isNotEmpty()) {
      ainntektspostRepository.saveAll(processedComparatorResult.childEntities)
    }
  }

  fun oppdaterSkattegrunnlagForGrunnlagspakke(
    grunnlagspakkeId: Int,
    newSkattegrunnlagForPersonId: List<PeriodComparableWithChildren<SkattegrunnlagDto, SkattegrunnlagspostDto>>,
    periodeFra: LocalDate,
    periodeTil: LocalDate,
    personId: String
  ) {
    val existingSkattegrunnlagForPersonId = hentSkattegrunnlagForPersonIdToCompare(grunnlagspakkeId, personId)
    val skattegrunnlagPeriodComparator = SkattegrunnlagPeriodComparator()

    // Finner ut hvilke inntekter som er oppdatert/nye siden sist, hvilke som ikke er endret og hvilke som er utløpt.
    val comparatorResult = skattegrunnlagPeriodComparator.comparePeriodEntities(
      Period(periodeFra, periodeTil),
      newSkattegrunnlagForPersonId,
      existingSkattegrunnlagForPersonId
    )
    val processedComparatorResult = processComparatorResult(comparatorResult)
    skattegrunnlagRepository.saveAll(processedComparatorResult.entities)
    if (processedComparatorResult.childEntities.isNotEmpty())
      skattegrunnlagspostRepository.saveAll(processedComparatorResult.childEntities)
  }

  fun <Entity, ChildEntity, Dto : IComparable<Entity, Nothing>, ChildDto : IComparableChild<ChildEntity, Entity>, Comparable : PeriodComparable<Dto, ChildDto>> processComparatorResult(
    comparatorResult: ComparatorResult<Comparable>
  ): ProcessedComparatorResult<Entity, ChildEntity> {
    val now = LocalDateTime.now()
    val entities = mutableListOf<Entity>()
    val childEntities = mutableListOf<ChildEntity>()

    AbstractPeriodComparator.LOGGER.info("Setter ${comparatorResult.expiredEntities.size} entiteter til utløpt.")
    comparatorResult.expiredEntities.forEach() { expiredEntity ->
      entities.add(expiredEntity.periodEntity.expire(now))
    }
    AbstractPeriodComparator.LOGGER.info("Oppdaterer ${comparatorResult.equalEntities.size} uendrede eksisterende entiteter med nytt hentet tidspunkt.")
    comparatorResult.equalEntities.forEach() { equalEntity ->
      entities.add(equalEntity.periodEntity.update(now))
    }
    AbstractPeriodComparator.LOGGER.info("Oppretter ${comparatorResult.updatedEntities.size} nye entiteter.")
    comparatorResult.updatedEntities.forEach() { updatedEntity ->
      val entity = updatedEntity.periodEntity.create(null)
      entities.add(entity)
      if (updatedEntity.children != null) {
        AbstractPeriodComparator.LOGGER.info("Oppretter ${updatedEntity.children.size} nye barne-entiteter.")
      }
      updatedEntity.children?.forEach() { skattegrunnlagspostDto ->
        childEntities.add(skattegrunnlagspostDto.create(entity))
      }
    }
    return ProcessedComparatorResult(entities, childEntities)
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

  fun hentAinntektForPersonIdToCompare(grunnlagspakkeId: Int, personId: String): List<PeriodComparableWithChildren<AinntektDto, AinntektspostDto>> {
    val ainntektForPersonIdListe = mutableListOf<PeriodComparableWithChildren<AinntektDto, AinntektspostDto>>()
    ainntektRepository.hentAinntekter(grunnlagspakkeId)
      .forEach { inntekt ->
        if (inntekt.personId == personId) {
          val ainntektspostListe = mutableListOf<AinntektspostDto>()
          ainntektspostRepository.hentInntektsposter(inntekt.inntektId)
            .forEach() { ainntektspost -> ainntektspostListe.add(ainntektspost.toAinntektspostDto()) }
          ainntektForPersonIdListe.add(
            PeriodComparableWithChildren(inntekt.toAinntektDto(), ainntektspostListe)
          )
        }
      }

    return ainntektForPersonIdListe
  }

  fun hentSkattegrunnlagForPersonIdToCompare(
    grunnlagspakkeId: Int,
    personId: String
  ): List<PeriodComparableWithChildren<SkattegrunnlagDto, SkattegrunnlagspostDto>> {
    val skattegrunnlagForPersonIdListe = mutableListOf<PeriodComparableWithChildren<SkattegrunnlagDto, SkattegrunnlagspostDto>>()
    skattegrunnlagRepository.hentSkattegrunnlag(grunnlagspakkeId)
      .forEach { skattegrunnlag ->
        if (skattegrunnlag.personId == personId) {
          val skattegrunnlagpostListe = mutableListOf<SkattegrunnlagspostDto>()
          skattegrunnlagspostRepository.hentSkattegrunnlagsposter(skattegrunnlag.skattegrunnlagId)
            .forEach() { skattegrunnlagspost -> skattegrunnlagpostListe.add(skattegrunnlagspost.toSkattegrunnlagspostDto()) }
          skattegrunnlagForPersonIdListe.add(
            PeriodComparableWithChildren(skattegrunnlag.toSkattegrunnlagDto(), skattegrunnlagpostListe)
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

data class ProcessedComparatorResult<Entity, ChildEntity>(val entities: List<Entity>, val childEntities: List<ChildEntity>)