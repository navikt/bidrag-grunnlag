package no.nav.bidrag.grunnlag.model

import no.nav.bidrag.behandling.felles.dto.grunnlag.OppdaterGrunnlagDto
import no.nav.bidrag.behandling.felles.enums.GrunnlagRequestType
import no.nav.bidrag.behandling.felles.enums.GrunnlagsRequestStatus
import no.nav.bidrag.grunnlag.bo.KontantstotteBo
import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.consumer.infotrygdkontantstottev2.KontantstotteConsumer
import no.nav.bidrag.grunnlag.consumer.infotrygdkontantstottev2.api.KontantstotteRequest
import no.nav.bidrag.grunnlag.consumer.infotrygdkontantstottev2.api.StonadDto
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.service.PersistenceService
import no.nav.bidrag.grunnlag.service.PersonIdOgPeriodeRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

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

      SECURE_LOGGER.info("Kaller kontantstøtte med request: $kontantstotteRequest")

      when (val restResponseKontantstotte =
        kontantstotteConsumer.hentKontantstotte(kontantstotteRequest)) {
        is RestResponse.Success -> {
          val kontantstotteResponse = restResponseKontantstotte.body
          SECURE_LOGGER.info("kontantstotte ga følgende respons: $kontantstotteResponse")

          persistenceService.oppdaterEksisterendeKontantstotteTilInaktiv(
            grunnlagspakkeId,
            personIdOgPeriode.personId,
            timestampOppdatering
          )

          kontantstotteResponse.data.forEach { ks ->
            if (ks.fom.isBefore(YearMonth.from(personIdOgPeriode.periodeTil))) {
              antallPerioderFunnet++
              //Hører første innslag i UtbetalingDto til første barn? Hvordan henger dette sammen?
              for (i in ks.barn.indices) {
                persistenceService.opprettKontantstotte(
                  KontantstotteBo(
                    grunnlagspakkeId = grunnlagspakkeId,
                    partPersonId = personIdOgPeriode.personId,
                    barnPersonId = ks.barn[i].fnr,
                    periodeFra = LocalDate.parse(ks.fom.toString() + "-01"),
                    // justerer frem tildato med én måned for å ha lik logikk som resten av appen. Tildato skal angis som til, men ikke inkludert, måned.
                    periodeTil = if (ks.tom != null) LocalDate.parse(ks.tom.toString() + "-01")
                      .plusMonths(1) else null,
                    aktiv = true,
                    brukFra = timestampOppdatering,
                    belop = beregnBelopForGjeldendeBarn(ks, i),
                    brukTil = null,
                    hentetTidspunkt = timestampOppdatering
                  )
                )
              }
            }
          }
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

  private fun beregnBelopForGjeldendeBarn(ks: StonadDto, index: Int): Int {
    val antallBarn = ks.barn.size
    val belop = ks.belop
    val belopPerParn = belop/antallBarn.toDouble()

    // Siden summen for alle barn er lagt sammen, og det kan være barn med forskjellige summer ved
    // feks 60% kontantstøtte så må vi håndtere at det kan bli desimaltall ved deling på hvert barn.
    return if (index != antallBarn-1) { Math.floor(belopPerParn).toInt() } else { Math.ceil(belopPerParn).toInt() }
  }
}