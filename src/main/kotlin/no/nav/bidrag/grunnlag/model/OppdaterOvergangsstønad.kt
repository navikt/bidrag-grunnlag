package no.nav.bidrag.grunnlag.model

import no.nav.bidrag.behandling.felles.dto.grunnlag.OppdaterGrunnlagDto
import no.nav.bidrag.behandling.felles.enums.GrunnlagRequestType
import no.nav.bidrag.behandling.felles.enums.GrunnlagsRequestStatus
import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.bo.OvergangsstønadBo
import no.nav.bidrag.grunnlag.consumer.familieefsak.FamilieEfSakConsumer
import no.nav.bidrag.grunnlag.consumer.familieefsak.api.EksternePerioderRequest
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.service.PersistenceService
import no.nav.bidrag.grunnlag.service.PersonIdOgPeriodeRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

class OppdaterOvergangsstønad(
    private val grunnlagspakkeId: Int,
    private val timestampOppdatering: LocalDateTime,
    private val persistenceService: PersistenceService,
    private val familieEfSakConsumer: FamilieEfSakConsumer
) : MutableList<OppdaterGrunnlagDto> by mutableListOf() {

    companion object {
        @JvmStatic
        private val LOGGER: Logger = LoggerFactory.getLogger(OppdaterOvergangsstønad::class.java)
    }

    fun oppdaterOvergangsstønad(overgangsstønadRequestListe: List<PersonIdOgPeriodeRequest>): OppdaterOvergangsstønad {
        overgangsstønadRequestListe.forEach { personIdOgPeriode ->
            var antallPerioderFunnet = 0

            // Input til tjeneste er en liste over alle personnr for en person,
            // kall PDL for å hente historikk på fnr?
            val innsynRequest = EksternePerioderRequest(
                personIdOgPeriode.personId,
                personIdOgPeriode.periodeFra,
                personIdOgPeriode.periodeTil
            )

            LOGGER.info("Kaller EF-sak og henter overgangsstønad")
            SECURE_LOGGER.info("Kaller EF-sak og henter overgangsstønad med request: $innsynRequest")

            when (
                val restResponseOvergangsstønad =
                    familieEfSakConsumer.hentOvergangsstønad(innsynRequest)
            ) {
                is RestResponse.Success -> {
                    val overgangsstønadResponse = restResponseOvergangsstønad.body
                    SECURE_LOGGER.info("EF-sak overgangsstønad ga følgende respons: $overgangsstønadResponse")

                    persistenceService.oppdaterEksisterendeOvergangsstønadTilInaktiv(
                        grunnlagspakkeId,
                        personIdOgPeriode.personId,
                        timestampOppdatering
                    )

                    // Overgangsstønad fra ef-sak
                    overgangsstønadResponse.data.perioder.forEach { periode ->
                        if (periode.fomDato.isBefore(personIdOgPeriode.periodeTil)) {
                            antallPerioderFunnet++
                            persistenceService.opprettOvergangsstønad(
                                OvergangsstønadBo(
                                    grunnlagspakkeId = grunnlagspakkeId,
                                    partPersonId = personIdOgPeriode.personId,
                                    periodeFra = periode.fomDato,
                                    // justerer frem tildato med én dag for å ha lik logikk som resten av appen. Tildato skal angis som til, men ikke inkludert, dato.
                                    periodeTil = periode.tomDato.plusMonths(1).withDayOfMonth(1),
                                    aktiv = true,
                                    brukFra = timestampOppdatering,
                                    belop = periode.beløp,
                                    brukTil = null,
                                    hentetTidspunkt = timestampOppdatering
                                )
                            )
                        }
                    }
                    this.add(
                        OppdaterGrunnlagDto(
                            GrunnlagRequestType.OVERGANGSSTONAD,
                            personIdOgPeriode.personId,
                            GrunnlagsRequestStatus.HENTET,
                            "Antall perioder funnet: $antallPerioderFunnet"
                        )
                    )
                }

                is RestResponse.Failure -> {
                    SECURE_LOGGER.info("EF-sak overgangsstønad ga feil og følgende respons: $restResponseOvergangsstønad")
                    this.add(
                        OppdaterGrunnlagDto(
                            GrunnlagRequestType.OVERGANGSSTONAD,
                            personIdOgPeriode.personId,
                            if (restResponseOvergangsstønad.statusCode == HttpStatus.NOT_FOUND) GrunnlagsRequestStatus.IKKE_FUNNET else GrunnlagsRequestStatus.FEILET,
                            "Feil ved henting av overgangsstønad for perioden: ${personIdOgPeriode.periodeFra} - ${personIdOgPeriode.periodeTil}."
                        )
                    )
                    SECURE_LOGGER.info("overgangsstønad familie-ef-sak svarer med feil, respons: $restResponseOvergangsstønad")
                }
            }
        }
        return this
    }
}
