package no.nav.bidrag.grunnlag.model

import no.nav.bidrag.behandling.felles.dto.grunnlag.OppdaterGrunnlagDto
import no.nav.bidrag.behandling.felles.enums.GrunnlagRequestType
import no.nav.bidrag.behandling.felles.enums.GrunnlagsRequestStatus
import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.bo.PersonBo
import no.nav.bidrag.grunnlag.bo.RelatertPersonBo
import no.nav.bidrag.grunnlag.consumer.bidragperson.BidragPersonConsumer
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.ForelderBarnRelasjonRolle
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.NavnFoedselDoedResponseDto
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.PersonRequest
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.service.PersistenceService
import no.nav.bidrag.grunnlag.service.PersonIdOgPeriodeRequest
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
  fun oppdaterRelatertePersoner(relatertePersonerRequestListe: List<PersonIdOgPeriodeRequest>): OppdaterRelatertePersoner {
    relatertePersonerRequestListe.forEach { personIdOgPeriode ->

      // Sett eksisterende forekomster av RelatertPerson til inaktiv
      persistenceService.oppdaterEksisterendeRelatertPersonTilInaktiv(
        grunnlagspakkeId,
        personIdOgPeriode.personId,
        timestampOppdatering
      )

      // henter alle husstandsmedlemmer til BM/BP
      val husstandsmedlemmerListe = hentHusstandsmedlemmer(personIdOgPeriode.personId)

      // henter alle barn av BM/BP
      val barnListe = hentBarn(PersonRequest(personIdOgPeriode.personId))

      // Alle husstandsmedlemmer lagres i tabell relatert_person. Det sjekkes om husstandsmedlem finnes i liste over barn.
      // erBarnAvmBp settes lik true i så fall
      husstandsmedlemmerListe.forEach { husstandsmedlem ->
        persistenceService.opprettRelatertPerson(
          RelatertPersonBo(
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
            hentetTidspunkt = timestampOppdatering
          )
        )
      }

      // Filtrer listen over BM/BPs barn slik at barn som ligger i listen over husstandsmedlemmer,
      // og som derfor allerede er lagret, fjernes og lagrer deretter de gjenværende i tabell relatert_person.
      val filtrertBarnListe = barnListe.filter { barn -> husstandsmedlemmerListe.any { it.personId != barn.personId } }

      filtrertBarnListe.forEach { barn ->
        persistenceService.opprettRelatertPerson(
          RelatertPersonBo(
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
            hentetTidspunkt = timestampOppdatering
          )
        )
      }
    }
    return this
  }


  private fun hentHusstandsmedlemmer(husstandsmedlemmerRequest: String): List<PersonBo> {

    LOGGER.info("Kaller bidrag-person Husstandsmedlemmer")
    SECURE_LOGGER.info("Kaller bidrag-person Husstandsmedlemmer med request: $husstandsmedlemmerRequest")

    val husstandsmedlemListe = mutableListOf<PersonBo>()

    when (val restResponseHusstandsmedlemmer =
      bidragPersonConsumer.hentHusstandsmedlemmer(PersonRequest(husstandsmedlemmerRequest))) {
      is RestResponse.Success -> {
        val husstandsmedlemmerResponseDto = restResponseHusstandsmedlemmer.body
        SECURE_LOGGER.info("Bidrag-person ga følgende respons på Husstandsmedlemmer for grunnlag EgneBarnIHusstanden: $husstandsmedlemmerResponseDto")

        if (!husstandsmedlemmerResponseDto.husstandListe.isNullOrEmpty()) {
          husstandsmedlemmerResponseDto.husstandListe.forEach { husstand ->
            husstand.husstandsmedlemmerListe.forEach { husstandsmedlem ->
              husstandsmedlemListe.add(
                PersonBo(husstandsmedlem.personId,
                  husstandsmedlem.fornavn + " " + husstandsmedlem.mellomnavn + " " + husstandsmedlem.etternavn,
                  husstandsmedlem.foedselsdato,
                  husstandsmedlem.gyldigFraOgMed,
                  husstandsmedlem.gyldigTilOgMed))
            }
          }
        }
        this.add(
          OppdaterGrunnlagDto(
            GrunnlagRequestType.HUSSTANDSMEDLEMMER,
            husstandsmedlemmerRequest,
            GrunnlagsRequestStatus.HENTET,
            "Antall husstandsmedlemmer funnet: ${husstandsmedlemListe.size}"
          )
        )
        return husstandsmedlemListe.sortedWith(compareBy({ it.personId }, { it.husstandsmedlemPeriodeFra } ))
      }

      is RestResponse.Failure -> this.add(
        OppdaterGrunnlagDto(
          GrunnlagRequestType.HUSSTANDSMEDLEMMER,
          husstandsmedlemmerRequest,
          if (restResponseHusstandsmedlemmer.statusCode == HttpStatus.NOT_FOUND) GrunnlagsRequestStatus.IKKE_FUNNET else GrunnlagsRequestStatus.FEILET,
          "Feil ved henting av husstandsmedlemmer for: ${husstandsmedlemmerRequest}."
        )
      )
    }
    return emptyList()
  }


  private fun hentBarn(forelderBarnRequest: PersonRequest): List<PersonBo> {

    LOGGER.info("Kaller bidrag-person Forelder-barn-relasjon")
    SECURE_LOGGER.info("Kaller bidrag-person Forelder-barn-relasjon med request: $forelderBarnRequest")

    val barnListe = mutableListOf<PersonBo>()

    // Henter en liste over BMs/BPs barn og henter så info om fødselsdag og navn for disse
    when (val restResponseForelderBarnRelasjon =
      bidragPersonConsumer.hentForelderBarnRelasjon(forelderBarnRequest)) {
      is RestResponse.Success -> {
        val forelderBarnRelasjonResponse = restResponseForelderBarnRelasjon.body

        if (!forelderBarnRelasjonResponse.forelderBarnRelasjon.isNullOrEmpty()) {
          SECURE_LOGGER.info("Bidrag-person ga følgende respons på forelder-barn-relasjoner: $forelderBarnRelasjonResponse")

          forelderBarnRelasjonResponse.forelderBarnRelasjon.forEach { forelderBarnRelasjon ->
            if (forelderBarnRelasjon.relatertPersonsRolle == ForelderBarnRelasjonRolle.BARN) {
              // Kaller bidrag-person for å hente info om fødselsdato og navn
              if (forelderBarnRelasjon.relatertPersonsIdent != null) {
                val navnFoedselDoedResponseDto = hentNavnFoedselDoed(PersonRequest(forelderBarnRelasjon.relatertPersonsIdent))
                // Lager en liste over fnr for alle barn som er funnet
                barnListe.add(
                  PersonBo(forelderBarnRelasjon.relatertPersonsIdent, navnFoedselDoedResponseDto?.navn, navnFoedselDoedResponseDto?.foedselsdato,
                    null, null))
              }
            }
          }
          return barnListe
        }
      }

      is RestResponse.Failure -> this.add(
        OppdaterGrunnlagDto(
          GrunnlagRequestType.EGNE_BARN_I_HUSSTANDEN,
          forelderBarnRequest.ident,
          if (restResponseForelderBarnRelasjon.statusCode == HttpStatus.NOT_FOUND) GrunnlagsRequestStatus.IKKE_FUNNET else GrunnlagsRequestStatus.FEILET,
          "Feil ved henting av egne barn i husstanden for: ${forelderBarnRequest.ident} ."
        )
      )
    }
    return emptyList()
  }

  private fun hentNavnFoedselDoed(personId: PersonRequest): NavnFoedselDoedResponseDto? {
    //hent navn, fødselsdato og eventuell dødsdato for personer fra bidrag-person
    LOGGER.info("Kaller bidrag-person hent navn og fødselsdato")
    SECURE_LOGGER.info("Kaller bidrag-person hent navn og fødselsdato for : $personId")
    when (val restResponseFoedselOgDoed =
      bidragPersonConsumer.hentNavnFoedselOgDoed(personId)) {
      is RestResponse.Success -> {
        val foedselOgDoedResponse = restResponseFoedselOgDoed.body
        SECURE_LOGGER.info("Bidrag-person ga følgende respons på hent navn og fødselsdato: $foedselOgDoedResponse")

        return NavnFoedselDoedResponseDto(
          foedselOgDoedResponse.navn,
          foedselOgDoedResponse.foedselsdato,
          foedselOgDoedResponse.foedselsaar,
          foedselOgDoedResponse.doedsdato
        )
      }

      is RestResponse.Failure ->
        return null
    }
  }
}