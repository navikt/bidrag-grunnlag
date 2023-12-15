package no.nav.bidrag.grunnlag.model

import no.nav.bidrag.domene.enums.grunnlag.GrunnlagRequestStatus
import no.nav.bidrag.domene.enums.grunnlag.GrunnlagRequestType
import no.nav.bidrag.domene.enums.person.Familierelasjon
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.bo.PersonBo
import no.nav.bidrag.grunnlag.bo.RelatertPersonBo
import no.nav.bidrag.grunnlag.consumer.bidragperson.BidragPersonConsumer
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.service.PersistenceService
import no.nav.bidrag.grunnlag.service.PersonIdOgPeriodeRequest
import no.nav.bidrag.transport.behandling.grunnlag.response.OppdaterGrunnlagDto
import no.nav.bidrag.transport.person.NavnFødselDødDto
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

class OppdaterRelatertePersoner(
    private val grunnlagspakkeId: Int,
    private val timestampOppdatering: LocalDateTime,
    private val persistenceService: PersistenceService,
    private val bidragPersonConsumer: BidragPersonConsumer,

) : MutableList<OppdaterGrunnlagDto> by mutableListOf() {

    companion object {
        @JvmStatic
        private val LOGGER: Logger = LoggerFactory.getLogger(OppdaterRelatertePersoner::class.java)
    }

    // Henter og lagrer først husstandsmedlemmer for så å hente forelder-barn-relasjoner.
    // Også barn som ikke bor i samme husstand som BM/BP skal være med i grunnlaget og lagres med null i husstandsmedlemPeriodeFra
    // og husstandsmedlemPeriodeTil.
    fun oppdaterRelatertePersoner(
        relatertePersonerRequestListe: List<PersonIdOgPeriodeRequest>,
        historiskeIdenterMap: Map<String, List<String>>,
    ): OppdaterRelatertePersoner {
        relatertePersonerRequestListe.forEach { personIdOgPeriode ->

            // Sett eksisterende forekomster av RelatertPerson til inaktiv
            persistenceService.oppdaterEksisterendeRelatertPersonTilInaktiv(
                grunnlagspakkeId = grunnlagspakkeId,
                personIdListe = historiskeIdenterMap[personIdOgPeriode.personId] ?: listOf(personIdOgPeriode.personId),
                timestampOppdatering = timestampOppdatering,
            )

            // henter alle husstandsmedlemmer til BM/BP
            val husstandsmedlemmerListe = hentHusstandsmedlemmer(personIdOgPeriode.personId)

            // henter alle barn av BM/BP
            val barnListe = hentBarn(Personident(personIdOgPeriode.personId))

            // Alle husstandsmedlemmer lagres i tabell relatert_person. Det sjekkes om husstandsmedlem finnes i liste over barn.
            // erBarnAvmBp settes lik true i så fall. Tester slik at person ikke lagres som eget husstandsmedlem.
            husstandsmedlemmerListe.forEach { husstandsmedlem ->
                if (husstandsmedlem.personId != personIdOgPeriode.personId) {
                    persistenceService.opprettRelatertPerson(
                        RelatertPersonBo(
                            grunnlagspakkeId = grunnlagspakkeId,
                            partPersonId = personIdOgPeriode.personId,
                            relatertPersonPersonId = husstandsmedlem.personId,
                            navn = husstandsmedlem.navn,
                            fodselsdato = husstandsmedlem.fodselsdato,
                            erBarnAvBmBp = barnListe.any { it.personId == husstandsmedlem.personId },
                            husstandsmedlemPeriodeFra = husstandsmedlem.husstandsmedlemPeriodeFra,
                            husstandsmedlemPeriodeTil = husstandsmedlem.husstandsmedlemPeriodeTil,
                            aktiv = true,
                            brukFra = timestampOppdatering,
                            brukTil = null,
                            hentetTidspunkt = timestampOppdatering,
                        ),
                    )
                }
            }

            // Filtrer listen over BM/BPs barn slik at barn som ligger i listen over husstandsmedlemmer,
            // og som derfor allerede er lagret, fjernes og lagrer deretter de gjenværende i tabell relatert_person.
            val filtrertBarnListe = barnListe.filter { barn -> husstandsmedlemmerListe.any { it.personId != barn.personId } }

            filtrertBarnListe.forEach { barn ->
                persistenceService.opprettRelatertPerson(
                    RelatertPersonBo(
                        grunnlagspakkeId = grunnlagspakkeId,
                        partPersonId = personIdOgPeriode.personId,
                        relatertPersonPersonId = barn.personId,
                        navn = barn.navn,
                        fodselsdato = barn.fodselsdato,
                        erBarnAvBmBp = barnListe.any { it.personId == barn.personId },
                        husstandsmedlemPeriodeFra = null,
                        husstandsmedlemPeriodeTil = null,
                        aktiv = true,
                        brukFra = timestampOppdatering,
                        brukTil = null,
                        hentetTidspunkt = timestampOppdatering,
                    ),
                )
            }
        }
        return this
    }

    private fun hentHusstandsmedlemmer(husstandsmedlemmerRequest: String): List<PersonBo> {
        LOGGER.info("Kaller bidrag-person Husstandsmedlemmer")
        SECURE_LOGGER.info("Kaller bidrag-person Husstandsmedlemmer med request: $husstandsmedlemmerRequest")

        val husstandsmedlemListe = mutableListOf<PersonBo>()

        when (
            val restResponseHusstandsmedlemmer =
                bidragPersonConsumer.hentHusstandsmedlemmer(Personident(husstandsmedlemmerRequest))
        ) {
            is RestResponse.Success -> {
                val husstandsmedlemmerResponseDto = restResponseHusstandsmedlemmer.body
                SECURE_LOGGER.info(
                    "Bidrag-person ga følgende respons på Husstandsmedlemmer for grunnlag EgneBarnIHusstanden: $husstandsmedlemmerResponseDto",
                )

                if (husstandsmedlemmerResponseDto.husstandListe.isNotEmpty()) {
                    husstandsmedlemmerResponseDto.husstandListe.forEach { husstand ->
                        husstand.husstandsmedlemListe.forEach { husstandsmedlem ->
                            husstandsmedlemListe.add(
                                PersonBo(
                                    husstandsmedlem.personId.verdi,
                                    husstandsmedlem.navn,
                                    husstandsmedlem.fødselsdato,
                                    husstandsmedlem.gyldigFraOgMed,
                                    husstandsmedlem.gyldigTilOgMed,
                                ),
                            )
                        }
                    }
                }
                this.add(
                    OppdaterGrunnlagDto(
                        GrunnlagRequestType.HUSSTANDSMEDLEMMER_OG_EGNE_BARN,
                        husstandsmedlemmerRequest,
                        GrunnlagRequestStatus.HENTET,
                        "Antall husstandsmedlemmer funnet: ${husstandsmedlemListe.size}",
                    ),
                )
                return slåSammenSammenhengendePerioder(husstandsmedlemListe.sortedWith(compareBy({ it.personId }, { it.husstandsmedlemPeriodeFra })))
            }

            is RestResponse.Failure -> this.add(
                OppdaterGrunnlagDto(
                    GrunnlagRequestType.HUSSTANDSMEDLEMMER_OG_EGNE_BARN,
                    husstandsmedlemmerRequest,
                    if (restResponseHusstandsmedlemmer.statusCode == HttpStatus.NOT_FOUND) {
                        GrunnlagRequestStatus.IKKE_FUNNET
                    } else {
                        GrunnlagRequestStatus.FEILET
                    },
                    "Feil ved henting av husstandsmedlemmer og egne barn for: $husstandsmedlemmerRequest.",
                ),
            )
        }
        return emptyList()
    }

    private fun hentBarn(forelderBarnRequest: Personident): List<PersonBo> {
        LOGGER.info("Kaller bidrag-person Forelder-barn-relasjon")
        SECURE_LOGGER.info("Kaller bidrag-person Forelder-barn-relasjon med request: $forelderBarnRequest")

        val barnListe = mutableListOf<PersonBo>()

        // Henter en liste over BMs/BPs barn og henter så info om fødselsdag og navn for disse
        when (
            val restResponseForelderBarnRelasjon =
                bidragPersonConsumer.hentForelderBarnRelasjon(forelderBarnRequest)
        ) {
            is RestResponse.Success -> {
                val forelderBarnRelasjonResponse = restResponseForelderBarnRelasjon.body

                if (forelderBarnRelasjonResponse.forelderBarnRelasjon.isNotEmpty()) {
                    SECURE_LOGGER.info("Bidrag-person ga følgende respons på forelder-barn-relasjoner: $forelderBarnRelasjonResponse")

                    forelderBarnRelasjonResponse.forelderBarnRelasjon.forEach { forelderBarnRelasjon ->
                        // Kaller bidrag-person for å hente info om fødselsdato og navn
                        if (forelderBarnRelasjon.relatertPersonsRolle == Familierelasjon.BARN && forelderBarnRelasjon.relatertPersonsIdent != null) {
                            val navnFoedselDoedResponseDto = hentNavnFoedselDoed(Personident(forelderBarnRelasjon.relatertPersonsIdent!!.verdi))
                            // Lager en liste over fnr for alle barn som er funnet
                            barnListe.add(
                                PersonBo(
                                    forelderBarnRelasjon.relatertPersonsIdent?.verdi,
                                    navnFoedselDoedResponseDto?.navn,
                                    navnFoedselDoedResponseDto?.fødselsdato,
                                    null,
                                    null,
                                ),
                            )
                        }
                    }
                    return barnListe
                }
            }

            is RestResponse.Failure -> this.add(
                OppdaterGrunnlagDto(
                    GrunnlagRequestType.HUSSTANDSMEDLEMMER_OG_EGNE_BARN,
                    forelderBarnRequest.verdi,
                    if (restResponseForelderBarnRelasjon.statusCode == HttpStatus.NOT_FOUND) {
                        GrunnlagRequestStatus.IKKE_FUNNET
                    } else {
                        GrunnlagRequestStatus.FEILET
                    },
                    "Feil ved henting av egne barn i husstanden for: ${forelderBarnRequest.verdi} .",
                ),
            )
        }
        return emptyList()
    }

    private fun hentNavnFoedselDoed(personident: Personident): NavnFødselDødDto? {
        // hent navn, fødselsdato og eventuell dødsdato for personer fra bidrag-person
        LOGGER.info("Kaller bidrag-person hent navn og fødselsdato")
        SECURE_LOGGER.info("Kaller bidrag-person hent navn og fødselsdato for : $personident")
        when (
            val restResponseFoedselOgDoed =
                bidragPersonConsumer.hentNavnFoedselOgDoed(personident)
        ) {
            is RestResponse.Success -> {
                val foedselOgDoedResponse = restResponseFoedselOgDoed.body
                SECURE_LOGGER.info("Bidrag-person ga følgende respons på hent navn og fødselsdato: $foedselOgDoedResponse")

                return NavnFødselDødDto(
                    foedselOgDoedResponse.navn,
                    foedselOgDoedResponse.fødselsdato,
                    foedselOgDoedResponse.fødselsår,
                    foedselOgDoedResponse.dødsdato,
                )
            }

            is RestResponse.Failure ->
                return null
        }
    }

    // Metode for å slå sammen sammenhengende perioder en person har vært husstandsmedlem. Responsen fra bidrag-person er gruppert per husstand,
    // det kan derfor være flere perioder for samme person. Metoden slår sammen perioder som er sammenhengende.
    private fun slåSammenSammenhengendePerioder(husstandsmedlemListe: List<PersonBo>): List<PersonBo> {
        if (husstandsmedlemListe.size < 2) {
            return husstandsmedlemListe
        }

        val resultatListe = mutableListOf<PersonBo>()
        var gjeldendeForekomst = husstandsmedlemListe.first()

        for (i in 1 until husstandsmedlemListe.size) {
            val nesteForekomst = husstandsmedlemListe[i]

            gjeldendeForekomst = if (gjeldendeForekomst.personId == nesteForekomst.personId &&
                gjeldendeForekomst.husstandsmedlemPeriodeTil == nesteForekomst.husstandsmedlemPeriodeFra
            ) {
                // Slår sammen periodene
                gjeldendeForekomst.copy(husstandsmedlemPeriodeTil = nesteForekomst.husstandsmedlemPeriodeTil)
            } else {
                // Det finnes ikke flere sammenhengende forekomster for person i listen, legg til gjeldende forekomst i resultatlisten
                resultatListe.add(gjeldendeForekomst)
                nesteForekomst
            }
        }

        // Legg til det siste objektet i resultatlisten
        resultatListe.add(gjeldendeForekomst)

        return resultatListe
    }
}
