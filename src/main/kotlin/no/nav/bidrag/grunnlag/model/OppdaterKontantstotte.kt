package no.nav.bidrag.grunnlag.model

import no.nav.bidrag.behandling.felles.dto.grunnlag.OppdaterGrunnlagDto
import no.nav.bidrag.behandling.felles.enums.GrunnlagRequestType
import no.nav.bidrag.behandling.felles.enums.GrunnlagsRequestStatus
import no.nav.bidrag.grunnlag.consumer.infotrygdkontantstottev2.KontantstotteConsumer
import no.nav.bidrag.grunnlag.consumer.infotrygdkontantstottev2.api.KontantstotteRequest
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.service.PersistenceService
import no.nav.bidrag.grunnlag.service.PersonIdOgPeriodeRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

class OppdaterKontantstotte(
  private val grunnlagspakkeId: Int,
  private val timestampOppdatering: LocalDateTime,
  private val persistenceService: PersistenceService,
  private val kontantstotteConsumer: KontantstotteConsumer
) : MutableList<OppdaterGrunnlagDto> by mutableListOf() {
  companion object {
    @JvmStatic
    private val LOGGER: Logger = LoggerFactory.getLogger(OppdaterKontantstotte::class.java)
  }

  fun oppdaterKontantstotte(kontantstotteRequestListe: List<PersonIdOgPeriodeRequest>): OppdaterKontantstotte {
    kontantstotteRequestListe.forEach { personIdOgPeriode ->
      var antallPerioderFunnet = 0

      // Input til tjeneste er en liste over alle personnr for en person,
      // kall PDL for å hente historikk på fnr?
      val kontantstotteRequest = KontantstotteRequest(
        listOf(personIdOgPeriode.personId)
      )

      LOGGER.info(
        "Kaller kontantstøtte med personIdent ********${
          kontantstotteRequest.fnr[0].substring(
            IntRange(8, 10)
          )
        } "
      )

      when (val restResponseKontantstotte =
        kontantstotteConsumer.hentKontantstotte(kontantstotteRequest)) {
        is RestResponse.Success -> {
          val kontantstotteResponse = restResponseKontantstotte.body
          LOGGER.info("kontantstotte ga følgende respons: $kontantstotteResponse")

//          if (kontantstotteResponse.data.isNotEmpty()) {
          /*            persistenceService.oppdaterEksisterendeKontantstotteTilInaktiv(
                        grunnlagspakkeId,
                        personIdOgPeriode.personId,
                        timestampOppdatering
                      )*/
          /*            kontantstotteResponse.data.forEach { ks ->
                        if (LocalDate.parse(ks.utbetalinger.toString() + "-01").isBefore(personIdOgPeriode.periodeTil)) {
                          antallPerioderFunnet++
                          persistenceService.opprettUtvidetBarnetrygdOgSmaabarnstillegg(
                            UtvidetBarnetrygdOgSmaabarnstilleggBo(
                              grunnlagspakkeId = grunnlagspakkeId,
                              personId = personIdOgPeriode.personId,
                              type = ks.stønadstype.toString(),
                              periodeFra = LocalDate.parse(ks.fomMåned.toString() + "-01"),
                              // justerer frem tildato med én måned for å ha lik logikk som resten av appen. Tildato skal angis som til, men ikke inkludert, måned.
                              periodeTil = if (ks.tomMåned != null) LocalDate.parse(ks.tomMåned.toString() + "-01")
                                .plusMonths(1) else null,
                              brukFra = timestampOppdatering,
                              belop = BigDecimal.valueOf(ks.beløp),
                              manueltBeregnet = ks.manueltBeregnet,
                              deltBosted = ks.deltBosted,
                              hentetTidspunkt = timestampOppdatering
                            )
                          )
                        }
                      }*/
//          }
          this.add(
            OppdaterGrunnlagDto(
              GrunnlagRequestType.KONTANTSTOTTE,
              personIdOgPeriode.personId,
              GrunnlagsRequestStatus.HENTET,
              "Antall perioder funnet: $antallPerioderFunnet"
            )
          )
        }

        is RestResponse.Failure -> this.add(
          OppdaterGrunnlagDto(
            GrunnlagRequestType.KONTANTSTOTTE,
            personIdOgPeriode.personId,
            if (restResponseKontantstotte.statusCode == HttpStatus.NOT_FOUND) GrunnlagsRequestStatus.IKKE_FUNNET else GrunnlagsRequestStatus.FEILET,
            "Feil ved henting av kontantstøtte for perioden: ${personIdOgPeriode.periodeFra} - ${personIdOgPeriode.periodeTil}."
          )
        )
      }
    }
    return this
  }
}