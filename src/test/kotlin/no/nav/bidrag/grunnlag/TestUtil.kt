package no.nav.bidrag.grunnlag

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.grunnlag.api.grunnlagspakke.GrunnlagRequest
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OppdaterGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OpprettGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.ainntekt.ArbeidsInntektInformasjon
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.ainntekt.ArbeidsInntektMaaned
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.ainntekt.HentInntektListeResponse
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.ainntekt.HentInntektRequest
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.ainntekt.InntektListe
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.skatt.HentSkattegrunnlagRequest
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.skatt.HentSkattegrunnlagResponse
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.skatt.Skattegrunnlag
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.BisysStønadstype
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.FamilieBaSakRequest
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.FamilieBaSakResponse
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.UtvidetBarnetrygdPeriode
import no.nav.bidrag.grunnlag.dto.AinntektDto
import no.nav.bidrag.grunnlag.dto.AinntektspostDto
import no.nav.bidrag.grunnlag.dto.GrunnlagspakkeDto
import no.nav.bidrag.grunnlag.dto.SkattegrunnlagDto
import no.nav.bidrag.grunnlag.dto.SkattegrunnlagspostDto
import no.nav.bidrag.grunnlag.dto.UtvidetBarnetrygdOgSmaabarnstilleggDto
import no.nav.bidrag.grunnlag.service.Formaal
import no.nav.bidrag.grunnlag.service.Grunnlagstype
import no.nav.bidrag.grunnlag.service.SkattegrunnlagType
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

    fun byggOppdaterGrunnlagspakkeRequest() = OppdaterGrunnlagspakkeRequest(
      gyldigTil = LocalDate.parse("2021-08-01"),
      innsynHistoriskeInntekterDato = null,
      grunnlagRequestListe = listOf(
        GrunnlagRequest(
          grunnlagstype = Grunnlagstype.AINNTEKT,
          personId = "12345678910",
          periodeFra = LocalDate.parse("2021-01-01"),
          periodeTil = LocalDate.parse("2022-01-01")
        ),
        GrunnlagRequest(
          grunnlagstype = Grunnlagstype.SKATTEGRUNNLAG,
          personId = "12345678910",
          periodeFra = LocalDate.parse("2021-01-01"),
          periodeTil = LocalDate.parse("2022-01-01")
        ),
        GrunnlagRequest(
          grunnlagstype = Grunnlagstype.UTVIDETBARNETRYGDOGSMAABARNSTILLEGG,
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

    fun byggFamilieBaSakResponse() = FamilieBaSakResponse(
      byggUtvidetBarnetrygdPeriode()
    )

    private fun byggUtvidetBarnetrygdPeriode(): List<UtvidetBarnetrygdPeriode> {
      val utvidetBarnetrygdOgSmaabarnstilleggPeriode = UtvidetBarnetrygdPeriode(
        stønadstype = BisysStønadstype.UTVIDET,
        fomMåned = YearMonth.now(),
        tomMåned = YearMonth.now(),
        beløp = 1000.00,
        manueltBeregnet = false,
        deltBosted = false
      )
      return mutableListOf(utvidetBarnetrygdOgSmaabarnstilleggPeriode)
    }

    fun byggHentInntektRequest() = HentInntektRequest(
      ident = "ident",
      innsynHistoriskeInntekterDato = LocalDate.now(),
      maanedFom = LocalDate.now().toString(),
      maanedTom = LocalDate.now().toString(),
      ainntektsfilter = "BidragA-Inntekt",
      formaal = "Bidrag"
    )

    fun byggHentInntektListeResponse() = HentInntektListeResponse(byggArbeidsInntektMaanedListe())

    private fun byggArbeidsInntektMaanedListe() = immutableListOf(
      ArbeidsInntektMaaned(
        aarMaaned = "2021-01",
        ArbeidsInntektInformasjon(byggInntektListe())
      )
    )

    private fun byggInntektListe(): List<InntektListe> {
      return immutableListOf(
        InntektListe(
          inntektType = "LOENNSINNTEKT",
          beloep = 10000,
          fordel = null,
          inntektskilde = null,
          inntektsperiodetype = null,
          inntektsstatus = null,
          leveringstidspunkt = null,
          opptjeningsland = null,
          opptjeningsperiodeFom = null,
          opptjeningsperiodeTom = null,
          utbetaltIMaaned = null,
          opplysningspliktig = null,
          virksomhet = null,
          tilleggsinformasjon = null,
          inntektsmottaker = null,
          inngaarIGrunnlagForTrekk = false,
          utloeserArbeidsgiveravgift = false,
          informasjonsstatus = null,
          beskrivelse = null,
          skatteOgAvgiftsregel = null,
          antall = null
        )
      )
    }

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
        else -> ObjectMapper().readValue(mvcResult.response.contentAsString, responseType)
      }
    }
  }
}
