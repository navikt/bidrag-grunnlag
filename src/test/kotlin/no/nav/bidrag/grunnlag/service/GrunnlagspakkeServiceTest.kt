package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.behandling.felles.dto.grunnlag.OpprettGrunnlagspakkeRequestDto
import no.nav.bidrag.behandling.felles.enums.BarnetilleggType
import no.nav.bidrag.behandling.felles.enums.Formaal
import no.nav.bidrag.behandling.felles.enums.SkattegrunnlagType
import no.nav.bidrag.grunnlag.BidragGrunnlagTest
import no.nav.bidrag.grunnlag.TestUtil
import no.nav.bidrag.grunnlag.bo.AinntektBo
import no.nav.bidrag.grunnlag.bo.AinntektspostBo
import no.nav.bidrag.grunnlag.bo.BarnetilleggBo
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
        etterbetalingsperiodeFra = LocalDate.of(2021, 10, 1),
        etterbetalingsperiodeTil = LocalDate.of(2021, 11, 1)
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
        etterbetalingsperiodeFra = LocalDate.of(2021, 10, 1),
        etterbetalingsperiodeTil = LocalDate.of(2021, 11, 1)
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
        etterbetalingsperiodeFra = LocalDate.of(2021, 10, 1),
        etterbetalingsperiodeTil = LocalDate.of(2021, 11, 1)
      )
    )

    // Test på inntekt fra Skatt
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

    // Test på utvidet barnetrygd og småbarnstillegg
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

    // Test på barnetillegg
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

    val komplettGrunnlagspakkeFunnet =
      grunnlagspakkeService.hentGrunnlagspakke(grunnlagspakkeIdOpprettet)

    assertAll(
      Executable { assertThat(komplettGrunnlagspakkeFunnet).isNotNull },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.grunnlagspakkeId).isEqualTo(
          grunnlagspakkeIdOpprettet
        )
      },
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe.size).isEqualTo(2) },
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].personId).isEqualTo("1234567") },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].periodeFra).isEqualTo(
          LocalDate.parse("2021-06-01")
        )
      },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].periodeTil).isEqualTo(
          LocalDate.parse("2021-07-01")
        )
      },
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].aktiv).isEqualTo(true) },
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].brukTil).isNull() },
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[1].personId).isEqualTo("999999") },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[1].periodeFra).isEqualTo(
          LocalDate.parse("2021-07-01")
        )
      },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[1].periodeTil).isEqualTo(
          LocalDate.parse("2021-08-01")
        )
      },
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[1].aktiv).isEqualTo(true) },
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[1].brukTil).isNull() },

      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe.size).isEqualTo(
          2
        )
      },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[0].utbetalingsperiode).isEqualTo(
          "202106"
        )
      },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[0].opptjeningsperiodeFra).isEqualTo(
          LocalDate.parse("2021-05-01")
        )
      },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[0].opptjeningsperiodeTil).isEqualTo(
          LocalDate.parse("2021-06-01")
        )
      },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[0].opplysningspliktigId).isEqualTo(
          "1234567890"
        )
      },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[0].virksomhetId).isEqualTo(
          "222444666"
        )
      },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[0].inntektType).isEqualTo(
          "Loenn"
        )
      },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[0].fordelType).isEqualTo(
          "Kontantytelse"
        )
      },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[0].beskrivelse).isEqualTo(
          "Loenn/fastloenn"
        )
      },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[0].belop).isEqualTo(
          BigDecimal.valueOf(17000.01)
        )
      },

      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[1].utbetalingsperiode).isEqualTo(
          "202106"
        )
      },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[1].opptjeningsperiodeFra).isEqualTo(
          LocalDate.parse("2020-01-01")
        )
      },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[1].opptjeningsperiodeTil).isEqualTo(
          LocalDate.parse("2021-01-01")
        )
      },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[1].opplysningspliktigId).isEqualTo(
          "1234567890"
        )
      },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[0].virksomhetId).isEqualTo(
          "222444666"
        )
      },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[1].inntektType).isEqualTo(
          "Loenn"
        )
      },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[1].fordelType).isEqualTo(
          "Kontantytelse"
        )
      },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[1].beskrivelse).isEqualTo(
          "Loenn/ferieLoenn"
        )
      },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[1].belop).isEqualTo(
          BigDecimal.valueOf(50000.01)
        )
      },

      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[1].ainntektspostListe.size).isEqualTo(
          1
        )
      },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[1].ainntektspostListe[0].utbetalingsperiode).isEqualTo(
          "202107"
        )
      },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[1].ainntektspostListe[0].opptjeningsperiodeFra).isEqualTo(
          LocalDate.parse("2021-06-01")
        )
      },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[1].ainntektspostListe[0].opptjeningsperiodeTil).isEqualTo(
          LocalDate.parse("2021-07-01")
        )
      },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[1].ainntektspostListe[0].opplysningspliktigId).isEqualTo(
          "9876543210"
        )
      },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[1].ainntektspostListe[0].virksomhetId).isEqualTo(
          "666444222"
        )
      },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[1].ainntektspostListe[0].inntektType).isEqualTo(
          "Loenn"
        )
      },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[1].ainntektspostListe[0].fordelType).isEqualTo(
          "Kontantytelse"
        )
      },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[1].ainntektspostListe[0].beskrivelse).isEqualTo(
          "Loenn/fastloenn"
        )
      },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[1].ainntektspostListe[0].belop).isEqualTo(
          BigDecimal.valueOf(666000.01)
        )
      },

      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.skattegrunnlagListe[0].skattegrunnlagListe.size).isEqualTo(
          1
        )
      },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.skattegrunnlagListe[0].personId).isEqualTo(
          "345678"
        )
      },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.skattegrunnlagListe[0].skattegrunnlagListe[0].skattegrunnlagType).isEqualTo(
          SkattegrunnlagType.ORDINAER.toString()
        )
      },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.skattegrunnlagListe[0].skattegrunnlagListe[0].inntektType).isEqualTo(
          "Loenn"
        )
      },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.skattegrunnlagListe[0].aktiv).isEqualTo(
          true
        )
      },
      Executable { assertThat(komplettGrunnlagspakkeFunnet.skattegrunnlagListe[0].brukTil).isNull() },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.skattegrunnlagListe[0].skattegrunnlagListe[0].belop).isEqualTo(
          BigDecimal.valueOf(23456.01)
        )
      },

      Executable { assertThat(komplettGrunnlagspakkeFunnet.ubstListe.size).isEqualTo(1) },
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ubstListe[0].personId).isEqualTo("22334455") },
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ubstListe[0].type).isEqualTo("Utvidet barnetrygd") },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.ubstListe[0].periodeFra).isEqualTo(
          LocalDate.parse("2021-05-01")
        )
      },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.ubstListe[0].periodeTil).isEqualTo(
          LocalDate.parse("2021-06-01")
        )
      },
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ubstListe[0].aktiv).isEqualTo(true) },
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ubstListe[0].brukTil).isNull() },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.ubstListe[0].belop).isEqualTo(
          BigDecimal.valueOf(
            12468.01
          )
        )
      },

      Executable { assertThat(komplettGrunnlagspakkeFunnet.barnetilleggListe.size).isEqualTo(1) },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.barnetilleggListe[0].partPersonId).isEqualTo(
          "22334455"
        )
      },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.barnetilleggListe[0].barnPersonId).isEqualTo(
          "1234567"
        )
      },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.barnetilleggListe[0].barnetilleggType).isEqualTo(
          BarnetilleggType.PENSJON.toString()
        )
      },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.barnetilleggListe[0].periodeFra).isEqualTo(
          LocalDate.parse("2021-05-01")
        )
      },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.barnetilleggListe[0].periodeTil).isEqualTo(
          LocalDate.parse("2021-06-01")
        )
      },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.barnetilleggListe[0].aktiv).isEqualTo(
          true
        )
      },
      Executable { assertThat(komplettGrunnlagspakkeFunnet.barnetilleggListe[0].brukTil).isNull() },
      Executable {
        assertThat(komplettGrunnlagspakkeFunnet.barnetilleggListe[0].belopBrutto).isEqualTo(
          BigDecimal.valueOf(1000.01)
        )
      }
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
}
