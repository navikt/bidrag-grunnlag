package no.nav.bidrag.grunnlag

import no.nav.bidrag.grunnlag.api.grunnlagspakke.HentGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OppdaterGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OpprettGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.BisysStønadstype
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.FamilieBaSakResponse
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.UtvidetBarnetrygdPeriode
import no.nav.bidrag.grunnlag.dto.GrunnlagspakkeDto
import no.nav.bidrag.grunnlag.dto.InntektAinntektDto
import no.nav.bidrag.grunnlag.dto.InntektSkattDto
import no.nav.bidrag.grunnlag.dto.InntektspostAinntektDto
import no.nav.bidrag.grunnlag.dto.InntektspostSkattDto
import no.nav.bidrag.grunnlag.dto.UtvidetBarnetrygdOgSmaabarnstilleggDto
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

    fun byggInntektAinntektDto() = InntektAinntektDto(
      inntektId = (1..100).random(),
      grunnlagspakkeId = (1..100).random(),
      personId = "1234567",
//      type = "Loennsinntekt",
      periodeFra = LocalDate.parse("2021-07-01"),
      periodeTil = LocalDate.parse("2021-08-01"),
      aktiv = true,
      hentetTidspunkt = LocalDateTime.now(),
      brukFra = LocalDateTime.now(),
      brukTil = null
    )

    fun byggInntektspostAinntektDto() = InntektspostAinntektDto(
      inntektspostId = (1..100).random(),
      inntektId = (1..100).random(),
      utbetalingsperiode = "202108",
      opptjeningsperiodeFra = LocalDate.parse("2021-07-01"),
      opptjeningsperiodeTil = LocalDate.parse("2021-08-01"),
      opplysningspliktigId = "123",
      type = "Loenn",
      fordelType = "Kontantytelse",
      beskrivelse = "Loenn/ferieLoenn",
      belop = BigDecimal.valueOf(50000),
    )

    fun byggInntektSkattDto() = InntektSkattDto(
      inntektId = (1..100).random(),
      grunnlagspakkeId = (1..100).random(),
      personId = "7654321",
      periodeFra = LocalDate.parse("2021-01-01"),
      periodeTil = LocalDate.parse("2021-12-01"),
      aktiv = true,
      hentetTidspunkt = LocalDateTime.now(),
      brukFra = LocalDateTime.now(),
      brukTil = null
    )

    fun byggInntektspostSkattDto() = InntektspostSkattDto(
      inntektspostId = (1..100).random(),
      inntektId = (1..100).random(),
      type = "Loenn",
      belop = BigDecimal.valueOf(171717),
    )

    fun byggUtvidetBarnetrygdOgSmaabarnstilleggDto() = UtvidetBarnetrygdOgSmaabarnstilleggDto(
      ubstId = (1..100).random(),
      grunnlagspakkeId = (1..100).random(),
      personId = "1234567",
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
      val utvidetBarnetrygdOgSmaabarnstilleggPeriode = UtvidetBarnetrygdPeriode(
//        stonadstype = BisysStonadstype.UTVIDET,
        stønadstype = BisysStønadstype.UTVIDET,
        fomMåned = YearMonth.now(),
        tomMåned = YearMonth.now(),
        beløp = 1000.00,
        manueltBeregnet = false
      )
      return mutableListOf(utvidetBarnetrygdOgSmaabarnstilleggPeriode)
    }

  }
}
