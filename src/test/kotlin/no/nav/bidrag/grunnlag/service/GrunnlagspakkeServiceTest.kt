package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.grunnlag.BidragGrunnlagTest
import no.nav.bidrag.grunnlag.api.grunnlagspakke.GrunnlagstypeRequest
import no.nav.bidrag.grunnlag.api.grunnlagspakke.LukkGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OppdaterGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OpprettGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.api.grunnlagspakke.PersonIdOgPeriodeRequest
import no.nav.bidrag.grunnlag.dto.AinntektDto
import no.nav.bidrag.grunnlag.dto.SkattegrunnlagDto
import no.nav.bidrag.grunnlag.dto.AinntektspostDto
import no.nav.bidrag.grunnlag.dto.SkattegrunnlagspostDto
import no.nav.bidrag.grunnlag.dto.UtvidetBarnetrygdOgSmaabarnstilleggDto

import no.nav.bidrag.grunnlag.persistence.repository.GrunnlagspakkeRepository
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
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
  fun `Test på opprette ny grunnlagspakke`() {
    val opprettGrunnlagspakkeRequest = OpprettGrunnlagspakkeRequest(
      Formaal.FORSKUDD,  "X123456"
    )

    val nyGrunnlagspakkeOpprettet =
      grunnlagspakkeService.opprettGrunnlagspakke(opprettGrunnlagspakkeRequest)

    assertAll(
      Executable { assertThat(nyGrunnlagspakkeOpprettet).isNotNull }
    )
  }

/*  @Test
  @Suppress("NonAsciiCharacters")
  fun `Test på oppdatere grunnlagspakke`() {
    val opprettGrunnlagspakkeRequest = OpprettGrunnlagspakkeRequest("X123456")
    val nyGrunnlagspakkeOpprettet =
      grunnlagspakkeService.opprettGrunnlagspakke(opprettGrunnlagspakkeRequest)

    val oppdaterGrunnlagspakkeResponse = grunnlagspakkeService.oppdaterGrunnlagspakke(
      OppdaterGrunnlagspakkeRequest(
        grunnlagspakkeId = nyGrunnlagspakkeOpprettet.grunnlagspakkeId,
        formaal = Formaal.BIDRAG.toString(),
        gyldigTil = null,
        mutableListOf(
          GrunnlagstypeRequest(
            Grunnlagstype.AINNTEKT.toString(),
            mutableListOf(
              PersonIdOgPeriodeRequest(
                personId = "1234567890",
                periodeFra = "2021-01-01",
                periodeTil = "2022-01-01"))))))

    assertAll(
      Executable { assertThat(nyGrunnlagspakkeOpprettet).isNotNull },
      Executable { assertThat(oppdaterGrunnlagspakkeResponse).isNotNull },
      Executable { assertThat(oppdaterGrunnlagspakkeResponse.grunnlagspakkeId).isEqualTo(nyGrunnlagspakkeOpprettet.grunnlagspakkeId)},
    )
  }*/

  @Test
  @Suppress("NonAsciiCharacters")
  fun `Test på å lukke en grunnlagspakke`() {
    val opprettGrunnlagspakkeRequest = OpprettGrunnlagspakkeRequest(
      Formaal.FORSKUDD,  "X123456"
    )

    val opprettGrunnlagspakkeResponse =
      grunnlagspakkeService.opprettGrunnlagspakke(opprettGrunnlagspakkeRequest)

    val lukketGrunnlagspakke = grunnlagspakkeService.lukkGrunnlagspakke(
      LukkGrunnlagspakkeRequest(
        opprettGrunnlagspakkeResponse.grunnlagspakkeId
      )
    )

    assertAll(
      Executable { assertThat(opprettGrunnlagspakkeResponse).isNotNull },
      Executable { assertThat(lukketGrunnlagspakke).isEqualTo(opprettGrunnlagspakkeResponse.grunnlagspakkeId) }
    )
  }

  @Test
  @Suppress("NonAsciiCharacters")
  fun `Test på hente grunnlagspakke med aktive og inaktive inntekter + utvidet barnetrygd og småbarnstillegg`() {
    val opprettGrunnlagspakkeRequest = OpprettGrunnlagspakkeRequest(Formaal.FORSKUDD, "X123456")
    val nyGrunnlagspakkeOpprettet =
      grunnlagspakkeService.opprettGrunnlagspakke(opprettGrunnlagspakkeRequest)

    val ainntektDto = AinntektDto(
      grunnlagspakkeId = nyGrunnlagspakkeOpprettet.grunnlagspakkeId,
      personId = "1234567",
      periodeFra = LocalDate.parse("2021-06-01"),
      periodeTil = LocalDate.parse("2021-07-01"),
      aktiv = true,
      brukFra = LocalDateTime.now(),
      brukTil = null,
      hentetTidspunkt = LocalDateTime.now()
    )

    val opprettetAinntekt = persistenceService.opprettAinntekt(ainntektDto)

    persistenceService.opprettAinntektspost(
      AinntektspostDto(
        inntektId = opprettetAinntekt.inntektId,
        utbetalingsperiode = "202106",
        opptjeningsperiodeFra = LocalDate.parse("2021-05-01"),
        opptjeningsperiodeTil = LocalDate.parse("2021-06-01"),
        opplysningspliktigId = "1234567890",
        virksomhetId = "222444666",
        inntektType = "Loenn",
        fordelType = "Kontantytelse",
        beskrivelse = "Loenn/fastloenn",
        belop = BigDecimal.valueOf(17000.01)
      )
    )
    persistenceService.opprettAinntektspost(
      AinntektspostDto(
        inntektId = opprettetAinntekt.inntektId,
        utbetalingsperiode = "202106",
        opptjeningsperiodeFra = LocalDate.parse("2020-01-01"),
        opptjeningsperiodeTil = LocalDate.parse("2021-01-01"),
        opplysningspliktigId = "1234567890",
        virksomhetId = "222444666",
        inntektType = "Loenn",
        fordelType = "Kontantytelse",
        beskrivelse = "Loenn/ferieLoenn",
        belop = BigDecimal.valueOf(50000.01)
      )
    )

    // tester at inntekt som er merket med aktiv = false ikke hentes
    val inaktivInntektDto = AinntektDto(
      grunnlagspakkeId = nyGrunnlagspakkeOpprettet.grunnlagspakkeId,
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
      AinntektspostDto(
        inntektId = inaktivAinntekt.inntektId,
        utbetalingsperiode = "202006",
        opptjeningsperiodeFra = LocalDate.parse("2020-05-01"),
        opptjeningsperiodeTil = LocalDate.parse("2020-06-01"),
        opplysningspliktigId = "1234567890",
        virksomhetId = "222444666",
        inntektType = "Loenn",
        fordelType = "Kontantytelse",
        beskrivelse = "Loenn/fastloenn",
        belop = BigDecimal.valueOf(50000.01)
      )
    )


    // Legger inn inntekt for person nr 2
    val inntektDto2 = AinntektDto(
      grunnlagspakkeId = nyGrunnlagspakkeOpprettet.grunnlagspakkeId,
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
      AinntektspostDto(
        inntektId = opprettetAinntekt2.inntektId,
        utbetalingsperiode = "202107",
        opptjeningsperiodeFra = LocalDate.parse("2021-06-01"),
        opptjeningsperiodeTil = LocalDate.parse("2021-07-01"),
        opplysningspliktigId = "9876543210",
        virksomhetId = "666444222",
        inntektType = "Loenn",
        fordelType = "Kontantytelse",
        beskrivelse = "Loenn/fastloenn",
        belop = BigDecimal.valueOf(666000.01)
      )
    )

    // Test på inntekt fra Skatt
    val skattegrunnlagDto = SkattegrunnlagDto(
      grunnlagspakkeId = nyGrunnlagspakkeOpprettet.grunnlagspakkeId,
      personId = "345678",
      periodeFra = LocalDate.parse("2021-01-01"),
      periodeTil = LocalDate.parse("2022-01-01"),
      aktiv = true,
      brukFra = LocalDateTime.now(),
      brukTil = null,
      hentetTidspunkt = LocalDateTime.now()
    )

    val opprettetSkattegrunnlag = persistenceService.opprettSkattegrunnlag(skattegrunnlagDto)

    persistenceService.opprettSkattegrunnlagspost(
      SkattegrunnlagspostDto(
        skattegrunnlagId = opprettetSkattegrunnlag.skattegrunnlagId,
        skattegrunnlagType = SkattegrunnlagType.ORDINAER.toString(),
        inntektType = "Loenn",
        belop = BigDecimal.valueOf(23456.01)
      )
    )

    // Test på utvidet barnetrygd og småbarnstillegg
    persistenceService.opprettUtvidetBarnetrygdOgSmaabarnstillegg(
      UtvidetBarnetrygdOgSmaabarnstilleggDto(
        grunnlagspakkeId = nyGrunnlagspakkeOpprettet.grunnlagspakkeId,
        personId = "22334455",
        type = "Utvidet barnetrygd",
        periodeFra = LocalDate.parse("2021-05-01"),
        periodeTil = LocalDate.parse("2021-06-01"),
        belop = BigDecimal.valueOf(12468.01)
      )
    )

    val komplettGrunnlagspakkeFunnet =
      grunnlagspakkeService.hentKomplettGrunnlagspakke(nyGrunnlagspakkeOpprettet.grunnlagspakkeId)

    assertAll(
      Executable { assertThat(komplettGrunnlagspakkeFunnet).isNotNull },
      Executable { assertThat(komplettGrunnlagspakkeFunnet.grunnlagspakkeId).isEqualTo(nyGrunnlagspakkeOpprettet.grunnlagspakkeId)},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe.size).isEqualTo(2) },
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].personId).isEqualTo("1234567")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].periodeFra).isEqualTo(LocalDate.parse("2021-06-01"))},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].periodeTil).isEqualTo(LocalDate.parse("2021-07-01"))},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].aktiv).isEqualTo(true)},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].brukTil).isNull()},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[1].personId).isEqualTo("999999")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[1].periodeFra).isEqualTo(LocalDate.parse("2021-07-01"))},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[1].periodeTil).isEqualTo(LocalDate.parse("2021-08-01"))},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[1].aktiv).isEqualTo(true)},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[1].brukTil).isNull()},

      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe.size).isEqualTo(2)},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[0].utbetalingsperiode).isEqualTo("202106")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[0].opptjeningsperiodeFra).isEqualTo(LocalDate.parse("2021-05-01"))},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[0].opptjeningsperiodeTil).isEqualTo(LocalDate.parse("2021-06-01"))},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[0].opplysningspliktigId).isEqualTo("1234567890")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[0].virksomhetId).isEqualTo("222444666")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[0].inntektType).isEqualTo("Loenn")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[0].fordelType).isEqualTo("Kontantytelse")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[0].beskrivelse).isEqualTo("Loenn/fastloenn")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[0].belop).isEqualTo(BigDecimal.valueOf(17000.01))},

      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[1].utbetalingsperiode).isEqualTo("202106")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[1].opptjeningsperiodeFra).isEqualTo(LocalDate.parse("2020-01-01"))},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[1].opptjeningsperiodeTil).isEqualTo(LocalDate.parse("2021-01-01"))},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[1].opplysningspliktigId).isEqualTo("1234567890")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[0].virksomhetId).isEqualTo("222444666")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[1].inntektType).isEqualTo("Loenn")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[1].fordelType).isEqualTo("Kontantytelse")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[1].beskrivelse).isEqualTo("Loenn/ferieLoenn")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[0].ainntektspostListe[1].belop).isEqualTo(BigDecimal.valueOf(50000.01))},

      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[1].ainntektspostListe.size).isEqualTo(1)},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[1].ainntektspostListe[0].utbetalingsperiode).isEqualTo("202107")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[1].ainntektspostListe[0].opptjeningsperiodeFra).isEqualTo(LocalDate.parse("2021-06-01"))},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[1].ainntektspostListe[0].opptjeningsperiodeTil).isEqualTo(LocalDate.parse("2021-07-01"))},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[1].ainntektspostListe[0].opplysningspliktigId).isEqualTo("9876543210")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[1].ainntektspostListe[0].virksomhetId).isEqualTo("666444222")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[1].ainntektspostListe[0].inntektType).isEqualTo("Loenn")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[1].ainntektspostListe[0].fordelType).isEqualTo("Kontantytelse")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[1].ainntektspostListe[0].beskrivelse).isEqualTo("Loenn/fastloenn")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ainntektListe[1].ainntektspostListe[0].belop).isEqualTo(BigDecimal.valueOf(666000.01))},

      Executable { assertThat(komplettGrunnlagspakkeFunnet.skattegrunnlagListe[0].skattegrunnlagListe.size).isEqualTo(1)},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.skattegrunnlagListe[0].personId).isEqualTo("345678")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.skattegrunnlagListe[0].skattegrunnlagListe[0].skattegrunnlagType).isEqualTo(SkattegrunnlagType.ORDINAER.toString())},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.skattegrunnlagListe[0].skattegrunnlagListe[0].inntektType).isEqualTo("Loenn")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.skattegrunnlagListe[0].aktiv).isEqualTo(true)},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.skattegrunnlagListe[0].brukTil).isNull()},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.skattegrunnlagListe[0].skattegrunnlagListe[0].belop).isEqualTo(BigDecimal.valueOf(23456.01))},

      Executable { assertThat(komplettGrunnlagspakkeFunnet.ubstListe.size).isEqualTo(1) },
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ubstListe[0].personId).isEqualTo("22334455") },
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ubstListe[0].type).isEqualTo("Utvidet barnetrygd") },
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ubstListe[0].periodeFra).isEqualTo(LocalDate.parse("2021-05-01"))},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ubstListe[0].periodeTil).isEqualTo(LocalDate.parse("2021-06-01"))},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ubstListe[0].aktiv).isEqualTo(true)},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ubstListe[0].brukTil).isNull()},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ubstListe[0].belop).isEqualTo(BigDecimal.valueOf(12468.01))},

      )
  }


  @Test
  @Disabled
  @Suppress("NonAsciiCharacters")
  fun `Test på sette eksisterende, overlappende grunnlag som inaktivt ved ny hent av grunnlag`() {
    val opprettGrunnlagspakkeRequest = OpprettGrunnlagspakkeRequest(Formaal.FORSKUDD, "X123456")
    val nyGrunnlagspakkeOpprettet =
      grunnlagspakkeService.opprettGrunnlagspakke(opprettGrunnlagspakkeRequest)

    val opprettetAinntekt1 = persistenceService.opprettAinntekt(
      AinntektDto(
        grunnlagspakkeId = nyGrunnlagspakkeOpprettet.grunnlagspakkeId,
        personId = "1234567",
        periodeFra = LocalDate.parse("2021-05-01"),
        periodeTil = LocalDate.parse("2021-06-01"),
        aktiv = true, hentetTidspunkt = LocalDateTime.now(),
        brukFra = LocalDateTime.now(), brukTil = null))

    persistenceService.opprettAinntektspost(
      AinntektspostDto(
        inntektId = opprettetAinntekt1.inntektId, utbetalingsperiode = "202106",
        inntektType = "Loenn", belop = BigDecimal.valueOf(17000.01)
      )
    )
    persistenceService.opprettAinntektspost(
      AinntektspostDto(
        inntektId = opprettetAinntekt1.inntektId, utbetalingsperiode = "202106",
        inntektType = "Loenn", belop = BigDecimal.valueOf(50000.01)
      )
    )

    val opprettetAinntekt2 = persistenceService.opprettAinntekt(
      AinntektDto(
        grunnlagspakkeId = nyGrunnlagspakkeOpprettet.grunnlagspakkeId,
        personId = "1234567",
        periodeFra = LocalDate.parse("2020-06-01"),
        periodeTil = LocalDate.parse("2020-07-01"),
        aktiv = true, hentetTidspunkt = LocalDateTime.now(),
        brukFra = LocalDateTime.now(), brukTil = null))

    persistenceService.opprettAinntektspost(
      AinntektspostDto(
        inntektId = opprettetAinntekt2.inntektId, utbetalingsperiode = "202007",
        inntektType = "Loenn", belop = BigDecimal.valueOf(12345.01)
      )
    )

    val opprettetAinntekt3 = persistenceService.opprettAinntekt(
      AinntektDto(
        grunnlagspakkeId = nyGrunnlagspakkeOpprettet.grunnlagspakkeId,
        personId = "1234567",
        periodeFra = LocalDate.parse("2020-07-01"),
        periodeTil = LocalDate.parse("2020-08-01"),
        aktiv = true, hentetTidspunkt = LocalDateTime.now(),
        brukFra = LocalDateTime.now(), brukTil = null))

    persistenceService.opprettAinntektspost(
      AinntektspostDto(
        inntektId = opprettetAinntekt3.inntektId, utbetalingsperiode = "202008",
        inntektType = "Loenn", belop = BigDecimal.valueOf(333.01)
      )
    )


    // Test på inntekt fra Skatt
    val opprettetSkattegrunnlag1 = persistenceService.opprettSkattegrunnlag(
      SkattegrunnlagDto(
        grunnlagspakkeId = nyGrunnlagspakkeOpprettet.grunnlagspakkeId, personId = "345678",
        periodeFra = LocalDate.parse("2020-01-01"),
        periodeTil = LocalDate.parse("2021-01-01"),
        aktiv = true, brukFra = LocalDateTime.now(), brukTil = null,
        hentetTidspunkt = LocalDateTime.now()))

    persistenceService.opprettSkattegrunnlagspost(
      SkattegrunnlagspostDto(
        skattegrunnlagId = opprettetSkattegrunnlag1.skattegrunnlagId,
        skattegrunnlagType = SkattegrunnlagType.ORDINAER.toString(),
        inntektType = "Loenn", belop = BigDecimal.valueOf(20202021.01)
      )
    )

    val opprettetSkattegrunnlag2 = persistenceService.opprettSkattegrunnlag(
      SkattegrunnlagDto(
        grunnlagspakkeId = nyGrunnlagspakkeOpprettet.grunnlagspakkeId, personId = "345678",
        periodeFra = LocalDate.parse("2021-01-01"),
        periodeTil = LocalDate.parse("2022-01-01"),
        aktiv = true, brukFra = LocalDateTime.now(), brukTil = null,
        hentetTidspunkt = LocalDateTime.now()))

    persistenceService.opprettSkattegrunnlagspost(
      SkattegrunnlagspostDto(
        skattegrunnlagId = opprettetSkattegrunnlag2.skattegrunnlagId,
        skattegrunnlagType = SkattegrunnlagType.ORDINAER.toString(),
        inntektType = "Loenn", belop = BigDecimal.valueOf(20212022.01)
      )
    )

    // Test på utvidet barnetrygd og småbarnstillegg
    persistenceService.opprettUtvidetBarnetrygdOgSmaabarnstillegg(
      UtvidetBarnetrygdOgSmaabarnstilleggDto(
        grunnlagspakkeId = nyGrunnlagspakkeOpprettet.grunnlagspakkeId,
        personId = "22334455", type = "Utvidet barnetrygd",
        periodeFra = LocalDate.parse("2021-05-01"),
        periodeTil = LocalDate.parse("2021-06-01"),
        belop = BigDecimal.valueOf(12468.01), aktiv = true, brukFra = LocalDateTime.now(),
        brukTil = null, deltBosted = false, hentetTidspunkt = LocalDateTime.now()))

    persistenceService.opprettUtvidetBarnetrygdOgSmaabarnstillegg(
      UtvidetBarnetrygdOgSmaabarnstilleggDto(
        grunnlagspakkeId = nyGrunnlagspakkeOpprettet.grunnlagspakkeId,
        personId = "22334455", type = "Utvidet barnetrygd",
        periodeFra = LocalDate.parse("2021-06-01"),
        periodeTil = LocalDate.parse("2021-10-01"),
        belop = BigDecimal.valueOf(6234.01), aktiv = true, brukFra = LocalDateTime.now(),
        brukTil = null, deltBosted = true, hentetTidspunkt = LocalDateTime.now()))

/*  val OriginalGrunnlagspakke =
    grunnlagspakkeService.hentKomplettGrunnlagspakke(nyGrunnlagspakkeOpprettet.grunnlagspakkeId)*/

    val oppdaterRequest = OppdaterGrunnlagspakkeRequest(
      grunnlagspakkeId = nyGrunnlagspakkeOpprettet.grunnlagspakkeId,
      gyldigTil = LocalDate.now(),
      innsynHistoriskeInntekterDato = null,
      grunnlagtypeRequestListe = mutableListOf(
        GrunnlagstypeRequest(
          Grunnlagstype.AINNTEKT,
          mutableListOf(
            PersonIdOgPeriodeRequest(
              personId = "1234567890",
              periodeFra = LocalDate.parse("2021-01-01"),
              periodeTil = LocalDate.parse("2022-01-01")
            )
          )
        ),
        GrunnlagstypeRequest(
          Grunnlagstype.SKATTEGRUNNLAG,
          mutableListOf(
            PersonIdOgPeriodeRequest(
              personId = "1234567890",
              periodeFra = LocalDate.parse("2021-01-01"),
              periodeTil = LocalDate.parse("2022-01-01")
            )
          )
        ),
        GrunnlagstypeRequest(
          Grunnlagstype.UTVIDETBARNETRYGDOGSMAABARNSTILLEGG,
          mutableListOf(
            PersonIdOgPeriodeRequest(
              personId = "1234567890",
              periodeFra = LocalDate.parse("2021-01-01"),
              periodeTil = LocalDate.parse("2022-01-01")
            )
          )
        )
      )
    )

    val komplettGrunnlagspakkeFunnet =
      grunnlagspakkeService.hentKomplettGrunnlagspakke(nyGrunnlagspakkeOpprettet.grunnlagspakkeId)

  }
}
