package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.grunnlag.BidragGrunnlagLocal
import no.nav.bidrag.grunnlag.api.grunnlagspakke.SettGyldigTilDatoForGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OpprettGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.dto.InntektAinntektDto
import no.nav.bidrag.grunnlag.dto.SkattegrunnlagDto
import no.nav.bidrag.grunnlag.dto.InntektspostAinntektDto
import no.nav.bidrag.grunnlag.dto.SkattegrunnlagspostDto
import no.nav.bidrag.grunnlag.dto.UtvidetBarnetrygdOgSmaabarnstilleggDto

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
@ActiveProfiles(BidragGrunnlagLocal.TEST_PROFILE)
@SpringBootTest(
  classes = [BidragGrunnlagLocal::class],
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
      "X123456"
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
  fun `Test på å sette gyldigTil-dato for en grunnlagspakke`() {
    val opprettGrunnlagspakkeRequest = OpprettGrunnlagspakkeRequest(
      "X123456"
    )

    val opprettGrunnlagspakkeResponse =
      grunnlagspakkeService.opprettGrunnlagspakke(opprettGrunnlagspakkeRequest)

    val endretGrunnlagspakke = grunnlagspakkeService.settGyldigTildatoGrunnlagspakke(SettGyldigTilDatoForGrunnlagspakkeRequest(
      opprettGrunnlagspakkeResponse.grunnlagspakkeId, "2021-11-10") )

    assertAll(
      Executable { assertThat(opprettGrunnlagspakkeResponse).isNotNull },
      Executable { assertThat(endretGrunnlagspakke).isEqualTo(opprettGrunnlagspakkeResponse.grunnlagspakkeId) }
    )
  }

  @Test
  @Suppress("NonAsciiCharacters")
  fun `Test på hente grunnlagspakke med aktive inntekter + utvidet barnetrygd og småbarnstillegg`() {
    val opprettGrunnlagspakkeRequest = OpprettGrunnlagspakkeRequest("X123456")
    val nyGrunnlagspakkeOpprettet =
      grunnlagspakkeService.opprettGrunnlagspakke(opprettGrunnlagspakkeRequest)

    val inntektAinntektDto = InntektAinntektDto(
      grunnlagspakkeId = nyGrunnlagspakkeOpprettet.grunnlagspakkeId,
      personId = "1234567",
      periodeFra = LocalDate.parse("2021-05-01"),
      periodeTil = LocalDate.parse("2021-06-01"),
      aktiv = true,
      hentetTidspunkt = LocalDateTime.now(),
      brukFra = LocalDateTime.now(),
      brukTil = null
    )

    val opprettetInntekt = persistenceService.opprettInntektAinntekt(inntektAinntektDto)

    persistenceService.opprettInntektspostAinntekt(
      InntektspostAinntektDto(
        inntektId = opprettetInntekt.inntektId,
        utbetalingsperiode = "202106",
        opptjeningsperiodeFra = LocalDate.parse("2021-05-01"),
        opptjeningsperiodeTil = LocalDate.parse("2021-06-01"),
        opplysningspliktigId = "1234567890",
        type = "Loenn",
        fordelType = "Kontantytelse",
        beskrivelse = "Loenn/fastloenn",
        belop = BigDecimal.valueOf(17000.01)
      )
    )
    persistenceService.opprettInntektspostAinntekt(
      InntektspostAinntektDto(
        inntektId = opprettetInntekt.inntektId,
        utbetalingsperiode = "202106",
        opptjeningsperiodeFra = LocalDate.parse("2021-05-01"),
        opptjeningsperiodeTil = LocalDate.parse("2021-06-01"),
        opplysningspliktigId = "1234567890",
        type = "Loenn",
        fordelType = "Kontantytelse",
        beskrivelse = "Loenn/ferieLoenn",
        belop = BigDecimal.valueOf(50000.01)
      )
    )

    // tester at inntekt som er merket med aktiv = false ikke hentes
    val innaktivInntektDto = InntektAinntektDto(
      grunnlagspakkeId = nyGrunnlagspakkeOpprettet.grunnlagspakkeId,
      personId = "1234567",
      periodeFra = LocalDate.parse("2020-05-01"),
      periodeTil = LocalDate.parse("2020-06-01"),
      aktiv = false,
      hentetTidspunkt = LocalDateTime.now(),
      brukFra = LocalDateTime.now(),
      brukTil = null
    )

    val innaktivInntekt = persistenceService.opprettInntektAinntekt(innaktivInntektDto)

    persistenceService.opprettInntektspostAinntekt(
      InntektspostAinntektDto(
        inntektId = innaktivInntekt.inntektId,
        utbetalingsperiode = "202006",
        opptjeningsperiodeFra = LocalDate.parse("2020-05-01"),
        opptjeningsperiodeTil = LocalDate.parse("2020-06-01"),
        opplysningspliktigId = "1234567890",
        type = "Loenn",
        fordelType = "Kontantytelse",
        beskrivelse = "Loenn/fastloenn",
        belop = BigDecimal.valueOf(50000.01)
      )
    )


    // Legger inn inntekt for person nr 2
    val inntektDto2 = InntektAinntektDto(
      grunnlagspakkeId = nyGrunnlagspakkeOpprettet.grunnlagspakkeId,
      personId = "999999",
      periodeFra = LocalDate.parse("2021-06-01"),
      periodeTil = LocalDate.parse("2021-07-01"),
      aktiv = true,
      hentetTidspunkt = LocalDateTime.now(),
      brukFra = LocalDateTime.now(),
      brukTil = null
    )

    val opprettetInntekt2 = persistenceService.opprettInntektAinntekt(inntektDto2)

    persistenceService.opprettInntektspostAinntekt(
      InntektspostAinntektDto(
        inntektId = opprettetInntekt2.inntektId,
        utbetalingsperiode = "202107",
        opptjeningsperiodeFra = LocalDate.parse("2021-06-01"),
        opptjeningsperiodeTil = LocalDate.parse("2021-07-01"),
        opplysningspliktigId = "9876543210",
        type = "Loenn",
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
      periodeTil = LocalDate.parse("2021-12-01"),
      aktiv = true,
      hentetTidspunkt = LocalDateTime.now(),
      brukFra = LocalDateTime.now(),
      brukTil = null
    )

    val opprettetSkattegrunnlag = persistenceService.opprettSkattegrunnlag(skattegrunnlagDto)

    persistenceService.opprettSkattegrunnlagspost(
      SkattegrunnlagspostDto(
        skattegrunnlagId = opprettetSkattegrunnlag.skattegrunnlagId,
        type = "Loenn",
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
      Executable { assertThat(komplettGrunnlagspakkeFunnet.inntektAinntektListe.size).isEqualTo(2) },
      Executable { assertThat(komplettGrunnlagspakkeFunnet.inntektAinntektListe[0].personId).isEqualTo("1234567")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.inntektAinntektListe[1].personId).isEqualTo("999999")},

      Executable { assertThat(komplettGrunnlagspakkeFunnet.inntektAinntektListe[0].inntektspostAinntektListe.size).isEqualTo(2)},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.inntektAinntektListe[0].inntektspostAinntektListe[0].utbetalingsperiode).isEqualTo("202106")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.inntektAinntektListe[0].inntektspostAinntektListe[0].opptjeningsperiodeFra).isEqualTo(LocalDate.parse("2021-05-01"))},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.inntektAinntektListe[0].inntektspostAinntektListe[0].opptjeningsperiodeTil).isEqualTo(LocalDate.parse("2021-06-01"))},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.inntektAinntektListe[0].inntektspostAinntektListe[0].opplysningspliktigId).isEqualTo("1234567890")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.inntektAinntektListe[0].inntektspostAinntektListe[0].inntektType).isEqualTo("Loenn")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.inntektAinntektListe[0].inntektspostAinntektListe[0].fordelType).isEqualTo("Kontantytelse")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.inntektAinntektListe[0].inntektspostAinntektListe[0].beskrivelse).isEqualTo("Loenn/fastloenn")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.inntektAinntektListe[0].inntektspostAinntektListe[0].belop).isEqualTo(BigDecimal.valueOf(17000.01))},

      Executable { assertThat(komplettGrunnlagspakkeFunnet.inntektAinntektListe[0].inntektspostAinntektListe[1].utbetalingsperiode).isEqualTo("202106")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.inntektAinntektListe[0].inntektspostAinntektListe[1].opptjeningsperiodeFra).isEqualTo(LocalDate.parse("2021-05-01"))},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.inntektAinntektListe[0].inntektspostAinntektListe[1].opptjeningsperiodeTil).isEqualTo(LocalDate.parse("2021-06-01"))},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.inntektAinntektListe[0].inntektspostAinntektListe[1].opplysningspliktigId).isEqualTo("1234567890")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.inntektAinntektListe[0].inntektspostAinntektListe[1].inntektType).isEqualTo("Loenn")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.inntektAinntektListe[0].inntektspostAinntektListe[1].fordelType).isEqualTo("Kontantytelse")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.inntektAinntektListe[0].inntektspostAinntektListe[1].beskrivelse).isEqualTo("Loenn/ferieLoenn")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.inntektAinntektListe[0].inntektspostAinntektListe[1].belop).isEqualTo(BigDecimal.valueOf(50000.01))},

      Executable { assertThat(komplettGrunnlagspakkeFunnet.inntektAinntektListe[1].inntektspostAinntektListe.size).isEqualTo(1)},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.inntektAinntektListe[1].inntektspostAinntektListe[0].utbetalingsperiode).isEqualTo("202107")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.inntektAinntektListe[1].inntektspostAinntektListe[0].opptjeningsperiodeFra).isEqualTo(LocalDate.parse("2021-06-01"))},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.inntektAinntektListe[1].inntektspostAinntektListe[0].opptjeningsperiodeTil).isEqualTo(LocalDate.parse("2021-07-01"))},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.inntektAinntektListe[1].inntektspostAinntektListe[0].opplysningspliktigId).isEqualTo("9876543210")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.inntektAinntektListe[1].inntektspostAinntektListe[0].inntektType).isEqualTo("Loenn")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.inntektAinntektListe[1].inntektspostAinntektListe[0].fordelType).isEqualTo("Kontantytelse")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.inntektAinntektListe[1].inntektspostAinntektListe[0].beskrivelse).isEqualTo("Loenn/fastloenn")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.inntektAinntektListe[1].inntektspostAinntektListe[0].belop).isEqualTo(BigDecimal.valueOf(666000.01))},

      Executable { assertThat(komplettGrunnlagspakkeFunnet.skattegrunnlagListe[0].inntektspostSkattListe.size).isEqualTo(1)},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.skattegrunnlagListe[0].personId).isEqualTo("345678")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.skattegrunnlagListe[0].inntektspostSkattListe[0].inntektType).isEqualTo("Loenn")},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.skattegrunnlagListe[0].inntektspostSkattListe[0].belop).isEqualTo(BigDecimal.valueOf(23456.01))},

      Executable { assertThat(komplettGrunnlagspakkeFunnet.ubstListe.size).isEqualTo(1) },
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ubstListe[0].personId).isEqualTo("22334455") },
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ubstListe[0].type).isEqualTo("Utvidet barnetrygd") },
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ubstListe[0].periodeFra).isEqualTo(LocalDate.parse("2021-05-01"))},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ubstListe[0].periodeTil).isEqualTo(LocalDate.parse("2021-06-01"))},
      Executable { assertThat(komplettGrunnlagspakkeFunnet.ubstListe[0].belop).isEqualTo(BigDecimal.valueOf(12468.01))},

      )
  }
}
