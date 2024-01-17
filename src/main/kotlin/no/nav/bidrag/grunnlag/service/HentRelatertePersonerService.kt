package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.domene.enums.person.Familierelasjon
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.bo.PersonBo
import no.nav.bidrag.grunnlag.consumer.bidragperson.BidragPersonConsumer
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.transport.behandling.grunnlag.response.BorISammeHusstandDto
import no.nav.bidrag.transport.behandling.grunnlag.response.RelatertPersonGrunnlagDto
import no.nav.bidrag.transport.person.NavnFødselDødDto
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate

// @Service
class HentRelatertePersonerService(
    private val bidragPersonConsumer: BidragPersonConsumer,
) : List<RelatertPersonGrunnlagDto> by listOf() {

    companion object {
        @JvmStatic
        val LOGGER: Logger = LoggerFactory.getLogger(HentRelatertePersonerService::class.java)
    }

    fun hentRelatertePersoner(relatertPersonRequestListe: List<PersonIdOgPeriodeRequest>): List<RelatertPersonGrunnlagDto> {
        LOGGER.info("Start HUSSTANDSMEDLEMMER_OG_EGNE_BARN")

        val relatertPersonListe = mutableListOf<RelatertPersonGrunnlagDto>()
        val relatertPersonInternListe = mutableListOf<RelatertPersonIntern>()

        relatertPersonRequestListe.forEach { personIdOgPeriode ->
            // Henter alle husstandsmedlemmer til BM/BP
            val husstandsmedlemmerListe = hentHusstandsmedlemmer(personIdOgPeriode.personId)

            // Henter alle barn av BM/BP
            val barnListe = hentBarn(Personident(personIdOgPeriode.personId))

            // Slår sammen listene over husstandsmedlemmer og barn. Innsendt personId lagres ikke som eget husstandsmedlem.
            // Hvis personen ligger i barnListe settes erBarnAvBmBp lik true.
            (husstandsmedlemmerListe + barnListe)
                .filterNot { it.personId == personIdOgPeriode.personId }
                .forEach { person ->
                    relatertPersonInternListe.add(
                        RelatertPersonIntern(
                            partPersonId = personIdOgPeriode.personId,
                            relatertPersonPersonId = person.personId,
                            navn = person.navn,
                            fødselsdato = person.fodselsdato,
                            erBarnAvBmBp = barnListe.any { it.personId == person.personId },
                            husstandsmedlemPeriodeFra =
                            if (husstandsmedlemmerListe.any { it.personId == person.personId }) {
                                person.husstandsmedlemPeriodeFra
                            } else {
                                null
                            },
                            husstandsmedlemPeriodeTil =
                            if (husstandsmedlemmerListe.any { it.personId == person.personId }) {
                                person.husstandsmedlemPeriodeTil
                            } else {
                                null
                            },
                        ),
                    )
                }

            // relatertPersonInternListe inneholder alle personer (partPersonId) grunnlaget husstandsmedlemmer er innhentet for.
            // For å unngå duplikate perioder i responsen må det sjekkes at periode gjelder for aktuell partPersonId.
            // En relatert person kan forekomme flere ganger i listen, én gang for hver periode personen har delt bolig med BM/BP (partPersonId).
            // I responsen skal hver person kun ligge én gang, med en liste over perioder personen har delt bolig med BM/BP (partPersonId).
            // Sjekker derfor om personen allerede ligger i responsen.
            relatertPersonInternListe
                .groupBy { it.partPersonId to it.relatertPersonPersonId }
                .values
                .forEach { group ->
                    val borISammeHusstandListe = group
                        .filter { it.husstandsmedlemPeriodeFra != null || it.husstandsmedlemPeriodeTil != null }
                        .map { BorISammeHusstandDto(it.husstandsmedlemPeriodeFra, it.husstandsmedlemPeriodeTil) }

                    val firstPerson = group.first()
                    relatertPersonListe.add(
                        RelatertPersonGrunnlagDto(
                            partPersonId = firstPerson.partPersonId,
                            relatertPersonPersonId = firstPerson.relatertPersonPersonId,
                            navn = firstPerson.navn,
                            fødselsdato = firstPerson.fødselsdato,
                            erBarnAvBmBp = firstPerson.erBarnAvBmBp,
                            borISammeHusstandDtoListe = borISammeHusstandListe,
                        ),
                    )
                }
        }

        LOGGER.info("Slutt HUSSTANDSMEDLEMMER_OG_EGNE_BARN")
        return relatertPersonListe
    }

    // Henter alle husstandsmedlemmer til BM/BP
    private fun hentHusstandsmedlemmer(ident: String): List<PersonBo> {
        val husstandsmedlemListe = mutableListOf<PersonBo>()

        when (
            val restResponseHusstandsmedlemmer = bidragPersonConsumer.hentHusstandsmedlemmer(Personident(ident))
        ) {
            is RestResponse.Success -> {
                val husstandsmedlemmerResponseDto = restResponseHusstandsmedlemmer.body
                SECURE_LOGGER.info("Bidrag-person ga følgende respons på husstandsmedlemmer for $ident: $husstandsmedlemmerResponseDto")

                husstandsmedlemmerResponseDto.husstandListe.forEach { husstand ->
                    husstand.husstandsmedlemListe.forEach {
                        husstandsmedlemListe.add(
                            PersonBo(
                                personId = it.personId.verdi,
                                navn = it.navn,
                                fodselsdato = it.fødselsdato,
                                husstandsmedlemPeriodeFra = it.gyldigFraOgMed,
                                husstandsmedlemPeriodeTil = it.gyldigTilOgMed,
                            ),
                        )
                    }
                }
            }

            is RestResponse.Failure -> {
                SECURE_LOGGER.warn("Feil ved henting av husstandsmedlemmer for $ident")
                return emptyList()
            }
        }
        return slåSammenSammenhengendePerioder(husstandsmedlemListe.sortedWith(compareBy({ it.personId }, { it.husstandsmedlemPeriodeFra })))
    }

    // Henter alle barn av BM/BP
    private fun hentBarn(personident: Personident): List<PersonBo> {
        val barnListe = mutableListOf<PersonBo>()

        // Henter en liste over BMs/BPs barn og henter så info om fødselsdag og navn for disse
        when (
            val restResponseForelderBarnRelasjon = bidragPersonConsumer.hentForelderBarnRelasjon(personident)
        ) {
            is RestResponse.Success -> {
                val forelderBarnRelasjonResponse = restResponseForelderBarnRelasjon.body
                SECURE_LOGGER.info(
                    "Bidrag-person ga følgende respons på forelder-barn-relasjoner for ${personident.verdi}: $forelderBarnRelasjonResponse",
                )

                forelderBarnRelasjonResponse.forelderBarnRelasjon.forEach {
                    // Kaller bidrag-person for å hente info om fødselsdato og navn
                    if (it.relatertPersonsRolle == Familierelasjon.BARN && it.relatertPersonsIdent != null) {
                        val navnFoedselDoedResponseDto = hentNavnFoedselDoed(Personident(it.relatertPersonsIdent!!.verdi))
                        // Lager en liste over fnr for alle barn som er funnet
                        barnListe.add(
                            PersonBo(
                                personId = it.relatertPersonsIdent?.verdi,
                                navn = navnFoedselDoedResponseDto?.navn,
                                fodselsdato = navnFoedselDoedResponseDto?.fødselsdato,
                                husstandsmedlemPeriodeFra = null,
                                husstandsmedlemPeriodeTil = null,
                            ),
                        )
                    }
                }
            }

            is RestResponse.Failure -> {
                SECURE_LOGGER.warn("Feil ved henting av forelder-barn-relasjoner for ${personident.verdi}")
                return emptyList()
            }
        }
        return barnListe
    }

    // Henter navn, fødselsdato og eventuell dødsdato for personer fra bidrag-person
    private fun hentNavnFoedselDoed(personident: Personident): NavnFødselDødDto? {
        val navnFødselDødDto: NavnFødselDødDto

        when (
            val restResponseFoedselOgDoed = bidragPersonConsumer.hentNavnFoedselOgDoed(personident)
        ) {
            is RestResponse.Success -> {
                val foedselOgDoedResponse = restResponseFoedselOgDoed.body
                SECURE_LOGGER.info("Bidrag-person ga følgende respons på hent navn og fødselsdato for ${personident.verdi}: $foedselOgDoedResponse")

                navnFødselDødDto = NavnFødselDødDto(
                    foedselOgDoedResponse.navn,
                    foedselOgDoedResponse.fødselsdato,
                    foedselOgDoedResponse.fødselsår,
                    foedselOgDoedResponse.dødsdato,
                )
            }

            is RestResponse.Failure -> {
                SECURE_LOGGER.warn("Feil ved henting av navn og fødselsdato for ${personident.verdi}")
                return null
            }
        }
        return navnFødselDødDto
    }

    // Metode for å slå sammen sammenhengende perioder en person har vært husstandsmedlem. Responsen fra bidrag-person er gruppert per husstand.
    // Det kan derfor være flere perioder for samme person. Metoden slår sammen perioder som er sammenhengende.
    private fun slåSammenSammenhengendePerioder(husstandsmedlemListe: List<PersonBo>): List<PersonBo> {
        if (husstandsmedlemListe.size < 2) {
            return husstandsmedlemListe
        }

        val resultatListe = mutableListOf<PersonBo>()
        var gjeldendeForekomst = husstandsmedlemListe.first()

        (1 until husstandsmedlemListe.size).forEach { i ->
            val nesteForekomst = husstandsmedlemListe[i]

            gjeldendeForekomst =
                if (gjeldendeForekomst.personId == nesteForekomst.personId &&
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

    // Intern dataklasse brukt for å simulere funksjonalitet fra oppdater- og hent-grunnlagspakke-tjenestene
    data class RelatertPersonIntern(
        val partPersonId: String?,
        val relatertPersonPersonId: String?,
        val navn: String?,
        val fødselsdato: LocalDate?,
        val erBarnAvBmBp: Boolean,
        val husstandsmedlemPeriodeFra: LocalDate?,
        val husstandsmedlemPeriodeTil: LocalDate?,
    )
}
