package no.nav.bidrag.grunnlag

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.grunnlag.api.grunnlagspakke.GrunnlagRequest
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OppdaterGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OpprettGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.ainntekt.HentInntektRequest
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.barnetillegg.BarnetilleggPensjon
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.barnetillegg.HentBarnetilleggPensjonRequest
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.barnetillegg.HentBarnetilleggPensjonResponse
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.skatt.HentSkattegrunnlagRequest
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.skatt.HentSkattegrunnlagResponse
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.skatt.Skattegrunnlag
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.BisysStønadstype
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.FamilieBaSakRequest
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.FamilieBaSakResponse
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.UtvidetBarnetrygdPeriode
import no.nav.bidrag.grunnlag.dto.AinntektDto
import no.nav.bidrag.grunnlag.dto.AinntektspostDto
import no.nav.bidrag.grunnlag.dto.BarnetilleggDto
import no.nav.bidrag.grunnlag.dto.GrunnlagspakkeDto
import no.nav.bidrag.grunnlag.dto.SkattegrunnlagDto
import no.nav.bidrag.grunnlag.dto.SkattegrunnlagspostDto
import no.nav.bidrag.grunnlag.dto.UtvidetBarnetrygdOgSmaabarnstilleggDto
import no.nav.bidrag.grunnlag.service.BarnType
import no.nav.bidrag.grunnlag.service.Formaal
import no.nav.bidrag.grunnlag.service.GrunnlagType
import no.nav.bidrag.grunnlag.service.SkattegrunnlagType
import no.nav.tjenester.aordningen.inntektsinformasjon.Aktoer
import no.nav.tjenester.aordningen.inntektsinformasjon.AktoerType
import no.nav.tjenester.aordningen.inntektsinformasjon.ArbeidsInntektInformasjon
import no.nav.tjenester.aordningen.inntektsinformasjon.ArbeidsInntektMaaned
import no.nav.tjenester.aordningen.inntektsinformasjon.ArbeidsforholdFrilanser
import no.nav.tjenester.aordningen.inntektsinformasjon.Avvik
import no.nav.tjenester.aordningen.inntektsinformasjon.Forskuddstrekk
import no.nav.tjenester.aordningen.inntektsinformasjon.Fradrag
import no.nav.tjenester.aordningen.inntektsinformasjon.inntekt.Inntekt
import no.nav.tjenester.aordningen.inntektsinformasjon.inntekt.InntektType
import no.nav.tjenester.aordningen.inntektsinformasjon.response.HentInntektListeResponse
import okhttp3.internal.immutableListOf
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockHttpServletRequestDsl
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.StatusResultMatchersDsl
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

class TestUtil {

  companion object {

    fun byggNyGrunnlagspakkeRequest() = OpprettGrunnlagspakkeRequest(
      opprettetAv = "RTV9999",
      formaal = Formaal.BIDRAG,
    )

    fun byggOppdaterGrunnlagspakkeRequestKomplett() = OppdaterGrunnlagspakkeRequest(
      gyldigTil = LocalDate.parse("2021-08-01"),
      grunnlagRequestListe = listOf(
        GrunnlagRequest(
          grunnlagType = GrunnlagType.AINNTEKT,
          personId = "12345678910",
          periodeFra = LocalDate.parse("2021-01-01"),
          periodeTil = LocalDate.parse("2022-01-01")
        ),
        GrunnlagRequest(
          grunnlagType = GrunnlagType.SKATTEGRUNNLAG,
          personId = "12345678910",
          periodeFra = LocalDate.parse("2021-01-01"),
          periodeTil = LocalDate.parse("2022-01-01")
        ),
        GrunnlagRequest(
          grunnlagType = GrunnlagType.UTVIDETBARNETRYGDOGSMAABARNSTILLEGG,
          personId = "12345678910",
          periodeFra = LocalDate.parse("2021-01-01"),
          periodeTil = LocalDate.parse("2022-01-01")
        ),
        GrunnlagRequest(
          grunnlagType = GrunnlagType.BARNETILLEGG,
          personId = "12345678910",
          periodeFra = LocalDate.parse("2021-01-01"),
          periodeTil = LocalDate.parse("2022-01-01")
        )
      )
    )

    fun byggOppdaterGrunnlagspakkeRequestUtvidetBarnetrygd() = OppdaterGrunnlagspakkeRequest(
      gyldigTil = LocalDate.parse("2021-08-01"),
      grunnlagRequestListe = listOf(
        GrunnlagRequest(
          grunnlagType = GrunnlagType.UTVIDETBARNETRYGDOGSMAABARNSTILLEGG,
          personId = "12345678910",
          periodeFra = LocalDate.parse("2021-01-01"),
          periodeTil = LocalDate.parse("2022-01-01")
        )
      )
    )

    fun byggOppdaterGrunnlagspakkeRequestBarnetillegg() = OppdaterGrunnlagspakkeRequest(
      gyldigTil = LocalDate.parse("2021-08-01"),
      grunnlagRequestListe = listOf(
        GrunnlagRequest(
          grunnlagType = GrunnlagType.BARNETILLEGG,
          personId = "12345678910",
          periodeFra = LocalDate.parse("2021-01-01"),
          periodeTil = LocalDate.parse("2022-01-01")
        )
      )
    )

    fun byggGrunnlagspakkeDto() = GrunnlagspakkeDto(
      grunnlagspakkeId = (1..100).random(),
      opprettetAv = "RTV9999",
      opprettetTimestamp = LocalDateTime.now(),
      endretTimestamp = LocalDateTime.now()
    )

    fun byggAinntektDto() = AinntektDto(
      inntektId = (1..100).random(),
      grunnlagspakkeId = (1..100).random(),
      personId = "1234567",
      periodeFra = LocalDate.parse("2021-07-01"),
      periodeTil = LocalDate.parse("2021-08-01"),
      aktiv = true,
      hentetTidspunkt = LocalDateTime.now(),
      brukFra = LocalDateTime.now(),
      brukTil = null
    )

    fun byggAinntektspostDto() = AinntektspostDto(
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
      etterbetalingsperiodeFra = LocalDate.of(2021, 10, 1),
      etterbetalingsperiodeTil = LocalDate.of(2021, 11, 1)
    )

    fun byggSkattegrunnlagSkattDto() = SkattegrunnlagDto(
      skattegrunnlagId = (1..100).random(),
      grunnlagspakkeId = (1..100).random(),
      personId = "7654321",
      periodeFra = LocalDate.parse("2021-01-01"),
      periodeTil = LocalDate.parse("2022-01-01"),
      aktiv = true,
      brukFra = LocalDateTime.now(),
      brukTil = null,
      hentetTidspunkt = LocalDateTime.now()
    )

    fun byggSkattegrunnlagspostDto() = SkattegrunnlagspostDto(
      skattegrunnlagspostId = (1..100).random(),
      skattegrunnlagId = (1..100).random(),
      skattegrunnlagType = SkattegrunnlagType.ORDINAER.toString(),
      inntektType = "Loenn",
      belop = BigDecimal.valueOf(171717),
    )

    fun byggUtvidetBarnetrygdOgSmaabarnstilleggDto() = UtvidetBarnetrygdOgSmaabarnstilleggDto(
      ubstId = (1..100).random(),
      grunnlagspakkeId = (1..100).random(),
      personId = "1234567",
      type = "Utvidet barnetrygd",
      periodeFra = LocalDate.parse("2021-01-01"),
      periodeTil = LocalDate.parse("2021-07-01"),
      aktiv = true,
      brukFra = LocalDateTime.now(),
      brukTil = null,
      belop = BigDecimal.valueOf(12468.01),
      manueltBeregnet = false,
      deltBosted = false
    )

    fun byggBarnetilleggDto() = BarnetilleggDto(
      barnetilleggId = (1..100).random(),
      grunnlagspakkeId = (1..100).random(),
      partPersonId = "1234567",
      barnPersonId = "0123456",
      barnetilleggType = "Utvidet barnetrygd",
      periodeFra = LocalDate.parse("2021-01-01"),
      periodeTil = LocalDate.parse("2021-07-01"),
      aktiv = true,
      brukFra = LocalDateTime.now(),
      brukTil = null,
      belopBrutto = BigDecimal.valueOf(1000),
      barnType = BarnType.FELLES.toString()
    )

    fun byggFamilieBaSakResponse() = FamilieBaSakResponse(
      immutableListOf(
        UtvidetBarnetrygdPeriode(
          stønadstype = BisysStønadstype.UTVIDET,
          fomMåned = YearMonth.parse("2021-01"),
          tomMåned = YearMonth.parse("2021-12"),
          beløp = 1000.11,
          manueltBeregnet = false,
          deltBosted = false
        ),
        UtvidetBarnetrygdPeriode(
          stønadstype = BisysStønadstype.UTVIDET,
          fomMåned = YearMonth.parse("2022-01"),
          tomMåned = YearMonth.parse("2022-12"),
          beløp = 2000.22,
          manueltBeregnet = false,
          deltBosted = false
        )
      )
    )

    fun byggHentInntektRequest() = HentInntektRequest(
      ident = "ident",
      innsynHistoriskeInntekterDato = LocalDate.now(),
      maanedFom = LocalDate.now().toString(),
      maanedTom = LocalDate.now().toString(),
      ainntektsfilter = "BidragA-Inntekt",
      formaal = "Bidrag"
    )

    fun byggHentInntektListeResponse() = HentInntektListeResponse(
      byggArbeidsInntektMaanedListe(), Aktoer("", AktoerType.NATURLIG_IDENT)
    )

    private fun byggArbeidsInntektMaanedListe(): List<ArbeidsInntektMaaned> {
      val arbeidsforholdListe = mutableListOf<ArbeidsforholdFrilanser>()
      val forskuddstrekkListe = mutableListOf<Forskuddstrekk>()
      val fradragListe = mutableListOf<Fradrag>()
      val avviksliste = mutableListOf<Avvik>()
      val arbeidsinntektliste = mutableListOf<ArbeidsInntektMaaned>()
      arbeidsinntektliste.add(
        ArbeidsInntektMaaned(
          YearMonth.parse("2021-01"),
          avviksliste,
          ArbeidsInntektInformasjon(
            arbeidsforholdListe,
            byggInntektListe(),
            forskuddstrekkListe,
            fradragListe
          )
        )
      )
      return arbeidsinntektliste
    }

    private fun byggInntektListe() = immutableListOf(
      Inntekt(
        InntektType.LOENNSINNTEKT,
        "",
        BigDecimal.valueOf(10000),
        null,
        null,
        null,
        null,
        YearMonth.parse("2021-10"),
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        false,
        false,
        null,
        null,
        null,
        null
      )
    )

    fun byggHentSkattegrunnlagRequest() = HentSkattegrunnlagRequest(
      inntektsAar = "2021",
      inntektsFilter = "inntektsfilter",
      personId = "personId"
    )

    fun byggHentSkattegrunnlagResponse() = HentSkattegrunnlagResponse(
      grunnlag = byggSkattegrunnlagListe(),
      svalbardGrunnlag = byggSkattegrunnlagListe(),
      skatteoppgjoersdato = LocalDate.now().toString()
    )

    private fun byggSkattegrunnlagListe() = immutableListOf(
      Skattegrunnlag(
        beloep = "100000",
        tekniskNavn = "tekniskNavn"
      )
    )

    fun byggFamilieBaSakRequest() = FamilieBaSakRequest(
      personIdent = "personIdent",
      fraDato = LocalDate.now()
    )

    fun byggHentBarnetilleggPensjonRequest() = HentBarnetilleggPensjonRequest(
      mottaker = "personIdent",
      fom = LocalDate.now(),
      tom = LocalDate.now()
    )

    fun byggHentBarnetilleggPensjonResponse() = HentBarnetilleggPensjonResponse(
      immutableListOf(
        BarnetilleggPensjon(
          barn = "barnIdent",
          beloep = BigDecimal.valueOf(1000.11),
          fom = LocalDate.parse("2021-01-01"),
          tom = LocalDate.parse("2021-12-31"),
          erFellesbarn = true
        ),
        BarnetilleggPensjon(
          barn = "barnIdent",
          beloep = BigDecimal.valueOf(2000.22),
          fom = LocalDate.parse("2022-01-01"),
          tom = LocalDate.parse("2022-12-31"),
          erFellesbarn = true
        )
      )
    )

    fun <Request, Response> performRequest(
      mockMvc: MockMvc,
      method: HttpMethod,
      url: String,
      input: Request?,
      responseType: Class<Response>,
      expectedStatus: StatusResultMatchersDsl.() -> Unit,
    ): Response {

      val mockHttpServletRequestDsl: MockHttpServletRequestDsl.() -> Unit = {
        contentType = MediaType.APPLICATION_JSON
        if (input != null) {
          content = when (input) {
            is String -> input
            else -> ObjectMapper().findAndRegisterModules().writeValueAsString(input)
          }
        }
        accept = MediaType.APPLICATION_JSON
      }

      val mvcResult = when (method) {
        HttpMethod.POST -> mockMvc.post(url) { mockHttpServletRequestDsl() }
        HttpMethod.GET -> mockMvc.get(url) { mockHttpServletRequestDsl() }
        else -> throw NotImplementedError()
      }.andExpect {
        status { expectedStatus() }
        content { contentType(MediaType.APPLICATION_JSON) }
      }.andReturn()

      return when (responseType) {
        String::class.java -> mvcResult.response.contentAsString as Response
        else -> ObjectMapper().findAndRegisterModules().readValue(mvcResult.response.contentAsString, responseType)
      }
    }
  }
}
