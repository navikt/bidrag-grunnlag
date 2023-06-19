package no.nav.bidrag.grunnlag.model

import no.nav.bidrag.behandling.felles.dto.grunnlag.OppdaterGrunnlagDto
import no.nav.bidrag.behandling.felles.enums.GrunnlagRequestType
import no.nav.bidrag.behandling.felles.enums.GrunnlagsRequestStatus
import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.consumer.aareg.AaregConsumer
import no.nav.bidrag.grunnlag.consumer.aareg.api.HentArbeidsforholdRequest
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.service.PersistenceService
import no.nav.bidrag.grunnlag.service.PersonIdOgPeriodeRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

class OppdaterArbeidsforhold(
    private val grunnlagspakkeId: Int,
    private val timestampOppdatering: LocalDateTime,
    private val persistenceService: PersistenceService,
    private val aaregConsumer: AaregConsumer
) : MutableList<OppdaterGrunnlagDto> by mutableListOf() {

    companion object {
        @JvmStatic
        private val LOGGER: Logger = LoggerFactory.getLogger(OppdaterArbeidsforhold::class.java)
    }

    fun oppdaterArbeidsforhold(arbeidsforholdRequestListe: List<PersonIdOgPeriodeRequest>): OppdaterArbeidsforhold {
        arbeidsforholdRequestListe.forEach { personIdOgPeriode ->
            var antallPerioderFunnet = 0
            val hentArbeidsforholdRequest = HentArbeidsforholdRequest(
                arbeidstakerId = personIdOgPeriode.personId
            )

            LOGGER.info("Kaller Aareg for å hente arbeidsforhold")
            SECURE_LOGGER.info("Kaller Aareg request: $hentArbeidsforholdRequest")

            when (
                val restResponseArbeidsforhold =
                    aaregConsumer.hentArbeidsforhold(hentArbeidsforholdRequest)
            ) {
                is RestResponse.Success -> {
                    val arbeidsforholdResponse = restResponseArbeidsforhold.body

                    SECURE_LOGGER.info("Aareg hent arbeidsforhold følgende respons: $arbeidsforholdResponse")

/*                    persistenceService.oppdaterEksisterendeArbeidsforholdTilInaktiv(
                        grunnlagspakkeId,
                        personIdOgPeriode.personId,
                        timestampOppdatering
                    )
                    arbeidsforholdResponse.arbeidsforholdListe?.forEach { af ->
                        antallPerioderFunnet++
                        persistenceService.opprettArbeidsforhold(
                            ArbeidsforholdBo(
                                grunnlagspakkeId = grunnlagspakkeId,
                                partPersonId = personIdOgPeriode.personId,
                                barnPersonId = af.barn,
                                barnetilleggType = BarnetilleggType.PENSJON.toString(),
                                periodeFra = af.fom,
                                // justerer frem tildato med én dag for å ha lik logikk som resten av appen. Tildato skal angis som til, men ikke inkludert, dato.
                                periodeTil = af.tom?.plusMonths(1)?.withDayOfMonth(1),
                                aktiv = true,
                                brukFra = timestampOppdatering,
                                brukTil = null,
                                belopBrutto = af.beloep,
                                barnType = if (af.erFellesbarn) BarnType.FELLES.toString() else BarnType.SÆRKULL.toString(),
                                hentetTidspunkt = timestampOppdatering
                            )
                        )
                    }
                    this.add(
                        OppdaterGrunnlagDto(
                            GrunnlagRequestType.ARBEIDSFORHOLD,
                            personIdOgPeriode.personId,
                            GrunnlagsRequestStatus.HENTET,
                            "Antall perioder funnet: $antallPerioderFunnet"
                        )
                    )*/
                }

                is RestResponse.Failure -> this.add(
                    OppdaterGrunnlagDto(
                        GrunnlagRequestType.ARBEIDSFORHOLD,
                        personIdOgPeriode.personId,
                        if (restResponseArbeidsforhold.statusCode == HttpStatus.NOT_FOUND) GrunnlagsRequestStatus.IKKE_FUNNET else GrunnlagsRequestStatus.FEILET,
                        "Feil ved henting av arbeidsforhold for perioden: ${personIdOgPeriode.periodeFra} - ${personIdOgPeriode.periodeTil}."
                    )
                )
            }
        }
        return this
    }
}
