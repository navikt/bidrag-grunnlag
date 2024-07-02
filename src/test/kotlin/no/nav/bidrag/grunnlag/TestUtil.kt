package no.nav.bidrag.grunnlag

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.domene.enums.barnetilsyn.Skolealder
import no.nav.bidrag.domene.enums.grunnlag.GrunnlagRequestType
import no.nav.bidrag.domene.enums.inntekt.Inntektstype
import no.nav.bidrag.domene.enums.inntekt.Skattegrunnlagstype
import no.nav.bidrag.domene.enums.person.BarnType
import no.nav.bidrag.domene.enums.person.Familierelasjon
import no.nav.bidrag.domene.enums.person.SivilstandskodePDL
import no.nav.bidrag.domene.enums.vedtak.Formål
import no.nav.bidrag.domene.ident.Personident
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
import no.nav.bidrag.grunnlag.consumer.arbeidsforhold.api.Ansettelsesperiode
import no.nav.bidrag.grunnlag.consumer.arbeidsforhold.api.Arbeidsforhold
import no.nav.bidrag.grunnlag.consumer.arbeidsforhold.api.Arbeidssted
import no.nav.bidrag.grunnlag.consumer.arbeidsforhold.api.Identer
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.HusstandsmedlemmerRequest
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
import no.nav.bidrag.grunnlag.consumer.inntektskomponenten.api.Aktoer
import no.nav.bidrag.grunnlag.consumer.inntektskomponenten.api.ArbeidsInntektInformasjonIntern
import no.nav.bidrag.grunnlag.consumer.inntektskomponenten.api.ArbeidsInntektMaanedIntern
import no.nav.bidrag.grunnlag.consumer.inntektskomponenten.api.HentInntektListeRequest
import no.nav.bidrag.grunnlag.consumer.inntektskomponenten.api.HentInntektListeResponseIntern
import no.nav.bidrag.grunnlag.consumer.inntektskomponenten.api.InntektIntern
import no.nav.bidrag.grunnlag.consumer.inntektskomponenten.api.OpplysningspliktigIntern
import no.nav.bidrag.grunnlag.consumer.inntektskomponenten.api.TilleggsinformasjonDetaljerIntern
import no.nav.bidrag.grunnlag.consumer.inntektskomponenten.api.TilleggsinformasjonIntern
import no.nav.bidrag.grunnlag.consumer.inntektskomponenten.api.VirksomhetIntern
import no.nav.bidrag.grunnlag.consumer.pensjon.api.BarnetilleggPensjon
import no.nav.bidrag.grunnlag.consumer.pensjon.api.HentBarnetilleggPensjonRequest
import no.nav.bidrag.grunnlag.consumer.skattegrunnlag.api.HentSummertSkattegrunnlagRequest
import no.nav.bidrag.grunnlag.consumer.skattegrunnlag.api.HentSummertSkattegrunnlagResponse
import no.nav.bidrag.grunnlag.consumer.skattegrunnlag.api.Skattegrunnlag
import no.nav.bidrag.grunnlag.persistence.entity.Ainntekt
import no.nav.bidrag.grunnlag.persistence.entity.Ainntektspost
import no.nav.bidrag.grunnlag.persistence.entity.Barnetillegg
import no.nav.bidrag.grunnlag.persistence.entity.Barnetilsyn
import no.nav.bidrag.grunnlag.persistence.entity.Grunnlagspakke
import no.nav.bidrag.grunnlag.persistence.entity.Kontantstotte
import no.nav.bidrag.grunnlag.persistence.entity.RelatertPerson
import no.nav.bidrag.grunnlag.persistence.entity.Skattegrunnlagspost
import no.nav.bidrag.grunnlag.persistence.entity.UtvidetBarnetrygdOgSmaabarnstillegg
import no.nav.bidrag.grunnlag.service.PersonIdOgPeriodeRequest
import no.nav.bidrag.transport.behandling.grunnlag.request.GrunnlagRequestDto
import no.nav.bidrag.transport.behandling.grunnlag.request.OppdaterGrunnlagspakkeRequestDto
import no.nav.bidrag.transport.behandling.grunnlag.request.OpprettGrunnlagspakkeRequestDto
import no.nav.bidrag.transport.person.ForelderBarnRelasjon
import no.nav.bidrag.transport.person.ForelderBarnRelasjonDto
import no.nav.bidrag.transport.person.HentePersonidenterRequest
import no.nav.bidrag.transport.person.Husstand
import no.nav.bidrag.transport.person.Husstandsmedlem
import no.nav.bidrag.transport.person.HusstandsmedlemmerDto
import no.nav.bidrag.transport.person.Identgruppe
import no.nav.bidrag.transport.person.NavnFødselDødDto
import no.nav.bidrag.transport.person.PersonRequest
import no.nav.bidrag.transport.person.PersonidentDto
import no.nav.bidrag.transport.person.SivilstandPdlDto
import no.nav.bidrag.transport.person.SivilstandPdlHistorikkDto
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
import no.nav.tjenester.aordningen.inntektsinformasjon.tilleggsinformasjondetaljer.Etterbetalingsperiode
import no.nav.tjenester.aordningen.inntektsinformasjon.tilleggsinformasjondetaljer.TilleggsinformasjonDetaljerType
import okhttp3.internal.immutableListOf
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
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
            formaal = Formål.BIDRAG,
        )

        fun byggOppdaterGrunnlagspakkeRequestKomplett() = OppdaterGrunnlagspakkeRequestDto(
            grunnlagRequestDtoListe = listOf(
                GrunnlagRequestDto(
                    type = GrunnlagRequestType.AINNTEKT,
                    personId = "12345678910",
                    periodeFra = LocalDate.parse("2021-01-01"),
                    periodeTil = LocalDate.parse("2022-01-01"),
                ),
                GrunnlagRequestDto(
                    type = GrunnlagRequestType.SKATTEGRUNNLAG,
                    personId = "12345678910",
                    periodeFra = LocalDate.parse("2021-01-01"),
                    periodeTil = LocalDate.parse("2022-01-01"),
                ),
                GrunnlagRequestDto(
                    type = GrunnlagRequestType.UTVIDET_BARNETRYGD_OG_SMÅBARNSTILLEGG,
                    personId = "12345678910",
                    periodeFra = LocalDate.parse("2021-01-01"),
                    periodeTil = LocalDate.parse("2022-01-01"),
                ),
                GrunnlagRequestDto(
                    type = GrunnlagRequestType.BARNETILLEGG,
                    personId = "12345678910",
                    periodeFra = LocalDate.parse("2021-01-01"),
                    periodeTil = LocalDate.parse("2022-01-01"),
                ),
                GrunnlagRequestDto(
                    type = GrunnlagRequestType.KONTANTSTØTTE,
                    personId = "12345678910",
                    periodeFra = LocalDate.parse("2021-01-01"),
                    periodeTil = LocalDate.parse("2022-01-01"),
                ),
                GrunnlagRequestDto(
                    type = GrunnlagRequestType.BARNETILSYN,
                    personId = "12345678910",
                    periodeFra = LocalDate.parse("2021-01-01"),
                    periodeTil = LocalDate.parse("2022-01-01"),
                ),
            ),
        )

        fun byggOppdaterGrunnlagspakkeRequestAInntekt() = OppdaterGrunnlagspakkeRequestDto(
            grunnlagRequestDtoListe = listOf(
                GrunnlagRequestDto(
                    type = GrunnlagRequestType.AINNTEKT,
                    personId = "12345678910",
                    periodeFra = LocalDate.parse("2021-01-01"),
                    periodeTil = LocalDate.parse("2022-01-01"),
                ),
            ),
        )

        fun byggOppdaterGrunnlagspakkeRequestSkattegrunnlag() = OppdaterGrunnlagspakkeRequestDto(
            grunnlagRequestDtoListe = listOf(
                GrunnlagRequestDto(
                    type = GrunnlagRequestType.SKATTEGRUNNLAG,
                    personId = "12345678910",
                    periodeFra = LocalDate.parse("2021-01-01"),
                    periodeTil = LocalDate.parse("2022-01-01"),
                ),
            ),
        )

        fun byggOppdaterGrunnlagspakkeRequestUtvidetBarnetrygd() = OppdaterGrunnlagspakkeRequestDto(
            grunnlagRequestDtoListe = listOf(
                GrunnlagRequestDto(
                    type = GrunnlagRequestType.UTVIDET_BARNETRYGD_OG_SMÅBARNSTILLEGG,
                    personId = "12345678910",
                    periodeFra = LocalDate.parse("2021-01-01"),
                    periodeTil = LocalDate.parse("2022-01-01"),
                ),
            ),
        )

        fun byggOppdaterGrunnlagspakkeRequestBarnetillegg() = OppdaterGrunnlagspakkeRequestDto(
            grunnlagRequestDtoListe = listOf(
                GrunnlagRequestDto(
                    type = GrunnlagRequestType.BARNETILLEGG,
                    personId = "12345678910",
                    periodeFra = LocalDate.parse("2021-01-01"),
                    periodeTil = LocalDate.parse("2022-02-01"),
                ),
            ),
        )

        fun byggOppdaterGrunnlagspakkeRequestHusstandsmedlemmerOgEgneBarn() = OppdaterGrunnlagspakkeRequestDto(
            grunnlagRequestDtoListe = listOf(
                GrunnlagRequestDto(
                    type = GrunnlagRequestType.HUSSTANDSMEDLEMMER_OG_EGNE_BARN,
                    personId = "12345678910",
                    periodeFra = LocalDate.parse("2021-01-01"),
                    periodeTil = LocalDate.parse("2022-01-01"),
                ),
            ),
        )

        fun byggOppdaterGrunnlagspakkeRequestKontantstotte() = OppdaterGrunnlagspakkeRequestDto(
            grunnlagRequestDtoListe = listOf(
                GrunnlagRequestDto(
                    type = GrunnlagRequestType.KONTANTSTØTTE,
                    personId = "12345678910",
                    periodeFra = LocalDate.parse("2022-01-01"),
                    periodeTil = LocalDate.parse("2023-07-01"),
                ),
            ),
        )

        fun byggOppdaterGrunnlagspakkeRequestKontantstotteForToPersoner() = OppdaterGrunnlagspakkeRequestDto(
            grunnlagRequestDtoListe = listOf(
                GrunnlagRequestDto(
                    type = GrunnlagRequestType.KONTANTSTØTTE,
                    personId = "12345678910",
                    periodeFra = LocalDate.parse("2022-01-01"),
                    periodeTil = LocalDate.parse("2023-07-01"),
                ),
                GrunnlagRequestDto(
                    type = GrunnlagRequestType.KONTANTSTØTTE,
                    personId = "22345678910",
                    periodeFra = LocalDate.parse("2022-01-01"),
                    periodeTil = LocalDate.parse("2023-07-01"),
                ),
            ),
        )

        fun byggOppdaterGrunnlagspakkeRequestForTreIdenter() = OppdaterGrunnlagspakkeRequestDto(
            grunnlagRequestDtoListe = listOf(
                GrunnlagRequestDto(
                    type = GrunnlagRequestType.KONTANTSTØTTE,
                    personId = "12345678910",
                    periodeFra = LocalDate.parse("2022-01-01"),
                    periodeTil = LocalDate.parse("2023-07-01"),
                ),
                GrunnlagRequestDto(
                    type = GrunnlagRequestType.KONTANTSTØTTE,
                    personId = "22345678910",
                    periodeFra = LocalDate.parse("2022-01-01"),
                    periodeTil = LocalDate.parse("2023-07-01"),
                ),
                GrunnlagRequestDto(
                    type = GrunnlagRequestType.BARNETILSYN,
                    personId = "12345678910",
                    periodeFra = LocalDate.parse("2022-01-01"),
                    periodeTil = LocalDate.parse("2023-07-01"),
                ),
            ),
        )

        fun byggOppdaterGrunnlagspakkeRequestBarnetilsyn() = OppdaterGrunnlagspakkeRequestDto(
            grunnlagRequestDtoListe = listOf(
                GrunnlagRequestDto(
                    type = GrunnlagRequestType.BARNETILSYN,
                    personId = "12345678910",
                    periodeFra = LocalDate.parse("2022-01-01"),
                    periodeTil = LocalDate.parse("2023-01-01"),
                ),
            ),
        )

        fun byggOppdaterGrunnlagspakkeRequestSivilstand() = OppdaterGrunnlagspakkeRequestDto(
            grunnlagRequestDtoListe = listOf(
                GrunnlagRequestDto(
                    type = GrunnlagRequestType.SIVILSTAND,
                    personId = "12345678910",
                    periodeFra = LocalDate.parse("2021-01-01"),
                    periodeTil = LocalDate.parse("2022-01-01"),
                ),
            ),
        )

        fun byggGrunnlagspakke() = Grunnlagspakke(
            grunnlagspakkeId = (1..100).random(),
            opprettetAv = "RTV9999",
            opprettetTimestamp = LocalDateTime.now(),
            endretTimestamp = LocalDateTime.now(),
            gyldigTil = null,
            formaal = Formål.BIDRAG.toString(),
            opprettetAvNavn = "",
            kildeapplikasjon = "bisys",
        )

        fun byggAinntektBo() = AinntektBo(
            grunnlagspakkeId = (1..100).random(),
            personId = "1234567",
            periodeFra = LocalDate.parse("2021-07-01"),
            periodeTil = LocalDate.parse("2021-08-01"),
            aktiv = true,
            brukFra = LocalDateTime.now(),
            brukTil = null,
            hentetTidspunkt = LocalDateTime.now(),
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
            hentetTidspunkt = LocalDateTime.now(),
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
            etterbetalingsperiodeTil = LocalDate.of(2021, 11, 1),
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
            etterbetalingsperiodeTil = LocalDate.of(2021, 11, 1),
        )

        fun byggSkattegrunnlagSkattBo() = SkattegrunnlagBo(
            grunnlagspakkeId = (1..100).random(),
            personId = "7654321",
            periodeFra = LocalDate.parse("2021-01-01"),
            periodeTil = LocalDate.parse("2022-01-01"),
            aktiv = true,
            brukFra = LocalDateTime.now(),
            brukTil = null,
            hentetTidspunkt = LocalDateTime.now(),
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
            hentetTidspunkt = LocalDateTime.now(),
        )

        fun byggSkattegrunnlagspostBo() = SkattegrunnlagspostBo(
            skattegrunnlagId = (1..100).random(),
            skattegrunnlagType = Skattegrunnlagstype.ORDINÆR.toString(),
            inntektType = "Loenn",
            belop = BigDecimal.valueOf(171717),
        )

        fun byggSkattegrunnlagspost() = Skattegrunnlagspost(
            skattegrunnlagspostId = (1..100).random(),
            skattegrunnlagId = (1..100).random(),
            skattegrunnlagType = Skattegrunnlagstype.ORDINÆR.toString(),
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
            hentetTidspunkt = LocalDateTime.now(),
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
            hentetTidspunkt = LocalDateTime.now(),
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
            hentetTidspunkt = LocalDateTime.now(),
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
            hentetTidspunkt = LocalDateTime.now(),
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
            hentetTidspunkt = LocalDateTime.now(),
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
            hentetTidspunkt = LocalDateTime.now(),
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
            hentetTidspunkt = LocalDateTime.now(),
        )

        fun byggSivilstandBo() = SivilstandBo(
            grunnlagspakkeId = (1..100).random(),
            personId = "1234",
            periodeFra = LocalDate.parse("2021-01-01"),
            periodeTil = LocalDate.parse("2021-07-01"),
            sivilstand = SivilstandskodePDL.SEPARERT_PARTNER.toString(),
            aktiv = true,
            brukFra = LocalDateTime.now(),
            brukTil = null,
            hentetTidspunkt = LocalDateTime.now(),
        )

        fun byggSivilstand() = no.nav.bidrag.grunnlag.persistence.entity.Sivilstand(
            sivilstandId = (1..100).random(),
            grunnlagspakkeId = (1..100).random(),
            personId = "1234",
            periodeFra = LocalDate.parse("2021-01-01"),
            periodeTil = LocalDate.parse("2021-07-01"),
            sivilstand = SivilstandskodePDL.SEPARERT_PARTNER.toString(),
            aktiv = true,
            brukFra = LocalDateTime.now(),
            brukTil = null,
            hentetTidspunkt = LocalDateTime.now(),
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
            hentetTidspunkt = LocalDateTime.now(),
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
            hentetTidspunkt = LocalDateTime.now(),
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
            hentetTidspunkt = LocalDateTime.now(),
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
            skolealder = Skolealder.UNDER,
        )

        fun byggFamilieBaSakResponse() = FamilieBaSakResponse(
            immutableListOf(
                UtvidetBarnetrygdPeriode(
                    stønadstype = BisysStønadstype.UTVIDET,
                    fomMåned = YearMonth.parse("2021-01"),
                    tomMåned = YearMonth.parse("2021-12"),
                    beløp = 1000.11,
                    manueltBeregnet = false,
                    deltBosted = false,
                ),
                UtvidetBarnetrygdPeriode(
                    stønadstype = BisysStønadstype.UTVIDET,
                    fomMåned = YearMonth.parse("2022-01"),
                    tomMåned = YearMonth.parse("2022-12"),
                    beløp = 2000.22,
                    manueltBeregnet = false,
                    deltBosted = false,
                ),
            ),
        )

        fun byggKontantstotteResponse() = BisysResponsDto(
            immutableListOf(
                InfotrygdPeriode(
                    fomMåned = YearMonth.parse("2022-01"),
                    tomMåned = YearMonth.parse("2022-12"),
                    beløp = 15001,
                    immutableListOf(
                        "11223344551",
                        "15544332211",
                    ),
                ),
            ),
            immutableListOf(
                KsSakPeriode(
                    fomMåned = YearMonth.parse("2023-01"),
                    tomMåned = YearMonth.parse("2023-06"),
                    no.nav.bidrag.grunnlag.consumer.familiekssak.api.Barn(
                        5000,
                        "11223344551",
                    ),
                ),
            ),
        )

        fun byggBarnetilsynResponse() = BarnetilsynResponse(
            immutableListOf(
                BarnetilsynBisysPerioder(
                    periode = Periode(
                        fom = LocalDate.parse("2021-01-01"),
                        tom = LocalDate.parse("2021-07-31"),
                    ),
                    barnIdenter = immutableListOf("01012212345", "01011034543"),
                ),
            ),
        )

        fun byggArbeidsforholdResponse() = immutableListOf(
            Arbeidsforhold(
                ansettelsesdetaljer = emptyList(),
                ansettelsesperiode = Ansettelsesperiode(startdato = LocalDate.now(), sluttdato = null),
                arbeidssted = byggArbeidsstedResponse(),
                arbeidstaker = null,
                bruksperiode = null,
                navArbeidsforholdId = null,
                id = null,
                navVersjon = null,
                opplysningspliktig = null,
                opprettet = null,
                rapporteringsordning = null,
                sistBekreftet = null,
                sistEndret = null,
                type = null,
                permisjoner = emptyList(),
                permitteringer = emptyList(),
            ),

            Arbeidsforhold(
                ansettelsesdetaljer = emptyList(),
                ansettelsesperiode = Ansettelsesperiode(startdato = LocalDate.now(), sluttdato = null),
                arbeidssted = null,
                arbeidstaker = null,
                bruksperiode = null,
                navArbeidsforholdId = null,
                id = null,
                navVersjon = null,
                opplysningspliktig = null,
                opprettet = null,
                rapporteringsordning = null,
                sistBekreftet = null,
                sistEndret = null,
                type = null,
                permisjoner = emptyList(),
                permitteringer = emptyList(),
            ),
        )

        fun byggArbeidsstedResponse() = Arbeidssted(
            identer = listOf(Identer(ident = "orgnr1", type = "ORGANISASJONSNUMMER")),
            type = "Underenhet",
        )

        fun byggHentInntektListeRequest() = HentInntektListeRequest(
            ident = Aktoer(identifikator = "ident"),
            maanedFom = YearMonth.of(LocalDate.now().year, LocalDate.now().month),
            maanedTom = YearMonth.of(LocalDate.now().year, LocalDate.now().month),
            ainntektsfilter = "BidragA-Inntekt",
            formaal = "Bidrag",
        )

        fun byggHentInntektListeResponse() = HentInntektListeResponse(
            byggArbeidsInntektMaanedListe(),
            no.nav.tjenester.aordningen.inntektsinformasjon.Aktoer("", AktoerType.NATURLIG_IDENT),
        )

        fun byggHentInntektListeResponseIntern() = byggArbeidsInntektMaanedListeIntern(HttpStatus.OK, byggArbeidsInntektMaanedListe())

        private fun byggArbeidsInntektMaanedListeIntern(
            httpStatus: HttpStatus,
            eksternRespons: List<ArbeidsInntektMaaned>,
        ): HentInntektListeResponseIntern {
            val arbeidsInntektMaanedListe = mutableListOf<ArbeidsInntektMaanedIntern>()

            eksternRespons.forEach { arbeidsInntektMaaned ->
                val inntektInternListe = mutableListOf<InntektIntern>()
                arbeidsInntektMaaned.arbeidsInntektInformasjon?.inntektListe?.forEach { inntekt ->
                    val inntektIntern = InntektIntern(
                        inntektType = inntekt.inntektType.toString(),
                        beloep = inntekt.beloep,
                        fordel = inntekt.fordel,
                        inntektsperiodetype = inntekt.inntektsperiodetype,
                        opptjeningsperiodeFom = inntekt.opptjeningsperiodeFom,
                        opptjeningsperiodeTom = inntekt.opptjeningsperiodeTom,
                        utbetaltIMaaned = inntekt.utbetaltIMaaned?.toString(),
                        opplysningspliktig = OpplysningspliktigIntern(
                            inntekt.opplysningspliktig?.identifikator,
                            inntekt.opplysningspliktig?.aktoerType.toString(),
                        ),
                        virksomhet = VirksomhetIntern(
                            inntekt.virksomhet?.identifikator,
                            inntekt.virksomhet?.aktoerType.toString(),
                        ),
                        tilleggsinformasjon = if (inntekt?.tilleggsinformasjon?.tilleggsinformasjonDetaljer?.detaljerType ==
                            TilleggsinformasjonDetaljerType.ETTERBETALINGSPERIODE
                        ) {
                            TilleggsinformasjonIntern(
                                inntekt.tilleggsinformasjon.kategori,
                                TilleggsinformasjonDetaljerIntern(
                                    (inntekt.tilleggsinformasjon?.tilleggsinformasjonDetaljer as Etterbetalingsperiode).etterbetalingsperiodeFom,
                                    (inntekt.tilleggsinformasjon?.tilleggsinformasjonDetaljer as Etterbetalingsperiode).etterbetalingsperiodeTom.plusDays(
                                        1,
                                    ),
                                ),
                            )
                        } else {
                            null
                        },
                        beskrivelse = inntekt.beskrivelse,
                    )
                    inntektInternListe.add(inntektIntern)
                }
                arbeidsInntektMaanedListe.add(
                    ArbeidsInntektMaanedIntern(
                        arbeidsInntektMaaned.aarMaaned.toString(),
                        ArbeidsInntektInformasjonIntern(inntektInternListe),
                    ),
                )
            }
            return HentInntektListeResponseIntern(httpStatus, "", arbeidsInntektMaanedListe, false)
        }

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
                        fradragListe,
                    ),
                ),
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
                no.nav.tjenester.aordningen.inntektsinformasjon.Aktoer("Testaktor", AktoerType.NATURLIG_IDENT),
                null,
                null,
                false,
                false,
                null,
                null,
                null,
                null,
            ),
        )

        fun byggHentSkattegrunnlagRequest() = HentSummertSkattegrunnlagRequest(
            inntektsAar = "2021",
            inntektsFilter = "inntektsfilter",
            personId = "personId",
        )

        fun byggHentSkattegrunnlagResponse() = HentSummertSkattegrunnlagResponse(
            grunnlag = byggSkattegrunnlagListe(),
            svalbardGrunnlag = byggSkattegrunnlagListe(),
            skatteoppgjoersdato = LocalDate.now().toString(),
        )

        private fun byggSkattegrunnlagListe() = immutableListOf(
            Skattegrunnlag(
                beloep = "100000",
                tekniskNavn = "tekniskNavn",
            ),
        )

        fun byggSkattegrunnlagRepositoryListeForEnIdent() = immutableListOf(
            no.nav.bidrag.grunnlag.persistence.entity.Skattegrunnlag(
                skattegrunnlagId = 1,
                grunnlagspakkeId = 1,
                personId = "12345678901",
            ),
        )

        fun byggSkattegrunnlagRepositoryListeForToIdenter() = immutableListOf(
            no.nav.bidrag.grunnlag.persistence.entity.Skattegrunnlag(
                skattegrunnlagId = 1,
                grunnlagspakkeId = 1,
                personId = "12345678901",
            ),
            no.nav.bidrag.grunnlag.persistence.entity.Skattegrunnlag(
                skattegrunnlagId = 1,
                grunnlagspakkeId = 1,
                personId = "12345678902",
            ),
        )

        fun byggSkattegrunnlagspostRepositoryListe() = immutableListOf(
            Skattegrunnlagspost(
                skattegrunnlagspostId = 1,
                skattegrunnlagId = 1,
                skattegrunnlagType = Skattegrunnlagstype.ORDINÆR.toString(),
                inntektType = Inntektstype.LØNNSINNTEKT.toString(),
                belop = BigDecimal.valueOf(10000),
            ),
        )

        fun byggFamilieBaSakRequest() = FamilieBaSakRequest(
            personIdent = "personident",
            fraDato = LocalDate.now(),
        )

        fun byggHusstandsmedlemmerRequest() = HusstandsmedlemmerRequest(PersonRequest(Personident("personident")), LocalDate.now())

        fun byggSivilstandRequest() = PersonRequest(Personident("personident"))

        fun byggHentPersonidenterRequest() = HentePersonidenterRequest("personident", setOf(Identgruppe.FOLKEREGISTERIDENT), true)

        fun byggKontantstotteRequest() = BisysDto(
            LocalDate.now(),
            listOf(
                "123",
            ),
        )

        fun byggBarnetilsynRequest() = BarnetilsynRequest(
            "123",
            LocalDate.now(),
        )

        fun byggHentBarnetilleggPensjonRequest() = HentBarnetilleggPensjonRequest(
            mottaker = "personident",
            fom = LocalDate.now(),
            tom = LocalDate.now(),
        )

        fun byggHentBarnetilleggPensjonResponse() = listOf(
            BarnetilleggPensjon(
                barn = "barnIdent",
                beloep = BigDecimal.valueOf(1000.11),
                fom = LocalDate.parse("2021-01-01"),
                tom = LocalDate.parse("2021-12-31"),
                erFellesbarn = true,
            ),
            BarnetilleggPensjon(
                barn = "barnIdent",
                beloep = BigDecimal.valueOf(2000.22),
                fom = LocalDate.parse("2022-01-01"),
                tom = LocalDate.parse("2022-12-31"),
                erFellesbarn = true,
            ),
        )

        fun byggHentForelderBarnRelasjonerResponse() = ForelderBarnRelasjonDto(
            immutableListOf(
                ForelderBarnRelasjon(
                    relatertPersonsIdent = Personident("111"),
                    relatertPersonsRolle = Familierelasjon.BARN,
                    minRolleForPerson = Familierelasjon.FAR,
                ),
                ForelderBarnRelasjon(
                    relatertPersonsIdent = Personident("222"),
                    relatertPersonsRolle = Familierelasjon.BARN,
                    minRolleForPerson = Familierelasjon.FAR,
                ),
                ForelderBarnRelasjon(
                    relatertPersonsIdent = Personident("333"),
                    relatertPersonsRolle = Familierelasjon.BARN,
                    minRolleForPerson = Familierelasjon.FAR,
                ),
                ForelderBarnRelasjon(
                    relatertPersonsIdent = Personident("999"),
                    relatertPersonsRolle = Familierelasjon.MOR,
                    minRolleForPerson = Familierelasjon.BARN,
                ),
            ),
        )

        fun byggHentForelderBarnRelasjonerForBarn() = ForelderBarnRelasjonDto(
            immutableListOf(
                ForelderBarnRelasjon(
                    relatertPersonsIdent = Personident("123456"),
                    relatertPersonsRolle = Familierelasjon.FAR,
                    minRolleForPerson = Familierelasjon.BARN,
                ),
                ForelderBarnRelasjon(
                    relatertPersonsIdent = Personident("888"),
                    relatertPersonsRolle = Familierelasjon.MOR,
                    minRolleForPerson = Familierelasjon.BARN,
                ),
            ),
        )

        fun byggHentNavnFoedselOgDoedResponse() = NavnFødselDødDto(
            navn = "Dunkel Sol",
            fødselsdato = LocalDate.parse("2001-04-17"),
            fødselsår = 2001,
            dødsdato = null,
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
                        Husstandsmedlem(
                            gyldigFraOgMed = LocalDate.parse("2011-01-01"),
                            gyldigTilOgMed = LocalDate.parse("2011-02-01"),
                            personId = Personident("111"),
                            navn = "fornavn1 mellomnavn1 etternavn1",
                            fødselsdato = LocalDate.parse("2001-04-17"),
                        ),
                        Husstandsmedlem(
                            gyldigFraOgMed = LocalDate.parse("2011-05-17"),
                            gyldigTilOgMed = LocalDate.parse("2018-01-01"),
                            personId = Personident("111"),
                            navn = "fornavn1 mellomnavn1 etternavn1",
                            fødselsdato = LocalDate.parse("2001-04-17"),
                        ),
                        Husstandsmedlem(
                            gyldigFraOgMed = LocalDate.parse("2011-01-01"),
                            gyldigTilOgMed = LocalDate.parse("2011-12-01"),
                            personId = Personident("333"),
                            navn = "fornavn3 mellomnavn3 etternavn3",
                            fødselsdato = LocalDate.parse("2001-04-17"),
                        ),
                        Husstandsmedlem(
                            gyldigFraOgMed = LocalDate.parse("2011-05-01"),
                            gyldigTilOgMed = LocalDate.parse("2011-06-01"),
                            personId = Personident("444"),
                            navn = "fornavn4 mellomnavn4 etternavn4",
                            fødselsdato = LocalDate.parse("1974-02-01"),
                        ),
                    ),
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
                        Husstandsmedlem(
                            gyldigFraOgMed = LocalDate.parse("2018-01-01"),
                            gyldigTilOgMed = null,
                            personId = Personident("111"),
                            navn = "fornavn1 mellomnavn1 etternavn1",
                            fødselsdato = LocalDate.parse("2001-04-17"),
                            dødsdato = null,
                        ),
                        Husstandsmedlem(
                            gyldigFraOgMed = LocalDate.parse("2020-01-01"),
                            gyldigTilOgMed = null,
                            personId = Personident("555"),
                            navn = "fornavn5 mellomnavn5 etternavn5",
                            fødselsdato = LocalDate.parse("1985-07-17"),
                            dødsdato = null,
                        ),
                    ),
                ),
            ),
        )

        fun byggHentHusstandsmedlemmerResponseTestPerioder() = HusstandsmedlemmerDto(
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
                        Husstandsmedlem(
                            gyldigFraOgMed = LocalDate.parse("2011-01-01"),
                            gyldigTilOgMed = LocalDate.parse("2011-02-01"),
                            personId = Personident("111"),
                            navn = "fornavn1 mellomnavn1 etternavn1",
                            fødselsdato = LocalDate.parse("2001-04-17"),
                        ),
                        Husstandsmedlem(
                            gyldigFraOgMed = LocalDate.parse("2011-05-17"),
                            gyldigTilOgMed = LocalDate.parse("2018-01-01"),
                            personId = Personident("111"),
                            navn = "fornavn1 mellomnavn1 etternavn1",
                            fødselsdato = LocalDate.parse("2001-04-17"),
                        ),
                        Husstandsmedlem(
                            gyldigFraOgMed = LocalDate.parse("2011-01-01"),
                            gyldigTilOgMed = LocalDate.parse("2011-12-01"),
                            personId = Personident("333"),
                            navn = "fornavn3 mellomnavn3 etternavn3",
                            fødselsdato = LocalDate.parse("2001-04-17"),
                        ),
                        Husstandsmedlem(
                            gyldigFraOgMed = LocalDate.parse("2011-05-01"),
                            gyldigTilOgMed = LocalDate.parse("2011-06-01"),
                            personId = Personident("444"),
                            navn = "fornavn4 mellomnavn4 etternavn4",
                            fødselsdato = LocalDate.parse("1974-02-01"),
                        ),
                    ),
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
                        Husstandsmedlem(
                            gyldigFraOgMed = LocalDate.parse("2018-01-01"),
                            gyldigTilOgMed = LocalDate.parse("2021-04-17"),
                            personId = Personident("111"),
                            navn = "fornavn1 mellomnavn1 etternavn1",
                            fødselsdato = LocalDate.parse("2001-04-17"),
                            dødsdato = null,
                        ),
                        Husstandsmedlem(
                            gyldigFraOgMed = LocalDate.parse("2022-04-17"),
                            gyldigTilOgMed = null,
                            personId = Personident("111"),
                            navn = "fornavn1 mellomnavn1 etternavn1",
                            fødselsdato = LocalDate.parse("2001-04-17"),
                            dødsdato = null,
                        ),
                        Husstandsmedlem(
                            gyldigFraOgMed = LocalDate.parse("2020-01-01"),
                            gyldigTilOgMed = null,
                            personId = Personident("555"),
                            navn = "fornavn5 mellomnavn5 etternavn5",
                            fødselsdato = LocalDate.parse("1985-07-17"),
                            dødsdato = null,
                        ),
                    ),
                ),
            ),
        )

        fun byggHentToHusstandsmedlemmer() = HusstandsmedlemmerDto(
            immutableListOf(
                Husstand(
                    gyldigFraOgMed = LocalDate.parse("2023-09-04"),
                    gyldigTilOgMed = LocalDate.parse("2023-11-12"),
                    adressenavn = "adressenavn1",
                    husnummer = "husnummer1",
                    husbokstav = "husbokstav1",
                    bruksenhetsnummer = "bruksenhetsnummer1",
                    postnummer = "postnr1",
                    bydelsnummer = "bydelsnummer1",
                    kommunenummer = "kommunenummer1",
                    matrikkelId = 12345,
                    immutableListOf(
                        Husstandsmedlem(
                            gyldigFraOgMed = LocalDate.parse("2023-09-04"),
                            gyldigTilOgMed = LocalDate.parse("2023-11-29"),
                            personId = Personident("111"),
                            navn = "fornavn1 mellomnavn1 etternavn1",
                            fødselsdato = LocalDate.parse("2001-04-17"),
                        ),
                        Husstandsmedlem(
                            gyldigFraOgMed = LocalDate.parse("2015-09-04"),
                            gyldigTilOgMed = LocalDate.parse("2018-11-29"),
                            personId = Personident("666"),
                            navn = "fornavn1 mellomnavn1 etternavn1",
                            fødselsdato = LocalDate.parse("2001-04-17"),
                        ),
                    ),
                ),
            ),
        )

        fun byggHentHusstandsmedlemmerSærbidrag() = HusstandsmedlemmerDto(
            immutableListOf(
                Husstand(
                    gyldigFraOgMed = LocalDate.parse("2023-09-04"),
                    gyldigTilOgMed = LocalDate.parse("2023-11-12"),
                    adressenavn = "adressenavn1",
                    husnummer = "husnummer1",
                    husbokstav = "husbokstav1",
                    bruksenhetsnummer = "bruksenhetsnummer1",
                    postnummer = "postnr1",
                    bydelsnummer = "bydelsnummer1",
                    kommunenummer = "kommunenummer1",
                    matrikkelId = 12345,
                    immutableListOf(
                        Husstandsmedlem(
                            gyldigFraOgMed = LocalDate.parse("2023-09-04"),
                            gyldigTilOgMed = LocalDate.parse("2023-11-29"),
                            personId = Personident("111"),
                            navn = "fornavn1 mellomnavn1 etternavn1",
                            fødselsdato = LocalDate.parse("2001-04-17"),
                        ),
                        Husstandsmedlem(
                            gyldigFraOgMed = LocalDate.parse("2015-09-04"),
                            gyldigTilOgMed = LocalDate.parse("2024-11-29"),
                            personId = Personident("666"),
                            navn = "fornavn1 mellomnavn1 etternavn1",
                            fødselsdato = LocalDate.parse("2001-04-17"),
                        ),
                        Husstandsmedlem(
                            gyldigFraOgMed = LocalDate.parse("2010-09-04"),
                            gyldigTilOgMed = null,
                            personId = Personident("777"),
                            navn = "Nåværende Ektefelle",
                            fødselsdato = LocalDate.parse("2000-01-17"),
                        ),
                        Husstandsmedlem(
                            gyldigFraOgMed = LocalDate.parse("2012-09-04"),
                            gyldigTilOgMed = null,
                            personId = Personident("888"),
                            navn = "Mor Til Felles Barn",
                            fødselsdato = LocalDate.parse("2004-04-17"),
                        ),
                        Husstandsmedlem(
                            gyldigFraOgMed = LocalDate.parse("2012-09-04"),
                            gyldigTilOgMed = null,
                            personId = Personident("999"),
                            navn = "Mor Til BP",
                            fødselsdato = LocalDate.parse("1950-04-17"),
                        ),
                    ),
                ),
            ),
        )

        fun byggHentSivilstandResponse() = SivilstandPdlHistorikkDto(
            immutableListOf(
                SivilstandPdlDto(
                    type = SivilstandskodePDL.SEPARERT_PARTNER,
                    gyldigFom = null,
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = LocalDateTime.parse("2021-01-01T12:00:00"),
                    historisk = true,
                ),
                SivilstandPdlDto(
                    type = SivilstandskodePDL.ENKE_ELLER_ENKEMANN,
                    gyldigFom = null,
                    bekreftelsesdato = LocalDate.parse("2021-02-01"),
                    master = "PDL",
                    registrert = LocalDateTime.parse("2021-02-01T12:00:00"),
                    historisk = true,
                ),
                SivilstandPdlDto(
                    type = SivilstandskodePDL.GJENLEVENDE_PARTNER,
                    gyldigFom = LocalDate.parse("2021-09-01"),
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = LocalDateTime.parse("2021-03-01T12:00:00"),
                    historisk = false,
                ),
            ),
        )

        fun byggHentSivilstandResponseTestSortering() = SivilstandPdlHistorikkDto(
            immutableListOf(
                SivilstandPdlDto(
                    type = SivilstandskodePDL.GIFT,
                    gyldigFom = LocalDate.parse("2017-07-17"),
                    bekreftelsesdato = LocalDate.parse("2016-06-16"),
                    master = "PDL",
                    registrert = LocalDateTime.now(),
                    historisk = true,
                ),
                SivilstandPdlDto(
                    type = SivilstandskodePDL.SKILT,
                    gyldigFom = null,
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = LocalDateTime.parse("2022-03-01T12:00:00"),
                    historisk = false,
                ),
                SivilstandPdlDto(
                    type = SivilstandskodePDL.UGIFT,
                    gyldigFom = null,
                    bekreftelsesdato = LocalDate.parse("2011-02-01"),
                    master = "PDL",
                    registrert = LocalDateTime.now(),
                    historisk = true,
                ),
                SivilstandPdlDto(
                    type = SivilstandskodePDL.UOPPGITT,
                    gyldigFom = null,
                    bekreftelsesdato = LocalDate.parse("2001-05-01"),
                    master = "PDL",
                    registrert = LocalDateTime.now(),
                    historisk = true,
                ),
                SivilstandPdlDto(
                    type = SivilstandskodePDL.SEPARERT,
                    gyldigFom = LocalDate.parse("2021-09-01"),
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = LocalDateTime.parse("2021-03-01T12:00:00"),
                    historisk = true,
                ),
            ),
        )

        fun byggHentSivilstandResponseTestUtenDatoerHistoriske() = SivilstandPdlHistorikkDto(
            immutableListOf(
                SivilstandPdlDto(
                    type = SivilstandskodePDL.SKILT,
                    gyldigFom = null,
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = null,
                    historisk = true,
                ),
                SivilstandPdlDto(
                    type = SivilstandskodePDL.GIFT,
                    gyldigFom = null,
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = null,
                    historisk = true,
                ),

            ),
        )

        fun byggHentSivilstandResponseTestUtenDatoer() = SivilstandPdlHistorikkDto(
            immutableListOf(
                SivilstandPdlDto(
                    type = SivilstandskodePDL.SKILT,
                    gyldigFom = null,
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = LocalDateTime.parse("2020-05-12T11:30:00"),
                    historisk = false,
                ),
                SivilstandPdlDto(
                    type = SivilstandskodePDL.GIFT,
                    gyldigFom = null,
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = LocalDateTime.parse("2017-03-01T12:00:00"),
                    historisk = true,
                ),

            ),
        )

        fun byggHentSivilstandResponseTestUtenDatoerMedRegistrertEnForekomstHistorisk() = SivilstandPdlHistorikkDto(
            immutableListOf(
                SivilstandPdlDto(
                    type = SivilstandskodePDL.GIFT,
                    gyldigFom = null,
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = LocalDateTime.parse("2017-03-01T12:00:00"),
                    historisk = true,
                ),
            ),
        )

        fun byggHentSivilstandResponseTestUtenDatoerMedRegistrertEnForekomstAktiv() = SivilstandPdlHistorikkDto(
            immutableListOf(
                SivilstandPdlDto(
                    type = SivilstandskodePDL.GIFT,
                    gyldigFom = null,
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = LocalDateTime.parse("2017-03-01T12:00:00"),
                    historisk = false,
                ),
            ),
        )

        fun byggHentSivilstandResponseTestUtenDatoerUtenRegistrertEnForekomstHistorisk() = SivilstandPdlHistorikkDto(
            immutableListOf(
                SivilstandPdlDto(
                    type = SivilstandskodePDL.GIFT,
                    gyldigFom = null,
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = null,
                    historisk = true,
                ),
            ),
        )

        fun byggHentSivilstandMedRelatertVedSivilstand() = SivilstandPdlHistorikkDto(
            immutableListOf(
                SivilstandPdlDto(
                    type = SivilstandskodePDL.GIFT,
                    gyldigFom = LocalDate.parse("2017-03-01"),
                    relatertVedSivilstand = "777",
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = LocalDateTime.parse("2021-01-01T12:00:00"),
                    historisk = false,
                ),
                SivilstandPdlDto(
                    type = SivilstandskodePDL.ENKE_ELLER_ENKEMANN,
                    gyldigFom = null,
                    relatertVedSivilstand = null,
                    bekreftelsesdato = LocalDate.parse("2011-02-01"),
                    master = "PDL",
                    registrert = LocalDateTime.parse("2011-02-01T12:00:00"),
                    historisk = true,
                ),
            ),
        )

        fun byggHentPersonidenterResponse() = immutableListOf(
            PersonidentDto(
                ident = "personident",
                historisk = false,
                gruppe = Identgruppe.FOLKEREGISTERIDENT,
            ),
            PersonidentDto(
                ident = "personident_historisk",
                historisk = true,
                gruppe = Identgruppe.FOLKEREGISTERIDENT,
            ),
        )

        fun byggPersonIdOgPeriodeRequest() = PersonIdOgPeriodeRequest(
            personId = "personident",
            periodeFra = LocalDate.parse("2023-01-01"),
            periodeTil = LocalDate.parse("2024-01-01"),
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
