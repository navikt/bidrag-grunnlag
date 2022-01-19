package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.grunnlag.TestUtil
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggAinntektDto
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggAinntektspostDto
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggBarnetilleggDto
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggGrunnlagspakkeDto
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggNyGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggSkattegrunnlagSkattDto
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggSkattegrunnlagspostDto
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggUtvidetBarnetrygdOgSmaabarnstilleggDto
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.BidragGcpProxyConsumer
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.barnetillegg.HentBarnetilleggPensjonRequest
import no.nav.bidrag.grunnlag.consumer.familiebasak.FamilieBaSakConsumer
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.BisysStønadstype
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.FamilieBaSakRequest
import no.nav.bidrag.grunnlag.dto.AinntektDto
import no.nav.bidrag.grunnlag.dto.AinntektspostDto
import no.nav.bidrag.grunnlag.dto.BarnetilleggDto
import no.nav.bidrag.grunnlag.dto.GrunnlagspakkeDto
import no.nav.bidrag.grunnlag.dto.SkattegrunnlagDto
import no.nav.bidrag.grunnlag.dto.SkattegrunnlagspostDto
import no.nav.bidrag.grunnlag.dto.UtvidetBarnetrygdOgSmaabarnstilleggDto
import no.nav.bidrag.grunnlag.exception.RestResponse
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
  private lateinit var ainntektDtoCaptor: ArgumentCaptor<AinntektDto>

  @Captor
  private lateinit var ainntektspostDtoCaptor: ArgumentCaptor<AinntektspostDto>

  @Captor
  private lateinit var skattegrunnlagDtoCaptor: ArgumentCaptor<SkattegrunnlagDto>

  @Captor
  private lateinit var skattegrunnlagspostDtoCaptor: ArgumentCaptor<SkattegrunnlagspostDto>

  @Captor
  private lateinit var utvidetBarnetrygdOgSmaabarnstilleggDtoCaptor: ArgumentCaptor<UtvidetBarnetrygdOgSmaabarnstilleggDto>

  @Captor
  private lateinit var barnetilleggDtoCaptor: ArgumentCaptor<BarnetilleggDto>

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
    Mockito.`when`(persistenceServiceMock.opprettAinntekt(MockitoHelper.capture(ainntektDtoCaptor)))
      .thenReturn(byggAinntektDto())
    Mockito.`when`(persistenceServiceMock.opprettAinntektspost(MockitoHelper.capture(ainntektspostDtoCaptor)))
      .thenReturn(byggAinntektspostDto())
    Mockito.`when`(persistenceServiceMock.opprettSkattegrunnlag(MockitoHelper.capture(skattegrunnlagDtoCaptor)))
      .thenReturn(byggSkattegrunnlagSkattDto())
    Mockito.`when`(persistenceServiceMock.opprettSkattegrunnlagspost(MockitoHelper.capture(skattegrunnlagspostDtoCaptor)))
      .thenReturn(byggSkattegrunnlagspostDto())
    Mockito.`when`(
      persistenceServiceMock.opprettUtvidetBarnetrygdOgSmaabarnstillegg(
        MockitoHelper.capture(
          utvidetBarnetrygdOgSmaabarnstilleggDtoCaptor
        )
      )
    )
      .thenReturn(byggUtvidetBarnetrygdOgSmaabarnstilleggDto())
    Mockito.`when`(persistenceServiceMock.opprettBarnetillegg(MockitoHelper.capture(barnetilleggDtoCaptor)))
      .thenReturn(byggBarnetilleggDto())

    val nyGrunnlagspakkeOpprettet = grunnlagspakkeService.opprettGrunnlagspakke(byggNyGrunnlagspakkeRequest())
    val nyAinntektOpprettet = persistenceServiceMock.opprettAinntekt(byggAinntektDto())
    val nyAinntektspostOpprettet = persistenceServiceMock.opprettAinntektspost(byggAinntektspostDto())
    val nyttSkattegrunnlagOpprettet = persistenceServiceMock.opprettSkattegrunnlag(byggSkattegrunnlagSkattDto())
    val nySkattegrunnlagspostOpprettet = persistenceServiceMock.opprettSkattegrunnlagspost(byggSkattegrunnlagspostDto())
    val nyUtvidetBarnetrygdOgSmaabarnstilleggOpprettet =
      persistenceServiceMock.opprettUtvidetBarnetrygdOgSmaabarnstillegg(byggUtvidetBarnetrygdOgSmaabarnstilleggDto())
    val nyBarnetilleggOpprettet = persistenceServiceMock.opprettBarnetillegg(byggBarnetilleggDto())

    val grunnlagspakkeDto = grunnlagspakkeDtoCaptor.value
    val ainntektDtoListe = ainntektDtoCaptor.allValues
    val ainntektspostDtoListe = ainntektspostDtoCaptor.allValues
    val skattegrunnlagDtoListe = skattegrunnlagDtoCaptor.allValues
    val skattegrunnlagspostDtoListe = skattegrunnlagspostDtoCaptor.allValues
    val ubstListe = utvidetBarnetrygdOgSmaabarnstilleggDtoCaptor.allValues
    val barnetilleggListe = barnetilleggDtoCaptor.allValues

    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettNyGrunnlagspakke(MockitoHelper.any(GrunnlagspakkeDto::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettAinntekt(MockitoHelper.any(AinntektDto::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettAinntektspost(MockitoHelper.any(AinntektspostDto::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettSkattegrunnlag(MockitoHelper.any(SkattegrunnlagDto::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettSkattegrunnlagspost(MockitoHelper.any(SkattegrunnlagspostDto::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(1))
      .opprettUtvidetBarnetrygdOgSmaabarnstillegg(MockitoHelper.any(UtvidetBarnetrygdOgSmaabarnstilleggDto::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettBarnetillegg(MockitoHelper.any(BarnetilleggDto::class.java))

    assertAll(
      Executable { assertThat(nyGrunnlagspakkeOpprettet).isNotNull() },
      Executable { assertThat(nyGrunnlagspakkeOpprettet.grunnlagspakkeId).isNotNull() },

      Executable { assertThat(nyAinntektOpprettet).isNotNull() },
      Executable { assertThat(nyAinntektOpprettet.inntektId).isNotNull() },

      Executable { assertThat(nyAinntektspostOpprettet).isNotNull() },
      Executable { assertThat(nyAinntektspostOpprettet.inntektspostId).isNotNull() },

      Executable { assertThat(nyttSkattegrunnlagOpprettet).isNotNull() },
      Executable { assertThat(nyttSkattegrunnlagOpprettet.skattegrunnlagId).isNotNull() },

      Executable { assertThat(nySkattegrunnlagspostOpprettet).isNotNull() },
      Executable { assertThat(nySkattegrunnlagspostOpprettet.skattegrunnlagspostId).isNotNull() },

      Executable { assertThat(nyUtvidetBarnetrygdOgSmaabarnstilleggOpprettet).isNotNull() },
      Executable { assertThat(nyUtvidetBarnetrygdOgSmaabarnstilleggOpprettet.ubstId).isNotNull() },

      Executable { assertThat(nyBarnetilleggOpprettet).isNotNull() },
      Executable { assertThat(nyBarnetilleggOpprettet.barnetilleggId).isNotNull() },

      // sjekk GrunnlagspakkeDto
      Executable { assertThat(grunnlagspakkeDto).isNotNull() },
      Executable { assertThat(grunnlagspakkeDto.grunnlagspakkeId).isNotNull() },
      Executable { assertThat(grunnlagspakkeDto.opprettetAv).isEqualTo("RTV9999") },
      Executable { assertThat(grunnlagspakkeDto.formaal).isEqualTo("BIDRAG") },

      // sjekk AinntektDto
      Executable { assertThat(ainntektDtoListe[0].personId).isEqualTo("1234567") },
      Executable { assertThat(ainntektDtoListe[0].aktiv).isTrue },
      Executable { assertThat(ainntektDtoListe[0].periodeFra).isEqualTo(LocalDate.parse("2021-07-01")) },
      Executable { assertThat(ainntektDtoListe[0].periodeTil).isEqualTo(LocalDate.parse("2021-08-01")) },

      // sjekk AinntektspostDto
      Executable { assertThat(ainntektspostDtoListe.size).isEqualTo(1) },

      Executable { assertThat(ainntektspostDtoListe[0].utbetalingsperiode).isEqualTo("202108") },
      Executable { assertThat(ainntektspostDtoListe[0].opptjeningsperiodeFra).isEqualTo(LocalDate.parse("2021-07-01")) },
      Executable { assertThat(ainntektspostDtoListe[0].opptjeningsperiodeTil).isEqualTo(LocalDate.parse("2021-08-01")) },
      Executable { assertThat(ainntektspostDtoListe[0].opplysningspliktigId).isEqualTo(("123")) },
      Executable { assertThat(ainntektspostDtoListe[0].inntektType).isEqualTo(("Loenn")) },
      Executable { assertThat(ainntektspostDtoListe[0].fordelType).isEqualTo(("Kontantytelse")) },
      Executable { assertThat(ainntektspostDtoListe[0].beskrivelse).isEqualTo(("Loenn/ferieLoenn")) },
      Executable { assertThat(ainntektspostDtoListe[0].belop).isEqualTo(BigDecimal.valueOf(50000)) },

      // sjekk SkattegrunnlagDto
      Executable { assertThat(skattegrunnlagDtoListe[0].personId).isEqualTo("7654321") },
      Executable { assertThat(skattegrunnlagDtoListe[0].aktiv).isTrue },
      Executable { assertThat(skattegrunnlagDtoListe[0].periodeFra).isEqualTo(LocalDate.parse("2021-01-01")) },
      Executable { assertThat(skattegrunnlagDtoListe[0].periodeTil).isEqualTo(LocalDate.parse("2022-01-01")) },

      // sjekk SkattegrunnlagspostDto
      Executable { assertThat(skattegrunnlagspostDtoListe.size).isEqualTo(1) },

      Executable { assertThat(skattegrunnlagspostDtoListe[0].inntektType).isEqualTo(("Loenn")) },
      Executable { assertThat(skattegrunnlagspostDtoListe[0].belop).isEqualTo(BigDecimal.valueOf(171717)) },

      // sjekk UtvidetBarnetrygdOgSmaabarnstilleggdDto
      Executable { assertThat(ubstListe[0].personId).isEqualTo("1234567") },
      Executable { assertThat(ubstListe[0].type).isEqualTo("Utvidet barnetrygd") },
      Executable { assertThat(ubstListe[0].periodeFra).isEqualTo(LocalDate.parse("2021-01-01")) },
      Executable { assertThat(ubstListe[0].periodeTil).isEqualTo(LocalDate.parse("2021-07-01")) },
      Executable { assertThat(ubstListe[0].belop).isEqualTo(BigDecimal.valueOf(12468.01)) },
      Executable { assertThat(ubstListe[0].manueltBeregnet).isFalse },
      Executable { assertThat(ubstListe[0].deltBosted).isFalse },

      // sjekk BarnetilleggDto
      Executable { assertThat(barnetilleggListe[0].partPersonId).isEqualTo("1234567") },
      Executable { assertThat(barnetilleggListe[0].barnPersonId).isEqualTo("0123456") },
      Executable { assertThat(barnetilleggListe[0].barnetilleggType).isEqualTo("Utvidet barnetrygd") },
      Executable { assertThat(barnetilleggListe[0].periodeFra).isEqualTo(LocalDate.parse("2021-01-01")) },
      Executable { assertThat(barnetilleggListe[0].periodeTil).isEqualTo(LocalDate.parse("2021-07-01")) },
      Executable { assertThat(barnetilleggListe[0].belopBrutto).isEqualTo(BigDecimal.valueOf(1000)) },
      Executable { assertThat(barnetilleggListe[0].barnType).isEqualTo(BarnType.FELLES.toString()) }
    )
  }

  @Test
  @Suppress("NonAsciiCharacters")
  fun `Skal oppdatere grunnlagspakke med utvidet barnetrygd`() {

    Mockito.`when`(persistenceServiceMock.opprettNyGrunnlagspakke(MockitoHelper.capture(grunnlagspakkeDtoCaptor))).thenReturn(byggGrunnlagspakkeDto())
    Mockito.`when`(
      persistenceServiceMock.opprettUtvidetBarnetrygdOgSmaabarnstillegg(
        MockitoHelper.capture(utvidetBarnetrygdOgSmaabarnstilleggDtoCaptor)
      )
    ).thenReturn(byggUtvidetBarnetrygdOgSmaabarnstilleggDto())
    Mockito.`when`(familieBaSakConsumerMock.hentFamilieBaSak(MockitoHelper.any(FamilieBaSakRequest::class.java)))
      .thenReturn(RestResponse.Success(TestUtil.byggFamilieBaSakResponse()))

    val nyGrunnlagspakkeOpprettet = grunnlagspakkeService.opprettGrunnlagspakke(byggNyGrunnlagspakkeRequest())
    val oppdatertGrunnlagspakke = grunnlagspakkeService.oppdaterGrunnlagspakke(
      nyGrunnlagspakkeOpprettet.grunnlagspakkeId,
      TestUtil.byggOppdaterGrunnlagspakkeRequestUtvidetBarnetrygd()
    )

    val grunnlagspakkeDto = grunnlagspakkeDtoCaptor.value
    val ubstListe = utvidetBarnetrygdOgSmaabarnstilleggDtoCaptor.allValues

    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettNyGrunnlagspakke(MockitoHelper.any(GrunnlagspakkeDto::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(1))
      .opprettUtvidetBarnetrygdOgSmaabarnstillegg(MockitoHelper.any(UtvidetBarnetrygdOgSmaabarnstilleggDto::class.java))

    assertAll(
      Executable { assertThat(nyGrunnlagspakkeOpprettet).isNotNull() },
      Executable { assertThat(nyGrunnlagspakkeOpprettet.grunnlagspakkeId).isNotNull() },

      // sjekk GrunnlagspakkeDto
      Executable { assertThat(grunnlagspakkeDto).isNotNull() },
      Executable { assertThat(grunnlagspakkeDto.grunnlagspakkeId).isNotNull() },
      Executable { assertThat(grunnlagspakkeDto.opprettetAv).isEqualTo("RTV9999") },
      Executable { assertThat(grunnlagspakkeDto.formaal).isEqualTo("BIDRAG") },

      // sjekk UtvidetBarnetrygdOgSmaabarnstilleggDto
      Executable { assertThat(ubstListe).isNotNull() },
      Executable { assertThat(ubstListe.size).isEqualTo(1) },
      Executable { assertThat(ubstListe[0].personId).isEqualTo("12345678910") },
      Executable { assertThat(ubstListe[0].type).isEqualTo(BisysStønadstype.UTVIDET.toString()) },
      Executable { assertThat(ubstListe[0].periodeFra).isEqualTo(LocalDate.parse("2021-01-01")) },
      Executable { assertThat(ubstListe[0].periodeTil).isEqualTo(LocalDate.parse("2022-01-01")) },
      Executable { assertThat(ubstListe[0].belop).isEqualTo(BigDecimal.valueOf(1000.11)) },
      Executable { assertThat(ubstListe[0].manueltBeregnet).isFalse },
      Executable { assertThat(ubstListe[0].deltBosted).isFalse },

      // sjekk oppdatertGrunnlagspakke
      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagspakkeId).isEqualTo(nyGrunnlagspakkeOpprettet.grunnlagspakkeId) },
      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagtypeResponsListe.size).isEqualTo(1) },
      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagtypeResponsListe[0].grunnlagType).isEqualTo(GrunnlagType.UTVIDETBARNETRYGDOGSMAABARNSTILLEGG) },
      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagtypeResponsListe[0].personId).isEqualTo("12345678910") },
      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagtypeResponsListe[0].status).isEqualTo(GrunnlagsRequestStatus.HENTET) },
      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagtypeResponsListe[0].statusMelding).isEqualTo("Antall perioder funnet: 1") }
    )
  }

  @Test
  @Suppress("NonAsciiCharacters")
  fun `Skal oppdatere grunnlagspakke med barnetillegg fra Pensjon`() {

    Mockito.`when`(persistenceServiceMock.opprettNyGrunnlagspakke(MockitoHelper.capture(grunnlagspakkeDtoCaptor))).thenReturn(byggGrunnlagspakkeDto())
    Mockito.`when`(persistenceServiceMock.opprettBarnetillegg(MockitoHelper.capture(barnetilleggDtoCaptor))).thenReturn(byggBarnetilleggDto())
    Mockito.`when`(bidragGcpProxyConsumerMock.hentBarnetilleggPensjon(MockitoHelper.any(HentBarnetilleggPensjonRequest::class.java)))
      .thenReturn(RestResponse.Success(TestUtil.byggHentBarnetilleggPensjonResponse()))

    val nyGrunnlagspakkeOpprettet = grunnlagspakkeService.opprettGrunnlagspakke(byggNyGrunnlagspakkeRequest())
    val oppdatertGrunnlagspakke = grunnlagspakkeService.oppdaterGrunnlagspakke(
      nyGrunnlagspakkeOpprettet.grunnlagspakkeId,
      TestUtil.byggOppdaterGrunnlagspakkeRequestBarnetillegg()
    )

    val grunnlagspakkeDto = grunnlagspakkeDtoCaptor.value
    val barnetilleggListe = barnetilleggDtoCaptor.allValues

    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettNyGrunnlagspakke(MockitoHelper.any(GrunnlagspakkeDto::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettBarnetillegg(MockitoHelper.any(BarnetilleggDto::class.java))

    assertAll(
      Executable { assertThat(nyGrunnlagspakkeOpprettet).isNotNull() },
      Executable { assertThat(nyGrunnlagspakkeOpprettet.grunnlagspakkeId).isNotNull() },

      // sjekk GrunnlagspakkeDto
      Executable { assertThat(grunnlagspakkeDto).isNotNull() },
      Executable { assertThat(grunnlagspakkeDto.grunnlagspakkeId).isNotNull() },
      Executable { assertThat(grunnlagspakkeDto.opprettetAv).isEqualTo("RTV9999") },
      Executable { assertThat(grunnlagspakkeDto.formaal).isEqualTo("BIDRAG") },

      // sjekk BarnetilleggDto
      Executable { assertThat(barnetilleggListe).isNotNull() },
      Executable { assertThat(barnetilleggListe.size).isEqualTo(1) },
      Executable { assertThat(barnetilleggListe[0].partPersonId).isEqualTo("12345678910") },
      Executable { assertThat(barnetilleggListe[0].barnPersonId).isEqualTo("barnIdent") },
      Executable { assertThat(barnetilleggListe[0].barnetilleggType).isEqualTo(BarnetilleggType.PENSJON.toString()) },
      Executable { assertThat(barnetilleggListe[0].periodeFra).isEqualTo(LocalDate.parse("2021-01-01")) },
      Executable { assertThat(barnetilleggListe[0].periodeTil).isEqualTo(LocalDate.parse("2022-01-01")) },
      Executable { assertThat(barnetilleggListe[0].belopBrutto).isEqualTo(BigDecimal.valueOf(1000.11)) },
      Executable { assertThat(barnetilleggListe[0].barnType).isEqualTo(BarnType.FELLES.toString()) },

      // sjekk oppdatertGrunnlagspakke
      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagspakkeId).isEqualTo(nyGrunnlagspakkeOpprettet.grunnlagspakkeId) },
      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagtypeResponsListe.size).isEqualTo(1) },
      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagtypeResponsListe[0].grunnlagType).isEqualTo(GrunnlagType.BARNETILLEGG) },
      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagtypeResponsListe[0].personId).isEqualTo("12345678910") },
      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagtypeResponsListe[0].status).isEqualTo(GrunnlagsRequestStatus.HENTET) },
      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagtypeResponsListe[0].statusMelding).isEqualTo("Antall perioder funnet: 1") }
    )
  }

  object MockitoHelper {

    // use this in place of captor.capture() if you are trying to capture an argument that is not nullable
    fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()
    fun <T> any(type: Class<T>): T = Mockito.any(type)
  }
}
