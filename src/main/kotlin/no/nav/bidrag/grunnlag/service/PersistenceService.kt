package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.behandling.felles.dto.grunnlag.AinntektDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.AinntektspostDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.BarnetilleggDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.HentGrunnlagspakkeDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.OpprettGrunnlagspakkeRequestDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.SkattegrunnlagDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.SkattegrunnlagspostDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.UtvidetBarnetrygdOgSmaabarnstilleggDto
import no.nav.bidrag.behandling.felles.enums.BarnetilleggType
import no.nav.bidrag.grunnlag.comparator.AinntektPeriodComparator
import no.nav.bidrag.grunnlag.comparator.Period
import no.nav.bidrag.grunnlag.comparator.PeriodComparable
import no.nav.bidrag.grunnlag.comparator.SkattegrunnlagPeriodComparator
import no.nav.bidrag.grunnlag.bo.AinntektBo
import no.nav.bidrag.grunnlag.bo.AinntektspostBo
import no.nav.bidrag.grunnlag.bo.BarnetilleggBo
import no.nav.bidrag.grunnlag.bo.GrunnlagspakkeBo
import no.nav.bidrag.grunnlag.bo.SkattegrunnlagBo
import no.nav.bidrag.grunnlag.bo.SkattegrunnlagspostBo
import no.nav.bidrag.grunnlag.bo.UtvidetBarnetrygdOgSmaabarnstilleggBo
import no.nav.bidrag.grunnlag.bo.toAinntektEntity
import no.nav.bidrag.grunnlag.bo.toAinntektspostEntity
import no.nav.bidrag.grunnlag.bo.toBarnetilleggEntity
import no.nav.bidrag.grunnlag.bo.toGrunnlagspakkeEntity
import no.nav.bidrag.grunnlag.bo.toSkattegrunnlagEntity
import no.nav.bidrag.grunnlag.bo.toSkattegrunnlagspostEntity
import no.nav.bidrag.grunnlag.bo.toUtvidetBarnetrygdOgSmaabarnstilleggEntity
import no.nav.bidrag.grunnlag.exception.custom.InvalidGrunnlagspakkeIdException
import no.nav.bidrag.grunnlag.persistence.entity.Ainntekt
import no.nav.bidrag.grunnlag.persistence.entity.Ainntektspost
import no.nav.bidrag.grunnlag.persistence.entity.Barnetillegg
import no.nav.bidrag.grunnlag.persistence.entity.Grunnlagspakke
import no.nav.bidrag.grunnlag.persistence.entity.Skattegrunnlag
import no.nav.bidrag.grunnlag.persistence.entity.Skattegrunnlagspost
import no.nav.bidrag.grunnlag.persistence.entity.UtvidetBarnetrygdOgSmaabarnstillegg
import no.nav.bidrag.grunnlag.persistence.entity.toAinntektBo
import no.nav.bidrag.grunnlag.persistence.entity.toAinntektspostBo
import no.nav.bidrag.grunnlag.persistence.entity.toBarnetilleggBo
import no.nav.bidrag.grunnlag.persistence.entity.toGrunnlagspakkeBo
import no.nav.bidrag.grunnlag.persistence.entity.toGrunnlagspakkeEntity
import no.nav.bidrag.grunnlag.persistence.entity.toSkattegrunnlagBo
import no.nav.bidrag.grunnlag.persistence.entity.toSkattegrunnlagspostBo
import no.nav.bidrag.grunnlag.persistence.entity.toUtvidetBarnetrygdOgSmaabarnstilleggBo
import no.nav.bidrag.grunnlag.persistence.repository.AinntektRepository
import no.nav.bidrag.grunnlag.persistence.repository.AinntektspostRepository
import no.nav.bidrag.grunnlag.persistence.repository.BarnetilleggRepository
import no.nav.bidrag.grunnlag.persistence.repository.GrunnlagspakkeRepository
import no.nav.bidrag.grunnlag.persistence.repository.SkattegrunnlagRepository
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
  val utvidetBarnetrygdOgSmaabarnstilleggRepository: UtvidetBarnetrygdOgSmaabarnstilleggRepository,
  val barnetilleggRepository: BarnetilleggRepository
) {

  private val LOGGER = LoggerFactory.getLogger(PersistenceService::class.java)

  fun opprettNyGrunnlagspakke(opprettGrunnlagspakkeRequestDto: OpprettGrunnlagspakkeRequestDto): Grunnlagspakke {
    val nyGrunnlagspakke = opprettGrunnlagspakkeRequestDto.toGrunnlagspakkeEntity()
    return grunnlagspakkeRepository.save(nyGrunnlagspakke)
  }

  fun opprettAinntekt(ainntektBo: AinntektBo): Ainntekt {
    val nyInntekt = ainntektBo.toAinntektEntity()
    return ainntektRepository.save(nyInntekt)
  }

  fun opprettAinntektspost(ainntektspostBo: AinntektspostBo): Ainntektspost {
    val nyInntektspost = ainntektspostBo.toAinntektspostEntity()
    return ainntektspostRepository.save(nyInntektspost)
  }

  fun opprettSkattegrunnlag(skattegrunnlagBo: SkattegrunnlagBo): Skattegrunnlag {
    val nyInntekt = skattegrunnlagBo.toSkattegrunnlagEntity()
    return skattegrunnlagRepository.save(nyInntekt)
  }

  fun opprettSkattegrunnlagspost(skattegrunnlagspostBo: SkattegrunnlagspostBo): Skattegrunnlagspost {
    val nyInntektspost = skattegrunnlagspostBo.toSkattegrunnlagspostEntity()
    return skattegrunnlagspostRepository.save(nyInntektspost)
  }

  fun oppdaterEksisterendeUtvidetBarnetrygOgSmaabarnstilleggTilInaktiv(grunnlagspakkeId: Int, personId: String, timestampOppdatering: LocalDateTime) {
    utvidetBarnetrygdOgSmaabarnstilleggRepository.oppdaterEksisterendeUtvidetBarnetrygOgSmaabarnstilleggTilInaktiv(
      grunnlagspakkeId,
      personId,
      timestampOppdatering
    )
  }

  fun opprettUtvidetBarnetrygdOgSmaabarnstillegg(utvidetBarnetrygdOgSmaabarnstilleggBo: UtvidetBarnetrygdOgSmaabarnstilleggBo)
  : UtvidetBarnetrygdOgSmaabarnstillegg {
    val nyUbst = utvidetBarnetrygdOgSmaabarnstilleggBo.toUtvidetBarnetrygdOgSmaabarnstilleggEntity()
    return utvidetBarnetrygdOgSmaabarnstilleggRepository.save(nyUbst)
  }

  fun oppdaterEksisterendeBarnetilleggPensjonTilInaktiv(grunnlagspakkeId: Int, partPersonId: String, timestampOppdatering: LocalDateTime) {
    barnetilleggRepository.oppdaterEksisterendeBarnetilleggTilInaktiv(
      grunnlagspakkeId,
      partPersonId,
      timestampOppdatering,
      BarnetilleggType.PENSJON.toString()
    )
  }

  fun opprettBarnetillegg(barnetilleggBo: BarnetilleggBo): Barnetillegg {
    val nyBarnetillegg = barnetilleggBo.toBarnetilleggEntity()
    return barnetilleggRepository.save(nyBarnetillegg)
  }

  // Returnerer lagret, komplett grunnlagspakke
  fun hentGrunnlagspakke(grunnlagspakkeId: Int): HentGrunnlagspakkeDto {
    return HentGrunnlagspakkeDto(
      grunnlagspakkeId, hentAinntekt(grunnlagspakkeId), hentSkattegrunnlag(grunnlagspakkeId),
      hentUtvidetBarnetrygdOgSmaabarnstillegg(grunnlagspakkeId), hentBarnetillegg(grunnlagspakkeId),
      emptyList(), emptyList(), emptyList(), emptyList()
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

  // Oppdaterer endret timestamp p?? grunnlagspakke, kalles ved oppdatering av grunnlag
  fun oppdaterEndretTimestamp(grunnlagspakkeId: Int, timestampOppdatering: LocalDateTime): Int {
    grunnlagspakkeRepository.oppdaterEndretTimestamp(grunnlagspakkeId, timestampOppdatering)
    return grunnlagspakkeId
  }

  fun oppdaterAinntektForGrunnlagspakke(
    grunnlagspakkeId: Int,
    newAinntektForPersonId: List<PeriodComparable<AinntektBo, AinntektspostBo>>,
    periodeFra: LocalDate,
    periodeTil: LocalDate,
    personId: String,
    timestampOppdatering: LocalDateTime
  ) {
    val existingAinntektForPersonId = hentAinntektForPersonIdToCompare(grunnlagspakkeId, personId)
    val ainntektPeriodComparator = AinntektPeriodComparator()

    // Finner ut hvilke inntekter som er oppdatert/nye siden sist, hvilke som ikke er endret og hvilke som er utl??pt.
    val comparatorResult =
      ainntektPeriodComparator.comparePeriodEntities(Period(periodeFra, periodeTil), newAinntektForPersonId, existingAinntektForPersonId)

    // Setter utl??pte Ainntekter til utl??pt.
    LOGGER.debug("Setter ${comparatorResult.expiredEntities.size} eksisterende Ainntekter til utl??pt.")
    comparatorResult.expiredEntities.forEach() { expiredEntity ->
      val expiredAinntekt = expiredEntity.periodEntity.copy(brukTil = timestampOppdatering, aktiv = false).toAinntektEntity()
      ainntektRepository.save(expiredAinntekt)
    }
    // Oppdaterer hentet tidspunkt for uendrede Ainntekter.
    LOGGER.debug("Oppdaterer ${comparatorResult.equalEntities.size} uendrede eksisterende Ainntekter med nytt hentet tidspunkt.")
    comparatorResult.equalEntities.forEach() { equalEntity ->
      val unchangedAinntekt = equalEntity.periodEntity.copy(hentetTidspunkt = timestampOppdatering).toAinntektEntity()
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
    newSkattegrunnlagForPersonId: List<PeriodComparable<SkattegrunnlagBo, SkattegrunnlagspostBo>>,
    periodeFra: LocalDate,
    periodeTil: LocalDate,
    personId: String,
    timestampOppdatering: LocalDateTime
  ) {
    val existingAinntektForPersonId = hentSkattegrunnlagForPersonIdToCompare(grunnlagspakkeId, personId)
    val ainntektPeriodComparator = SkattegrunnlagPeriodComparator()

    // Finner ut hvilke skattegrunnlag som er oppdatert/nye siden sist, hvilke som ikke er endret og hvilke som er utl??pt.
    val comparatorResult =
      ainntektPeriodComparator.comparePeriodEntities(Period(periodeFra, periodeTil), newSkattegrunnlagForPersonId, existingAinntektForPersonId)

    // Setter utl??pte skattegrunnlag til utl??pt.
    LOGGER.debug("Setter ${comparatorResult.expiredEntities.size} eksisterende skattegrunnlag til utl??pt.")
    comparatorResult.expiredEntities.forEach() { expiredEntity ->
      val expiredSkattegrunnlag = expiredEntity.periodEntity.copy(aktiv = false, brukTil = timestampOppdatering).toSkattegrunnlagEntity()
      skattegrunnlagRepository.save(expiredSkattegrunnlag)
    }
    // Oppdaterer hentet tidspunkt for uendrede skattegrunnlag.
    LOGGER.debug("Oppdaterer ${comparatorResult.equalEntities.size} uendrede eksisterende skattegrunnlag med nytt hentet tidspunkt.")
    comparatorResult.equalEntities.forEach() { equalEntity ->
      val unchangedSkattegrunnlag = equalEntity.periodEntity.copy(hentetTidspunkt = timestampOppdatering).toSkattegrunnlagEntity()
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

  fun hentAinntekt(grunnlagspakkeId: Int): List<AinntektDto> {
    val ainntektDtoListe = mutableListOf<AinntektDto>()
    ainntektRepository.hentAinntekter(grunnlagspakkeId)
      .forEach { inntekt ->
        val hentAinntektspostListe = mutableListOf<AinntektspostDto>()
        ainntektspostRepository.hentInntektsposter(inntekt.inntektId)
          .forEach { inntektspost ->
            hentAinntektspostListe.add(
              AinntektspostDto(
                inntektspost.utbetalingsperiode,
                inntektspost.opptjeningsperiodeFra,
                inntektspost.opptjeningsperiodeTil,
                inntektspost.opplysningspliktigId,
                inntektspost.virksomhetId,
                inntektspost.inntektType,
                inntektspost.fordelType,
                inntektspost.beskrivelse,
                inntektspost.belop,
                inntektspost.etterbetalingsperiodeFra,
                inntektspost.etterbetalingsperiodeTil
              )
            )
          }
        ainntektDtoListe.add(
          AinntektDto(
            personId = inntekt.personId,
            periodeFra = inntekt.periodeFra,
            periodeTil = inntekt.periodeTil,
            aktiv = inntekt.aktiv,
            brukFra = inntekt.brukFra,
            brukTil = inntekt.brukTil,
            opprettetTidspunkt = inntekt.hentetTidspunkt,
            ainntektspostListe = hentAinntektspostListe
          )
        )
      }

    return ainntektDtoListe

  }

  fun hentAinntektForPersonIdToCompare(grunnlagspakkeId: Int, personId: String): List<PeriodComparable<AinntektBo, AinntektspostBo>> {
    val ainntektForPersonIdListe = mutableListOf<PeriodComparable<AinntektBo, AinntektspostBo>>()
    ainntektRepository.hentAinntekter(grunnlagspakkeId)
      .forEach { inntekt ->
        if (inntekt.personId == personId) {
          val ainntektspostListe = mutableListOf<AinntektspostBo>()
          ainntektspostRepository.hentInntektsposter(inntekt.inntektId)
            .forEach() { ainntektspost -> ainntektspostListe.add(ainntektspost.toAinntektspostBo()) }
          ainntektForPersonIdListe.add(
            PeriodComparable(inntekt.toAinntektBo(), ainntektspostListe)
          )
        }
      }

    return ainntektForPersonIdListe
  }

  fun hentSkattegrunnlagForPersonIdToCompare(
    grunnlagspakkeId: Int,
    personId: String
  ): List<PeriodComparable<SkattegrunnlagBo, SkattegrunnlagspostBo>> {
    val skattegrunnlagForPersonIdListe = mutableListOf<PeriodComparable<SkattegrunnlagBo, SkattegrunnlagspostBo>>()
    skattegrunnlagRepository.hentSkattegrunnlag(grunnlagspakkeId)
      .forEach { skattegrunnlag ->
        if (skattegrunnlag.personId == personId) {
          val skattegrunnlagpostListe = mutableListOf<SkattegrunnlagspostBo>()
          skattegrunnlagspostRepository.hentSkattegrunnlagsposter(skattegrunnlag.skattegrunnlagId)
            .forEach() { skattegrunnlagspost -> skattegrunnlagpostListe.add(skattegrunnlagspost.toSkattegrunnlagspostBo()) }
          skattegrunnlagForPersonIdListe.add(
            PeriodComparable(skattegrunnlag.toSkattegrunnlagBo(), skattegrunnlagpostListe)
          )
        }
      }
    return skattegrunnlagForPersonIdListe
  }

  fun hentSkattegrunnlag(grunnlagspakkeId: Int): List<SkattegrunnlagDto> {
    val skattegrunnlagDtoListe = mutableListOf<SkattegrunnlagDto>()
    skattegrunnlagRepository.hentSkattegrunnlag(grunnlagspakkeId)
      .forEach { inntekt ->
        val hentSkattegrunnlagspostListe = mutableListOf<SkattegrunnlagspostDto>()
        skattegrunnlagspostRepository.hentSkattegrunnlagsposter(inntekt.skattegrunnlagId)
          .forEach { inntektspost ->
            hentSkattegrunnlagspostListe.add(
              SkattegrunnlagspostDto(
                skattegrunnlagType = inntektspost.skattegrunnlagType,
                inntektType = inntektspost.inntektType,
                belop = inntektspost.belop
              )
            )
          }
        skattegrunnlagDtoListe.add(
          SkattegrunnlagDto(
            personId = inntekt.personId,
            periodeFra = inntekt.periodeFra,
            periodeTil = inntekt.periodeTil,
            aktiv = inntekt.aktiv,
            brukFra = inntekt.brukFra,
            brukTil = inntekt.brukTil,
            opprettetTidspunkt = inntekt.hentetTidspunkt,
            hentSkattegrunnlagspostListe
          )
        )
      }

    return skattegrunnlagDtoListe

  }

  fun hentUtvidetBarnetrygdOgSmaabarnstillegg(grunnlagspakkeId: Int): List<UtvidetBarnetrygdOgSmaabarnstilleggDto> {
    val utvidetBarnetrygdOgSmaabarnstilleggDtoListe = mutableListOf<UtvidetBarnetrygdOgSmaabarnstilleggDto>()
    utvidetBarnetrygdOgSmaabarnstilleggRepository.hentUbst(grunnlagspakkeId)
      .forEach { ubst ->
        utvidetBarnetrygdOgSmaabarnstilleggDtoListe.add(
          UtvidetBarnetrygdOgSmaabarnstilleggDto(
            personId = ubst.personId,
            type = ubst.type,
            periodeFra = ubst.periodeFra,
            periodeTil = ubst.periodeTil,
            aktiv = ubst.aktiv,
            brukFra = ubst.brukFra,
            brukTil = ubst.brukTil,
            belop = ubst.belop,
            manueltBeregnet = ubst.manueltBeregnet,
            opprettetTidspunkt = ubst.hentetTidspunkt
          )
        )
      }
    return utvidetBarnetrygdOgSmaabarnstilleggDtoListe
  }

  fun hentBarnetillegg(grunnlagspakkeId: Int): List<BarnetilleggDto> {
    val barnetilleggDtoListe = mutableListOf<BarnetilleggDto>()
    barnetilleggRepository.hentBarnetillegg(grunnlagspakkeId)
      .forEach { barnetillegg ->
        barnetilleggDtoListe.add(
          BarnetilleggDto(
            partPersonId = barnetillegg.partPersonId,
            barnPersonId = barnetillegg.barnPersonId,
            barnetilleggType = barnetillegg.barnetilleggType,
            periodeFra = barnetillegg.periodeFra,
            periodeTil = barnetillegg.periodeTil,
            aktiv = barnetillegg.aktiv,
            brukFra = barnetillegg.brukFra,
            brukTil = barnetillegg.brukTil,
            belopBrutto = barnetillegg.belopBrutto,
            barnType = barnetillegg.barnType,
            opprettetTidspunkt = barnetillegg.hentetTidspunkt
          )
        )
      }
    return barnetilleggDtoListe
  }
}