package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.grunnlag.TestUtil.Companion.byggGrunnlagspakkeDto
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggInntektDto
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggInntektspostDto
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggNyGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggStonadDto
import no.nav.bidrag.grunnlag.consumer.FamilieBaSakConsumer
import no.nav.bidrag.grunnlag.dto.GrunnlagspakkeDto
import no.nav.bidrag.grunnlag.dto.InntektDto
import no.nav.bidrag.grunnlag.dto.InntektspostDto
import no.nav.bidrag.grunnlag.dto.StonadDto

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.function.Executable
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.LocalDate

@DisplayName("GrunnlagspakkeServiceMockTest")
@ExtendWith(MockitoExtension::class)
class GrunnlagspakkeServiceMockTest {

  @InjectMocks
  private lateinit var grunnlagspakkeService: GrunnlagspakkeService

  @Mock
  private lateinit var persistenceServiceMock: PersistenceService

  @Mock
  private lateinit var familieBaSakConsumerMock: FamilieBaSakConsumer

  @Captor
  private lateinit var grunnlagspakkeDtoCaptor: ArgumentCaptor<GrunnlagspakkeDto>

  @Captor
  private lateinit var inntektDtoCaptor: ArgumentCaptor<InntektDto>

  @Captor
  private lateinit var inntektspostDtoCaptor: ArgumentCaptor<InntektspostDto>

  @Captor
  private lateinit var stonadDtoCaptor: ArgumentCaptor<StonadDto>

  @Test
  fun `Skal opprette ny grunnlagspakke`() {
    Mockito.`when`(persistenceServiceMock.opprettNyGrunnlagspakke(MockitoHelper.capture(grunnlagspakkeDtoCaptor)))
      .thenReturn(byggGrunnlagspakkeDto())
    val nyGrunnlagspakkeOpprettet = grunnlagspakkeService.opprettGrunnlagspakke(byggNyGrunnlagspakkeRequest())
    val grunnlagspakkeDto = grunnlagspakkeDtoCaptor.value
    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettNyGrunnlagspakke(MockitoHelper.any(GrunnlagspakkeDto::class.java))
    assertAll(
      Executable { assertThat(nyGrunnlagspakkeOpprettet).isNotNull() },
      // sjekk GrunnlagspakkeDto
      Executable { assertThat(grunnlagspakkeDto).isNotNull() }
    )
  }

  @Test
  @Suppress("NonAsciiCharacters")
  fun `Skal hente grunnlagspakke med tilhørende grunnlag`() {

    Mockito.`when`(persistenceServiceMock.opprettNyGrunnlagspakke(MockitoHelper.capture(grunnlagspakkeDtoCaptor)))
      .thenReturn(byggGrunnlagspakkeDto())
    Mockito.`when`(persistenceServiceMock.opprettInntekt(MockitoHelper.capture(inntektDtoCaptor)))
      .thenReturn(byggInntektDto())
    Mockito.`when`(persistenceServiceMock.opprettInntektspost(MockitoHelper.capture(inntektspostDtoCaptor)))
      .thenReturn(byggInntektspostDto())
    Mockito.`when`(persistenceServiceMock.opprettStonad(MockitoHelper.capture(stonadDtoCaptor)))
      .thenReturn(byggStonadDto())

    val nyGrunnlagspakkeOpprettet = grunnlagspakkeService.opprettGrunnlagspakke(byggNyGrunnlagspakkeRequest())
    val nyInntektOpprettet = persistenceServiceMock.opprettInntekt(byggInntektDto())
    val nyInntektspostOpprettet = persistenceServiceMock.opprettInntektspost(byggInntektspostDto())
    val nyStonadOpprettet = persistenceServiceMock.opprettStonad(byggStonadDto())

    val grunnlagspakkeDto = grunnlagspakkeDtoCaptor.value
    val inntektDtoListe = inntektDtoCaptor.allValues
    val inntektspostDtoListe = inntektspostDtoCaptor.allValues
    val stonadDtoListe = stonadDtoCaptor.allValues

    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettNyGrunnlagspakke(MockitoHelper.any(GrunnlagspakkeDto::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettInntekt(MockitoHelper.any(InntektDto::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettInntektspost(MockitoHelper.any(InntektspostDto::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettStonad(MockitoHelper.any(StonadDto::class.java))

    assertAll(
      Executable { assertThat(nyGrunnlagspakkeOpprettet).isNotNull() },
      Executable { assertThat(nyGrunnlagspakkeOpprettet.grunnlagspakkeId).isNotNull() },

      Executable { assertThat(nyInntektOpprettet).isNotNull() },
      Executable { assertThat(nyInntektOpprettet.inntektId).isNotNull() },

      Executable { assertThat(nyInntektspostOpprettet).isNotNull() },
      Executable { assertThat(nyInntektspostOpprettet.inntektspostId).isNotNull() },

      Executable { assertThat(nyStonadOpprettet).isNotNull() },
      Executable { assertThat(nyStonadOpprettet.stonadId).isNotNull() },

      // sjekk GrunnlagspakkeDto
      Executable { assertThat(grunnlagspakkeDto).isNotNull() },
      Executable { assertThat(grunnlagspakkeDto.grunnlagspakkeId).isNotNull() },
      Executable { assertThat(grunnlagspakkeDto.opprettetAv).isEqualTo("RTV9999") },

      // sjekk InntektDto
      Executable { assertThat(inntektDtoListe[0].personId).isEqualTo(1234567) },
      Executable { assertThat(inntektDtoListe[0].type).isEqualTo("Loennsinntekt") },
      Executable { assertThat(inntektDtoListe[0].aktiv).isTrue },
      Executable { assertThat(inntektDtoListe[0].gyldigFra).isEqualTo(LocalDate.parse("2021-07-01")) },
      Executable { assertThat(inntektDtoListe[0].gyldigTil).isEqualTo(LocalDate.parse("2021-08-01")) },

      // sjekk InntektspostDto
      Executable { assertThat(inntektspostDtoListe.size).isEqualTo(1) },

      Executable { assertThat(inntektspostDtoListe[0].utbetalingsperiode).isEqualTo("202108") },
      Executable { assertThat(inntektspostDtoListe[0].opptjeningsperiodeFra).isEqualTo(LocalDate.parse("2021-07-01")) },
      Executable { assertThat(inntektspostDtoListe[0].opptjeningsperiodeTil).isEqualTo(LocalDate.parse("2021-08-01")) },
      Executable { assertThat(inntektspostDtoListe[0].opplysningspliktigId).isEqualTo(("123")) },
      Executable { assertThat(inntektspostDtoListe[0].inntektType).isEqualTo(("Loenn")) },
      Executable { assertThat(inntektspostDtoListe[0].fordelType).isEqualTo(("Kontantytelse")) },
      Executable { assertThat(inntektspostDtoListe[0].beskrivelse).isEqualTo(("Loenn/ferieLoenn")) },
      Executable { assertThat(inntektspostDtoListe[0].belop).isEqualTo(BigDecimal.valueOf(50000)) },

      )
  }

//  @Test
//  @Suppress("NonAsciiCharacters")
//  fun `Skal oppdatere grunnlagspakke`() {
//    Mockito.`when`(persistenceServiceMock.opprettNyGrunnlagspakke(MockitoHelper.any(GrunnlagspakkeDto::class.java)))
//      .thenReturn(byggGrunnlagspakkeDto())
//    Mockito.`when`(familieBaSakConsumerMock.hentFamilieBaSak(MockitoHelper.any(FamilieBaSakRequest::class.java)))
//      .thenReturn(byggFamilieBaSakResponse())
//
//    val opprettGrunnlagspakkeRequest = OpprettGrunnlagspakkeRequest("X123456")
//    val nyGrunnlagspakkeOpprettet =
//      grunnlagspakkeService.opprettGrunnlagspakke(opprettGrunnlagspakkeRequest)
//
//    val oppdatertGrunnlagspakke =
//      grunnlagspakkeService.oppdaterGrunnlagspakke(TestUtil.byggOppdaterGrunnlagspakkeRequest(nyGrunnlagspakkeOpprettet.grunnlagspakkeId))
//
//    assertAll(
//      Executable { assertThat(nyGrunnlagspakkeOpprettet).isNotNull },
//      Executable { assertThat(oppdatertGrunnlagspakke).isNotNull }
//    )
//  }

  object MockitoHelper {
    // use this in place of captor.capture() if you are trying to capture an argument that is not nullable
    fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()
    fun <T> any(type: Class<T>): T = Mockito.any(type)
  }
}
