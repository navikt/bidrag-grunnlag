package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.grunnlag.BidragGrunnlagLocal
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OpprettGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.dto.InntektAinntektDto
import no.nav.bidrag.grunnlag.dto.InntektSkattDto
import no.nav.bidrag.grunnlag.dto.InntektspostAinntektDto
import no.nav.bidrag.grunnlag.dto.InntektspostSkattDto
import no.nav.bidrag.grunnlag.dto.UtvidetBarnetrygdOgSmaabarnstilleggDto

import no.nav.bidrag.grunnlag.persistence.repository.GrunnlagspakkeRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
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
@Disabled
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

  @Test
  @Suppress("NonAsciiCharacters")
  fun `Test på oppdatere grunnlagspakke`() {
    val opprettGrunnlagspakkeRequest = OpprettGrunnlagspakkeRequest("X123456")
    val nyGrunnlagspakkeOpprettet =
      grunnlagspakkeService.opprettGrunnlagspakke(opprettGrunnlagspakkeRequest)

    assertAll(
      Executable { assertThat(nyGrunnlagspakkeOpprettet).isNotNull }
    )
  }

  @Test
  @Suppress("NonAsciiCharacters")
  fun `Test på hente grunnlagspakke med aktive inntekter + stønader`() {
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
    val inntektSkattDto = InntektSkattDto(
      grunnlagspakkeId = nyGrunnlagspakkeOpprettet.grunnlagspakkeId,
      personId = "345678",
      periodeFra = LocalDate.parse("2021-01-01"),
      periodeTil = LocalDate.parse("2021-12-01"),
      aktiv = true,
      hentetTidspunkt = LocalDateTime.now(),
      brukFra = LocalDateTime.now(),
      brukTil = null
    )

    persistenceService.opprettInntektSkatt(inntektSkattDto)

    persistenceService.opprettInntektspostSkatt(
      InntektspostSkattDto(
        inntektId = inntektSkattDto.inntektId,
        type = "Loenn",
        belop = BigDecimal.valueOf(23456.01)
      )
    )

    persistenceService.opprettUtvidetBarnetrygdOgSmaabarnstillegg(
      UtvidetBarnetrygdOgSmaabarnstilleggDto(
        grunnlagspakkeId = nyGrunnlagspakkeOpprettet.grunnlagspakkeId,
        personId = "1234567",
        type = "Utvidet barnetrygd",
        periodeFra = LocalDate.parse("2021-05-01"),
        periodeTil = LocalDate.parse("2021-06-01"),
        belop = BigDecimal.valueOf(12468.01)
      )
    )

    val grunnlagspakkeFunnet =
      grunnlagspakkeService.hentGrunnlagspakke(nyGrunnlagspakkeOpprettet.grunnlagspakkeId)

    assertAll(
      Executable { assertThat(grunnlagspakkeFunnet).isNotNull },
      Executable { assertThat(grunnlagspakkeFunnet.grunnlagspakkeId).isEqualTo(nyGrunnlagspakkeOpprettet.grunnlagspakkeId)},
      Executable { assertThat(grunnlagspakkeFunnet.inntektAinntektListe.size).isEqualTo(2) },
      Executable { assertThat(grunnlagspakkeFunnet.inntektAinntektListe[0].personId).isEqualTo(1234567)},
      Executable { assertThat(grunnlagspakkeFunnet.inntektAinntektListe[1].personId).isEqualTo(999999)},

      Executable { assertThat(grunnlagspakkeFunnet.inntektAinntektListe[0].inntektspostAinntektListe.size).isEqualTo(2)},
      Executable { assertThat(grunnlagspakkeFunnet.inntektAinntektListe[0].inntektspostAinntektListe[0].utbetalingsperiode).isEqualTo("202106")},
      Executable { assertThat(grunnlagspakkeFunnet.inntektAinntektListe[0].inntektspostAinntektListe[0].opptjeningsperiodeFra).isEqualTo(LocalDate.parse("2021-05-01"))},
      Executable { assertThat(grunnlagspakkeFunnet.inntektAinntektListe[0].inntektspostAinntektListe[0].opptjeningsperiodeTil).isEqualTo(LocalDate.parse("2021-06-01"))},
      Executable { assertThat(grunnlagspakkeFunnet.inntektAinntektListe[0].inntektspostAinntektListe[0].opplysningspliktigId).isEqualTo("1234567890")},
      Executable { assertThat(grunnlagspakkeFunnet.inntektAinntektListe[0].inntektspostAinntektListe[0].inntektType).isEqualTo("Loenn")},
      Executable { assertThat(grunnlagspakkeFunnet.inntektAinntektListe[0].inntektspostAinntektListe[0].fordelType).isEqualTo("Kontantytelse")},
      Executable { assertThat(grunnlagspakkeFunnet.inntektAinntektListe[0].inntektspostAinntektListe[0].beskrivelse).isEqualTo("Loenn/fastloenn")},
      Executable { assertThat(grunnlagspakkeFunnet.inntektAinntektListe[0].inntektspostAinntektListe[0].belop).isEqualTo(BigDecimal.valueOf(17000.01))},

      Executable { assertThat(grunnlagspakkeFunnet.inntektAinntektListe[0].inntektspostAinntektListe[1].utbetalingsperiode).isEqualTo("202106")},
      Executable { assertThat(grunnlagspakkeFunnet.inntektAinntektListe[0].inntektspostAinntektListe[1].opptjeningsperiodeFra).isEqualTo(LocalDate.parse("2021-05-01"))},
      Executable { assertThat(grunnlagspakkeFunnet.inntektAinntektListe[0].inntektspostAinntektListe[1].opptjeningsperiodeTil).isEqualTo(LocalDate.parse("2021-06-01"))},
      Executable { assertThat(grunnlagspakkeFunnet.inntektAinntektListe[0].inntektspostAinntektListe[1].opplysningspliktigId).isEqualTo("1234567890")},
      Executable { assertThat(grunnlagspakkeFunnet.inntektAinntektListe[0].inntektspostAinntektListe[1].inntektType).isEqualTo("Loenn")},
      Executable { assertThat(grunnlagspakkeFunnet.inntektAinntektListe[0].inntektspostAinntektListe[1].fordelType).isEqualTo("Kontantytelse")},
      Executable { assertThat(grunnlagspakkeFunnet.inntektAinntektListe[0].inntektspostAinntektListe[1].beskrivelse).isEqualTo("Loenn/ferieLoenn")},
      Executable { assertThat(grunnlagspakkeFunnet.inntektAinntektListe[0].inntektspostAinntektListe[1].belop).isEqualTo(BigDecimal.valueOf(50000.01))},

      Executable { assertThat(grunnlagspakkeFunnet.inntektAinntektListe[1].inntektspostAinntektListe.size).isEqualTo(1)},
      Executable { assertThat(grunnlagspakkeFunnet.inntektAinntektListe[1].inntektspostAinntektListe[0].utbetalingsperiode).isEqualTo("202107")},
      Executable { assertThat(grunnlagspakkeFunnet.inntektAinntektListe[1].inntektspostAinntektListe[0].opptjeningsperiodeFra).isEqualTo(LocalDate.parse("2021-06-01"))},
      Executable { assertThat(grunnlagspakkeFunnet.inntektAinntektListe[1].inntektspostAinntektListe[0].opptjeningsperiodeTil).isEqualTo(LocalDate.parse("2021-07-01"))},
      Executable { assertThat(grunnlagspakkeFunnet.inntektAinntektListe[1].inntektspostAinntektListe[0].opplysningspliktigId).isEqualTo("9876543210")},
      Executable { assertThat(grunnlagspakkeFunnet.inntektAinntektListe[1].inntektspostAinntektListe[0].inntektType).isEqualTo("Loenn")},
      Executable { assertThat(grunnlagspakkeFunnet.inntektAinntektListe[1].inntektspostAinntektListe[0].fordelType).isEqualTo("Kontantytelse")},
      Executable { assertThat(grunnlagspakkeFunnet.inntektAinntektListe[1].inntektspostAinntektListe[0].beskrivelse).isEqualTo("Loenn/fastloenn")},
      Executable { assertThat(grunnlagspakkeFunnet.inntektAinntektListe[1].inntektspostAinntektListe[0].belop).isEqualTo(BigDecimal.valueOf(666000.01))},

      Executable { assertThat(grunnlagspakkeFunnet.inntektSkattListe[0].inntektspostSkattListe.size).isEqualTo(1)},
      Executable { assertThat(grunnlagspakkeFunnet.inntektSkattListe[0].personId).isEqualTo("34567")},
      Executable { assertThat(grunnlagspakkeFunnet.inntektSkattListe[0].inntektspostSkattListe[0].inntektType).isEqualTo("Loenn")},
      Executable { assertThat(grunnlagspakkeFunnet.inntektSkattListe[0].inntektspostSkattListe[0].belop).isEqualTo(BigDecimal.valueOf(23456.01))},

      Executable { assertThat(grunnlagspakkeFunnet.ubstListe.size).isEqualTo(1) },
      Executable { assertThat(grunnlagspakkeFunnet.ubstListe[0].personId).isEqualTo(1234567) },
      Executable { assertThat(grunnlagspakkeFunnet.ubstListe[0].type).isEqualTo("Utvidet barnetrygd") },
      Executable { assertThat(grunnlagspakkeFunnet.ubstListe[0].periodeFra).isEqualTo(LocalDate.parse("2021-05-01"))},
      Executable { assertThat(grunnlagspakkeFunnet.ubstListe[0].periodeTil).isEqualTo(LocalDate.parse("2021-06-01"))},
      Executable { assertThat(grunnlagspakkeFunnet.ubstListe[0].belop).isEqualTo(BigDecimal.valueOf(12468.01))},

      )
  }
}
