package no.nav.bidrag.grunnlag.model

import no.nav.bidrag.behandling.felles.dto.grunnlag.OppdaterGrunnlagDto
import no.nav.bidrag.behandling.felles.enums.GrunnlagRequestType
import no.nav.bidrag.behandling.felles.enums.GrunnlagsRequestStatus
import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.bo.KontantstotteBo
import no.nav.bidrag.grunnlag.consumer.familiekssak.FamilieKsSakConsumer
import no.nav.bidrag.grunnlag.consumer.familiekssak.api.BisysDto
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.service.PersistenceService
import no.nav.bidrag.grunnlag.service.PersonIdOgPeriodeRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

class OppdaterKontantstotte(
    private val grunnlagspakkeId: Int,
    private val timestampOppdatering: LocalDateTime,
    private val persistenceService: PersistenceService,
    private val familieKsSakConsumer: FamilieKsSakConsumer
) : MutableList<OppdaterGrunnlagDto> by mutableListOf() {

    companion object {
        @JvmStatic
        private val LOGGER: Logger = LoggerFactory.getLogger(OppdaterBarnetillegg::class.java)
    }

    fun oppdaterKontantstotte(kontantstotteRequestListe: List<PersonIdOgPeriodeRequest>): OppdaterKontantstotte {
        kontantstotteRequestListe.forEach { personIdOgPeriode ->
            var antallPerioderFunnet = 0

            // Input til tjeneste er en liste over alle personnr for en person,
            // kall PDL for å hente historikk på fnr?
            val innsynRequest = BisysDto(
                personIdOgPeriode.periodeFra,
                listOf(personIdOgPeriode.personId)
            )

            LOGGER.info("Kaller kontantstøtte")
            SECURE_LOGGER.info("Kaller kontantstøtte med request: $innsynRequest")

            when (
                val restResponseKontantstotte =
                    familieKsSakConsumer.hentKontantstotte(innsynRequest)
            ) {
                is RestResponse.Success -> {
                    val kontantstotteResponse = restResponseKontantstotte.body
                    SECURE_LOGGER.info("kontantstøtte ga følgende respons: $kontantstotteResponse")

                    persistenceService.oppdaterEksisterendeKontantstotteTilInaktiv(
                        grunnlagspakkeId,
                        personIdOgPeriode.personId,
                        timestampOppdatering
                    )

                    // Kontantstøtte fra Infotrygd
                    kontantstotteResponse.infotrygdPerioder.forEach { ks ->
                        val belopPerParn = ks.beløp.div(ks.barna.size.toInt())
                        ks.barna.forEach { barnPersonId ->
                            antallPerioderFunnet++
                            persistenceService.opprettKontantstotte(
                                KontantstotteBo(
                                    grunnlagspakkeId = grunnlagspakkeId,
                                    partPersonId = personIdOgPeriode.personId,
                                    barnPersonId = barnPersonId,
                                    periodeFra = LocalDate.parse(ks.fomMåned.toString() + "-01"),
                                    // justerer frem tildato med én dag for å ha lik logikk som resten av appen. Tildato skal angis som til, men ikke inkludert, dato.
                                    periodeTil = if (ks.tomMåned != null) LocalDate.parse(ks.tomMåned.toString() + "-01").plusMonths(1) else null,
                                    aktiv = true,
                                    brukFra = timestampOppdatering,
                                    belop = belopPerParn,
                                    brukTil = null,
                                    hentetTidspunkt = timestampOppdatering
                                )
                            )
                        }
                    }

                    // Kontantstøtte fra ks-sak
                    kontantstotteResponse.ksSakPerioder.forEach { ks ->
                        if (ks.fomMåned.isBefore(YearMonth.of(personIdOgPeriode.periodeTil.year, personIdOgPeriode.periodeTil.month))) {
                            antallPerioderFunnet++
                            persistenceService.opprettKontantstotte(
                                KontantstotteBo(
                                    grunnlagspakkeId = grunnlagspakkeId,
                                    partPersonId = personIdOgPeriode.personId,
                                    barnPersonId = ks.barn.ident,
                                    periodeFra = LocalDate.parse(ks.fomMåned.toString() + "-01"),
                                    // justerer frem tildato med én dag for å ha lik logikk som resten av appen. Tildato skal angis som til, men ikke inkludert, dato.
                                    periodeTil = if (ks.tomMåned != null) LocalDate.parse(ks.tomMåned.toString() + "-01").plusMonths(1) else null,
                                    aktiv = true,
                                    brukFra = timestampOppdatering,
                                    belop = ks.barn.beløp,
                                    brukTil = null,
                                    hentetTidspunkt = timestampOppdatering
                                )
                            )
                        }
                    }
                    this.add(
                        OppdaterGrunnlagDto(
                            GrunnlagRequestType.KONTANTSTOTTE,
                            personIdOgPeriode.personId,
                            GrunnlagsRequestStatus.HENTET,
                            "Antall perioder funnet: $antallPerioderFunnet"
                        )
                    )
                }

                is RestResponse.Failure -> {
                    this.add(
                        OppdaterGrunnlagDto(
                            GrunnlagRequestType.KONTANTSTOTTE,
                            personIdOgPeriode.personId,
                            if (restResponseKontantstotte.statusCode == HttpStatus.NOT_FOUND) GrunnlagsRequestStatus.IKKE_FUNNET else GrunnlagsRequestStatus.FEILET,
                            "Feil ved henting av kontantstøtte for perioden: ${personIdOgPeriode.periodeFra} - ${personIdOgPeriode.periodeTil}."
                        )
                    )
                    SECURE_LOGGER.info("kontantstøtte familie-ks-sak svarer med feil, respons: $restResponseKontantstotte")
                }
            }
        }
        return this
    }
}
