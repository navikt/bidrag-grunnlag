package no.nav.bidrag.grunnlag.model

import no.nav.bidrag.behandling.felles.dto.grunnlag.OppdaterGrunnlagDto
import no.nav.bidrag.behandling.felles.enums.GrunnlagRequestType
import no.nav.bidrag.behandling.felles.enums.GrunnlagsRequestStatus
import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.bo.HusstandsmedlemskapBo
import no.nav.bidrag.grunnlag.consumer.bidragperson.BidragPersonConsumer
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.ForelderBarnRelasjonRolle
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.ForelderBarnRequest
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.HusstandsmedlemmerRequest
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.NavnFoedselDoedResponseDto
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.service.PersistenceService
import no.nav.bidrag.grunnlag.service.PersonIdOgPeriodeRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

class OppdaterEgneBarnIHusstanden(
  private val grunnlagspakkeId: Int,
  private val timestampOppdatering: LocalDateTime,
  private val persistenceService: PersistenceService,
  private val bidragPersonConsumer: BidragPersonConsumer,

  ) : MutableList<OppdaterGrunnlagDto> by mutableListOf() {

  companion object {
    @JvmStatic
    private val LOGGER: Logger = LoggerFactory.getLogger(OppdaterEgneBarnIHusstanden::class.java)
  }

  // Henter og lagrer først husstandsmedlemmer for så å hente forelder-barn-relasjoner for å kunne returnere grunnlaget egneBarnIHusstanden
  // Også barn som ikke bor i samme husstand som BM/BP skal være med i grunnlaget
  fun oppdaterEgneBarnIHusstanden(egneBarnIHusstandenRequestListe: List<PersonIdOgPeriodeRequest>): OppdaterEgneBarnIHusstanden {

    egneBarnIHusstandenRequestListe.forEach { personIdOgPeriode ->
      var antallBarnFunnet = 0
      val forelderBarnRequest = ForelderBarnRequest(
        personId = personIdOgPeriode.personId,
        periodeFra = personIdOgPeriode.periodeFra,
      )

      LOGGER.info("Kaller bidrag-person Forelder-barn-relasjon")
      SECURE_LOGGER.info("Kaller bidrag-person Forelder-barn-relasjon med request: $forelderBarnRequest")

      // Henter først en liste over BMs/BPs barn
      when (val restResponseForelderBarnRelasjon =
        bidragPersonConsumer.hentForelderBarnRelasjon(forelderBarnRequest)) {
        is RestResponse.Success -> {
          val forelderBarnRelasjonResponse = restResponseForelderBarnRelasjon.body

          if (!forelderBarnRelasjonResponse.forelderBarnRelasjonResponse.isNullOrEmpty()) {
            val barnListe = mutableListOf<String>()
            SECURE_LOGGER.info("Bidrag-person ga følgende respons på forelder-barn-relasjoner: $forelderBarnRelasjonResponse")



            forelderBarnRelasjonResponse.forelderBarnRelasjonResponse.forEach { forelderBarnRelasjon ->
              if (forelderBarnRelasjon.relatertPersonsRolle == ForelderBarnRelasjonRolle.BARN) {
                // Lager en liste over fnr for alle barn som er funnet. Listen brukes under til å lagre om et husstandsmedlem er barn av BM/BP eller ikke
                antallBarnFunnet++
                barnListe.add(forelderBarnRelasjon.relatertPersonsIdent)
              }
            }

            val husstandsmedlemmerRequest = HusstandsmedlemmerRequest(
              personId = personIdOgPeriode.personId,
              periodeFra = personIdOgPeriode.periodeFra,
            )

            LOGGER.info("Kaller bidrag-person Husstandsmedlemmer for grunnlag EgneBarnIHusstanden")
            SECURE_LOGGER.info("Kaller bidrag-person Husstandsmedlemmer for grunnlag EgneBarnIHusstanden med request: $husstandsmedlemmerRequest")
            var antallHusstanderFunnet = 0

            when (val restResponseHusstandsmedlemmer =
              bidragPersonConsumer.hentHusstandsmedlemmer(husstandsmedlemmerRequest)) {
              is RestResponse.Success -> {
                val husstandsmedlemmerResponse = restResponseHusstandsmedlemmer.body
                SECURE_LOGGER.info("Bidrag-person ga følgende respons på Husstandsmedlemmer for grunnlag EgneBarnIHusstanden: $husstandsmedlemmerResponse")

                if (!husstandsmedlemmerResponse.husstandResponseListe.isNullOrEmpty()) {
                  husstandsmedlemmerResponse.husstandResponseListe.forEach { husstand ->
                    antallHusstanderFunnet++

                    // Sett eksisterende forekomster av Husstandsmedlemskap til inaktiv
                    persistenceService.oppdaterEksisterendeHusstandsmedlemskapTilInaktiv(
                      grunnlagspakkeId,
                      personIdOgPeriode.personId,
                      timestampOppdatering
                    )

                    husstand.husstandsmedlemmerResponseListe.forEach { husstandsmedlem ->
                      persistenceService.opprettHusstandsmedlemskap(
                        HusstandsmedlemskapBo(
                          partPersonId = personIdOgPeriode.personId,
                          husstandsmedlemPersonId = husstandsmedlem.personId,
                          navn = husstandsmedlem.fornavn + " " +
                            husstandsmedlem.mellomnavn + " " +
                            husstandsmedlem.etternavn,
                          fodselsdato = husstandsmedlem.foedselsdato,
                          erBarnAvBmBp = barnListe.any { it == husstandsmedlem.personId },
                          periodeFra = husstandsmedlem.gyldigFraOgMed,
                          periodeTil = husstandsmedlem.gyldigTilOgMed,
                          aktiv = true,
                          brukFra = timestampOppdatering,
                          brukTil = null,
                          hentetTidspunkt = timestampOppdatering
                        )
                      )
                    }
                  }
                }
                this.add(
                  OppdaterGrunnlagDto(
                    GrunnlagRequestType.HUSSTANDSMEDLEMMER,
                    personIdOgPeriode.personId,
                    GrunnlagsRequestStatus.HENTET,
                    "Antall husstander funnet: $antallHusstanderFunnet"
                  )
                )
              }

              is RestResponse.Failure -> this.add(
                OppdaterGrunnlagDto(
                  GrunnlagRequestType.HUSSTANDSMEDLEMMER,
                  personIdOgPeriode.personId,
                  if (restResponseHusstandsmedlemmer.statusCode == HttpStatus.NOT_FOUND) GrunnlagsRequestStatus.IKKE_FUNNET else GrunnlagsRequestStatus.FEILET,
                  "Feil ved henting av husstandsmedlemmer for perioden: ${personIdOgPeriode.periodeFra} - ${personIdOgPeriode.periodeTil}."
                )
              )
            }
          }
        }

        is RestResponse.Failure -> this.add(
          OppdaterGrunnlagDto(
            GrunnlagRequestType.EGNE_BARN_I_HUSSTANDEN,
            personIdOgPeriode.personId,
            if (restResponseForelderBarnRelasjon.statusCode == HttpStatus.NOT_FOUND) GrunnlagsRequestStatus.IKKE_FUNNET else GrunnlagsRequestStatus.FEILET,
            "Feil ved henting av egne barn i husstanden for perioden: ${personIdOgPeriode.periodeFra} - ${personIdOgPeriode.periodeTil}."
          )
        )
      }
    }
    return this

  }

  fun hentNavnFoedselDoed(personId: String): NavnFoedselDoedResponseDto? {
    //hent navn, fødselsdato og eventuell dødsdato for personer fra bidrag-person
    when (val restResponseFoedselOgDoed =
      bidragPersonConsumer.hentNavnFoedselOgDoed(personId)) {
      is RestResponse.Success -> {
        val foedselOgDoedResponse = restResponseFoedselOgDoed.body
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