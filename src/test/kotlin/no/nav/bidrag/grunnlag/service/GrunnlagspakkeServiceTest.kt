package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.behandling.felles.dto.grunnlag.OpprettGrunnlagspakkeRequestDto
import no.nav.bidrag.behandling.felles.enums.BarnetilleggType
import no.nav.bidrag.behandling.felles.enums.Formaal
import no.nav.bidrag.behandling.felles.enums.SivilstandKode
import no.nav.bidrag.behandling.felles.enums.SkattegrunnlagType
import no.nav.bidrag.grunnlag.BidragGrunnlagTest
import no.nav.bidrag.grunnlag.TestUtil
import no.nav.bidrag.grunnlag.bo.AinntektBo
import no.nav.bidrag.grunnlag.bo.AinntektspostBo
import no.nav.bidrag.grunnlag.bo.BarnBo
import no.nav.bidrag.grunnlag.bo.BarnetilleggBo
import no.nav.bidrag.grunnlag.bo.ForelderBarnBo
import no.nav.bidrag.grunnlag.bo.HusstandBo
import no.nav.bidrag.grunnlag.bo.HusstandsmedlemBo
import no.nav.bidrag.grunnlag.bo.ForelderBo
import no.nav.bidrag.grunnlag.bo.SivilstandBo
import no.nav.bidrag.grunnlag.bo.SkattegrunnlagBo
import no.nav.bidrag.grunnlag.bo.SkattegrunnlagspostBo
import no.nav.bidrag.grunnlag.bo.UtvidetBarnetrygdOgSmaabarnstilleggBo
import no.nav.bidrag.grunnlag.persistence.repository.GrunnlagspakkeRepository
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@DisplayName("GrunnlagspakkeServiceTest")
@ActiveProfiles(BidragGrunnlagTest.TEST_PROFILE)
@SpringBootTest(
  classes = [BidragGrunnlagTest::class],
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@EnableMockOAuth2Server
@AutoConfigureWireMock(port = 0)
class GrunnlagspakkeServiceTest {

  @Autowired
  private lateinit var grunnlagspakkeRepository: GrunnlagspakkeRepository

  @Autowired
  private lateinit var grunnlagspakkeService: GrunnlagspakkeService

  @Autowired
  private lateinit var persistenceService: PersistenceService

  @BeforeEach
  fun `init`() {
    // Sletter alle forekomster
    grunnlagspakkeRepository.deleteAll()
  }

  @Test
  @Suppress("NonAsciiCharacters")
  fun `Test på å opprette ny grunnlagspakke`() {
    val opprettGrunnlagspakkeRequestDto = OpprettGrunnlagspakkeRequestDto(
      Formaal.FORSKUDD, "X123456"
    )

    val nyGrunnlagspakkeOpprettet =
      grunnlagspakkeService.opprettGrunnlagspakke(opprettGrunnlagspakkeRequestDto)

    assertAll(
      Executable { assertThat(nyGrunnlagspakkeOpprettet).isNotNull }
    )
  }

  @Test
  @Suppress("NonAsciiCharacters")
  fun `Test på å lukke en grunnlagspakke`() {
    val opprettGrunnlagspakkeRequestDto =
      OpprettGrunnlagspakkeRequestDto(Formaal.FORSKUDD, "X123456")
    val grunnlagspakkeIdOpprettet =
      grunnlagspakkeService.opprettGrunnlagspakke(opprettGrunnlagspakkeRequestDto)
    val lukketGrunnlagspakke = grunnlagspakkeService.lukkGrunnlagspakke(grunnlagspakkeIdOpprettet)

    assertAll(
      Executable { assertThat(grunnlagspakkeIdOpprettet).isNotNull },
      Executable { assertThat(lukketGrunnlagspakke).isEqualTo(grunnlagspakkeIdOpprettet) }
    )
  }

  @Test
  @Suppress("NonAsciiCharacters")
  fun `Test på å hente grunnlagspakke med aktive og inaktive inntekter + andre grunnlag`() {
    val opprettGrunnlagspakkeRequestDto =
      OpprettGrunnlagspakkeRequestDto(Formaal.FORSKUDD, "X123456")
    val grunnlagspakkeIdOpprettet =
      grunnlagspakkeService.opprettGrunnlagspakke(opprettGrunnlagspakkeRequestDto)

    val ainntektBo = AinntektBo(
      grunnlagspakkeId = grunnlagspakkeIdOpprettet,
      personId = "1234567",
      periodeFra = LocalDate.parse("2021-06-01"),
      periodeTil = LocalDate.parse("2021-07-01"),
      aktiv = true,
      brukFra = LocalDateTime.now(),
      brukTil = null,
      hentetTidspunkt = LocalDateTime.now()
    )

    val opprettetAinntekt = persistenceService.opprettAinntekt(ainntektBo)

    persistenceService.opprettAinntektspost(
      AinntektspostBo(
        inntektId = opprettetAinntekt.inntektId,
        utbetalingsperiode = "202106",
        opptjeningsperiodeFra = LocalDate.parse("2021-05-01"),
        opptjeningsperiodeTil = LocalDate.parse("2021-06-01"),
        opplysningspliktigId = "1234567890",
        virksomhetId = "222444666",
        inntektType = "Loenn",
        fordelType = "Kontantytelse",
        beskrivelse = "Loenn/fastloenn",
        belop = BigDecimal.valueOf(17000.01),
        etterbetalingsperiodeFra = LocalDate.of(2020, 9, 1),
        etterbetalingsperiodeTil = LocalDate.of(2020, 10, 1)
      )
    )
    persistenceService.opprettAinntektspost(
      AinntektspostBo(
        inntektId = opprettetAinntekt.inntektId,
        utbetalingsperiode = "202106",
        opptjeningsperiodeFra = LocalDate.parse("2020-01-01"),
        opptjeningsperiodeTil = LocalDate.parse("2021-01-01"),
        opplysningspliktigId = "1234567890",
        virksomhetId = "222444666",
        inntektType = "Loenn",
        fordelType = "Kontantytelse",
        beskrivelse = "Loenn/ferieLoenn",
        belop = BigDecimal.valueOf(50000.01),
        etterbetalingsperiodeFra = LocalDate.of(2020, 10, 1),
        etterbetalingsperiodeTil = LocalDate.of(2020, 11, 1)
      )
    )

    // tester at inntekt som er merket med aktiv = false ikke hentes
    val inaktivInntektDto = AinntektBo(
      grunnlagspakkeId = grunnlagspakkeIdOpprettet,
      personId = "1234567",
      periodeFra = LocalDate.parse("2020-06-01"),
      periodeTil = LocalDate.parse("2020-07-01"),
      aktiv = false,
      hentetTidspunkt = LocalDateTime.now(),
      brukFra = LocalDateTime.now(),
      brukTil = null
    )

    val inaktivAinntekt = persistenceService.opprettAinntekt(inaktivInntektDto)

    persistenceService.opprettAinntektspost(
      AinntektspostBo(
        inntektId = inaktivAinntekt.inntektId,
        utbetalingsperiode = "202006",
        opptjeningsperiodeFra = LocalDate.parse("2020-05-01"),
        opptjeningsperiodeTil = LocalDate.parse("2020-06-01"),
        opplysningspliktigId = "1234567890",
        virksomhetId = "222444666",
        inntektType = "Loenn",
        fordelType = "Kontantytelse",
        beskrivelse = "Loenn/fastloenn",
        belop = BigDecimal.valueOf(50000.01),
        etterbetalingsperiodeFra = LocalDate.of(2021, 10, 1),
        etterbetalingsperiodeTil = LocalDate.of(2021, 11, 1)
      )
    )

    // Legger inn inntekt for person nr 2
    val inntektDto2 = AinntektBo(
      grunnlagspakkeId = grunnlagspakkeIdOpprettet,
      personId = "999999",
      periodeFra = LocalDate.parse("2021-07-01"),
      periodeTil = LocalDate.parse("2021-08-01"),
      aktiv = true,
      hentetTidspunkt = LocalDateTime.now(),
      brukFra = LocalDateTime.now(),
      brukTil = null
    )

    val opprettetAinntekt2 = persistenceService.opprettAinntekt(inntektDto2)

    persistenceService.opprettAinntektspost(
      AinntektspostBo(
        inntektId = opprettetAinntekt2.inntektId,
        utbetalingsperiode = "202107",
        opptjeningsperiodeFra = LocalDate.parse("2021-06-01"),
        opptjeningsperiodeTil = LocalDate.parse("2021-07-01"),
        opplysningspliktigId = "9876543210",
        virksomhetId = "666444222",
        inntektType = "Loenn",
        fordelType = "Kontantytelse",
        beskrivelse = "Loenn/fastloenn",
        belop = BigDecimal.valueOf(666000.01),
        etterbetalingsperiodeFra = LocalDate.of(2021, 1, 1),
        etterbetalingsperiodeTil = LocalDate.of(2021, 2, 1)
      )
    )

    // Legger inn inntekt fra Skatt
    val skattegrunnlagBo = SkattegrunnlagBo(
      grunnlagspakkeId = grunnlagspakkeIdOpprettet,
      personId = "345678",
      periodeFra = LocalDate.parse("2021-01-01"),
      periodeTil = LocalDate.parse("2022-01-01"),
      aktiv = true,
      brukFra = LocalDateTime.now(),
      brukTil = null,
      hentetTidspunkt = LocalDateTime.now()
    )

    val opprettetSkattegrunnlag = persistenceService.opprettSkattegrunnlag(skattegrunnlagBo)

    persistenceService.opprettSkattegrunnlagspost(
      SkattegrunnlagspostBo(
        skattegrunnlagId = opprettetSkattegrunnlag.skattegrunnlagId,
        skattegrunnlagType = SkattegrunnlagType.ORDINAER.toString(),
        inntektType = "Loenn",
        belop = BigDecimal.valueOf(23456.01)
      )
    )

    // Legger inn utvidet barnetrygd og småbarnstillegg
    persistenceService.opprettUtvidetBarnetrygdOgSmaabarnstillegg(
      UtvidetBarnetrygdOgSmaabarnstilleggBo(
        grunnlagspakkeId = grunnlagspakkeIdOpprettet,
        personId = "22334455",
        type = "Utvidet barnetrygd",
        periodeFra = LocalDate.parse("2021-05-01"),
        periodeTil = LocalDate.parse("2021-06-01"),
        aktiv = true,
        brukFra = LocalDateTime.now(),
        brukTil = null,
        belop = BigDecimal.valueOf(12468.01),
        manueltBeregnet = false,
        deltBosted = false,
        hentetTidspunkt = LocalDateTime.now()
      )
    )

    // Legger inn barnetillegg
    persistenceService.opprettBarnetillegg(
      BarnetilleggBo(
        grunnlagspakkeId = grunnlagspakkeIdOpprettet,
        partPersonId = "22334455",
        barnPersonId = "1234567",
        barnetilleggType = BarnetilleggType.PENSJON.toString(),
        periodeFra = LocalDate.parse("2021-05-01"),
        periodeTil = LocalDate.parse("2021-06-01"),
        belopBrutto = BigDecimal.valueOf(1000.01)
      )
    )

    val grunnlagspakkeFunnet =
      grunnlagspakkeService.hentGrunnlagspakke(grunnlagspakkeIdOpprettet)

    assertAll(
      Executable { assertThat(grunnlagspakkeFunnet).isNotNull },
      Executable { assertThat(grunnlagspakkeFunnet.grunnlagspakkeId).isEqualTo(grunnlagspakkeIdOpprettet) },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe.size).isEqualTo(2) },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[0].personId).isEqualTo("1234567") },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[0].periodeFra).isEqualTo(LocalDate.parse("2021-06-01")) },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[0].periodeTil).isEqualTo(LocalDate.parse("2021-07-01")) },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[0].aktiv).isEqualTo(true) },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[0].brukTil).isNull() },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[1].personId).isEqualTo("999999") },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[1].periodeFra).isEqualTo(LocalDate.parse("2021-07-01")) },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[1].periodeTil).isEqualTo(LocalDate.parse("2021-08-01")) },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[1].aktiv).isEqualTo(true) },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[1].brukTil).isNull() },

      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe.size).isEqualTo(2) },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[0].utbetalingsperiode).isEqualTo("202106") },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[0].opptjeningsperiodeFra).isEqualTo(LocalDate.parse("2021-05-01")) },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[0].opptjeningsperiodeTil).isEqualTo(LocalDate.parse("2021-06-01")) },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[0].opplysningspliktigId).isEqualTo("1234567890") },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[0].virksomhetId).isEqualTo("222444666") },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[0].inntektType).isEqualTo("Loenn") },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[0].fordelType).isEqualTo("Kontantytelse") },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[0].beskrivelse).isEqualTo("Loenn/fastloenn") },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[0].belop).isEqualTo(BigDecimal.valueOf(17000.01)) },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[0].etterbetalingsperiodeFra).isEqualTo(LocalDate.parse("2020-09-01")) },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[0].etterbetalingsperiodeTil).isEqualTo(LocalDate.parse("2020-10-01")) },

      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[1].utbetalingsperiode).isEqualTo("202106") },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[1].opptjeningsperiodeFra).isEqualTo(LocalDate.parse("2020-01-01")) },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[1].opptjeningsperiodeTil).isEqualTo(LocalDate.parse("2021-01-01")) },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[1].opplysningspliktigId).isEqualTo("1234567890") },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[1].virksomhetId).isEqualTo("222444666") },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[1].inntektType).isEqualTo("Loenn") },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[1].fordelType).isEqualTo("Kontantytelse") },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[1].beskrivelse).isEqualTo("Loenn/ferieLoenn") },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[1].belop).isEqualTo(BigDecimal.valueOf(50000.01)) },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[1].etterbetalingsperiodeFra).isEqualTo(LocalDate.parse("2020-10-01")) },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[1].etterbetalingsperiodeTil).isEqualTo(LocalDate.parse("2020-11-01")) },

      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[1].ainntektspostListe.size).isEqualTo(1) },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[1].ainntektspostListe[0].utbetalingsperiode).isEqualTo("202107") },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[1].ainntektspostListe[0].opptjeningsperiodeFra).isEqualTo(LocalDate.parse("2021-06-01")) },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[1].ainntektspostListe[0].opptjeningsperiodeTil).isEqualTo(LocalDate.parse("2021-07-01")) },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[1].ainntektspostListe[0].opplysningspliktigId).isEqualTo("9876543210") },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[1].ainntektspostListe[0].virksomhetId).isEqualTo("666444222") },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[1].ainntektspostListe[0].inntektType).isEqualTo("Loenn") },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[1].ainntektspostListe[0].fordelType).isEqualTo("Kontantytelse") },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[1].ainntektspostListe[0].beskrivelse).isEqualTo("Loenn/fastloenn") },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[1].ainntektspostListe[0].belop).isEqualTo(BigDecimal.valueOf(666000.01)) },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[1].ainntektspostListe[0].etterbetalingsperiodeFra).isEqualTo(LocalDate.parse("2021-01-01")) },
      Executable { assertThat(grunnlagspakkeFunnet.ainntektListe[1].ainntektspostListe[0].etterbetalingsperiodeTil).isEqualTo(LocalDate.parse("2021-02-01")) },

      Executable { assertThat(grunnlagspakkeFunnet.skattegrunnlagListe[0].skattegrunnlagListe.size).isEqualTo(1) },
      Executable { assertThat(grunnlagspakkeFunnet.skattegrunnlagListe[0].personId).isEqualTo("345678") },
      Executable { assertThat(grunnlagspakkeFunnet.skattegrunnlagListe[0].skattegrunnlagListe[0].skattegrunnlagType).isEqualTo(SkattegrunnlagType.ORDINAER.toString())},
      Executable { assertThat(grunnlagspakkeFunnet.skattegrunnlagListe[0].skattegrunnlagListe[0].inntektType).isEqualTo("Loenn") },
      Executable { assertThat(grunnlagspakkeFunnet.skattegrunnlagListe[0].aktiv).isEqualTo(true) },
      Executable { assertThat(grunnlagspakkeFunnet.skattegrunnlagListe[0].brukTil).isNull() },
      Executable { assertThat(grunnlagspakkeFunnet.skattegrunnlagListe[0].skattegrunnlagListe[0].belop).isEqualTo(BigDecimal.valueOf(23456.01)) },

      Executable { assertThat(grunnlagspakkeFunnet.ubstListe.size).isEqualTo(1) },
      Executable { assertThat(grunnlagspakkeFunnet.ubstListe[0].personId).isEqualTo("22334455") },
      Executable { assertThat(grunnlagspakkeFunnet.ubstListe[0].type).isEqualTo("Utvidet barnetrygd") },
      Executable { assertThat(grunnlagspakkeFunnet.ubstListe[0].periodeFra).isEqualTo(LocalDate.parse("2021-05-01")) },
      Executable { assertThat(grunnlagspakkeFunnet.ubstListe[0].periodeTil).isEqualTo(LocalDate.parse("2021-06-01")) },
      Executable { assertThat(grunnlagspakkeFunnet.ubstListe[0].aktiv).isEqualTo(true) },
      Executable { assertThat(grunnlagspakkeFunnet.ubstListe[0].brukTil).isNull() },
      Executable { assertThat(grunnlagspakkeFunnet.ubstListe[0].belop).isEqualTo(BigDecimal.valueOf(12468.01)) },

      Executable { assertThat(grunnlagspakkeFunnet.barnetilleggListe.size).isEqualTo(1) },
      Executable { assertThat(grunnlagspakkeFunnet.barnetilleggListe[0].partPersonId).isEqualTo("22334455") },
      Executable { assertThat(grunnlagspakkeFunnet.barnetilleggListe[0].barnPersonId).isEqualTo("1234567") },
      Executable { assertThat(grunnlagspakkeFunnet.barnetilleggListe[0].barnetilleggType).isEqualTo(BarnetilleggType.PENSJON.toString()) },
      Executable { assertThat(grunnlagspakkeFunnet.barnetilleggListe[0].periodeFra).isEqualTo(LocalDate.parse("2021-05-01")) },
      Executable { assertThat(grunnlagspakkeFunnet.barnetilleggListe[0].periodeTil).isEqualTo(LocalDate.parse("2021-06-01")) },
      Executable { assertThat(grunnlagspakkeFunnet.barnetilleggListe[0].aktiv).isEqualTo(true) },
      Executable { assertThat(grunnlagspakkeFunnet.barnetilleggListe[0].brukTil).isNull() },
      Executable { assertThat(grunnlagspakkeFunnet.barnetilleggListe[0].belopBrutto).isEqualTo(BigDecimal.valueOf(1000.01)) }

    )
  }

  @Test
  @Suppress("NonAsciiCharacters")
  fun `Test på å oppdatere en grunnlagspakke`() {
    val opprettGrunnlagspakkeRequestDto = OpprettGrunnlagspakkeRequestDto(
      Formaal.FORSKUDD, "X123456"
    )

    val grunnlagspakkeId =
      grunnlagspakkeService.opprettGrunnlagspakke(opprettGrunnlagspakkeRequestDto)

    val oppdatertGrunnlagspakke = grunnlagspakkeService.oppdaterGrunnlagspakke(
      grunnlagspakkeId,
      TestUtil.byggOppdaterGrunnlagspakkeRequestBarnetillegg()
    )

    assertAll(
      { assertThat(oppdatertGrunnlagspakke.grunnlagspakkeId).isEqualTo(grunnlagspakkeId) }
    )
  }

  @Test
  @Suppress("NonAsciiCharacters")
  fun `Test på å hente grunnlagspakke med pdl-data`() {
    val opprettGrunnlagspakkeRequestDto = OpprettGrunnlagspakkeRequestDto(Formaal.FORSKUDD, "X123456")
    val grunnlagspakkeIdOpprettet =
      grunnlagspakkeService.opprettGrunnlagspakke(opprettGrunnlagspakkeRequestDto)

    // Legger inn forelder
    val opprettetForelder = persistenceService.opprettForelder(
      ForelderBo(
        grunnlagspakkeId = grunnlagspakkeIdOpprettet,
        personId = "44448888",
        navn = "Sliten Kartong",
        foedselsdato = LocalDate.parse("1990-04-04"),
        doedsdato = LocalDate.parse("2021-07-01"),
        aktiv = true,
        brukFra = LocalDateTime.now(),
        brukTil = null,
        opprettetAv = null,
        hentetTidspunkt = LocalDateTime.now()
      )
    )

    // Legger inn barn
    val opprettetBarnUnder18 = persistenceService.opprettBarn(
      BarnBo(
        grunnlagspakkeId = grunnlagspakkeIdOpprettet,
        personId = "22233344455",
        navn = "Svett Elefant",
        foedselsdato = LocalDate.parse("2017-05-17"),
        foedselsaar = 2017,
        doedsdato = LocalDate.parse("2021-06-23"),
        aktiv = true,
        brukFra = LocalDateTime.now(),
        brukTil = null,
        opprettetAv = null,
        hentetTidspunkt = LocalDateTime.now()
      )
    )
    val opprettetBarnOver18 = persistenceService.opprettBarn(
      BarnBo(
        grunnlagspakkeId = grunnlagspakkeIdOpprettet,
        personId = "98798798765",
        navn = "Trang Poncho",
        foedselsdato = LocalDate.parse("2000-12-02"),
        foedselsaar = 2000,
        doedsdato = null,
        aktiv = true,
        brukFra = LocalDateTime.now(),
        brukTil = null,
        opprettetAv = null,
        hentetTidspunkt = LocalDateTime.now()
      )
    )

    // Legger inn forelder-barn-relasjon
    persistenceService.opprettForelderBarn (
      ForelderBarnBo(
        forelderId = opprettetForelder.forelderId,
        barnId = opprettetBarnUnder18.barnId
      )
    )
    persistenceService.opprettForelderBarn (
      ForelderBarnBo(
        forelderId = opprettetForelder.forelderId,
        barnId = opprettetBarnOver18.barnId
      )
    )

    // Legger inn husstand
    val opprettetHusstand = persistenceService.opprettHusstand(
      HusstandBo(
        grunnlagspakkeId = grunnlagspakkeIdOpprettet,
        personId = "44448888",
        periodeFra = LocalDate.parse("2001-05-01"),
        periodeTil = LocalDate.parse("2022-07-01"),
        adressenavn = "adressenavn1",
        husnummer = "husnummer1",
        husbokstav = "husbokstav1",
        bruksenhetsnummer = "bruksenhetsnummer1",
        postnummer = "postnummer1",
        bydelsnummer = "bydelsnummer1",
        kommunenummer = "kommunenummer1",
        matrikkelId = 12345,
        aktiv = true,
        brukFra = LocalDateTime.now(),
        brukTil = null,
        opprettetAv = null,
        hentetTidspunkt = LocalDateTime.now()
      )
    )

    // Legger inn barna som husstandsmedlemmer
    persistenceService.opprettHusstandsmedlem(
      HusstandsmedlemBo(
        husstandId = opprettetHusstand.husstandId ,
        periodeFra = LocalDate.parse("2021-05-01"),
        periodeTil = LocalDate.parse("2021-06-01"),
        personId = "22233344455",
        navn = "Svett Elefant",
        foedselsdato = LocalDate.parse("2017-05-17"),
        doedsdato = LocalDate.parse("2021-06-23"),
        opprettetAv = null,
        hentetTidspunkt = LocalDateTime.now()
      )
    )
    persistenceService.opprettHusstandsmedlem(
      HusstandsmedlemBo(
        husstandId = opprettetHusstand.husstandId ,
        periodeFra = LocalDate.parse("2000-05-01"),
        periodeTil = LocalDate.parse("2021-07-01"),
        personId = "98798798765",
        navn = "Trang Poncho",
        foedselsdato = LocalDate.parse("2000-12-02"),
        doedsdato = null,
        opprettetAv = null,
        hentetTidspunkt = LocalDateTime.now()
      )
    )

    // Legger inn voksent husstandsmedlem
    persistenceService.opprettHusstandsmedlem(
      HusstandsmedlemBo(
        husstandId = opprettetHusstand.husstandId ,
        periodeFra = LocalDate.parse("2020-02-01"),
        periodeTil = LocalDate.parse("2020-09-01"),
        personId = "99988877766",
        navn = "Klam Trøffel",
        foedselsdato = LocalDate.parse("1997-02-12"),
        doedsdato = null,
        opprettetAv = null,
        hentetTidspunkt = LocalDateTime.now()
      )
    )

    // Legger inn sivilstand
    persistenceService.opprettSivilstand(
      SivilstandBo(
        grunnlagspakkeId = grunnlagspakkeIdOpprettet,
        personId = "22334455",
        periodeFra = LocalDate.parse("2021-05-01"),
        periodeTil = LocalDate.parse("2021-06-01"),
        sivilstand = "ENSLIG",
        aktiv = true,
        brukFra = LocalDateTime.now(),
        brukTil = null,
        opprettetAv = null,
        hentetTidspunkt = LocalDateTime.now()
      )
    )

    val grunnlagspakkeFunnet =
      grunnlagspakkeService.hentGrunnlagspakke(grunnlagspakkeIdOpprettet)

    assertAll(
      Executable { assertThat(grunnlagspakkeFunnet).isNotNull },
      Executable { assertThat(grunnlagspakkeFunnet.grunnlagspakkeId).isEqualTo(grunnlagspakkeIdOpprettet) },
      Executable { assertThat(grunnlagspakkeFunnet.egneBarnListe.size).isEqualTo(1) }, // Barn over 18 år skal ikke returneres
      Executable { assertThat(grunnlagspakkeFunnet.egneBarnListe[0].personIdBarn).isEqualTo("22233344455") },
      Executable { assertThat(grunnlagspakkeFunnet.egneBarnListe[0].navn).isEqualTo("Svett Elefant") },
      Executable { assertThat(grunnlagspakkeFunnet.egneBarnListe[0].foedselsdato).isEqualTo(LocalDate.parse("2017-05-17")) },
      Executable { assertThat(grunnlagspakkeFunnet.egneBarnListe[0].foedselsaar).isEqualTo(2017) },
      Executable { assertThat(grunnlagspakkeFunnet.egneBarnListe[0].doedsdato).isEqualTo(LocalDate.parse("2021-06-23"))},
      Executable { assertThat(grunnlagspakkeFunnet.egneBarnListe[0].opprettetAv).isNull() },
      Executable { assertThat(grunnlagspakkeFunnet.egneBarnListe[0].hentetTidspunkt).isNotNull() },
      Executable { assertThat(grunnlagspakkeFunnet.egneBarnListe[0].borISammeHusstandDtoListe?.get(0)?.periodeFra).isEqualTo(LocalDate.parse("2021-05-01")) },
      Executable { assertThat(grunnlagspakkeFunnet.egneBarnListe[0].borISammeHusstandDtoListe?.get(0)?.periodeTil).isEqualTo(LocalDate.parse("2021-06-01")) },
      Executable { assertThat(grunnlagspakkeFunnet.egneBarnListe[0].borISammeHusstandDtoListe?.get(0)?.opprettetAv).isNull() },
      Executable { assertThat(grunnlagspakkeFunnet.egneBarnListe[0].borISammeHusstandDtoListe?.get(0)?.hentetTidspunkt).isNotNull() },

      Executable { assertThat(grunnlagspakkeFunnet.husstandListe.size).isEqualTo(1) },
      Executable { assertThat(grunnlagspakkeFunnet.husstandListe[0].periodeFra).isEqualTo(LocalDate.parse("2001-05-01")) },
      Executable { assertThat(grunnlagspakkeFunnet.husstandListe[0].periodeTil).isEqualTo(LocalDate.parse("2022-07-01")) },
      Executable { assertThat(grunnlagspakkeFunnet.husstandListe[0].adressenavn).isEqualTo("adressenavn1") },
      Executable { assertThat(grunnlagspakkeFunnet.husstandListe[0].husnummer).isEqualTo("husnummer1") },
      Executable { assertThat(grunnlagspakkeFunnet.husstandListe[0].husbokstav).isEqualTo("husbokstav1") },
      Executable { assertThat(grunnlagspakkeFunnet.husstandListe[0].bruksenhetsnummer).isEqualTo("bruksenhetsnummer1") },
      Executable { assertThat(grunnlagspakkeFunnet.husstandListe[0].postnummer).isEqualTo("postnummer1") },
      Executable { assertThat(grunnlagspakkeFunnet.husstandListe[0].bydelsnummer).isEqualTo("bydelsnummer1") },
      Executable { assertThat(grunnlagspakkeFunnet.husstandListe[0].kommunenummer).isEqualTo("kommunenummer1") },
      Executable { assertThat(grunnlagspakkeFunnet.husstandListe[0].matrikkelId).isEqualTo(12345) },
      Executable { assertThat(grunnlagspakkeFunnet.husstandListe[0].opprettetAv).isNull() },
      Executable { assertThat(grunnlagspakkeFunnet.husstandListe[0].hentetTidspunkt).isNotNull() },
      Executable { assertThat(grunnlagspakkeFunnet.husstandListe[0].husstandsmedlemmerListe?.size).isEqualTo(1) },
      Executable { assertThat(grunnlagspakkeFunnet.husstandListe[0].husstandsmedlemmerListe?.get(0)?.periodeFra).isEqualTo(LocalDate.parse("2020-02-01")) },
      Executable { assertThat(grunnlagspakkeFunnet.husstandListe[0].husstandsmedlemmerListe?.get(0)?.periodeTil).isEqualTo(LocalDate.parse("2020-09-01")) },
      Executable { assertThat(grunnlagspakkeFunnet.husstandListe[0].husstandsmedlemmerListe?.get(0)?.personId).isEqualTo("99988877766") },
      Executable { assertThat(grunnlagspakkeFunnet.husstandListe[0].husstandsmedlemmerListe?.get(0)?.navn).isEqualTo("Klam Trøffel") },
      Executable { assertThat(grunnlagspakkeFunnet.husstandListe[0].husstandsmedlemmerListe?.get(0)?.foedselsdato).isEqualTo(LocalDate.parse("1997-02-12")) },
      Executable { assertThat(grunnlagspakkeFunnet.husstandListe[0].husstandsmedlemmerListe?.get(0)?.doedsdato).isNull() },
      Executable { assertThat(grunnlagspakkeFunnet.husstandListe[0].husstandsmedlemmerListe?.get(0)?.opprettetAv).isNull() },
      Executable { assertThat(grunnlagspakkeFunnet.husstandListe[0].husstandsmedlemmerListe?.get(0)?.hentetTidspunkt).isNotNull() },
      Executable { assertThat(grunnlagspakkeFunnet.husstandListe[0].husstandsmedlemmerListe?.size).isEqualTo(1) },

      Executable { assertThat(grunnlagspakkeFunnet.sivilstandListe.size).isEqualTo(1) },
      Executable { assertThat(grunnlagspakkeFunnet.sivilstandListe[0].periodeFra).isEqualTo(LocalDate.parse("2021-05-01")) },
      Executable { assertThat(grunnlagspakkeFunnet.sivilstandListe[0].periodeTil).isEqualTo(LocalDate.parse("2021-06-01")) },
      Executable { assertThat(grunnlagspakkeFunnet.sivilstandListe[0].sivilstand).isEqualTo(SivilstandKode.ENSLIG) },
      Executable { assertThat(grunnlagspakkeFunnet.sivilstandListe[0].opprettetAv).isNull() },
      Executable { assertThat(grunnlagspakkeFunnet.sivilstandListe[0].hentetTidspunkt).isNotNull() },

      )
  }

}
