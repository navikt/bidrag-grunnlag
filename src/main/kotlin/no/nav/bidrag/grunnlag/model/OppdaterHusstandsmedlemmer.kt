package no.nav.bidrag.grunnlag.model

import no.nav.bidrag.behandling.felles.dto.grunnlag.OppdaterGrunnlagDto
import no.nav.bidrag.behandling.felles.enums.BarnType
import no.nav.bidrag.behandling.felles.enums.BarnetilleggType
import no.nav.bidrag.behandling.felles.enums.GrunnlagRequestType
import no.nav.bidrag.behandling.felles.enums.GrunnlagsRequestStatus
import no.nav.bidrag.grunnlag.bo.BarnBo
import no.nav.bidrag.grunnlag.bo.BarnetilleggBo
import no.nav.bidrag.grunnlag.bo.ForelderBarnBo
import no.nav.bidrag.grunnlag.bo.ForelderBo
import no.nav.bidrag.grunnlag.bo.HusstandBo
import no.nav.bidrag.grunnlag.bo.HusstandsmedlemBo
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.BidragGcpProxyConsumer
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.barnetillegg.HentBarnetilleggPensjonRequest
import no.nav.bidrag.grunnlag.consumer.bidragperson.BidragPersonConsumer
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.ForelderBarnRelasjonRolle
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.ForelderBarnRequest
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.HusstandsmedlemmerRequest
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.service.PersistenceService
import no.nav.bidrag.grunnlag.service.PersonIdOgPeriodeRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

class OppdaterHusstandsmedlemmer(
  private val grunnlagspakkeId: Int,
  private val timestampOppdatering: LocalDateTime,
  private val persistenceService: PersistenceService,
  private val bidragPersonConsumer: BidragPersonConsumer,

  ) : MutableList<OppdaterGrunnlagDto> by mutableListOf() {

  companion object {
    @JvmStatic
    private val LOGGER: Logger = LoggerFactory.getLogger(OppdaterHusstandsmedlemmer::class.java)
  }

  // Henter og lagrer husstandsmedlemmer
  fun oppdaterHusstandsmedlemmer(husstandsmedlemmerRequestListe: List<PersonIdOgPeriodeRequest>): OppdaterHusstandsmedlemmer {

    val oppdaterGrunnlagDtoListe = mutableListOf<OppdaterGrunnlagDto>()

    husstandsmedlemmerRequestListe.forEach { personIdOgPeriode ->
      var antallHusstanderFunnet = 0
      val husstandsmedlemmerRequest = HusstandsmedlemmerRequest(
        personId = personIdOgPeriode.personId,
        periodeFra = personIdOgPeriode.periodeFra,
      )

      LOGGER.info(
        "Kaller bidrag-person Husstandsmedlemmer med personIdent ********${
          husstandsmedlemmerRequest.personId.substring(IntRange(8, 10))
        } " +
            ", fraDato " + "${husstandsmedlemmerRequest.periodeFra}"
      )

      when (val restResponseHusstandsmedlemmer =
        bidragPersonConsumer.hentHusstandsmedlemmer(husstandsmedlemmerRequest)) {
        is RestResponse.Success -> {
          val husstandsmedlemmerResponse = restResponseHusstandsmedlemmer.body
//          LOGGER.info("Bidrag-person ga følgende respons på Husstandsmedlemmer: $husstandsmedlemmerResponse")

          if ((husstandsmedlemmerResponse.husstandResponseListe != null) && (husstandsmedlemmerResponse.husstandResponseListe.isNotEmpty())) {
            husstandsmedlemmerResponse.husstandResponseListe.forEach { husstand ->
              antallHusstanderFunnet++

              // Sett eksisterende forekomst av Husstandsmedlemmer til inaktiv
              persistenceService.oppdaterEksisterendeHusstandTilInaktiv(
                grunnlagspakkeId,
                personIdOgPeriode.personId,
                timestampOppdatering
              )

              val opprettetHusstand = persistenceService.opprettHusstand(
                HusstandBo(
                  grunnlagspakkeId = grunnlagspakkeId,
                  personId = personIdOgPeriode.personId,
                  periodeFra = husstand.gyldigFraOgMed,
                  periodeTil = husstand.gyldigTilOgMed,
                  adressenavn = husstand.adressenavn,
                  husnummer = husstand.husnummer,
                  husbokstav = husstand.husbokstav,
                  bruksenhetsnummer = husstand.bruksenhetsnummer,
                  postnummer = husstand.postnummer,
                  bydelsnummer = husstand.bydelsnummer,
                  kommunenummer = husstand.kommunenummer,
                  matrikkelId = husstand.matrikkelId,
                  aktiv = true,
                  brukFra = timestampOppdatering,
                  brukTil = null,
                  opprettetAv = null,
                  hentetTidspunkt = timestampOppdatering
                )
              )

              husstand.husstandsmedlemmerResponseListe.forEach { husstandsmedlem ->
                persistenceService.opprettHusstandsmedlem(
                  HusstandsmedlemBo(
                    periodeFra = husstandsmedlem.gyldigFraOgMed,
                    periodeTil = husstandsmedlem.gyldigTilOgMed,
                    husstandId = opprettetHusstand.husstandId,
                    personId = husstandsmedlem.personId,
                    navn = husstandsmedlem.fornavn + " " +
                        husstandsmedlem.mellomnavn + " " +
                        husstandsmedlem.etternavn,
                    foedselsdato = husstandsmedlem.foedselsdato,
                    doedsdato = husstandsmedlem.doedsdato,
                    opprettetAv = null,
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
    return this
  }
}