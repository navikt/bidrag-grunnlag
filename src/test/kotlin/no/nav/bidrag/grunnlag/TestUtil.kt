package no.nav.bidrag.grunnlag

import no.nav.bidrag.grunnlag.api.HentGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.api.OppdaterGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.api.OpprettGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.consumer.familiebasak.BisysStønadstype
import no.nav.bidrag.grunnlag.consumer.familiebasak.FamilieBaSakResponse
import no.nav.bidrag.grunnlag.consumer.familiebasak.UtvidetBarnetrygdPeriode
import no.nav.bidrag.grunnlag.dto.GrunnlagspakkeDto
import no.nav.bidrag.grunnlag.dto.InntektDto
import no.nav.bidrag.grunnlag.dto.InntektspostDto
import no.nav.bidrag.grunnlag.dto.StonadDto
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

class TestUtil {

  companion object {

    fun byggNyGrunnlagspakkeRequest() = OpprettGrunnlagspakkeRequest(
      opprettetAv = "RTV9999"
    )

    fun byggOppdaterGrunnlagspakkeRequest(grunnlagspakkeId: Int) = OppdaterGrunnlagspakkeRequest(
      grunnlagspakkeId = grunnlagspakkeId,
      behandlingType = "BIDRAG",
      identListe = listOf("123456789", "234567890", "345678901"),
      periodeFom = "2021-07",
      periodeTom = "2021-08",
      gyldigTom = "2021-08"
    )


    fun byggHentGrunnlagspakkeRequest() = HentGrunnlagspakkeRequest(
      grunnlagspakkeId = 1
    )


/*      personId = 1234567,
      type = "Loennsinntekt",
      gyldigFra = LocalDate.parse("2021-07-01"),
      gyldigTil = LocalDate.parse("2021-08-01"),
      aktiv = true,
      inntektspostListe = listOf(
        OpprettInntektspostRequest(
          utbetalingsperiode = "202108",
          opptjeningsperiodeFra = LocalDate.parse("2021-07-01"),
          opptjeningsperiodeTil = LocalDate.parse("2021-08-01"),
          opplysningspliktigId = "123",
          inntektType = "Loenn",
          fordelType = "Kontantytelse",
          beskrivelse = "Loenn/fastloenn",
          belop = BigDecimal.valueOf(17000),
        ),
        OpprettInntektspostRequest(
          utbetalingsperiode = "202108",
          opptjeningsperiodeFra = LocalDate.parse("2021-07-01"),
          opptjeningsperiodeTil = LocalDate.parse("2021-08-01"),
          opplysningspliktigId = "123",
          inntektType = "Loenn",
          fordelType = "Kontantytelse",
          beskrivelse = "Loenn/ferieLoenn",
          belop = BigDecimal.valueOf(50000),
        )
      )
    )*/

    fun byggGrunnlagspakkeDto() = GrunnlagspakkeDto(
      grunnlagspakkeId = (1..100).random(),
      opprettetAv = "RTV9999",
      opprettetTimestamp = LocalDateTime.now(),
      endretTimestamp = LocalDateTime.now()
    )

    fun byggInntektDto() = InntektDto(
      inntektId = (1..100).random(),
      grunnlagspakkeId = (1..100).random(),
      personId = 1234567,
      type = "Loennsinntekt",
      gyldigFra = LocalDate.parse("2021-07-01"),
      gyldigTil = LocalDate.parse("2021-08-01"),
      aktiv = true,
      hentetTidspunkt = LocalDateTime.now(),
      brukFra = LocalDateTime.now(),
      brukTil = null
    )

    fun byggInntektspostDto() = InntektspostDto(
      inntektspostId = (1..100).random(),
      inntektId = (1..100).random(),
      utbetalingsperiode = "202108",
      opptjeningsperiodeFra = LocalDate.parse("2021-07-01"),
      opptjeningsperiodeTil = LocalDate.parse("2021-08-01"),
      opplysningspliktigId = "123",
      inntektType = "Loenn",
      fordelType = "Kontantytelse",
      beskrivelse = "Loenn/ferieLoenn",
      belop = BigDecimal.valueOf(50000),
    )

    fun byggStonadDto() = StonadDto(
      stonadId = (1..100).random(),
      grunnlagspakkeId = (1..100).random(),
      personId = 1234567,
      type = "Utvidet barnetrygd",
      periodeFra = LocalDate.parse("2021-01-01"),
      periodeTil = LocalDate.parse("2021-07-01"),
      belop = BigDecimal.valueOf(12468.01),
      manueltBeregnet = false

    )

    fun byggFamilieBaSakResponse() = FamilieBaSakResponse(
      byggUtvidetBarnetrygdPeriode()
    )

    fun byggUtvidetBarnetrygdPeriode() : List<UtvidetBarnetrygdPeriode> {
      val utvidetBarnetrygdPeriode = UtvidetBarnetrygdPeriode(
        stønadstype = BisysStønadstype.UTVIDET,
        fomMåned = YearMonth.now(),
        tomMåned = YearMonth.now(),
        beløp = 1000.00,
        manueltBeregnet = false
      )
      return mutableListOf(utvidetBarnetrygdPeriode)
    }

  }
}
