package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.grunnlag.TestUtil.Companion.byggGrunnlagspakkeDto
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggInntektAinntektDto
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggInntektspostAinntektDto
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggNyGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggUtvidetBarnetrygdOgSmaabarnstilleggDto
import no.nav.bidrag.grunnlag.dto.GrunnlagspakkeDto
import no.nav.bidrag.grunnlag.dto.InntektAinntektDto
import no.nav.bidrag.grunnlag.dto.InntektSkattDto
import no.nav.bidrag.grunnlag.dto.InntektspostAinntektDto
import no.nav.bidrag.grunnlag.dto.UtvidetBarnetrygdOgSmaabarnstilleggDto

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

  @Captor
  private lateinit var grunnlagspakkeDtoCaptor: ArgumentCaptor<GrunnlagspakkeDto>

  @Captor
  private lateinit var inntektDtoCaptor: ArgumentCaptor<InntektAinntektDto>

  @Captor
  private lateinit var inntektspostAinntektDtoCaptor: ArgumentCaptor<InntektspostAinntektDto>

  @Captor
  private lateinit var utvidetBarnetrygdOgSmaabarnstilleggDtoCaptor: ArgumentCaptor<UtvidetBarnetrygdOgSmaabarnstilleggDto>

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
    Mockito.`when`(persistenceServiceMock.opprettInntektAinntekt(MockitoHelper.capture(inntektDtoCaptor)))
      .thenReturn(byggInntektAinntektDto())
    Mockito.`when`(persistenceServiceMock.opprettInntektspostAinntekt(MockitoHelper.capture(inntektspostAinntektDtoCaptor)))
      .thenReturn(byggInntektspostAinntektDto())
    Mockito.`when`(persistenceServiceMock.opprettUtvidetBarnetrygdOgSmaabarnstillegg(MockitoHelper.capture(utvidetBarnetrygdOgSmaabarnstilleggDtoCaptor)))
      .thenReturn(byggUtvidetBarnetrygdOgSmaabarnstilleggDto())

    val nyGrunnlagspakkeOpprettet = grunnlagspakkeService.opprettGrunnlagspakke(byggNyGrunnlagspakkeRequest())
    val nyInntektOpprettet = persistenceServiceMock.opprettInntektAinntekt(byggInntektAinntektDto())
    val nyInntektspostOpprettet = persistenceServiceMock.opprettInntektspostAinntekt(byggInntektspostAinntektDto())
    val nyUtvidetBarnetrygdOgSmaabarnstilleggOpprettet = persistenceServiceMock.opprettUtvidetBarnetrygdOgSmaabarnstillegg(byggUtvidetBarnetrygdOgSmaabarnstilleggDto())

    val grunnlagspakkeDto = grunnlagspakkeDtoCaptor.value
    val inntektDtoListe = inntektDtoCaptor.allValues
    val inntektspostDtoListe = inntektspostAinntektDtoCaptor.allValues
    val ubstListe = utvidetBarnetrygdOgSmaabarnstilleggDtoCaptor.allValues

    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettNyGrunnlagspakke(MockitoHelper.any(GrunnlagspakkeDto::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettInntektAinntekt(MockitoHelper.any(InntektAinntektDto::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettInntektspostAinntekt(MockitoHelper.any(InntektspostAinntektDto::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettUtvidetBarnetrygdOgSmaabarnstillegg(MockitoHelper.any(UtvidetBarnetrygdOgSmaabarnstilleggDto::class.java))

    assertAll(
      Executable { assertThat(nyGrunnlagspakkeOpprettet).isNotNull() },
      Executable { assertThat(nyGrunnlagspakkeOpprettet.grunnlagspakkeId).isNotNull() },

      Executable { assertThat(nyInntektOpprettet).isNotNull() },
      Executable { assertThat(nyInntektOpprettet.inntektId).isNotNull() },

      Executable { assertThat(nyInntektspostOpprettet).isNotNull() },
      Executable { assertThat(nyInntektspostOpprettet.inntektspostId).isNotNull() },

      Executable { assertThat(nyUtvidetBarnetrygdOgSmaabarnstilleggOpprettet).isNotNull() },
      Executable { assertThat(nyUtvidetBarnetrygdOgSmaabarnstilleggOpprettet.ubstId).isNotNull() },

      // sjekk GrunnlagspakkeDto
      Executable { assertThat(grunnlagspakkeDto).isNotNull() },
      Executable { assertThat(grunnlagspakkeDto.grunnlagspakkeId).isNotNull() },
      Executable { assertThat(grunnlagspakkeDto.opprettetAv).isEqualTo("RTV9999") },

      // sjekk InntektDto
      Executable { assertThat(inntektDtoListe[0].personId).isEqualTo("1234567") },
      Executable { assertThat(inntektDtoListe[0].aktiv).isTrue },
      Executable { assertThat(inntektDtoListe[0].periodeFra).isEqualTo(LocalDate.parse("2021-07-01")) },
      Executable { assertThat(inntektDtoListe[0].periodeTil).isEqualTo(LocalDate.parse("2021-08-01")) },

      // sjekk InntektspostDto
      Executable { assertThat(inntektspostDtoListe.size).isEqualTo(1) },
      Executable { assertThat(inntektspostDtoListe[0].utbetalingsperiode).isEqualTo("202108") },
      Executable { assertThat(inntektspostDtoListe[0].opptjeningsperiodeFra).isEqualTo(LocalDate.parse("2021-07-01")) },
      Executable { assertThat(inntektspostDtoListe[0].opptjeningsperiodeTil).isEqualTo(LocalDate.parse("2021-08-01")) },
      Executable { assertThat(inntektspostDtoListe[0].opplysningspliktigId).isEqualTo(("123")) },
      Executable { assertThat(inntektspostDtoListe[0].type).isEqualTo(("Loenn")) },
      Executable { assertThat(inntektspostDtoListe[0].fordelType).isEqualTo(("Kontantytelse")) },
      Executable { assertThat(inntektspostDtoListe[0].beskrivelse).isEqualTo(("Loenn/ferieLoenn")) },
      Executable { assertThat(inntektspostDtoListe[0].belop).isEqualTo(BigDecimal.valueOf(50000)) },

      // sjekk StonadDto
      Executable { assertThat(ubstListe[0].personId).isEqualTo("1234567") },
      Executable { assertThat(ubstListe[0].type).isEqualTo("Utvidet barnetrygd") },
      Executable { assertThat(ubstListe[0].periodeFra).isEqualTo(LocalDate.parse("2021-01-01")) },
      Executable { assertThat(ubstListe[0].periodeTil).isEqualTo(LocalDate.parse("2021-07-01")) },
      Executable { assertThat(ubstListe[0].belop).isEqualTo(BigDecimal.valueOf(12468.01)) },
      Executable { assertThat(ubstListe[0].manueltBeregnet).isFalse },

    )
  }

  object MockitoHelper {
    // use this in place of captor.capture() if you are trying to capture an argument that is not nullable
    fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()
    fun <T> any(type: Class<T>): T = Mockito.any(type)
  }

}