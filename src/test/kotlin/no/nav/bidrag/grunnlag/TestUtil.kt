package no.nav.bidrag.grunnlag

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.behandling.felles.dto.grunnlag.GrunnlagRequestDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.OppdaterGrunnlagspakkeRequestDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.OpprettGrunnlagspakkeRequestDto
import no.nav.bidrag.behandling.felles.enums.BarnType
import no.nav.bidrag.behandling.felles.enums.Formaal
import no.nav.bidrag.behandling.felles.enums.GrunnlagRequestType
import no.nav.bidrag.behandling.felles.enums.SkattegrunnlagType
import no.nav.bidrag.grunnlag.bo.AinntektBo
import no.nav.bidrag.grunnlag.bo.AinntektspostBo
import no.nav.bidrag.grunnlag.bo.BarnetilleggBo
import no.nav.bidrag.grunnlag.bo.KontantstotteBo
import no.nav.bidrag.grunnlag.bo.SkattegrunnlagBo
import no.nav.bidrag.grunnlag.bo.SkattegrunnlagspostBo
import no.nav.bidrag.grunnlag.bo.UtvidetBarnetrygdOgSmaabarnstilleggBo
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
import no.nav.bidrag.grunnlag.consumer.infotrygdkontantstottev2.api.KontantstotteRequest
import no.nav.bidrag.grunnlag.consumer.infotrygdkontantstottev2.api.KontantstotteResponse
import no.nav.bidrag.grunnlag.consumer.infotrygdkontantstottev2.api.StonadDto
import no.nav.bidrag.grunnlag.consumer.infotrygdkontantstottev2.api.UtbetalingDto
import no.nav.bidrag.grunnlag.persistence.entity.Ainntekt
import no.nav.bidrag.grunnlag.persistence.entity.Ainntektspost
import no.nav.bidrag.grunnlag.persistence.entity.Barnetillegg
import no.nav.bidrag.grunnlag.persistence.entity.Grunnlagspakke
import no.nav.bidrag.grunnlag.persistence.entity.Kontantstotte
import no.nav.bidrag.grunnlag.persistence.entity.Skattegrunnlagspost
import no.nav.bidrag.grunnlag.persistence.entity.UtvidetBarnetrygdOgSmaabarnstillegg
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

    fun byggNyGrunnlagspakkeRequest() = OpprettGrunnlagspakkeRequestDto(
      opprettetAv = "RTV9999",
      formaal = Formaal.BIDRAG,
    )

    fun byggOppdaterGrunnlagspakkeRequestKomplett() = OppdaterGrunnlagspakkeRequestDto(
      grunnlagRequestDtoListe = listOf(
        GrunnlagRequestDto(
          type = GrunnlagRequestType.AINNTEKT,
          personId = "12345678910",
          periodeFra = LocalDate.parse("2021-01-01"),
          periodeTil = LocalDate.parse("2022-01-01")
        ),
        GrunnlagRequestDto(
          type = GrunnlagRequestType.SKATTEGRUNNLAG,
          personId = "12345678910",
          periodeFra = LocalDate.parse("2021-01-01"),
          periodeTil = LocalDate.parse("2022-01-01")
        ),
        GrunnlagRequestDto(
          type = GrunnlagRequestType.UTVIDET_BARNETRYGD_OG_SMAABARNSTILLEGG,
          personId = "12345678910",
          periodeFra = LocalDate.parse("2021-01-01"),
          periodeTil = LocalDate.parse("2022-01-01")
        ),
        GrunnlagRequestDto(
          type = GrunnlagRequestType.BARNETILLEGG,
          personId = "12345678910",
          periodeFra = LocalDate.parse("2021-01-01"),
          periodeTil = LocalDate.parse("2022-01-01")
        ),
        GrunnlagRequestDto(
          type = GrunnlagRequestType.KONTANTSTOTTE,
          personId = "12345678910",
          periodeFra = LocalDate.parse("2021-01-01"),
          periodeTil = LocalDate.parse("2022-01-01")
        )
      )
    )

    fun byggOppdaterGrunnlagspakkeRequestUtvidetBarnetrygd() = OppdaterGrunnlagspakkeRequestDto(
      grunnlagRequestDtoListe = listOf(
        GrunnlagRequestDto(
          type = GrunnlagRequestType.UTVIDET_BARNETRYGD_OG_SMAABARNSTILLEGG,
          personId = "12345678910",
          periodeFra = LocalDate.parse("2021-01-01"),
          periodeTil = LocalDate.parse("2022-01-01")
        )
      )
    )

    fun byggOppdaterGrunnlagspakkeRequestBarnetillegg() = OppdaterGrunnlagspakkeRequestDto(
      grunnlagRequestDtoListe = listOf(
        GrunnlagRequestDto(
          type = GrunnlagRequestType.BARNETILLEGG,
          personId = "12345678910",
          periodeFra = LocalDate.parse("2021-01-01"),
          periodeTil = LocalDate.parse("2022-01-01")
        )
      )
    )

    fun byggOppdaterGrunnlagspakkeRequestKontantstotte() = OppdaterGrunnlagspakkeRequestDto(
      grunnlagRequestDtoListe = listOf(
        GrunnlagRequestDto(
          type = GrunnlagRequestType.KONTANTSTOTTE,
          personId = "12345678910",
          periodeFra = LocalDate.parse("2022-01-01"),
          periodeTil = LocalDate.parse("2023-01-01")
        )
      )
    )

    fun byggGrunnlagspakke() = Grunnlagspakke(
      grunnlagspakkeId = (1..100).random(),
      opprettetAv = "RTV9999",
      opprettetTimestamp = LocalDateTime.now(),
      endretTimestamp = LocalDateTime.now(),
      gyldigTil = null,
      formaal = Formaal.BIDRAG.toString()
    )

    fun byggAinntektBo() = AinntektBo(
      grunnlagspakkeId = (1..100).random(),
      personId = "1234567",
      periodeFra = LocalDate.parse("2021-07-01"),
      periodeTil = LocalDate.parse("2021-08-01"),
      aktiv = true,
      brukFra = LocalDateTime.now(),
      brukTil = null,
      hentetTidspunkt = LocalDateTime.now()
    )

    fun byggAinntekt() = Ainntekt(
      inntektId = (1..100).random(),
      grunnlagspakkeId = (1..100).random(),
      personId = "1234567",
      periodeFra = LocalDate.parse("2021-07-01"),
      periodeTil = LocalDate.parse("2021-08-01"),
      aktiv = true,
      brukFra = LocalDateTime.now(),
      brukTil = null,
      hentetTidspunkt = LocalDateTime.now()
    )

    fun byggAinntektspostBo() = AinntektspostBo(
      inntektId = (1..100).random(),
      utbetalingsperiode = "202108",
      opptjeningsperiodeFra = LocalDate.parse("2021-07-01"),
      opptjeningsperiodeTil = LocalDate.parse("2021-08-01"),
      opplysningspliktigId = "123",
      virksomhetId = null,
      inntektType = "Loenn",
      fordelType = "Kontantytelse",
      beskrivelse = "Loenn/ferieLoenn",
      belop = BigDecimal.valueOf(50000),
      etterbetalingsperiodeFra = LocalDate.of(2021, 10, 1),
      etterbetalingsperiodeTil = LocalDate.of(2021, 11, 1)
    )

    fun byggAinntektspost() = Ainntektspost(
      inntektspostId = (1..100).random(),
      inntektId = (1..100).random(),
      utbetalingsperiode = "202108",
      opptjeningsperiodeFra = LocalDate.parse("2021-07-01"),
      opptjeningsperiodeTil = LocalDate.parse("2021-08-01"),
      opplysningspliktigId = "123",
      virksomhetId = null,
      inntektType = "Loenn",
      fordelType = "Kontantytelse",
      beskrivelse = "Loenn/ferieLoenn",
      belop = BigDecimal.valueOf(50000),
      etterbetalingsperiodeFra = LocalDate.of(2021, 10, 1),
      etterbetalingsperiodeTil = LocalDate.of(2021, 11, 1)
    )

    fun byggSkattegrunnlagSkattBo() = SkattegrunnlagBo(
      grunnlagspakkeId = (1..100).random(),
      personId = "7654321",
      periodeFra = LocalDate.parse("2021-01-01"),
      periodeTil = LocalDate.parse("2022-01-01"),
      aktiv = true,
      brukFra = LocalDateTime.now(),
      brukTil = null,
      hentetTidspunkt = LocalDateTime.now()
    )

    fun byggSkattegrunnlagSkatt() = no.nav.bidrag.grunnlag.persistence.entity.Skattegrunnlag(
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

    fun byggSkattegrunnlagspostBo() = SkattegrunnlagspostBo(
      skattegrunnlagId = (1..100).random(),
      skattegrunnlagType = SkattegrunnlagType.ORDINAER.toString(),
      inntektType = "Loenn",
      belop = BigDecimal.valueOf(171717),
    )

    fun byggSkattegrunnlagspost() = Skattegrunnlagspost(
      skattegrunnlagspostId = (1..100).random(),
      skattegrunnlagId = (1..100).random(),
      skattegrunnlagType = SkattegrunnlagType.ORDINAER.toString(),
      inntektType = "Loenn",
      belop = BigDecimal.valueOf(171717),
    )

    fun byggUtvidetBarnetrygdOgSmaabarnstilleggBo() = UtvidetBarnetrygdOgSmaabarnstilleggBo(
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
      deltBosted = false,
      hentetTidspunkt = LocalDateTime.now()
    )

    fun byggUtvidetBarnetrygdOgSmaabarnstillegg() = UtvidetBarnetrygdOgSmaabarnstillegg(
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
      deltBosted = false,
      hentetTidspunkt = LocalDateTime.now()
    )

    fun byggBarnetilleggBo() = BarnetilleggBo(
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
      barnType = BarnType.FELLES.toString(),
      hentetTidspunkt = LocalDateTime.now()
    )

    fun byggBarnetillegg() = Barnetillegg(
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
      barnType = BarnType.FELLES.toString(),
      hentetTidspunkt = LocalDateTime.now()
    )

    fun byggKontantstotteBo() = KontantstotteBo(
      grunnlagspakkeId = (1..100).random(),
      partPersonId = "1234567",
      barnPersonId = "0123456",
      periodeFra = LocalDate.parse("2021-01-01"),
      periodeTil = LocalDate.parse("2021-07-01"),
      aktiv = true,
      brukFra = LocalDateTime.now(),
      brukTil = null,
      belop = 7500,
      hentetTidspunkt = LocalDateTime.now()
    )

    fun byggKontantstotte() = Kontantstotte(
      kontantstotteId = (1..100).random(),
      grunnlagspakkeId = (1..100).random(),
      partPersonId = "1234567",
      barnPersonId = "0123456",
      periodeFra = LocalDate.parse("2021-01-01"),
      periodeTil = LocalDate.parse("2021-07-01"),
      aktiv = true,
      brukFra = LocalDateTime.now(),
      brukTil = null,
      belop = 7500,
      hentetTidspunkt = LocalDateTime.now()
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

    fun byggKontantstotteResponse() = KontantstotteResponse(
      immutableListOf(
        StonadDto(
          fnr = "12345678901",
          fom = YearMonth.parse("2022-01"),
          tom = YearMonth.parse("2022-07"),
          immutableListOf(
            UtbetalingDto(
              fom = YearMonth.parse("2022-01"),
              tom = YearMonth.parse("2022-07"),
              belop = 17
            )
          ),
          immutableListOf(
            no.nav.bidrag.grunnlag.consumer.infotrygdkontantstottev2.api.BarnDto(
              "11223344551"
            )
          )
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
        Aktoer("Testaktor", AktoerType.NATURLIG_IDENT),
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

    fun byggKontantstotteRequest() = KontantstotteRequest(
      listOf(
        "123"
      )
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
        else -> ObjectMapper().findAndRegisterModules()
          .readValue(mvcResult.response.contentAsString, responseType)
      }
    }
  }
}
