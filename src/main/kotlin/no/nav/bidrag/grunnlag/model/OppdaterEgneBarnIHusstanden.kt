package no.nav.bidrag.grunnlag.model

import no.nav.bidrag.behandling.felles.dto.grunnlag.OppdaterGrunnlagDto
import no.nav.bidrag.behandling.felles.enums.GrunnlagRequestType
import no.nav.bidrag.behandling.felles.enums.GrunnlagsRequestStatus
import no.nav.bidrag.grunnlag.bo.BarnBo
import no.nav.bidrag.grunnlag.bo.ForelderBarnBo
import no.nav.bidrag.grunnlag.bo.ForelderBo
import no.nav.bidrag.grunnlag.consumer.bidragperson.BidragPersonConsumer
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.ForelderBarnRelasjonRolle
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.ForelderBarnRequest
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

  // Henter og lagrer forelder-barn-relasjoner og navn og fødselsinfo om barn
  fun oppdaterEgneBarnIHusstanden(egneBarnIHusstandenRequestListe: List<PersonIdOgPeriodeRequest>): OppdaterEgneBarnIHusstanden {

//    val oppdaterGrunnlagDtoListe = mutableListOf<OppdaterGrunnlagDto>()

    egneBarnIHusstandenRequestListe.forEach { personIdOgPeriode ->
      var antallBarnFunnet = 0
      val forelderBarnRequest = ForelderBarnRequest(
        personId = personIdOgPeriode.personId,
        periodeFra = personIdOgPeriode.periodeFra,
      )

      LOGGER.info(
        "Kaller bidrag-person Forelder-barn-relasjon med personIdent ********${
          forelderBarnRequest.personId.substring(IntRange(8, 10))
        } " +
            ", fraDato " + "${forelderBarnRequest.periodeFra}"
      )

      when (val restResponseForelderBarnRelasjon =
        bidragPersonConsumer.hentForelderBarnRelasjon(forelderBarnRequest)) {
        is RestResponse.Success -> {
          val forelderBarnRelasjonResponse = restResponseForelderBarnRelasjon.body
//          LOGGER.info("Bidrag-person ga følgende respons på forelder-barn: $forelderBarnRelasjonResponse")

          if ((forelderBarnRelasjonResponse.forelderBarnRelasjonResponse != null) && (forelderBarnRelasjonResponse.forelderBarnRelasjonResponse.isNotEmpty())) {

            // Henter og lagrer informasjon om forelder
            val foedselOgDoedForelder = hentNavnFoedselDoed(personIdOgPeriode.personId)
//            LOGGER.info("Bidrag-person ga følgende respons på hent navn og fødselsinfo for forelderen: $foedselOgDoedForelder")
            // Sett eksisterende forekomst av Forelder til inaktiv
            persistenceService.oppdaterEksisterendeForelderTilInaktiv(
              grunnlagspakkeId,
              personIdOgPeriode.personId,
              timestampOppdatering
            )

            val opprettetForelder = persistenceService.opprettForelder(
              ForelderBo(
                grunnlagspakkeId = grunnlagspakkeId,
                personId = personIdOgPeriode.personId,
                navn = foedselOgDoedForelder?.navn,
                foedselsdato = foedselOgDoedForelder?.foedselsdato,
                doedsdato = foedselOgDoedForelder?.doedsdato,
                aktiv = true,
                brukFra = timestampOppdatering,
                brukTil = null,
                opprettetAv = null,
                hentetTidspunkt = timestampOppdatering
              )
            )

            forelderBarnRelasjonResponse.forelderBarnRelasjonResponse.forEach { forelderBarnRelasjon ->
              if (forelderBarnRelasjon.relatertPersonsRolle == ForelderBarnRelasjonRolle.BARN) {
                // Henter og lagrer informasjon om alle barn i responsen
                antallBarnFunnet++
                val navnFoedselOgDoed = hentNavnFoedselDoed(forelderBarnRelasjon.relatertPersonsIdent)
//                LOGGER.info("Bidrag-person ga følgende respons på hent navn, fødselsinfo og evt dødsfall for barn: $navnFoedselOgDoed")

                // Sett eksisterende forekomst av Barn til inaktiv
                persistenceService.oppdaterEksisterendeBarnTilInaktiv(
                  grunnlagspakkeId,
                  forelderBarnRelasjon.relatertPersonsIdent,
                  timestampOppdatering
                )

                val opprettetBarn = persistenceService.opprettBarn(
                  BarnBo(
                    grunnlagspakkeId = grunnlagspakkeId,
                    personId = forelderBarnRelasjon.relatertPersonsIdent,
                    navn = navnFoedselOgDoed?.navn,
                    foedselsdato = navnFoedselOgDoed?.foedselsdato,
                    foedselsaar = navnFoedselOgDoed?.foedselsaar,
                    doedsdato = navnFoedselOgDoed?.doedsdato,
                    aktiv = true,
                    brukFra = timestampOppdatering,
                    brukTil = null,
                    opprettetAv = null,
                    hentetTidspunkt = timestampOppdatering
                  )
                )

                // Lagrer relasjonen mellom forelder og barn
                persistenceService.opprettForelderBarn(
                  ForelderBarnBo(
                    forelderId = opprettetForelder.forelderId,
                    barnId = opprettetBarn.barnId
                  )
                )

              }
            }
            this.add(
              OppdaterGrunnlagDto(
                GrunnlagRequestType.EGNE_BARN_I_HUSSTANDEN,
                personIdOgPeriode.personId,
                GrunnlagsRequestStatus.HENTET,
                "Antall barn funnet: $antallBarnFunnet"
              )
            )
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