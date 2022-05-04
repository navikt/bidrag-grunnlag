package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.behandling.felles.dto.grunnlag.OpprettGrunnlagspakkeRequestDto
import no.nav.bidrag.behandling.felles.enums.BarnType
import no.nav.bidrag.behandling.felles.enums.BarnetilleggType
import no.nav.bidrag.behandling.felles.enums.Formaal
import no.nav.bidrag.behandling.felles.enums.GrunnlagType
import no.nav.bidrag.behandling.felles.enums.GrunnlagsRequestStatus
import no.nav.bidrag.grunnlag.TestUtil
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggAinntekt
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggAinntektBo
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggAinntektspost
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggAinntektspostBo
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggBarnetillegg
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggBarnetilleggBo
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggGrunnlagspakke
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggNyGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggSkattegrunnlagSkatt
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggSkattegrunnlagSkattBo
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggSkattegrunnlagspost
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggSkattegrunnlagspostBo
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggUtvidetBarnetrygdOgSmaabarnstillegg
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggUtvidetBarnetrygdOgSmaabarnstilleggBo
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.BidragGcpProxyConsumer
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.barnetillegg.HentBarnetilleggPensjonRequest
import no.nav.bidrag.grunnlag.consumer.familiebasak.FamilieBaSakConsumer
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.BisysStønadstype
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.FamilieBaSakRequest
import no.nav.bidrag.grunnlag.bo.AinntektBo
import no.nav.bidrag.grunnlag.bo.AinntektspostBo
import no.nav.bidrag.grunnlag.bo.BarnetilleggBo
import no.nav.bidrag.grunnlag.bo.SkattegrunnlagBo
import no.nav.bidrag.grunnlag.bo.SkattegrunnlagspostBo
import no.nav.bidrag.grunnlag.bo.UtvidetBarnetrygdOgSmaabarnstilleggBo
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.persistence.entity.Grunnlagspakke
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
  private lateinit var grunnlagspakkeCaptor: ArgumentCaptor<Grunnlagspakke>

  @Captor
  private lateinit var opprettGrunnlagspakkeRequestDtoCaptor: ArgumentCaptor<OpprettGrunnlagspakkeRequestDto>

  @Captor
  private lateinit var ainntektBoCaptor: ArgumentCaptor<AinntektBo>

  @Captor
  private lateinit var ainntektspostBoCaptor: ArgumentCaptor<AinntektspostBo>

  @Captor
  private lateinit var skattegrunnlagBoCaptor: ArgumentCaptor<SkattegrunnlagBo>

  @Captor
  private lateinit var skattegrunnlagspostBoCaptor: ArgumentCaptor<SkattegrunnlagspostBo>

  @Captor
  private lateinit var utvidetBarnetrygdOgSmaabarnstilleggBoCaptor: ArgumentCaptor<UtvidetBarnetrygdOgSmaabarnstilleggBo>

  @Captor
  private lateinit var barnetilleggBoCaptor: ArgumentCaptor<BarnetilleggBo>

  @Test
  fun `Skal opprette ny grunnlagspakke`() {
    Mockito.`when`(persistenceServiceMock.opprettNyGrunnlagspakke(MockitoHelper.capture(opprettGrunnlagspakkeRequestDtoCaptor)))
      .thenReturn(byggGrunnlagspakke())
    val nyGrunnlagspakkeIdOpprettet = grunnlagspakkeService.opprettGrunnlagspakke(byggNyGrunnlagspakkeRequest())
    val grunnlagspakke = opprettGrunnlagspakkeRequestDtoCaptor.value
    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettNyGrunnlagspakke(MockitoHelper.any(OpprettGrunnlagspakkeRequestDto::class.java))
    assertAll(
      Executable { assertThat(nyGrunnlagspakkeIdOpprettet).isNotNull() },
      // sjekk GrunnlagspakkeDto
      Executable { assertThat(grunnlagspakke).isNotNull() }
    )
  }

  @Test
  @Suppress("NonAsciiCharacters")
  fun `Skal hente grunnlagspakke med tilhørende grunnlag`() {

    Mockito.`when`(persistenceServiceMock.opprettNyGrunnlagspakke(MockitoHelper.capture(opprettGrunnlagspakkeRequestDtoCaptor)))
      .thenReturn(byggGrunnlagspakke())
    Mockito.`when`(persistenceServiceMock.opprettAinntekt(MockitoHelper.capture(ainntektBoCaptor)))
      .thenReturn(byggAinntekt())
    Mockito.`when`(persistenceServiceMock.opprettAinntektspost(MockitoHelper.capture(ainntektspostBoCaptor)))
      .thenReturn(byggAinntektspost())
    Mockito.`when`(persistenceServiceMock.opprettSkattegrunnlag(MockitoHelper.capture(skattegrunnlagBoCaptor)))
      .thenReturn(byggSkattegrunnlagSkatt())
    Mockito.`when`(persistenceServiceMock.opprettSkattegrunnlagspost(MockitoHelper.capture(skattegrunnlagspostBoCaptor)))
      .thenReturn(byggSkattegrunnlagspost())
    Mockito.`when`(persistenceServiceMock.opprettUtvidetBarnetrygdOgSmaabarnstillegg(
        MockitoHelper.capture(utvidetBarnetrygdOgSmaabarnstilleggBoCaptor)))
      .thenReturn(byggUtvidetBarnetrygdOgSmaabarnstillegg())
    Mockito.`when`(persistenceServiceMock.opprettBarnetillegg(MockitoHelper.capture(barnetilleggBoCaptor)))
      .thenReturn(byggBarnetillegg())

    val grunnlagspakkeIdOpprettet = grunnlagspakkeService.opprettGrunnlagspakke(byggNyGrunnlagspakkeRequest())
    val nyAinntektOpprettet = persistenceServiceMock.opprettAinntekt(byggAinntektBo())
    val nyAinntektspostOpprettet = persistenceServiceMock.opprettAinntektspost(byggAinntektspostBo())
    val nyttSkattegrunnlagOpprettet = persistenceServiceMock.opprettSkattegrunnlag(byggSkattegrunnlagSkattBo())
    val nySkattegrunnlagspostOpprettet = persistenceServiceMock.opprettSkattegrunnlagspost(byggSkattegrunnlagspostBo())
    val nyUtvidetBarnetrygdOgSmaabarnstilleggOpprettet =
      persistenceServiceMock.opprettUtvidetBarnetrygdOgSmaabarnstillegg(byggUtvidetBarnetrygdOgSmaabarnstilleggBo())
    val nyBarnetilleggOpprettet = persistenceServiceMock.opprettBarnetillegg(byggBarnetilleggBo())

    val opprettGrunnlagspakkeRequestDto = opprettGrunnlagspakkeRequestDtoCaptor.value
    val ainntektDtoListe = ainntektBoCaptor.allValues
    val ainntektspostDtoListe = ainntektspostBoCaptor.allValues
    val skattegrunnlagDtoListe = skattegrunnlagBoCaptor.allValues
    val skattegrunnlagspostDtoListe = skattegrunnlagspostBoCaptor.allValues
    val ubstListe = utvidetBarnetrygdOgSmaabarnstilleggBoCaptor.allValues
    val barnetilleggListe = barnetilleggBoCaptor.allValues

    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettNyGrunnlagspakke(MockitoHelper.any(OpprettGrunnlagspakkeRequestDto::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettAinntekt(MockitoHelper.any(AinntektBo::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettAinntektspost(MockitoHelper.any(AinntektspostBo::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettSkattegrunnlag(MockitoHelper.any(SkattegrunnlagBo::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettSkattegrunnlagspost(MockitoHelper.any(SkattegrunnlagspostBo::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(1))
      .opprettUtvidetBarnetrygdOgSmaabarnstillegg(MockitoHelper.any(UtvidetBarnetrygdOgSmaabarnstilleggBo::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettBarnetillegg(MockitoHelper.any(BarnetilleggBo::class.java))

    assertAll(
      Executable { assertThat(grunnlagspakkeIdOpprettet).isNotNull() },
      Executable { assertThat(grunnlagspakkeIdOpprettet).isNotNull() },

      Executable { assertThat(nyAinntektOpprettet).isNotNull() },
      Executable { assertThat(nyAinntektOpprettet.personId).isNotNull() },

      Executable { assertThat(nyAinntektspostOpprettet).isNotNull() },
      Executable { assertThat(nyAinntektspostOpprettet.inntektId).isNotNull() },

      Executable { assertThat(nyttSkattegrunnlagOpprettet).isNotNull() },
      Executable { assertThat(nyttSkattegrunnlagOpprettet.personId).isNotNull() },

      Executable { assertThat(nySkattegrunnlagspostOpprettet).isNotNull() },
      Executable { assertThat(nySkattegrunnlagspostOpprettet.skattegrunnlagId).isNotNull() },

      Executable { assertThat(nyUtvidetBarnetrygdOgSmaabarnstilleggOpprettet).isNotNull() },
      Executable { assertThat(nyUtvidetBarnetrygdOgSmaabarnstilleggOpprettet.grunnlagspakkeId).isNotNull() },

      Executable { assertThat(nyBarnetilleggOpprettet).isNotNull() },
      Executable { assertThat(nyBarnetilleggOpprettet.grunnlagspakkeId).isNotNull() },

      // sjekk GrunnlagspakkeDto
      Executable { assertThat(opprettGrunnlagspakkeRequestDto).isNotNull() },
      Executable { assertThat(opprettGrunnlagspakkeRequestDto.opprettetAv).isNotNull() },
      Executable { assertThat(opprettGrunnlagspakkeRequestDto.opprettetAv).isEqualTo("RTV9999") },
      Executable { assertThat(opprettGrunnlagspakkeRequestDto.formaal).isEqualTo(Formaal.BIDRAG) },

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

    Mockito.`when`(persistenceServiceMock.opprettNyGrunnlagspakke(MockitoHelper.capture(opprettGrunnlagspakkeRequestDtoCaptor)))
      .thenReturn(byggGrunnlagspakke())
    Mockito.`when`(
      persistenceServiceMock.opprettUtvidetBarnetrygdOgSmaabarnstillegg(
        MockitoHelper.capture(utvidetBarnetrygdOgSmaabarnstilleggBoCaptor)
      )
    ).thenReturn(byggUtvidetBarnetrygdOgSmaabarnstillegg())
    Mockito.`when`(familieBaSakConsumerMock.hentFamilieBaSak(MockitoHelper.any(FamilieBaSakRequest::class.java)))
      .thenReturn(RestResponse.Success(TestUtil.byggFamilieBaSakResponse()))

    val grunnlagspakkeIdOpprettet = grunnlagspakkeService.opprettGrunnlagspakke(byggNyGrunnlagspakkeRequest())
    val oppdatertGrunnlagspakke = grunnlagspakkeService.oppdaterGrunnlagspakke(
      grunnlagspakkeIdOpprettet,
      TestUtil.byggOppdaterGrunnlagspakkeRequestUtvidetBarnetrygd()
    )

    val opprettGrunnlagspakkeRequestDto = opprettGrunnlagspakkeRequestDtoCaptor.value
    val ubstListe = utvidetBarnetrygdOgSmaabarnstilleggBoCaptor.allValues

    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettNyGrunnlagspakke(MockitoHelper.any(OpprettGrunnlagspakkeRequestDto::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(1))
      .opprettUtvidetBarnetrygdOgSmaabarnstillegg(MockitoHelper.any(UtvidetBarnetrygdOgSmaabarnstilleggBo::class.java))

    assertAll(
      Executable { assertThat(grunnlagspakkeIdOpprettet).isNotNull() },
      Executable { assertThat(grunnlagspakkeIdOpprettet).isNotNull() },

      // sjekk GrunnlagspakkeDto
      Executable { assertThat(opprettGrunnlagspakkeRequestDto).isNotNull() },
      Executable { assertThat(opprettGrunnlagspakkeRequestDto.opprettetAv).isEqualTo("RTV9999") },
      Executable { assertThat(opprettGrunnlagspakkeRequestDto.formaal).isEqualTo(Formaal.BIDRAG) },

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
      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagspakkeId).isEqualTo(grunnlagspakkeIdOpprettet) },
      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagtypeResponsListe.size).isEqualTo(1) },
      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagtypeResponsListe[0].grunnlagType).isEqualTo(
        GrunnlagType.UTVIDETBARNETRYGDOGSMAABARNSTILLEGG) },
      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagtypeResponsListe[0].personId).isEqualTo("12345678910") },
      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagtypeResponsListe[0].status).isEqualTo(
        GrunnlagsRequestStatus.HENTET) },
      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagtypeResponsListe[0].statusMelding).isEqualTo("Antall perioder funnet: 1") }
    )
  }

  @Test
  @Suppress("NonAsciiCharacters")
  fun `Skal oppdatere grunnlagspakke med barnetillegg fra Pensjon`() {

    Mockito.`when`(persistenceServiceMock.opprettNyGrunnlagspakke(MockitoHelper.capture(opprettGrunnlagspakkeRequestDtoCaptor)))
      .thenReturn(byggGrunnlagspakke())
    Mockito.`when`(persistenceServiceMock.opprettBarnetillegg(MockitoHelper.capture(barnetilleggBoCaptor))).thenReturn(byggBarnetillegg())
    Mockito.`when`(bidragGcpProxyConsumerMock.hentBarnetilleggPensjon(MockitoHelper.any(HentBarnetilleggPensjonRequest::class.java)))
      .thenReturn(RestResponse.Success(TestUtil.byggHentBarnetilleggPensjonResponse()))

    val grunnlagspakkeIdOpprettet = grunnlagspakkeService.opprettGrunnlagspakke(byggNyGrunnlagspakkeRequest())
    val oppdatertGrunnlagspakke = grunnlagspakkeService.oppdaterGrunnlagspakke(
      grunnlagspakkeIdOpprettet,
      TestUtil.byggOppdaterGrunnlagspakkeRequestBarnetillegg()
    )

    val opprettGrunnlagspakkeRequestDto = opprettGrunnlagspakkeRequestDtoCaptor.value
    val barnetilleggListe = barnetilleggBoCaptor.allValues

    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettNyGrunnlagspakke(MockitoHelper.any(OpprettGrunnlagspakkeRequestDto::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettBarnetillegg(MockitoHelper.any(BarnetilleggBo::class.java))

    assertAll(
      Executable { assertThat(grunnlagspakkeIdOpprettet).isNotNull() },
      Executable { assertThat(grunnlagspakkeIdOpprettet).isNotNull() },

      // sjekk GrunnlagspakkeDto
      Executable { assertThat(opprettGrunnlagspakkeRequestDto).isNotNull() },
      Executable { assertThat(opprettGrunnlagspakkeRequestDto.opprettetAv).isEqualTo("RTV9999") },
      Executable { assertThat(opprettGrunnlagspakkeRequestDto.formaal).isEqualTo(Formaal.BIDRAG) },

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
      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagspakkeId).isEqualTo(grunnlagspakkeIdOpprettet) },
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
