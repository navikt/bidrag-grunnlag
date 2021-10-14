package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.grunnlag.BidragGrunnlagLocal
import no.nav.bidrag.grunnlag.api.OpprettGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.dto.InntektDto
import no.nav.bidrag.grunnlag.dto.InntektspostDto
import no.nav.bidrag.grunnlag.dto.StonadDto
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

    val inntektDto = InntektDto(
      grunnlagspakkeId = nyGrunnlagspakkeOpprettet.grunnlagspakkeId,
      personId = 1234567,
      type = "Loennsinntekt",
      gyldigFra = LocalDate.parse("2021-05-01"),
      gyldigTil = LocalDate.parse("2021-06-01"),
      aktiv = true,
      hentetTidspunkt = LocalDateTime.now(),
      brukFra = LocalDateTime.now(),
      brukTil = null
    )

    val opprettetInntekt = persistenceService.opprettInntekt(inntektDto)

    persistenceService.opprettInntektspost(
      InntektspostDto(
        inntektId = opprettetInntekt.inntektId,
        utbetalingsperiode = "202106",
        opptjeningsperiodeFra = LocalDate.parse("2021-05-01"),
        opptjeningsperiodeTil = LocalDate.parse("2021-06-01"),
        opplysningspliktigId = "1234567890",
        inntektType = "Loenn",
        fordelType = "Kontantytelse",
        beskrivelse = "Loenn/fastloenn",
        belop = BigDecimal.valueOf(17000.01)
      )
    )

    persistenceService.opprettInntektspost(
      InntektspostDto(
        inntektId = opprettetInntekt.inntektId,
        utbetalingsperiode = "202106",
        opptjeningsperiodeFra = LocalDate.parse("2021-05-01"),
        opptjeningsperiodeTil = LocalDate.parse("2021-06-01"),
        opplysningspliktigId = "1234567890",
        inntektType = "Loenn",
        fordelType = "Kontantytelse",
        beskrivelse = "Loenn/ferieLoenn",
        belop = BigDecimal.valueOf(50000.01)
      )
    )

    // tester at inntekt som er merket med aktiv = false ikke hentes
    val innaktivInntektDto = InntektDto(
      grunnlagspakkeId = nyGrunnlagspakkeOpprettet.grunnlagspakkeId,
      personId = 1234567,
      type = "Loennsinntekt",
      gyldigFra = LocalDate.parse("2020-05-01"),
      gyldigTil = LocalDate.parse("2020-06-01"),
      aktiv = false,
      hentetTidspunkt = LocalDateTime.now(),
      brukFra = LocalDateTime.now(),
      brukTil = null
    )

    val innaktivInntekt = persistenceService.opprettInntekt(innaktivInntektDto)

    persistenceService.opprettInntektspost(
      InntektspostDto(
        inntektId = innaktivInntekt.inntektId,
        utbetalingsperiode = "202006",
        opptjeningsperiodeFra = LocalDate.parse("2020-05-01"),
        opptjeningsperiodeTil = LocalDate.parse("2020-06-01"),
        opplysningspliktigId = "1234567890",
        inntektType = "Loenn",
        fordelType = "Kontantytelse",
        beskrivelse = "Loenn/fastloenn",
        belop = BigDecimal.valueOf(50000.01)
      )
    )


    // Legger inn inntekt for person nr 2
    val inntektDto2 = InntektDto(
      grunnlagspakkeId = nyGrunnlagspakkeOpprettet.grunnlagspakkeId,
      personId = 999999,
      type = "Loennsinntekt",
      gyldigFra = LocalDate.parse("2021-06-01"),
      gyldigTil = LocalDate.parse("2021-07-01"),
      aktiv = true,
      hentetTidspunkt = LocalDateTime.now(),
      brukFra = LocalDateTime.now(),
      brukTil = null
    )

    val opprettetInntekt2 = persistenceService.opprettInntekt(inntektDto2)

    persistenceService.opprettInntektspost(
      InntektspostDto(
        inntektId = opprettetInntekt2.inntektId,
        utbetalingsperiode = "202107",
        opptjeningsperiodeFra = LocalDate.parse("2021-06-01"),
        opptjeningsperiodeTil = LocalDate.parse("2021-07-01"),
        opplysningspliktigId = "9876543210",
        inntektType = "Loenn",
        fordelType = "Kontantytelse",
        beskrivelse = "Loenn/fastloenn",
        belop = BigDecimal.valueOf(666000.01)
      )
    )

    persistenceService.opprettStonad(
      StonadDto(
        grunnlagspakkeId = nyGrunnlagspakkeOpprettet.grunnlagspakkeId,
        personId = 1234567,
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
      Executable {
        assertThat(grunnlagspakkeFunnet.grunnlagspakkeId).isEqualTo(
          nyGrunnlagspakkeOpprettet.grunnlagspakkeId
        )
      },
      Executable { assertThat(grunnlagspakkeFunnet.inntektListe.size).isEqualTo(2) },
      Executable { assertThat(grunnlagspakkeFunnet.inntektListe[0].personId).isEqualTo(1234567) },
      Executable { assertThat(grunnlagspakkeFunnet.inntektListe[0].type).isEqualTo("Loennsinntekt") },
      Executable { assertThat(grunnlagspakkeFunnet.inntektListe[1].personId).isEqualTo(999999) },
      Executable { assertThat(grunnlagspakkeFunnet.inntektListe[1].type).isEqualTo("Loennsinntekt") },

      Executable {
        assertThat(grunnlagspakkeFunnet.inntektListe[0].inntektspostListe.size).isEqualTo(
          2
        )
      },
      Executable {
        assertThat(grunnlagspakkeFunnet.inntektListe[0].inntektspostListe[0].utbetalingsperiode).isEqualTo(
          "202106"
        )
      },
      Executable {
        assertThat(grunnlagspakkeFunnet.inntektListe[0].inntektspostListe[0].opptjeningsperiodeFra).isEqualTo(
          LocalDate.parse("2021-05-01")
        )
      },
      Executable {
        assertThat(grunnlagspakkeFunnet.inntektListe[0].inntektspostListe[0].opptjeningsperiodeTil).isEqualTo(
          LocalDate.parse("2021-06-01")
        )
      },
      Executable {
        assertThat(grunnlagspakkeFunnet.inntektListe[0].inntektspostListe[0].opplysningspliktigId).isEqualTo(
          "1234567890"
        )
      },
      Executable {
        assertThat(grunnlagspakkeFunnet.inntektListe[0].inntektspostListe[0].inntektType).isEqualTo(
          "Loenn"
        )
      },
      Executable {
        assertThat(grunnlagspakkeFunnet.inntektListe[0].inntektspostListe[0].fordelType).isEqualTo(
          "Kontantytelse"
        )
      },
      Executable {
        assertThat(grunnlagspakkeFunnet.inntektListe[0].inntektspostListe[0].beskrivelse).isEqualTo(
          "Loenn/fastloenn"
        )
      },
      Executable {
        assertThat(grunnlagspakkeFunnet.inntektListe[0].inntektspostListe[0].belop).isEqualTo(
          BigDecimal.valueOf(17000.01)
        )
      },

      Executable {
        assertThat(grunnlagspakkeFunnet.inntektListe[0].inntektspostListe[1].utbetalingsperiode).isEqualTo(
          "202106"
        )
      },
      Executable {
        assertThat(grunnlagspakkeFunnet.inntektListe[0].inntektspostListe[1].opptjeningsperiodeFra).isEqualTo(
          LocalDate.parse("2021-05-01")
        )
      },
      Executable {
        assertThat(grunnlagspakkeFunnet.inntektListe[0].inntektspostListe[1].opptjeningsperiodeTil).isEqualTo(
          LocalDate.parse("2021-06-01")
        )
      },
      Executable {
        assertThat(grunnlagspakkeFunnet.inntektListe[0].inntektspostListe[1].opplysningspliktigId).isEqualTo(
          "1234567890"
        )
      },
      Executable {
        assertThat(grunnlagspakkeFunnet.inntektListe[0].inntektspostListe[1].inntektType).isEqualTo(
          "Loenn"
        )
      },
      Executable {
        assertThat(grunnlagspakkeFunnet.inntektListe[0].inntektspostListe[1].fordelType).isEqualTo(
          "Kontantytelse"
        )
      },
      Executable {
        assertThat(grunnlagspakkeFunnet.inntektListe[0].inntektspostListe[1].beskrivelse).isEqualTo(
          "Loenn/ferieLoenn"
        )
      },
      Executable {
        assertThat(grunnlagspakkeFunnet.inntektListe[0].inntektspostListe[1].belop).isEqualTo(
          BigDecimal.valueOf(50000.01)
        )
      },

      Executable {
        assertThat(grunnlagspakkeFunnet.inntektListe[1].inntektspostListe.size).isEqualTo(
          1
        )
      },
      Executable {
        assertThat(grunnlagspakkeFunnet.inntektListe[1].inntektspostListe[0].utbetalingsperiode).isEqualTo(
          "202107"
        )
      },
      Executable {
        assertThat(grunnlagspakkeFunnet.inntektListe[1].inntektspostListe[0].opptjeningsperiodeFra).isEqualTo(
          LocalDate.parse("2021-06-01")
        )
      },
      Executable {
        assertThat(grunnlagspakkeFunnet.inntektListe[1].inntektspostListe[0].opptjeningsperiodeTil).isEqualTo(
          LocalDate.parse("2021-07-01")
        )
      },
      Executable {
        assertThat(grunnlagspakkeFunnet.inntektListe[1].inntektspostListe[0].opplysningspliktigId).isEqualTo(
          "9876543210"
        )
      },
      Executable {
        assertThat(grunnlagspakkeFunnet.inntektListe[1].inntektspostListe[0].inntektType).isEqualTo(
          "Loenn"
        )
      },
      Executable {
        assertThat(grunnlagspakkeFunnet.inntektListe[1].inntektspostListe[0].fordelType).isEqualTo(
          "Kontantytelse"
        )
      },
      Executable {
        assertThat(grunnlagspakkeFunnet.inntektListe[1].inntektspostListe[0].beskrivelse).isEqualTo(
          "Loenn/fastloenn"
        )
      },
      Executable {
        assertThat(grunnlagspakkeFunnet.inntektListe[1].inntektspostListe[0].belop).isEqualTo(
          BigDecimal.valueOf(666000.01)
        )
      },

      Executable { assertThat(grunnlagspakkeFunnet.stonadListe.size).isEqualTo(1) },
      Executable { assertThat(grunnlagspakkeFunnet.stonadListe[0].personId).isEqualTo(1234567) },
      Executable { assertThat(grunnlagspakkeFunnet.stonadListe[0].type).isEqualTo("Utvidet barnetrygd") },
      Executable {
        assertThat(grunnlagspakkeFunnet.stonadListe[0].periodeFra).isEqualTo(
          LocalDate.parse(
            "2021-05-01"
          )
        )
      },
      Executable {
        assertThat(grunnlagspakkeFunnet.stonadListe[0].periodeTil).isEqualTo(
          LocalDate.parse(
            "2021-06-01"
          )
        )
      },
      Executable {
        assertThat(grunnlagspakkeFunnet.stonadListe[0].belop).isEqualTo(
          BigDecimal.valueOf(
            12468.01
          )
        )
      },

      )
  }
}
