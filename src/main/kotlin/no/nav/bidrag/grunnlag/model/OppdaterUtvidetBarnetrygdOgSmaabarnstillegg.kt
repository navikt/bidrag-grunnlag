package no.nav.bidrag.grunnlag.model

import no.nav.bidrag.behandling.felles.dto.grunnlag.OppdaterGrunnlagDto
import no.nav.bidrag.behandling.felles.enums.GrunnlagRequestType
import no.nav.bidrag.behandling.felles.enums.GrunnlagsRequestStatus
import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.bo.UtvidetBarnetrygdOgSmaabarnstilleggBo
import no.nav.bidrag.grunnlag.consumer.familiebasak.FamilieBaSakConsumer
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.FamilieBaSakRequest
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.service.PersistenceService
import no.nav.bidrag.grunnlag.service.PersonIdOgPeriodeRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

class OppdaterUtvidetBarnetrygdOgSmaabarnstillegg(
    private val grunnlagspakkeId: Int,
    private val timestampOppdatering: LocalDateTime,
    private val persistenceService: PersistenceService,
    private val familieBaSakConsumer: FamilieBaSakConsumer
) : MutableList<OppdaterGrunnlagDto> by mutableListOf() {

    companion object {
        @JvmStatic
        private val LOGGER: Logger =
            LoggerFactory.getLogger(OppdaterUtvidetBarnetrygdOgSmaabarnstillegg::class.java)
    }

    fun oppdaterUtvidetBarnetrygdOgSmaabarnstillegg(utvidetBarnetrygdOgSmaabarnstilleggRequestListe: List<PersonIdOgPeriodeRequest>): OppdaterUtvidetBarnetrygdOgSmaabarnstillegg {
        utvidetBarnetrygdOgSmaabarnstilleggRequestListe.forEach { personIdOgPeriode ->
            var antallPerioderFunnet = 0
            val familieBaSakRequest = FamilieBaSakRequest(
                personIdent = personIdOgPeriode.personId,
                fraDato = personIdOgPeriode.periodeFra
            )

            LOGGER.info("Kaller familie-ba-sak")
            SECURE_LOGGER.info("Kaller familie-ba-sak med request: $familieBaSakRequest")

            when (
                val restResponseFamilieBaSak =
                    familieBaSakConsumer.hentFamilieBaSak(familieBaSakRequest)
            ) {
                is RestResponse.Success -> {
                    val familieBaSakResponse = restResponseFamilieBaSak.body
                    SECURE_LOGGER.info("familie-ba-sak ga følgende respons: $familieBaSakResponse")
                    persistenceService.oppdaterEksisterendeUtvidetBarnetrygOgSmaabarnstilleggTilInaktiv(
                        grunnlagspakkeId,
                        personIdOgPeriode.personId,
                        timestampOppdatering
                    )
                    familieBaSakResponse.perioder.forEach { ubst ->
                        antallPerioderFunnet++
                        persistenceService.opprettUtvidetBarnetrygdOgSmaabarnstillegg(
                            UtvidetBarnetrygdOgSmaabarnstilleggBo(
                                grunnlagspakkeId = grunnlagspakkeId,
                                personId = personIdOgPeriode.personId,
                                type = ubst.stønadstype.toString(),
                                periodeFra = LocalDate.parse(ubst.fomMåned.toString() + "-01"),
                                // justerer frem tildato med én dag for å ha lik logikk som resten av appen. Tildato skal angis som til, men ikke inkludert, dato.
                                periodeTil = if (ubst.tomMåned != null) {
                                    LocalDate.parse(ubst.tomMåned.toString() + "-01")
                                        .plusMonths(1)
                                } else {
                                    null
                                },
                                brukFra = timestampOppdatering,
                                belop = BigDecimal.valueOf(ubst.beløp),
                                manueltBeregnet = ubst.manueltBeregnet,
                                deltBosted = ubst.deltBosted,
                                hentetTidspunkt = timestampOppdatering
                            )
                        )
                    }
                    this.add(
                        OppdaterGrunnlagDto(
                            GrunnlagRequestType.UTVIDET_BARNETRYGD_OG_SMAABARNSTILLEGG,
                            personIdOgPeriode.personId,
                            GrunnlagsRequestStatus.HENTET,
                            "Antall perioder funnet: $antallPerioderFunnet"
                        )
                    )
                }

                is RestResponse.Failure -> this.add(
                    OppdaterGrunnlagDto(
                        GrunnlagRequestType.UTVIDET_BARNETRYGD_OG_SMAABARNSTILLEGG,
                        personIdOgPeriode.personId,
                        if (restResponseFamilieBaSak.statusCode == HttpStatus.NOT_FOUND) GrunnlagsRequestStatus.IKKE_FUNNET else GrunnlagsRequestStatus.FEILET,
                        "Feil ved henting av familie-ba-sak for perioden: ${personIdOgPeriode.periodeFra} - ${personIdOgPeriode.periodeTil}."
                    )
                )
            }
        }
        return this
    }
}
