package no.nav.bidrag.grunnlag.model

import no.nav.bidrag.behandling.felles.dto.grunnlag.BorISammeHusstandDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.HusstandDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.OppdaterGrunnlagDto
import no.nav.bidrag.behandling.felles.enums.BarnType
import no.nav.bidrag.behandling.felles.enums.BarnetilleggType
import no.nav.bidrag.behandling.felles.enums.GrunnlagRequestType
import no.nav.bidrag.behandling.felles.enums.GrunnlagsRequestStatus
import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.bo.BarnBo
import no.nav.bidrag.grunnlag.bo.BarnetilleggBo
import no.nav.bidrag.grunnlag.bo.ForelderBarnBo
import no.nav.bidrag.grunnlag.bo.ForelderBo
import no.nav.bidrag.grunnlag.bo.SivilstandBo
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.BidragGcpProxyConsumer
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.barnetillegg.HentBarnetilleggPensjonRequest
import no.nav.bidrag.grunnlag.consumer.bidragperson.BidragPersonConsumer
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.ForelderBarnRelasjonRolle
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.ForelderBarnRequest
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.NavnFoedselDoedResponseDto
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.SivilstandRequest
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.SivilstandResponse
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.service.PersistenceService
import no.nav.bidrag.grunnlag.service.PersonIdOgPeriodeRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

class OppdaterSivilstand(
  private val grunnlagspakkeId: Int,
  private val timestampOppdatering: LocalDateTime,
  private val persistenceService: PersistenceService,
  private val bidragPersonConsumer: BidragPersonConsumer,

  ) : MutableList<OppdaterGrunnlagDto> by mutableListOf() {

  companion object {
    @JvmStatic
    private val LOGGER: Logger = LoggerFactory.getLogger(OppdaterSivilstand::class.java)
  }


  fun oppdaterSivilstand(sivilstandRequestListe: List<PersonIdOgPeriodeRequest>): OppdaterSivilstand {

    sivilstandRequestListe.forEach { personIdOgPeriode ->

      var antallPerioderFunnet = 0

      val hentSivilstandRequest = SivilstandRequest(
        personId = personIdOgPeriode.personId,
        periodeFra = personIdOgPeriode.periodeFra,
      )

      SECURE_LOGGER.info(
        "Kaller bidrag-person og henter sivilstand for personIdent ********${
          hentSivilstandRequest.personId.substring(
            IntRange(8, 10)
          )
        } " +
            ", fraDato " + "${hentSivilstandRequest.periodeFra}"
      )

      when (val restResponseSivilstand =
        bidragPersonConsumer.hentSivilstand(hentSivilstandRequest)) {
        is RestResponse.Success -> {
          val sivilstandResponse = restResponseSivilstand.body
          SECURE_LOGGER.info("Kall til bidrag-person for å hente sivilstand ga følgende respons: $sivilstandResponse")

          if ((sivilstandResponse.sivilstand != null) && (sivilstandResponse.sivilstand.isNotEmpty())) {
            persistenceService.oppdaterEksisterendeSivilstandTilInaktiv(
              grunnlagspakkeId,
              personIdOgPeriode.personId,
              timestampOppdatering
            )
            sivilstandResponse.sivilstand.forEach { sivilstand ->
              // Pga vekslende datakvalitet fra PDL må det taes høyde for at begge disse datoene kan være null.
              // Hvis de er det så kan ikke periodekontroll gjøres og sivilstanden må lagres uten fra-dato
              val dato = sivilstand.gyldigFraOgMed ?: sivilstand.bekreftelsesdato
              if ((dato != null && dato.isBefore(personIdOgPeriode.periodeTil)) || (dato == null)) {
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
    sivilstand: SivilstandResponse,
    grunnlagspakkeId: Int,
    timestampOppdatering: LocalDateTime,
    personId: String
  ) {
    persistenceService.opprettSivilstand(
      SivilstandBo(
        grunnlagspakkeId = grunnlagspakkeId,
        personId = personId,
        periodeFra = sivilstand.gyldigFraOgMed ?: sivilstand.bekreftelsesdato,
        // justerer frem tildato med én måned for å ha lik logikk som resten av appen. Tildato skal angis som til, men ikke inkludert, måned.
//        periodeTil = if (sivilstand.tom != null) si.tom.plusMonths(1)
//          .withDayOfMonth(1) else null,
        periodeTil = null,
        sivilstand = sivilstand.type,
        aktiv = true,
        brukFra = timestampOppdatering,
        brukTil = null,
        opprettetAv = null,
        hentetTidspunkt = timestampOppdatering
      )
    )


  }


}