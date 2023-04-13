package no.nav.bidrag.grunnlag.model

import no.nav.bidrag.behandling.felles.dto.grunnlag.OppdaterGrunnlagDto
import no.nav.bidrag.behandling.felles.enums.GrunnlagRequestType
import no.nav.bidrag.behandling.felles.enums.GrunnlagsRequestStatus
import no.nav.bidrag.behandling.felles.enums.barnetilsyn.Skolealder
import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.bo.BarnetilsynBo
import no.nav.bidrag.grunnlag.consumer.familieefsak.FamilieEfSakConsumer
import no.nav.bidrag.grunnlag.consumer.familieefsak.api.BarnetilsynRequest
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.service.PersistenceService
import no.nav.bidrag.grunnlag.service.PersonIdOgPeriodeRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.format.DateTimeFormatter

class OppdaterBarnetilsyn(
    private val grunnlagspakkeId: Int,
    private val timestampOppdatering: LocalDateTime,
    private val persistenceService: PersistenceService,
    private val familieEfSakConsumer: FamilieEfSakConsumer
) : MutableList<OppdaterGrunnlagDto> by mutableListOf() {

    companion object {
        @JvmStatic
        private val LOGGER: Logger = LoggerFactory.getLogger(OppdaterBarnetilsyn::class.java)
    }

    fun oppdaterBarnetilsyn(barnetilsynRequestListe: List<PersonIdOgPeriodeRequest>): OppdaterBarnetilsyn {
        barnetilsynRequestListe.forEach { personIdOgPeriode ->
            var antallPerioderFunnet = 0

            val barnetilsynRequest = BarnetilsynRequest(
                personIdOgPeriode.personId,
                personIdOgPeriode.periodeFra
            )

            LOGGER.info("Henter barnetilsyn for enslig forsørger")
            SECURE_LOGGER.info("Kaller barnetilsyn enslig forsørger med request: $barnetilsynRequest")

            when (
                val restResponseBarnetilsyn =
                    familieEfSakConsumer.hentBarnetilsyn(barnetilsynRequest)
            ) {
                is RestResponse.Success -> {
                    val barnetilsynResponse = restResponseBarnetilsyn.body
                    SECURE_LOGGER.info("Barnetilsyn ga følgende respons: $barnetilsynResponse")

                    persistenceService.oppdaterEksisterendeBarnetilsynTilInaktiv(
                        grunnlagspakkeId,
                        personIdOgPeriode.personId,
                        timestampOppdatering
                    )

                    barnetilsynResponse.barnetilsynBisysPerioder.forEach { bts ->
                        antallPerioderFunnet++
                        for (barnIdent in bts.barnIdenter) {
                            persistenceService.opprettBarnetilsyn(
                                BarnetilsynBo(
                                    grunnlagspakkeId = grunnlagspakkeId,
                                    partPersonId = personIdOgPeriode.personId,
                                    barnPersonId = barnIdent,
                                    periodeFra = bts.periode.fom,
                                    // justerer frem tildato med én dag for å ha lik logikk som resten av appen. Tildato skal angis som til, men ikke inkludert, dato.
                                    periodeTil = bts.periode.tom.plusMonths(1).withDayOfMonth(1),
                                    aktiv = true,
                                    brukFra = timestampOppdatering,
                                    brukTil = null,
                                    belop = null,
                                    tilsynstype = null,
                                    skolealder = null,
                                    hentetTidspunkt = timestampOppdatering
                                )
                            )
                        }
                    }
                    this.add(
                        OppdaterGrunnlagDto(
                            GrunnlagRequestType.BARNETILSYN,
                            personIdOgPeriode.personId,
                            GrunnlagsRequestStatus.HENTET,
                            "Antall perioder funnet: $antallPerioderFunnet"
                        )
                    )
                }

                is RestResponse.Failure -> this.add(
                    OppdaterGrunnlagDto(
                        GrunnlagRequestType.BARNETILSYN,
                        personIdOgPeriode.personId,
                        GrunnlagsRequestStatus.IKKE_FUNNET,
                        "Feil ved henting av barnetilsyn for perioden: ${personIdOgPeriode.periodeFra} - ${personIdOgPeriode.periodeTil}."
                    )
                )
            }
        }
        return this
    }

    // TODO: Vurdere om beregning av skolealder er noe som kan skje automatisk, eller om saksbehandler må ta stilling til dette
    // MR: for barn med dnr så vil ikke denne logikken fungere. Hente fødselsdato fra Barn-tabellen i Grunnlag i stedet?
    fun beregnSkolealder(barnIdent: String, fom: LocalDate): Skolealder {
        val dateFormatter = DateTimeFormatter.ofPattern("ddMMyy")
        val fodselsdato = LocalDate.parse(barnIdent.substring(IntRange(0, 5)), dateFormatter)

        if (alderErOver7Ar(fodselsdato) || (
            alderEr6Ar(fodselsdato) || (
                alderEr5Ar(fodselsdato) && fodtEtterForsteAugust(
                        fodselsdato
                    )
                )
            ) && barnetilsynGjelderFraFomForsteAugust(fom)
        ) {
            return Skolealder.OVER
        }
        return Skolealder.UNDER
    }

    private fun barnetilsynGjelderFraFomForsteAugust(fom: LocalDate) =
        fom.isAfter(LocalDate.of(fom.year, Month.JULY, 31))

    private fun fodtEtterForsteAugust(fodselsdato: LocalDate) =
        fodselsdato.isAfter(LocalDate.of(fodselsdato.year, Month.AUGUST, 1))

    private fun alderEr5Ar(fodselsdato: LocalDate) = hentAlderAr(fodselsdato) == 5

    private fun alderEr6Ar(fodselsdato: LocalDate) = hentAlderAr(fodselsdato) == 6

    private fun alderErOver7Ar(fodselsdato: LocalDate) = hentAlderAr(fodselsdato) >= 7

    private fun hentAlderAr(fodselsdato: LocalDate) = LocalDate.now().year - fodselsdato.year
}
