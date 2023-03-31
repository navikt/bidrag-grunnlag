package no.nav.bidrag.grunnlag.model

import no.nav.bidrag.behandling.felles.dto.grunnlag.OppdaterGrunnlagDto
import no.nav.bidrag.behandling.felles.enums.GrunnlagRequestType
import no.nav.bidrag.behandling.felles.enums.GrunnlagsRequestStatus
import no.nav.bidrag.domain.ident.PersonIdent
import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.bo.SivilstandBo
import no.nav.bidrag.grunnlag.consumer.bidragperson.BidragPersonConsumer
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.service.PersistenceService
import no.nav.bidrag.grunnlag.service.PersonIdOgPeriodeRequest
import no.nav.bidrag.transport.person.Sivilstand
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

class OppdaterSivilstand(
    private val grunnlagspakkeId: Int,
    private val timestampOppdatering: LocalDateTime,
    private val persistenceService: PersistenceService,
    private val bidragPersonConsumer: BidragPersonConsumer

) : MutableList<OppdaterGrunnlagDto> by mutableListOf() {

    companion object {
        @JvmStatic
        private val LOGGER: Logger = LoggerFactory.getLogger(OppdaterSivilstand::class.java)
    }

    fun oppdaterSivilstand(sivilstandRequestListe: List<PersonIdOgPeriodeRequest>): OppdaterSivilstand {
        sivilstandRequestListe.forEach { personIdOgPeriode ->

            var antallPerioderFunnet = 0

            LOGGER.info("Kaller bidrag-person og henter sivilstand")
            SECURE_LOGGER.info("Kaller bidrag-person og henter sivilstand for: $personIdOgPeriode.personId")

            when (
                val restResponseSivilstand =
                    bidragPersonConsumer.hentSivilstand(PersonIdent(personIdOgPeriode.personId))
            ) {
                is RestResponse.Success -> {
                    val sivilstandResponse = restResponseSivilstand.body
                    SECURE_LOGGER.info("Kall til bidrag-person for å hente sivilstand ga følgende respons: $sivilstandResponse")

                    if (sivilstandResponse.sivilstand.isNotEmpty()) {
                        persistenceService.oppdaterEksisterendeSivilstandTilInaktiv(
                            grunnlagspakkeId,
                            personIdOgPeriode.personId,
                            timestampOppdatering
                        )
                        sivilstandResponse.sivilstand.forEach { sivilstand ->
                            // Pga vekslende datakvalitet fra PDL må det taes høyde for at begge disse datoene kan være null.
                            // Hvis de er det så kan ikke periodekontroll gjøres og sivilstanden må lagres uten fra-dato
                            val dato = sivilstand.gyldigFraOgMed ?: sivilstand.bekreftelsesdato
                            if ((dato != null && dato.verdi.isBefore(personIdOgPeriode.periodeTil)) || (dato == null)) {
                                antallPerioderFunnet++
                                lagreSivilstand(
                                    sivilstand,
                                    grunnlagspakkeId,
                                    timestampOppdatering,
                                    personIdOgPeriode.personId
                                )
                            }
                        }
                    }
                    this.add(
                        OppdaterGrunnlagDto(
                            GrunnlagRequestType.SIVILSTAND,
                            personIdOgPeriode.personId,
                            GrunnlagsRequestStatus.HENTET,
                            "Antall perioder funnet: $antallPerioderFunnet"
                        )
                    )
                }
                is RestResponse.Failure -> this.add(
                    OppdaterGrunnlagDto(
                        GrunnlagRequestType.SIVILSTAND,
                        personIdOgPeriode.personId,
                        if (restResponseSivilstand.statusCode == HttpStatus.NOT_FOUND) GrunnlagsRequestStatus.IKKE_FUNNET else GrunnlagsRequestStatus.FEILET,
                        "Feil ved henting av sivilstand fra bidrag-person/PDL for perioden: ${personIdOgPeriode.periodeFra} - ${personIdOgPeriode.periodeTil}."
                    )
                )
            }
        }
        return this
    }

    fun lagreSivilstand(
        sivilstand: Sivilstand,
        grunnlagspakkeId: Int,
        timestampOppdatering: LocalDateTime,
        personId: String
    ) {
        persistenceService.opprettSivilstand(
            SivilstandBo(
                grunnlagspakkeId = grunnlagspakkeId,
                personId = personId,
                periodeFra = sivilstand.gyldigFraOgMed?.verdi ?: sivilstand.bekreftelsesdato?.verdi,
                // justerer frem tildato med én måned for å ha lik logikk som resten av appen. Tildato skal angis som til, men ikke inkludert, måned.
//        periodeTil = if (sivilstand.tom != null) si.tom.plusMonths(1)
//          .withDayOfMonth(1) else null,
                periodeTil = null,
                sivilstand = sivilstand.type.toString(),
                aktiv = true,
                brukFra = timestampOppdatering,
                brukTil = null,
                hentetTidspunkt = timestampOppdatering
            )
        )
    }
}
