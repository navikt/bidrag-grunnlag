package no.nav.bidrag.grunnlag.model

import no.nav.bidrag.behandling.felles.dto.grunnlag.OppdaterGrunnlagDto
import no.nav.bidrag.behandling.felles.enums.GrunnlagRequestType
import no.nav.bidrag.behandling.felles.enums.GrunnlagsRequestStatus
import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.consumer.familiekssak.KontantstotteConsumer
import no.nav.bidrag.grunnlag.consumer.familiekssak.api.BisysDto
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
    private val LOGGER: Logger = LoggerFactory.getLogger(OppdaterBarnetillegg::class.java)
  }

  fun oppdaterKontantstotte(kontantstotteRequestListe: List<PersonIdOgPeriodeRequest>): OppdaterKontantstotte {
    kontantstotteRequestListe.forEach { personIdOgPeriode ->
      var antallPerioderFunnet = 0

      // Input til tjeneste er en liste over alle personnr for en person,
      // kall PDL for å hente historikk på fnr?
      val innsynRequest = BisysDto(
        listOf(personIdOgPeriode.personId)
//        , personIdOgPeriode.periodeFra
      )

      LOGGER.info("Kaller kontantstøtte")
      SECURE_LOGGER.info("Kaller kontantstøtte med request: $innsynRequest")

      when (val restResponseKontantstotte =
        kontantstotteConsumer.hentKontantstotte(innsynRequest)) {
        is RestResponse.Success -> {
          val kontantstotteResponse = restResponseKontantstotte.body
          SECURE_LOGGER.info("kontantstotte ga følgende respons: $kontantstotteResponse")

          persistenceService.oppdaterEksisterendeKontantstotteTilInaktiv(
            grunnlagspakkeId,
            personIdOgPeriode.personId,
            timestampOppdatering
          )

/*          kontantstotteResponse.utbetalingsinfo.get(0)
            .values.forEach { ks ->
            antallPerioderFunnet++
            for (i in ks.indices) {
              persistenceService.opprettKontantstotte(
                KontantstotteBo(
                  grunnlagspakkeId = grunnlagspakkeId,
                  partPersonId = personIdOgPeriode.personId,
                  barnPersonId = kontantstotteResponse.utbetalingsinfo.keys.toString(),
                  periodeFra = LocalDate.parse(ks.fom.toString() + "-01"),
                  // justerer frem tildato med én dag for å ha lik logikk som resten av appen. Tildato skal angis som til, men ikke inkludert, dato.
                  periodeTil = if (ks.tom != null) LocalDate.parse(ks.tom.toString() + "-01").plusMonths(1) else null,
                  aktiv = true,
                  brukFra = timestampOppdatering,
                  belop = beregnBelopForGjeldendeBarn(ks, i),
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
          )*/
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

/*  private fun beregnBelopForGjeldendeBarn(ks: StonadDto, index: Int): Int {
    val antallBarn = ks.barn.size
    val belop = ks.belop
    if (belop != null) {
      val belopPerParn = belop.div(antallBarn.toDouble())
      // Siden summen for alle barn er lagt sammen, og det kan være barn med forskjellige summer ved
      // feks 60% kontantstøtte så må vi håndtere at det kan bli desimaltall ved deling på hvert barn.
      return if (index != antallBarn - 1) {
        Math.floor(belopPerParn).toInt()
      } else {
        Math.ceil(belopPerParn).toInt()
      }
    } else
      return 0
  }*/
}