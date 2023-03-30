package no.nav.bidrag.grunnlag

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.behandling.felles.dto.grunnlag.GrunnlagRequestDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.OppdaterGrunnlagspakkeRequestDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.OpprettGrunnlagspakkeRequestDto
import no.nav.bidrag.behandling.felles.enums.BarnType
import no.nav.bidrag.behandling.felles.enums.Formaal
import no.nav.bidrag.behandling.felles.enums.GrunnlagRequestType
import no.nav.bidrag.behandling.felles.enums.SivilstandKode
import no.nav.bidrag.behandling.felles.enums.SkattegrunnlagType
import no.nav.bidrag.behandling.felles.enums.barnetilsyn.Skolealder
import no.nav.bidrag.grunnlag.bo.AinntektBo
import no.nav.bidrag.grunnlag.bo.AinntektspostBo
import no.nav.bidrag.grunnlag.bo.BarnetilleggBo
import no.nav.bidrag.grunnlag.bo.BarnetilsynBo
import no.nav.bidrag.grunnlag.bo.KontantstotteBo
import no.nav.bidrag.grunnlag.bo.RelatertPersonBo
import no.nav.bidrag.grunnlag.bo.SivilstandBo
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
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.ForelderBarnRelasjon
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.ForelderBarnRelasjonDto
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.ForelderBarnRelasjonRolle
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.Husstand
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.Husstandsmedlemmer
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.HusstandsmedlemmerDto
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.NavnFoedselDoedResponseDto
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.PersonRequest
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.SivilstandDto
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.BisysStønadstype
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.FamilieBaSakRequest
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.FamilieBaSakResponse
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.UtvidetBarnetrygdPeriode
import no.nav.bidrag.grunnlag.consumer.familieefsak.api.BarnetilsynBisysPerioder
import no.nav.bidrag.grunnlag.consumer.familieefsak.api.BarnetilsynRequest
import no.nav.bidrag.grunnlag.consumer.familieefsak.api.BarnetilsynResponse
import no.nav.bidrag.grunnlag.consumer.familieefsak.api.Periode
import no.nav.bidrag.grunnlag.consumer.familiekssak.api.BisysDto
import no.nav.bidrag.grunnlag.consumer.familiekssak.api.BisysResponsDto
import no.nav.bidrag.grunnlag.consumer.familiekssak.api.InfotrygdPeriode
import no.nav.bidrag.grunnlag.consumer.familiekssak.api.KsSakPeriode
import no.nav.bidrag.grunnlag.persistence.entity.Ainntekt
import no.nav.bidrag.grunnlag.persistence.entity.Ainntektspost
import no.nav.bidrag.grunnlag.persistence.entity.Barnetillegg
import no.nav.bidrag.grunnlag.persistence.entity.Barnetilsyn
import no.nav.bidrag.grunnlag.persistence.entity.Grunnlagspakke
import no.nav.bidrag.grunnlag.persistence.entity.Kontantstotte
import no.nav.bidrag.grunnlag.persistence.entity.RelatertPerson
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
            formaal = Formaal.BIDRAG
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
                    periodeTil = LocalDate.parse("2022-02-01")
                )
            )
        )

        fun byggOppdaterGrunnlagspakkeRequestEgneBarnIHusstanden() = OppdaterGrunnlagspakkeRequestDto(
            grunnlagRequestDtoListe = listOf(
                GrunnlagRequestDto(
                    type = GrunnlagRequestType.EGNE_BARN_I_HUSSTANDEN,
                    personId = "12345678910",
                    periodeFra = LocalDate.parse("2021-01-01"),
                    periodeTil = LocalDate.parse("2022-01-01")
                )
            )
        )

        fun byggOppdaterGrunnlagspakkeRequestHusstandsmedlemmer() = OppdaterGrunnlagspakkeRequestDto(
            grunnlagRequestDtoListe = listOf(
                GrunnlagRequestDto(
                    type = GrunnlagRequestType.HUSSTANDSMEDLEMMER,
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
                    periodeTil = LocalDate.parse("2023-07-01")
                )
            )
        )

        fun byggOppdaterGrunnlagspakkeRequestBarnetilsyn() = OppdaterGrunnlagspakkeRequestDto(
            grunnlagRequestDtoListe = listOf(
                GrunnlagRequestDto(
                    type = GrunnlagRequestType.BARNETILSYN,
                    personId = "12345678910",
                    periodeFra = LocalDate.parse("2022-01-01"),
                    periodeTil = LocalDate.parse("2023-01-01")
                )
            )
        )

        fun byggOppdaterGrunnlagspakkeRequestSivilstand() = OppdaterGrunnlagspakkeRequestDto(
            grunnlagRequestDtoListe = listOf(
                GrunnlagRequestDto(
                    type = GrunnlagRequestType.SIVILSTAND,
                    personId = "12345678910",
                    periodeFra = LocalDate.parse("2021-01-01"),
                    periodeTil = LocalDate.parse("2022-01-01")
                )
            )
        )

        fun byggOppdaterGrunnlagspakkeRequestAinntekt() = OppdaterGrunnlagspakkeRequestDto(
            grunnlagRequestDtoListe = listOf(
                GrunnlagRequestDto(
                    type = GrunnlagRequestType.AINNTEKT,
                    personId = "12345678910",
                    periodeFra = LocalDate.parse("2021-01-01"),
                    periodeTil = LocalDate.parse("2022-01-01")
                )
            )
        )

        fun byggOppdaterGrunnlagspakkeRequestAinntektTidligereEnn2015() = OppdaterGrunnlagspakkeRequestDto(
            grunnlagRequestDtoListe = listOf(
                GrunnlagRequestDto(
                    type = GrunnlagRequestType.AINNTEKT,
                    personId = "12345678910",
                    periodeFra = LocalDate.parse("2011-01-01"),
                    periodeTil = LocalDate.parse("2022-01-01")
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
            belop = BigDecimal.valueOf(171717)
        )

        fun byggSkattegrunnlagspost() = Skattegrunnlagspost(
            skattegrunnlagspostId = (1..100).random(),
            skattegrunnlagId = (1..100).random(),
            skattegrunnlagType = SkattegrunnlagType.ORDINAER.toString(),
            inntektType = "Loenn",
            belop = BigDecimal.valueOf(171717)
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

        // PDL-data
        fun byggRelatertPersonBo() = RelatertPersonBo(
            grunnlagspakkeId = (1..100).random(),
            partPersonId = "1234567",
            relatertPersonPersonId = "7654321",
            navn = "navn1",
            fodselsdato = LocalDate.parse("1997-05-23"),
            erBarnAvBmBp = true,
            husstandsmedlemPeriodeFra = LocalDate.parse("2021-01-01"),
            husstandsmedlemPeriodeTil = LocalDate.parse("2021-07-01"),
            aktiv = true,
            brukFra = LocalDateTime.now(),
            brukTil = null,
            hentetTidspunkt = LocalDateTime.now()
        )

        fun byggEgetBarnIHusstanden() = RelatertPerson(
            relatertPersonId = (1..100).random(),
            grunnlagspakkeId = (1..100).random(),
            partPersonId = "1234567",
            relatertPersonPersonId = "7654321",
            navn = "navn1Barn",
            fodselsdato = LocalDate.parse("2013-05-23"),
            erBarnAvBmBp = true,
            husstandsmedlemPeriodeFra = null,
            husstandsmedlemPeriodeTil = null,
            aktiv = true,
            brukFra = LocalDateTime.now(),
            brukTil = null,
            hentetTidspunkt = LocalDateTime.now()
        )

        fun byggHusstandsmedlem() = RelatertPerson(
            relatertPersonId = (1..100).random(),
            grunnlagspakkeId = (1..100).random(),
            partPersonId = "1234567",
            relatertPersonPersonId = "2582582",
            navn = "navn1Husstandsmedlem",
            fodselsdato = LocalDate.parse("1997-05-23"),
            erBarnAvBmBp = false,
            husstandsmedlemPeriodeFra = LocalDate.parse("2021-01-01"),
            husstandsmedlemPeriodeTil = LocalDate.parse("2021-07-01"),
            aktiv = true,
            brukFra = LocalDateTime.now(),
            brukTil = null,
            hentetTidspunkt = LocalDateTime.now()
        )

        fun byggSivilstandBo() = SivilstandBo(
            grunnlagspakkeId = (1..100).random(),
            personId = "1234",
            periodeFra = LocalDate.parse("2021-01-01"),
            periodeTil = LocalDate.parse("2021-07-01"),
            sivilstand = SivilstandKode.SAMBOER.toString(),
            aktiv = true,
            brukFra = LocalDateTime.now(),
            brukTil = null,
            hentetTidspunkt = LocalDateTime.now()
        )

        fun byggSivilstand() = no.nav.bidrag.grunnlag.persistence.entity.Sivilstand(
            sivilstandId = (1..100).random(),
            grunnlagspakkeId = (1..100).random(),
            personId = "1234",
            periodeFra = LocalDate.parse("2021-01-01"),
            periodeTil = LocalDate.parse("2021-07-01"),
            sivilstand = SivilstandKode.SAMBOER.toString(),
            aktiv = true,
            brukFra = LocalDateTime.now(),
            brukTil = null,
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
            barnPersonId = "11223344551",
            periodeFra = LocalDate.parse("2022-01-01"),
            periodeTil = LocalDate.parse("2023-01-01"),
            aktiv = true,
            brukFra = LocalDateTime.now(),
            brukTil = null,
            belop = 7500,
            hentetTidspunkt = LocalDateTime.now()
        )

        fun byggBarnetilsynBo() = BarnetilsynBo(
            grunnlagspakkeId = (1..100).random(),
            partPersonId = "1234567",
            barnPersonId = "0123456",
            periodeFra = LocalDate.parse("2021-01-01"),
            periodeTil = LocalDate.parse("2021-07-01"),
            aktiv = true,
            brukFra = LocalDateTime.now(),
            brukTil = null,
            belop = 7500,
            tilsynstype = null,
            skolealder = Skolealder.UNDER,
            hentetTidspunkt = LocalDateTime.now()
        )

        fun byggBarnetilsyn() = Barnetilsyn(
            barnetilsynId = (1..100).random(),
            grunnlagspakkeId = (1..100).random(),
            partPersonId = "1234567",
            barnPersonId = "0123456",
            periodeFra = LocalDate.parse("2021-01-01"),
            periodeTil = LocalDate.parse("2021-07-01"),
            aktiv = true,
            brukFra = LocalDateTime.now(),
            brukTil = null,
            belop = 7500,
            hentetTidspunkt = LocalDateTime.now(),
            tilsynstype = null,
            skolealder = Skolealder.UNDER
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

        fun byggKontantstotteResponse() = BisysResponsDto(
            immutableListOf(
                InfotrygdPeriode(
                    fomMåned = YearMonth.parse("2022-01"),
                    tomMåned = YearMonth.parse("2022-12"),
                    beløp = 15001,
                    immutableListOf(
                        "11223344551",
                        "15544332211"
                    )
                )
            ),
            immutableListOf(
                KsSakPeriode(
                    fomMåned = YearMonth.parse("2023-01"),
                    tomMåned = YearMonth.parse("2023-06"),
                    no.nav.bidrag.grunnlag.consumer.familiekssak.api.Barn(
                        5000,
                        "11223344551"
                    )
                )
            )
        )

        fun byggBarnetilsynResponse() = BarnetilsynResponse(
            immutableListOf(
                BarnetilsynBisysPerioder(
                    periode = Periode(
                        fom = LocalDate.parse("2021-01-01"),
                        tom = LocalDate.parse("2021-07-31")
                    ),
                    barnIdenter = immutableListOf("01012212345", "01011034543")
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
            byggArbeidsInntektMaanedListe(),
            Aktoer("", AktoerType.NATURLIG_IDENT)
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

        fun byggForelderBarnRequest() = PersonRequest("personIdent")

        fun byggHusstandsmedlemmerRequest() = PersonRequest("personIdent")

        fun byggSivilstandRequest() = PersonRequest("personIdent")

        fun byggKontantstotteRequest() = BisysDto(
            LocalDate.now(),
            listOf(
                "123"
            )
        )

        fun byggBarnetilsynRequest() = BarnetilsynRequest(
            "123",
            LocalDate.now()
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

        fun byggHentForelderBarnRelasjonerResponse() = ForelderBarnRelasjonDto(
            immutableListOf(
                ForelderBarnRelasjon(
                    relatertPersonsIdent = "111",
                    relatertPersonsRolle = ForelderBarnRelasjonRolle.BARN,
                    minRolleForPerson = ForelderBarnRelasjonRolle.FAR
                ),
                ForelderBarnRelasjon(
                    relatertPersonsIdent = "222",
                    relatertPersonsRolle = ForelderBarnRelasjonRolle.BARN,
                    minRolleForPerson = ForelderBarnRelasjonRolle.FAR
                ),
                ForelderBarnRelasjon(
                    relatertPersonsIdent = "333",
                    relatertPersonsRolle = ForelderBarnRelasjonRolle.BARN,
                    minRolleForPerson = ForelderBarnRelasjonRolle.FAR
                )
            )
        )

        fun byggHentNavnFoedselOgDoedResponse() = NavnFoedselDoedResponseDto(
            navn = "Dunkel Sol",
            foedselsdato = LocalDate.parse("2001-04-17"),
            foedselsaar = 2001,
            doedsdato = null
        )

        fun byggHentHusstandsmedlemmerResponse() = HusstandsmedlemmerDto(
            immutableListOf(
                Husstand(
                    gyldigFraOgMed = LocalDate.parse("2011-01-01"),
                    gyldigTilOgMed = LocalDate.parse("2011-10-01"),
                    adressenavn = "adressenavn1",
                    husnummer = "husnummer1",
                    husbokstav = "husbokstav1",
                    bruksenhetsnummer = "bruksenhetsnummer1",
                    postnummer = "postnr1",
                    bydelsnummer = "bydelsnummer1",
                    kommunenummer = "kommunenummer1",
                    matrikkelId = 12345,
                    immutableListOf(
                        Husstandsmedlemmer(
                            gyldigFraOgMed = LocalDate.parse("2011-01-01"),
                            gyldigTilOgMed = LocalDate.parse("2011-02-01"),
                            personId = "111",
                            fornavn = "fornavn1",
                            mellomnavn = "mellomnavn1",
                            etternavn = "etternavn1",
                            foedselsdato = LocalDate.parse("2001-04-17")
                        ),
                        Husstandsmedlemmer(
                            gyldigFraOgMed = LocalDate.parse("2011-05-17"),
                            gyldigTilOgMed = null,
                            personId = "111",
                            fornavn = "fornavn1",
                            mellomnavn = "mellomnavn1",
                            etternavn = "etternavn1",
                            foedselsdato = LocalDate.parse("2001-04-17")
                        ),
                        Husstandsmedlemmer(
                            gyldigFraOgMed = LocalDate.parse("2011-01-01"),
                            gyldigTilOgMed = LocalDate.parse("2011-12-01"),
                            personId = "333",
                            fornavn = "fornavn3",
                            mellomnavn = "mellomnavn3",
                            etternavn = "etternavn3",
                            foedselsdato = LocalDate.parse("2001-04-17")
                        ),
                        Husstandsmedlemmer(
                            gyldigFraOgMed = LocalDate.parse("2011-05-01"),
                            gyldigTilOgMed = LocalDate.parse("2011-06-01"),
                            personId = "444",
                            fornavn = "fornavn4",
                            mellomnavn = "mellomnavn4",
                            etternavn = "etternavn4",
                            foedselsdato = LocalDate.parse("1974-02-01")
                        )
                    )
                ),
                Husstand(
                    gyldigFraOgMed = LocalDate.parse("2011-10-01"),
                    gyldigTilOgMed = null,
                    adressenavn = "adressenavn2",
                    husnummer = "husnummer2",
                    husbokstav = "husbokstav2",
                    bruksenhetsnummer = "bruksenhetsnummer2",
                    postnummer = "postnr2",
                    bydelsnummer = "bydelsnummer2",
                    kommunenummer = "kommunenummer2",
                    matrikkelId = 54321,
                    immutableListOf(
                        Husstandsmedlemmer(
                            gyldigFraOgMed = LocalDate.parse("2018-01-01"),
                            gyldigTilOgMed = LocalDate.parse("2018-02-01"),
                            personId = "111",
                            fornavn = "fornavn1",
                            mellomnavn = "mellomnavn1",
                            etternavn = "etternavn1",
                            foedselsdato = LocalDate.parse("2001-04-17"),
                            doedsdato = null
                        ),
                        Husstandsmedlemmer(
                            gyldigFraOgMed = LocalDate.parse("2020-01-01"),
                            gyldigTilOgMed = null,
                            personId = "555",
                            fornavn = "fornavn5",
                            mellomnavn = "mellomnavn5",
                            etternavn = "etternavn5",
                            foedselsdato = LocalDate.parse("1985-07-17"),
                            doedsdato = null
                        )
                    )
                )
            )
        )

        fun byggHentSivilstandResponse() = SivilstandDto(
            immutableListOf(
                no.nav.bidrag.grunnlag.consumer.bidragperson.api.Sivilstand(
                    type = "ENSLIG",
                    gyldigFraOgMed = null,
                    bekreftelsesdato = null
                ),
                no.nav.bidrag.grunnlag.consumer.bidragperson.api.Sivilstand(
                    type = "SAMBOER",
                    gyldigFraOgMed = null,
                    bekreftelsesdato = LocalDate.parse("2021-01-01")
                ),
                no.nav.bidrag.grunnlag.consumer.bidragperson.api.Sivilstand(
                    type = "GIFT",
                    gyldigFraOgMed = LocalDate.parse("2021-09-01"),
                    bekreftelsesdato = null
                )
            )
        )

        fun <Request, Response> performRequest(
            mockMvc: MockMvc,
            method: HttpMethod,
            url: String,
            input: Request?,
            responseType: Class<Response>,
            expectedStatus: StatusResultMatchersDsl.() -> Unit
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
