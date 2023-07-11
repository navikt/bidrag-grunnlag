package no.nav.bidrag.grunnlag.model

import no.nav.bidrag.domain.enums.GrunnlagRequestType
import no.nav.bidrag.domain.enums.GrunnlagsRequestStatus
import no.nav.bidrag.domain.enums.SkattegrunnlagType
import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.bo.SkattegrunnlagBo
import no.nav.bidrag.grunnlag.bo.SkattegrunnlagspostBo
import no.nav.bidrag.grunnlag.comparator.PeriodComparable
import no.nav.bidrag.grunnlag.consumer.skattegrunnlag.SigrunConsumer
import no.nav.bidrag.grunnlag.consumer.skattegrunnlag.api.HentSummertSkattegrunnlagRequest
import no.nav.bidrag.grunnlag.consumer.skattegrunnlag.api.Skattegrunnlag
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.service.PersistenceService
import no.nav.bidrag.grunnlag.service.PersonIdOgPeriodeRequest
import no.nav.bidrag.transport.behandling.grunnlag.reponse.OppdaterGrunnlagDto
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

class OppdaterSkattegrunnlag(
    private val grunnlagspakkeId: Int,
    private val timestampOppdatering: LocalDateTime,
    private val persistenceService: PersistenceService,
    private val sigrunConsumer: SigrunConsumer
) : MutableList<OppdaterGrunnlagDto> by mutableListOf() {

    companion object {
        @JvmStatic
        private val LOGGER: Logger = LoggerFactory.getLogger(OppdaterSkattegrunnlag::class.java)
    }

    fun oppdaterSkattegrunnlag(skattegrunnlagRequestListe: List<PersonIdOgPeriodeRequest>): OppdaterSkattegrunnlag {
        skattegrunnlagRequestListe.forEach { personIdOgPeriode ->

            var inntektAar = personIdOgPeriode.periodeFra.year
            val sluttAar = personIdOgPeriode.periodeTil.year

            val periodeFra = LocalDate.of(inntektAar, 1, 1)
            val periodeTil = LocalDate.of(sluttAar, 1, 1)

            val nyeSkattegrunnlag =
                mutableListOf<PeriodComparable<SkattegrunnlagBo, SkattegrunnlagspostBo>>()

            while (inntektAar < sluttAar) {
                val skattegrunnlagRequest = HentSummertSkattegrunnlagRequest(
                    inntektAar.toString(),
                    "SummertSkattegrunnlagBidrag",
                    personIdOgPeriode.personId
                )

                LOGGER.info("Kaller Sigrun (skattegrunnlag)")
                SECURE_LOGGER.info("Kaller Sigrun (skattegrunnlag) med request: $skattegrunnlagRequest")

                when (
                    val restResponseSkattegrunnlag =
                        sigrunConsumer.hentSummertSkattegrunnlag(skattegrunnlagRequest)
                ) {
                    is RestResponse.Success -> {
                        var antallSkattegrunnlagsposter = 0
                        val skattegrunnlagResponse = restResponseSkattegrunnlag.body
                        SECURE_LOGGER.info("Sigrun (skattegrunnlag) ga følgende respons: $skattegrunnlagResponse")

                        val skattegrunnlagsPosterOrdinaer = mutableListOf<Skattegrunnlag>()
                        val skattegrunnlagsPosterSvalbard = mutableListOf<Skattegrunnlag>()
                        skattegrunnlagsPosterOrdinaer.addAll(skattegrunnlagResponse.grunnlag!!.toMutableList())
                        skattegrunnlagsPosterSvalbard.addAll(skattegrunnlagResponse.svalbardGrunnlag!!.toMutableList())

                        if (skattegrunnlagsPosterOrdinaer.size > 0 || skattegrunnlagsPosterSvalbard.size > 0) {
                            val skattegrunnlag = SkattegrunnlagBo(
                                grunnlagspakkeId = grunnlagspakkeId,
                                personId = personIdOgPeriode.personId,
                                periodeFra = LocalDate.parse("$inntektAar-01-01"),
                                // justerer frem tildato med én dag for å ha lik logikk som resten av appen. Tildato skal angis som til, men ikke inkludert, dato.
                                periodeTil = LocalDate.parse("$inntektAar-01-01").plusYears(1),
                                brukFra = timestampOppdatering,
                                hentetTidspunkt = timestampOppdatering
                            )
                            val skattegrunnlagsposter = mutableListOf<SkattegrunnlagspostBo>()
                            skattegrunnlagsPosterOrdinaer.forEach { skattegrunnlagsPost ->
                                antallSkattegrunnlagsposter++
                                skattegrunnlagsposter.add(
                                    SkattegrunnlagspostBo(
                                        skattegrunnlagType = SkattegrunnlagType.ORDINAER.toString(),
                                        inntektType = skattegrunnlagsPost.tekniskNavn,
                                        belop = BigDecimal(skattegrunnlagsPost.beloep)
                                    )
                                )
                            }
                            skattegrunnlagsPosterSvalbard.forEach { skattegrunnlagsPost ->
                                antallSkattegrunnlagsposter++
                                skattegrunnlagsposter.add(
                                    SkattegrunnlagspostBo(
                                        skattegrunnlagType = SkattegrunnlagType.SVALBARD.toString(),
                                        inntektType = skattegrunnlagsPost.tekniskNavn,
                                        belop = BigDecimal(skattegrunnlagsPost.beloep)
                                    )
                                )
                            }
                            nyeSkattegrunnlag.add(PeriodComparable(skattegrunnlag, skattegrunnlagsposter))
                        }
                        persistenceService.oppdaterSkattegrunnlagForGrunnlagspakke(
                            grunnlagspakkeId,
                            nyeSkattegrunnlag,
                            periodeFra,
                            periodeTil,
                            personIdOgPeriode.personId,
                            timestampOppdatering
                        )
                        this.add(
                            OppdaterGrunnlagDto(
                                GrunnlagRequestType.SKATTEGRUNNLAG,
                                personIdOgPeriode.personId,
                                GrunnlagsRequestStatus.HENTET,
                                "Antall skattegrunnlagsposter funnet for inntektsåret $inntektAar: $antallSkattegrunnlagsposter"
                            )
                        )
                    }

                    is RestResponse.Failure -> this.add(
                        OppdaterGrunnlagDto(
                            GrunnlagRequestType.SKATTEGRUNNLAG,
                            personIdOgPeriode.personId,
                            if (restResponseSkattegrunnlag.statusCode == HttpStatus.NOT_FOUND) GrunnlagsRequestStatus.IKKE_FUNNET else GrunnlagsRequestStatus.FEILET,
                            "Feil ved henting av skattegrunnlag for inntektsåret $inntektAar."
                        )
                    )
                }
                inntektAar++
            }
        }
        return this
    }
}
