package no.nav.bidrag.grunnlag.model

import no.nav.bidrag.domain.enums.BarnType
import no.nav.bidrag.domain.enums.BarnetilleggType
import no.nav.bidrag.domain.enums.GrunnlagRequestType
import no.nav.bidrag.domain.enums.GrunnlagsRequestStatus
import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.bo.BarnetilleggBo
import no.nav.bidrag.grunnlag.consumer.pensjon.PensjonConsumer
import no.nav.bidrag.grunnlag.consumer.pensjon.api.HentBarnetilleggPensjonRequest
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.service.PersistenceService
import no.nav.bidrag.grunnlag.service.PersonIdOgPeriodeRequest
import no.nav.bidrag.transport.behandling.grunnlag.response.OppdaterGrunnlagDto
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

class OppdaterBarnetillegg(
    private val grunnlagspakkeId: Int,
    private val timestampOppdatering: LocalDateTime,
    private val persistenceService: PersistenceService,
    private val pensjonConsumer: PensjonConsumer
) : MutableList<OppdaterGrunnlagDto> by mutableListOf() {

    companion object {
        @JvmStatic
        private val LOGGER: Logger = LoggerFactory.getLogger(OppdaterBarnetillegg::class.java)
    }

    fun oppdaterBarnetillegg(barnetilleggRequestListe: List<PersonIdOgPeriodeRequest>): OppdaterBarnetillegg {
        barnetilleggRequestListe.forEach { personIdOgPeriode ->
            var antallPerioderFunnet = 0
            val hentBarnetilleggPensjonRequest = HentBarnetilleggPensjonRequest(
                mottaker = personIdOgPeriode.personId,
                fom = personIdOgPeriode.periodeFra,
                tom = personIdOgPeriode.periodeTil.minusDays(1)
            )

            LOGGER.info("Kaller barnetillegg pensjon")
            SECURE_LOGGER.info("Kaller barnetillegg pensjon med request: $hentBarnetilleggPensjonRequest")

            when (
                val restResponseBarnetilleggPensjon =
                    pensjonConsumer.hentBarnetilleggPensjon(hentBarnetilleggPensjonRequest)
            ) {
                is RestResponse.Success -> {
                    val barnetilleggPensjonResponse = restResponseBarnetilleggPensjon.body

                    SECURE_LOGGER.info("Barnetillegg pensjon ga følgende respons: $barnetilleggPensjonResponse")

                    persistenceService.oppdaterEksisterendeBarnetilleggPensjonTilInaktiv(
                        grunnlagspakkeId,
                        personIdOgPeriode.personId,
                        timestampOppdatering
                    )
                    barnetilleggPensjonResponse.barnetilleggPensjonListe?.forEach { bt ->
                        antallPerioderFunnet++
                        persistenceService.opprettBarnetillegg(
                            BarnetilleggBo(
                                grunnlagspakkeId = grunnlagspakkeId,
                                partPersonId = personIdOgPeriode.personId,
                                barnPersonId = bt.barn,
                                barnetilleggType = BarnetilleggType.PENSJON.toString(),
                                periodeFra = bt.fom,
                                // justerer frem tildato med én dag for å ha lik logikk som resten av appen. Tildato skal angis som til, men ikke inkludert, dato.
                                periodeTil = bt.tom.plusMonths(1)?.withDayOfMonth(1),
                                aktiv = true,
                                brukFra = timestampOppdatering,
                                brukTil = null,
                                belopBrutto = bt.beloep,
                                barnType = if (bt.erFellesbarn) BarnType.FELLES.toString() else BarnType.SÆRKULL.toString(),
                                hentetTidspunkt = timestampOppdatering
                            )
                        )
                    }
                    this.add(
                        OppdaterGrunnlagDto(
                            GrunnlagRequestType.BARNETILLEGG,
                            personIdOgPeriode.personId,
                            GrunnlagsRequestStatus.HENTET,
                            "Antall perioder funnet: $antallPerioderFunnet"
                        )
                    )
                }

                is RestResponse.Failure -> this.add(
                    OppdaterGrunnlagDto(
                        GrunnlagRequestType.BARNETILLEGG,
                        personIdOgPeriode.personId,
                        if (restResponseBarnetilleggPensjon.statusCode == HttpStatus.NOT_FOUND) GrunnlagsRequestStatus.IKKE_FUNNET else GrunnlagsRequestStatus.FEILET,
                        "Feil ved henting av barnetillegg pensjon for perioden: ${personIdOgPeriode.periodeFra} - ${personIdOgPeriode.periodeTil}."
                    )
                )
            }
        }
        return this
    }
}
