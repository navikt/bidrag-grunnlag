package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.grunnlag.TestUtil.Companion.byggGrunnlagspakkeDto
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggInntektAinntektDto
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggSkattegrunnlagSkattDto
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggInntektspostAinntektDto
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggSkattegrunnlagspostDto
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggNyGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggUtvidetBarnetrygdOgSmaabarnstilleggDto
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.BidragGcpProxyConsumer

import no.nav.bidrag.grunnlag.consumer.familiebasak.FamilieBaSakConsumer
import no.nav.bidrag.grunnlag.dto.GrunnlagspakkeDto
import no.nav.bidrag.grunnlag.dto.InntektAinntektDto
import no.nav.bidrag.grunnlag.dto.SkattegrunnlagDto
import no.nav.bidrag.grunnlag.dto.InntektspostAinntektDto
import no.nav.bidrag.grunnlag.dto.SkattegrunnlagspostDto
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

  @Mock
  private lateinit var familieBaSakConsumerMock: FamilieBaSakConsumer

  @Mock
  private lateinit var bidragGcpProxyConsumerMock: BidragGcpProxyConsumer

  @Captor
  private lateinit var grunnlagspakkeDtoCaptor: ArgumentCaptor<GrunnlagspakkeDto>

  @Captor
  private lateinit var inntektAinntektDtoCaptor: ArgumentCaptor<InntektAinntektDto>

  @Captor
  private lateinit var inntektspostAinntektDtoCaptor: ArgumentCaptor<InntektspostAinntektDto>

  @Captor
  private lateinit var skattegrunnlagDtoCaptor: ArgumentCaptor<SkattegrunnlagDto>

  @Captor
  private lateinit var skattegrunnlagspostDtoCaptor: ArgumentCaptor<SkattegrunnlagspostDto>

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
    Mockito.`when`(persistenceServiceMock.opprettInntektAinntekt(MockitoHelper.capture(inntektAinntektDtoCaptor)))
      .thenReturn(byggInntektAinntektDto())
    Mockito.`when`(persistenceServiceMock.opprettInntektspostAinntekt(MockitoHelper.capture(inntektspostAinntektDtoCaptor)))
      .thenReturn(byggInntektspostAinntektDto())
    Mockito.`when`(persistenceServiceMock.opprettSkattegrunnlag(MockitoHelper.capture(skattegrunnlagDtoCaptor)))
      .thenReturn(byggSkattegrunnlagSkattDto())
    Mockito.`when`(persistenceServiceMock.opprettSkattegrunnlagspost(MockitoHelper.capture(skattegrunnlagspostDtoCaptor)))
      .thenReturn(byggSkattegrunnlagspostDto())
    Mockito.`when`(persistenceServiceMock.opprettUtvidetBarnetrygdOgSmaabarnstillegg(MockitoHelper.capture(utvidetBarnetrygdOgSmaabarnstilleggDtoCaptor)))
      .thenReturn(byggUtvidetBarnetrygdOgSmaabarnstilleggDto())

    val nyGrunnlagspakkeOpprettet = grunnlagspakkeService.opprettGrunnlagspakke(byggNyGrunnlagspakkeRequest())
    val nyInntektAinntektOpprettet = persistenceServiceMock.opprettInntektAinntekt(byggInntektAinntektDto())
    val nyInntektspostAinntektOpprettet = persistenceServiceMock.opprettInntektspostAinntekt(byggInntektspostAinntektDto())
    val nyttSkattegrunnlagOpprettet = persistenceServiceMock.opprettSkattegrunnlag(byggSkattegrunnlagSkattDto())
    val nySkattegrunnlagspostOpprettet = persistenceServiceMock.opprettSkattegrunnlagspost(byggSkattegrunnlagspostDto())
    val nyUtvidetBarnetrygdOgSmaabarnstilleggOpprettet = persistenceServiceMock.opprettUtvidetBarnetrygdOgSmaabarnstillegg(byggUtvidetBarnetrygdOgSmaabarnstilleggDto())

    val grunnlagspakkeDto = grunnlagspakkeDtoCaptor.value
    val inntektAinntektDtoListe = inntektAinntektDtoCaptor.allValues
    val inntektspostAinntektDtoListe = inntektspostAinntektDtoCaptor.allValues
    val skattegrunnlagDtoListe = skattegrunnlagDtoCaptor.allValues
    val skattegrunnlagspostDtoListe = skattegrunnlagspostDtoCaptor.allValues
    val ubstListe = utvidetBarnetrygdOgSmaabarnstilleggDtoCaptor.allValues

    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettNyGrunnlagspakke(MockitoHelper.any(GrunnlagspakkeDto::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettInntektAinntekt(MockitoHelper.any(InntektAinntektDto::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettInntektspostAinntekt(MockitoHelper.any(InntektspostAinntektDto::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettSkattegrunnlag(MockitoHelper.any(SkattegrunnlagDto::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettSkattegrunnlagspost(MockitoHelper.any(SkattegrunnlagspostDto::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettUtvidetBarnetrygdOgSmaabarnstillegg(MockitoHelper.any(UtvidetBarnetrygdOgSmaabarnstilleggDto::class.java))

    assertAll(
      Executable { assertThat(nyGrunnlagspakkeOpprettet).isNotNull() },
      Executable { assertThat(nyGrunnlagspakkeOpprettet.grunnlagspakkeId).isNotNull() },

      Executable { assertThat(nyInntektAinntektOpprettet).isNotNull() },
      Executable { assertThat(nyInntektAinntektOpprettet.inntektId).isNotNull() },

      Executable { assertThat(nyInntektspostAinntektOpprettet).isNotNull() },
      Executable { assertThat(nyInntektspostAinntektOpprettet.inntektspostId).isNotNull() },

      Executable { assertThat(nyttSkattegrunnlagOpprettet).isNotNull() },
      Executable { assertThat(nyttSkattegrunnlagOpprettet.skattegrunnlagId).isNotNull() },

      Executable { assertThat(nySkattegrunnlagspostOpprettet).isNotNull() },
      Executable { assertThat(nySkattegrunnlagspostOpprettet.skattegrunnlagspostId).isNotNull() },

      Executable { assertThat(nyUtvidetBarnetrygdOgSmaabarnstilleggOpprettet).isNotNull() },
      Executable { assertThat(nyUtvidetBarnetrygdOgSmaabarnstilleggOpprettet.ubstId).isNotNull() },

      // sjekk GrunnlagspakkeDto
      Executable { assertThat(grunnlagspakkeDto).isNotNull() },
      Executable { assertThat(grunnlagspakkeDto.grunnlagspakkeId).isNotNull() },
      Executable { assertThat(grunnlagspakkeDto.opprettetAv).isEqualTo("RTV9999") },

      // sjekk InntektAinntektDto
      Executable { assertThat(inntektAinntektDtoListe[0].personId).isEqualTo("1234567") },
      Executable { assertThat(inntektAinntektDtoListe[0].aktiv).isTrue },
      Executable { assertThat(inntektAinntektDtoListe[0].periodeFra).isEqualTo(LocalDate.parse("2021-07-01")) },
      Executable { assertThat(inntektAinntektDtoListe[0].periodeTil).isEqualTo(LocalDate.parse("2021-08-01")) },

      // sjekk InntektspostAinntektDto
      Executable { assertThat(inntektspostAinntektDtoListe.size).isEqualTo(1) },

      Executable { assertThat(inntektspostAinntektDtoListe[0].utbetalingsperiode).isEqualTo("202108") },
      Executable { assertThat(inntektspostAinntektDtoListe[0].opptjeningsperiodeFra).isEqualTo(LocalDate.parse("2021-07-01")) },
      Executable { assertThat(inntektspostAinntektDtoListe[0].opptjeningsperiodeTil).isEqualTo(LocalDate.parse("2021-08-01")) },
      Executable { assertThat(inntektspostAinntektDtoListe[0].opplysningspliktigId).isEqualTo(("123")) },
      Executable { assertThat(inntektspostAinntektDtoListe[0].type).isEqualTo(("Loenn")) },
      Executable { assertThat(inntektspostAinntektDtoListe[0].fordelType).isEqualTo(("Kontantytelse")) },
      Executable { assertThat(inntektspostAinntektDtoListe[0].beskrivelse).isEqualTo(("Loenn/ferieLoenn")) },
      Executable { assertThat(inntektspostAinntektDtoListe[0].belop).isEqualTo(BigDecimal.valueOf(50000)) },


      // sjekk SkattegrunnlagDto
      Executable { assertThat(skattegrunnlagDtoListe[0].personId).isEqualTo("7654321") },
      Executable { assertThat(skattegrunnlagDtoListe[0].aktiv).isTrue },
      Executable { assertThat(skattegrunnlagDtoListe[0].periodeFra).isEqualTo(LocalDate.parse("2021-01-01")) },
      Executable { assertThat(skattegrunnlagDtoListe[0].periodeTil).isEqualTo(LocalDate.parse("2021-12-01")) },

      // sjekk SkattegrunnlagspostDto
      Executable { assertThat(skattegrunnlagspostDtoListe.size).isEqualTo(1) },

      Executable { assertThat(skattegrunnlagspostDtoListe[0].type).isEqualTo(("Loenn")) },
      Executable { assertThat(skattegrunnlagspostDtoListe[0].belop).isEqualTo(BigDecimal.valueOf(171717)) },

      // sjekk UtvidetBarnetrygdOgSmaabarnstilleggdDto
      Executable { assertThat(ubstListe[0].personId).isEqualTo("1234567") },
      Executable { assertThat(ubstListe[0].type).isEqualTo("Utvidet barnetrygd") },
      Executable { assertThat(ubstListe[0].periodeFra).isEqualTo(LocalDate.parse("2021-01-01")) },
      Executable { assertThat(ubstListe[0].periodeTil).isEqualTo(LocalDate.parse("2021-07-01")) },
      Executable { assertThat(ubstListe[0].belop).isEqualTo(BigDecimal.valueOf(12468.01)) },
      Executable { assertThat(ubstListe[0].manueltBeregnet).isFalse },

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
