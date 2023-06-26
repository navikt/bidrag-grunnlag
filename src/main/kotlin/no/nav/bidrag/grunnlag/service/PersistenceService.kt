package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.behandling.felles.dto.grunnlag.AinntektDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.AinntektspostDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.BarnetilleggDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.BarnetilsynDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.BorISammeHusstandDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.KontantstotteDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.OpprettGrunnlagspakkeRequestDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.OvergangsstonadDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.RelatertPersonDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.SivilstandDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.SkattegrunnlagDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.SkattegrunnlagspostDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.UtvidetBarnetrygdOgSmaabarnstilleggDto
import no.nav.bidrag.behandling.felles.enums.BarnetilleggType
import no.nav.bidrag.behandling.felles.enums.SivilstandKode
import no.nav.bidrag.behandling.felles.enums.barnetilsyn.Skolealder
import no.nav.bidrag.behandling.felles.enums.barnetilsyn.Tilsyntype
import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.bo.AinntektBo
import no.nav.bidrag.grunnlag.bo.AinntektspostBo
import no.nav.bidrag.grunnlag.bo.BarnetilleggBo
import no.nav.bidrag.grunnlag.bo.BarnetilsynBo
import no.nav.bidrag.grunnlag.bo.KontantstotteBo
import no.nav.bidrag.grunnlag.bo.OvergangsstønadBo
import no.nav.bidrag.grunnlag.bo.RelatertPersonBo
import no.nav.bidrag.grunnlag.bo.SivilstandBo
import no.nav.bidrag.grunnlag.bo.SkattegrunnlagBo
import no.nav.bidrag.grunnlag.bo.SkattegrunnlagspostBo
import no.nav.bidrag.grunnlag.bo.UtvidetBarnetrygdOgSmaabarnstilleggBo
import no.nav.bidrag.grunnlag.bo.toAinntektEntity
import no.nav.bidrag.grunnlag.bo.toAinntektspostEntity
import no.nav.bidrag.grunnlag.bo.toBarnetilleggEntity
import no.nav.bidrag.grunnlag.bo.toBarnetilsynEntity
import no.nav.bidrag.grunnlag.bo.toKontantstotteEntity
import no.nav.bidrag.grunnlag.bo.toOvergangsstønadEntity
import no.nav.bidrag.grunnlag.bo.toRelatertPersonEntity
import no.nav.bidrag.grunnlag.bo.toSivilstandEntity
import no.nav.bidrag.grunnlag.bo.toSkattegrunnlagEntity
import no.nav.bidrag.grunnlag.bo.toSkattegrunnlagspostEntity
import no.nav.bidrag.grunnlag.bo.toUtvidetBarnetrygdOgSmaabarnstilleggEntity
import no.nav.bidrag.grunnlag.comparator.AinntektPeriodComparator
import no.nav.bidrag.grunnlag.comparator.Period
import no.nav.bidrag.grunnlag.comparator.PeriodComparable
import no.nav.bidrag.grunnlag.comparator.SkattegrunnlagPeriodComparator
import no.nav.bidrag.grunnlag.exception.custom.InvalidGrunnlagspakkeIdException
import no.nav.bidrag.grunnlag.persistence.entity.Ainntekt
import no.nav.bidrag.grunnlag.persistence.entity.Ainntektspost
import no.nav.bidrag.grunnlag.persistence.entity.Barnetillegg
import no.nav.bidrag.grunnlag.persistence.entity.Barnetilsyn
import no.nav.bidrag.grunnlag.persistence.entity.Grunnlagspakke
import no.nav.bidrag.grunnlag.persistence.entity.Kontantstotte
import no.nav.bidrag.grunnlag.persistence.entity.Overgangsstonad
import no.nav.bidrag.grunnlag.persistence.entity.RelatertPerson
import no.nav.bidrag.grunnlag.persistence.entity.Sivilstand
import no.nav.bidrag.grunnlag.persistence.entity.Skattegrunnlag
import no.nav.bidrag.grunnlag.persistence.entity.Skattegrunnlagspost
import no.nav.bidrag.grunnlag.persistence.entity.UtvidetBarnetrygdOgSmaabarnstillegg
import no.nav.bidrag.grunnlag.persistence.entity.toAinntektBo
import no.nav.bidrag.grunnlag.persistence.entity.toAinntektspostBo
import no.nav.bidrag.grunnlag.persistence.entity.toGrunnlagspakkeEntity
import no.nav.bidrag.grunnlag.persistence.entity.toSkattegrunnlagBo
import no.nav.bidrag.grunnlag.persistence.entity.toSkattegrunnlagspostBo
import no.nav.bidrag.grunnlag.persistence.repository.AinntektRepository
import no.nav.bidrag.grunnlag.persistence.repository.AinntektspostRepository
import no.nav.bidrag.grunnlag.persistence.repository.BarnetilleggRepository
import no.nav.bidrag.grunnlag.persistence.repository.BarnetilsynRepository
import no.nav.bidrag.grunnlag.persistence.repository.GrunnlagspakkeRepository
import no.nav.bidrag.grunnlag.persistence.repository.KontantstotteRepository
import no.nav.bidrag.grunnlag.persistence.repository.OvergangsstonadRepository
import no.nav.bidrag.grunnlag.persistence.repository.RelatertPersonRepository
import no.nav.bidrag.grunnlag.persistence.repository.SivilstandRepository
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
    val barnetilleggRepository: BarnetilleggRepository,
    val relatertPersonRepository: RelatertPersonRepository,
    val sivilstandRepository: SivilstandRepository,
    val kontantstotteRepository: KontantstotteRepository,
    val barnetilsynRepository: BarnetilsynRepository,
    val overgangsstønadRepository: OvergangsstonadRepository
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

    fun opprettUtvidetBarnetrygdOgSmaabarnstillegg(utvidetBarnetrygdOgSmaabarnstilleggBo: UtvidetBarnetrygdOgSmaabarnstilleggBo): UtvidetBarnetrygdOgSmaabarnstillegg {
        val nyUbst = utvidetBarnetrygdOgSmaabarnstilleggBo.toUtvidetBarnetrygdOgSmaabarnstilleggEntity()
        return utvidetBarnetrygdOgSmaabarnstilleggRepository.save(nyUbst)
    }

    fun opprettBarnetillegg(barnetilleggBo: BarnetilleggBo): Barnetillegg {
        val nyBarnetillegg = barnetilleggBo.toBarnetilleggEntity()
        return barnetilleggRepository.save(nyBarnetillegg)
    }

    fun opprettRelatertPerson(relatertPersonBo: RelatertPersonBo): RelatertPerson {
        val nyRelatertPerson = relatertPersonBo.toRelatertPersonEntity()
        return relatertPersonRepository.save(nyRelatertPerson)
    }

    fun opprettSivilstand(sivilstandBo: SivilstandBo): Sivilstand {
        val nySivilstand = sivilstandBo.toSivilstandEntity()
        return sivilstandRepository.save(nySivilstand)
    }

    fun opprettKontantstotte(kontantstotteBo: KontantstotteBo): Kontantstotte {
        val nyKontantstotte = kontantstotteBo.toKontantstotteEntity()
        return kontantstotteRepository.save(nyKontantstotte)
    }

    fun opprettBarnetilsyn(barnetilsynBo: BarnetilsynBo): Barnetilsyn {
        val nyBarnetilsyn = barnetilsynBo.toBarnetilsynEntity()
        return barnetilsynRepository.save(nyBarnetilsyn)
    }

    fun opprettOvergangsstønad(overgangsstønadBo: OvergangsstønadBo): Overgangsstonad {
        val nyOvergangsstønad = overgangsstønadBo.toOvergangsstønadEntity()
        return overgangsstønadRepository.save(nyOvergangsstønad)
    }

    fun oppdaterEksisterendeBarnetilleggPensjonTilInaktiv(
        grunnlagspakkeId: Int,
        partPersonId: String,
        timestampOppdatering: LocalDateTime
    ) {
        barnetilleggRepository.oppdaterEksisterendeBarnetilleggTilInaktiv(
            grunnlagspakkeId,
            partPersonId,
            timestampOppdatering,
            BarnetilleggType.PENSJON.toString()
        )
    }

    fun oppdaterEksisterendeRelatertPersonTilInaktiv(
        grunnlagspakkeId: Int,
        partPersonId: String,
        timestampOppdatering: LocalDateTime
    ) {
        relatertPersonRepository.oppdaterEksisterendeRelatertPersonTilInaktiv(
            grunnlagspakkeId,
            partPersonId,
            timestampOppdatering
        )
    }

    fun oppdaterEksisterendeSivilstandTilInaktiv(
        grunnlagspakkeId: Int,
        partPersonId: String,
        timestampOppdatering: LocalDateTime
    ) {
        sivilstandRepository.oppdaterEksisterendeSivilstandTilInaktiv(
            grunnlagspakkeId,
            partPersonId,
            timestampOppdatering
        )
    }

    fun oppdaterEksisterendeUtvidetBarnetrygOgSmaabarnstilleggTilInaktiv(
        grunnlagspakkeId: Int,
        personId: String,
        timestampOppdatering: LocalDateTime
    ) {
        utvidetBarnetrygdOgSmaabarnstilleggRepository.oppdaterEksisterendeUtvidetBarnetrygOgSmaabarnstilleggTilInaktiv(
            grunnlagspakkeId,
            personId,
            timestampOppdatering
        )
    }

    fun oppdaterEksisterendeBarnetilsynTilInaktiv(
        grunnlagspakkeId: Int,
        partPersonId: String,
        timestampOppdatering: LocalDateTime
    ) {
        barnetilsynRepository.oppdaterEksisterendeBarnetilsynTilInaktiv(
            grunnlagspakkeId,
            partPersonId,
            timestampOppdatering
        )
    }

    fun oppdaterEksisterendeKontantstotteTilInaktiv(
        grunnlagspakkeId: Int,
        partPersonId: String,
        timestampOppdatering: LocalDateTime
    ) {
        kontantstotteRepository.oppdaterEksisterendeKontantstotteTilInaktiv(
            grunnlagspakkeId,
            partPersonId,
            timestampOppdatering
        )
    }

    fun oppdaterEksisterendeOvergangsstønadTilInaktiv(
        grunnlagspakkeId: Int,
        partPersonId: String,
        timestampOppdatering: LocalDateTime
    ) {
        overgangsstønadRepository.oppdaterEksisterendeOvergangsstonadTilInaktiv(
            grunnlagspakkeId,
            partPersonId,
            timestampOppdatering
        )
    }

    // Returnerer formaal som er angitt for grunnlagspakken
    fun hentFormaalGrunnlagspakke(grunnlagspakkeId: Int): String {
        return grunnlagspakkeRepository.hentFormaalGrunnlagspakke(grunnlagspakkeId)
    }

    // Valider at grunnlagspakke eksisterer
    fun validerGrunnlagspakke(grunnlagspakkeId: Int) {
        if (!grunnlagspakkeRepository.existsById(grunnlagspakkeId)) {
            throw InvalidGrunnlagspakkeIdException("Grunnlagspakke med id $grunnlagspakkeId finnes ikke")
        }
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
        newAinntektForPersonId: List<PeriodComparable<AinntektBo, AinntektspostBo>>,
        periodeFra: LocalDate,
        periodeTil: LocalDate,
        personId: String,
        timestampOppdatering: LocalDateTime
    ) {
        val existingAinntektForPersonId = hentAinntektForPersonIdToCompare(grunnlagspakkeId, personId)
        val ainntektPeriodComparator = AinntektPeriodComparator()

        // Finner ut hvilke inntekter som er oppdatert/nye siden sist, hvilke som ikke er endret og hvilke som er utløpt.
        val comparatorResult =
            ainntektPeriodComparator.comparePeriodEntities(
                Period(periodeFra, periodeTil),
                newAinntektForPersonId,
                existingAinntektForPersonId
            )

        // Setter utløpte Ainntekter til utløpt.
        SECURE_LOGGER.debug("Setter ${comparatorResult.expiredEntities.size} eksisterende Ainntekter til utløpt.")
        comparatorResult.expiredEntities.forEach() { expiredEntity ->
            val expiredAinntekt =
                expiredEntity.periodEntity.copy(brukTil = timestampOppdatering, aktiv = false)
                    .toAinntektEntity()
            ainntektRepository.save(expiredAinntekt)
        }
        // Oppdaterer hentet tidspunkt for uendrede Ainntekter.
        SECURE_LOGGER.debug("Oppdaterer ${comparatorResult.equalEntities.size} uendrede eksisterende Ainntekter med nytt hentet tidspunkt.")
        comparatorResult.equalEntities.forEach() { equalEntity ->
            val unchangedAinntekt =
                equalEntity.periodEntity.copy(hentetTidspunkt = timestampOppdatering).toAinntektEntity()
            SECURE_LOGGER.debug("Oppdaterer for inntektId = ${unchangedAinntekt.inntektId}")
            ainntektRepository.save(unchangedAinntekt)
        }
        // Lagrer nye Ainntekter og Ainntektsposter.
        SECURE_LOGGER.debug("Oppretter ${comparatorResult.updatedEntities.size} nye Ainntekter med underliggende inntektsposter")
        comparatorResult.updatedEntities.forEach() { updatedEntity ->
            val ainntekt = ainntektRepository.save(updatedEntity.periodEntity.toAinntektEntity())
            updatedEntity.children?.forEach() { ainntektspostDto ->
                val updatedAinntekt =
                    ainntektspostDto.copy(inntektId = ainntekt.inntektId).toAinntektspostEntity()
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
        val existingAinntektForPersonId =
            hentSkattegrunnlagForPersonIdToCompare(grunnlagspakkeId, personId)
        val ainntektPeriodComparator = SkattegrunnlagPeriodComparator()

        // Finner ut hvilke skattegrunnlag som er oppdatert/nye siden sist, hvilke som ikke er endret og hvilke som er utløpt.
        val comparatorResult =
            ainntektPeriodComparator.comparePeriodEntities(
                Period(periodeFra, periodeTil),
                newSkattegrunnlagForPersonId,
                existingAinntektForPersonId
            )

        // Setter utløpte skattegrunnlag til utløpt.
        SECURE_LOGGER.debug("Setter ${comparatorResult.expiredEntities.size} eksisterende skattegrunnlag til utløpt.")
        comparatorResult.expiredEntities.forEach() { expiredEntity ->
            val expiredSkattegrunnlag =
                expiredEntity.periodEntity.copy(aktiv = false, brukTil = timestampOppdatering)
                    .toSkattegrunnlagEntity()
            skattegrunnlagRepository.save(expiredSkattegrunnlag)
        }
        // Oppdaterer hentet tidspunkt for uendrede skattegrunnlag.
        SECURE_LOGGER.debug("Oppdaterer ${comparatorResult.equalEntities.size} uendrede eksisterende skattegrunnlag med nytt hentet tidspunkt.")
        comparatorResult.equalEntities.forEach() { equalEntity ->
            val unchangedSkattegrunnlag =
                equalEntity.periodEntity.copy(hentetTidspunkt = timestampOppdatering)
                    .toSkattegrunnlagEntity()
            skattegrunnlagRepository.save(unchangedSkattegrunnlag)
        }
        // Lagrer nye skattegrunnlag og skattegrunnlagsposter.
        SECURE_LOGGER.debug("Oppretter ${comparatorResult.updatedEntities.size} nye skattegrunnlag med underliggende skattegrunnlagsposter")
        comparatorResult.updatedEntities.forEach() { updatedEntity ->
            val updatedSkattegrunnlag =
                skattegrunnlagRepository.save(updatedEntity.periodEntity.toSkattegrunnlagEntity())
            updatedEntity.children?.forEach() { ainntektspostDto ->
                val skattegrunnlagspost =
                    ainntektspostDto.copy(skattegrunnlagId = updatedSkattegrunnlag.skattegrunnlagId)
                        .toSkattegrunnlagspostEntity()
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
                        hentetTidspunkt = inntekt.hentetTidspunkt,
                        ainntektspostListe = hentAinntektspostListe
                    )
                )
            }

        return ainntektDtoListe
    }

    fun hentAinntektForPersonIdToCompare(
        grunnlagspakkeId: Int,
        personId: String
    ): List<PeriodComparable<AinntektBo, AinntektspostBo>> {
        val ainntektForPersonIdListe = mutableListOf<PeriodComparable<AinntektBo, AinntektspostBo>>()
        ainntektRepository.hentAinntekter(grunnlagspakkeId)
            .forEach { inntekt ->
                SECURE_LOGGER.debug("Hentet eksisterende ainntekter med id ${inntekt.inntektId}")
                if (inntekt.personId == personId) {
                    val ainntektspostListe = mutableListOf<AinntektspostBo>()
                    ainntektspostRepository.hentInntektsposter(inntekt.inntektId)
                        .forEach() { ainntektspost ->
                            ainntektspostListe.add(ainntektspost.toAinntektspostBo())
                        }
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
        val skattegrunnlagForPersonIdListe =
            mutableListOf<PeriodComparable<SkattegrunnlagBo, SkattegrunnlagspostBo>>()
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
                        hentetTidspunkt = inntekt.hentetTidspunkt,
                        hentSkattegrunnlagspostListe
                    )
                )
            }

        return skattegrunnlagDtoListe
    }

    fun hentUtvidetBarnetrygdOgSmaabarnstillegg(grunnlagspakkeId: Int): List<UtvidetBarnetrygdOgSmaabarnstilleggDto> {
        val utvidetBarnetrygdOgSmaabarnstilleggDtoListe =
            mutableListOf<UtvidetBarnetrygdOgSmaabarnstilleggDto>()
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
                        hentetTidspunkt = ubst.hentetTidspunkt
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
                        hentetTidspunkt = barnetillegg.hentetTidspunkt
                    )
                )
            }
        return barnetilleggDtoListe
    }

    fun hentKontantstotte(grunnlagspakkeId: Int): List<KontantstotteDto> {
        val kontantstotteDtoListe = mutableListOf<KontantstotteDto>()
        kontantstotteRepository.hentKontantstotte(grunnlagspakkeId)
            .forEach { kontantstotte ->
                kontantstotteDtoListe.add(
                    KontantstotteDto(
                        partPersonId = kontantstotte.partPersonId,
                        barnPersonId = kontantstotte.barnPersonId,
                        periodeFra = kontantstotte.periodeFra,
                        periodeTil = kontantstotte.periodeTil,
                        aktiv = kontantstotte.aktiv,
                        brukFra = kontantstotte.brukFra,
                        brukTil = kontantstotte.brukTil,
                        belop = kontantstotte.belop,
                        hentetTidspunkt = kontantstotte.hentetTidspunkt
                    )
                )
            }
        return kontantstotteDtoListe
    }

    // Henter alle husstandsmedlemmer og personenes egne barn, uavhengig om de bor i samme husstand.

    fun hentHusstandsmedlemmerOgEgneBarn(grunnlagspakkeId: Int): List<RelatertPersonDto> {
        val husstandsmedlemmerOgEgneBarnListe = mutableListOf<RelatertPersonDto>()

        val relatertePersonerListe = relatertPersonRepository.hentRelatertePersoner(grunnlagspakkeId)
        // relatertePersonerListe inneholder alle personer (partPersonId) grunnlaget husstandsmedlemmer er innhentet for i grunnlagspakken.
        // For å unngå duplikate perioder i responsen må det sjekkes at periode gjelder for aktuell partPersonId.
        var behandletPartPersonId: String? = null

        relatertePersonerListe.forEach { part ->
            // En relatert person kan forekomme flere ganger i uttrekk fra tabell, én gang for hver periode personen har delt bolig
            // med BM/BP (partPersonId). I responsen fra bidrag-grunnlag skal hver person kun ligge én gang, med en liste over perioder personen
            // har delt bolig med BM/BP (part_person_id). Sjekker derfor under om personen allerede har blitt lagt på responsen.

            if (part.partPersonId != behandletPartPersonId) {
                val filtrertListe = relatertePersonerListe.filter { it.partPersonId == part.partPersonId }
                behandletPartPersonId = part.partPersonId

                var behandletPerson: String? = null

                filtrertListe.forEach { relatertPerson ->
                    if (relatertPerson.relatertPersonPersonId != behandletPerson) {
                        val borISammeHusstandListe = mutableListOf<BorISammeHusstandDto>()
                        val alleForekomsterAvRelatertPerson =
                            filtrertListe.filter { it.relatertPersonPersonId == relatertPerson.relatertPersonPersonId }
                        alleForekomsterAvRelatertPerson.forEach { person ->
                            if (person.husstandsmedlemPeriodeFra != null || person.husstandsmedlemPeriodeTil != null) {
                                borISammeHusstandListe.add(BorISammeHusstandDto(person.husstandsmedlemPeriodeFra, person.husstandsmedlemPeriodeTil))
                            }
                        }

                        husstandsmedlemmerOgEgneBarnListe.add(
                            RelatertPersonDto(
                                partPersonId = relatertPerson.partPersonId,
                                relatertPersonPersonId = relatertPerson.relatertPersonPersonId,
                                navn = relatertPerson.navn,
                                fodselsdato = relatertPerson.fodselsdato,
                                erBarnAvBmBp = relatertPerson.erBarnAvBmBp,
                                aktiv = relatertPerson.aktiv,
                                brukFra = relatertPerson.brukFra,
                                brukTil = relatertPerson.brukTil,
                                hentetTidspunkt = relatertPerson.hentetTidspunkt,
                                borISammeHusstandDtoListe = borISammeHusstandListe
                            )
                        )
                        behandletPerson = relatertPerson.relatertPersonPersonId
                    }
                }
            }
        }

        return husstandsmedlemmerOgEgneBarnListe
    }

    fun hentSivilstand(grunnlagspakkeId: Int): List<SivilstandDto> {
        val sivilstandDtoListe = mutableListOf<SivilstandDto>()
        sivilstandRepository.hentSivilstand(grunnlagspakkeId)
            .forEach { sivilstand ->
                sivilstandDtoListe.add(
                    SivilstandDto(
                        personId = sivilstand.personId,
                        periodeFra = sivilstand.periodeFra,
                        periodeTil = sivilstand.periodeTil,
                        sivilstand = SivilstandKode.valueOf(sivilstand.sivilstand),
                        aktiv = sivilstand.aktiv,
                        brukFra = sivilstand.brukFra,
                        brukTil = sivilstand.brukTil,
                        hentetTidspunkt = sivilstand.hentetTidspunkt
                    )
                )
            }
        return sivilstandDtoListe
    }

    fun hentBarnetilsyn(grunnlagspakkeId: Int): List<BarnetilsynDto> {
        val barnetilsynDtoListe = mutableListOf<BarnetilsynDto>()
        barnetilsynRepository.hentBarnetilsyn(grunnlagspakkeId)
            .forEach { barnetilsyn ->
                barnetilsynDtoListe.add(
                    BarnetilsynDto(
                        partPersonId = barnetilsyn.partPersonId,
                        barnPersonId = barnetilsyn.barnPersonId,
                        periodeFra = barnetilsyn.periodeFra,
                        periodeTil = barnetilsyn.periodeTil,
                        aktiv = barnetilsyn.aktiv,
                        brukFra = barnetilsyn.brukFra,
                        brukTil = barnetilsyn.brukTil,
                        belop = barnetilsyn.belop,
                        tilsynstype = barnetilsyn.tilsynstype ?: Tilsyntype.IKKE_ANGITT,
                        skolealder = barnetilsyn.skolealder ?: Skolealder.IKKE_ANGITT,
                        hentetTidspunkt = barnetilsyn.hentetTidspunkt
                    )
                )
            }
        return barnetilsynDtoListe
    }

    fun hentOvergangsstønad(grunnlagspakkeId: Int): List<OvergangsstonadDto> {
        val overgangsstønadDtoListe = mutableListOf<OvergangsstonadDto>()
        overgangsstønadRepository.hentOvergangsstonad(grunnlagspakkeId)
            .forEach { overgangsstønad ->
                overgangsstønadDtoListe.add(
                    OvergangsstonadDto(
                        partPersonId = overgangsstønad.partPersonId,
                        periodeFra = overgangsstønad.periodeFra,
                        periodeTil = overgangsstønad.periodeTil,
                        aktiv = overgangsstønad.aktiv,
                        brukFra = overgangsstønad.brukFra,
                        brukTil = overgangsstønad.brukTil,
                        belop = overgangsstønad.belop,
                        hentetTidspunkt = overgangsstønad.hentetTidspunkt
                    )
                )
            }
        return overgangsstønadDtoListe
    }
}
