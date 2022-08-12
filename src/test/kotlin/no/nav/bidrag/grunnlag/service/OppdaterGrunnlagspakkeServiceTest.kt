package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.behandling.felles.enums.BarnType
import no.nav.bidrag.behandling.felles.enums.BarnetilleggType
import no.nav.bidrag.behandling.felles.enums.GrunnlagRequestType
import no.nav.bidrag.behandling.felles.enums.GrunnlagsRequestStatus
import no.nav.bidrag.grunnlag.TestUtil
import no.nav.bidrag.grunnlag.bo.BarnetilleggBo
import no.nav.bidrag.grunnlag.bo.UtvidetBarnetrygdOgSmaabarnstilleggBo
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.BidragGcpProxyConsumer
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.barnetillegg.HentBarnetilleggPensjonRequest
import no.nav.bidrag.grunnlag.consumer.familiebasak.FamilieBaSakConsumer
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.BisysStønadstype
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.FamilieBaSakRequest
import no.nav.bidrag.grunnlag.consumer.infotrygdkontantstottev2.KontantstotteConsumer
import no.nav.bidrag.grunnlag.consumer.infotrygdkontantstottev2.api.KontantstotteRequest
import no.nav.bidrag.grunnlag.exception.RestResponse
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@DisplayName("OppdaterGrunnlagspakkeServiceTest")
@ExtendWith(MockitoExtension::class)
class OppdaterGrunnlagspakkeServiceTest {

  @InjectMocks
  private lateinit var oppdaterGrunnlagspakkeService: OppdaterGrunnlagspakkeService

  @Mock
  private lateinit var persistenceServiceMock: PersistenceService
  @Mock
  private lateinit var familieBaSakConsumerMock: FamilieBaSakConsumer
  @Mock
  private lateinit var bidragGcpProxyConsumerMock: BidragGcpProxyConsumer
  @Mock
  private lateinit var kontantstotteConsumerMock: KontantstotteConsumer

  @Captor
  private lateinit var utvidetBarnetrygdOgSmaabarnstilleggBoCaptor: ArgumentCaptor<UtvidetBarnetrygdOgSmaabarnstilleggBo>
  @Captor
  private lateinit var barnetilleggBoCaptor: ArgumentCaptor<BarnetilleggBo>
//  @Captor
//  private lateinit var kontantstotteBoCaptor: ArgumentCaptor<KontantstotteBo>

  @Test
  fun `Skal oppdatere grunnlagspakke med utvidet barnetrygd`() {

    Mockito.`when`(
      persistenceServiceMock.opprettUtvidetBarnetrygdOgSmaabarnstillegg(
        GrunnlagspakkeServiceMockTest.MockitoHelper.capture(utvidetBarnetrygdOgSmaabarnstilleggBoCaptor)
      )
    ).thenReturn(TestUtil.byggUtvidetBarnetrygdOgSmaabarnstillegg())
    Mockito.`when`(familieBaSakConsumerMock.hentFamilieBaSak(
      GrunnlagspakkeServiceMockTest.MockitoHelper.any(
        FamilieBaSakRequest::class.java)))
      .thenReturn(RestResponse.Success(TestUtil.byggFamilieBaSakResponse()))

    val grunnlagspakkeIdOpprettet = TestUtil.byggGrunnlagspakke().grunnlagspakkeId
    val oppdatertGrunnlagspakke = oppdaterGrunnlagspakkeService.oppdaterGrunnlagspakke(
      grunnlagspakkeIdOpprettet,
      TestUtil.byggOppdaterGrunnlagspakkeRequestUtvidetBarnetrygd(),
      LocalDateTime.now()
    )

    val ubstListe = utvidetBarnetrygdOgSmaabarnstilleggBoCaptor.allValues

    Mockito.verify(persistenceServiceMock, Mockito.times(1))
      .opprettUtvidetBarnetrygdOgSmaabarnstillegg(
        GrunnlagspakkeServiceMockTest.MockitoHelper.any(
          UtvidetBarnetrygdOgSmaabarnstilleggBo::class.java))

    assertAll(
      {
        Assertions.assertThat(grunnlagspakkeIdOpprettet).isNotNull()
      },
       { Assertions.assertThat(grunnlagspakkeIdOpprettet).isNotNull() },

      // sjekk UtvidetBarnetrygdOgSmaabarnstilleggDto
       { Assertions.assertThat(ubstListe).isNotNull() },
       { Assertions.assertThat(ubstListe.size).isEqualTo(1) },
       { Assertions.assertThat(ubstListe[0].personId).isEqualTo("12345678910") },
       { Assertions.assertThat(ubstListe[0].type).isEqualTo(BisysStønadstype.UTVIDET.toString()) },
       { Assertions.assertThat(ubstListe[0].periodeFra).isEqualTo(LocalDate.parse("2021-01-01")) },
       { Assertions.assertThat(ubstListe[0].periodeTil).isEqualTo(LocalDate.parse("2022-01-01")) },
       { Assertions.assertThat(ubstListe[0].belop).isEqualTo(BigDecimal.valueOf(1000.11)) },
       { Assertions.assertThat(ubstListe[0].manueltBeregnet).isFalse },
       { Assertions.assertThat(ubstListe[0].deltBosted).isFalse },

      // sjekk oppdatertGrunnlagspakke
       { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagspakkeId).isEqualTo(grunnlagspakkeIdOpprettet) },
       { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe.size).isEqualTo(1) },
       { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].type).isEqualTo(
        GrunnlagRequestType.UTVIDET_BARNETRYGD_OG_SMAABARNSTILLEGG) },
       { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].personId)
        .isEqualTo("12345678910") },
       { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].status)
        .isEqualTo(
        GrunnlagsRequestStatus.HENTET) },
       { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].statusMelding)
        .isEqualTo("Antall perioder funnet: 1") }
    )
  }

  @Test
  fun `Skal oppdatere grunnlagspakke med barnetillegg fra Pensjon`() {
    Mockito.`when`(persistenceServiceMock.opprettBarnetillegg(GrunnlagspakkeServiceMockTest.MockitoHelper.capture(barnetilleggBoCaptor))).thenReturn(
      TestUtil.byggBarnetillegg()
    )
    Mockito.`when`(bidragGcpProxyConsumerMock.hentBarnetilleggPensjon(
      GrunnlagspakkeServiceMockTest.MockitoHelper.any(
        HentBarnetilleggPensjonRequest::class.java)))
      .thenReturn(RestResponse.Success(TestUtil.byggHentBarnetilleggPensjonResponse()))

    val grunnlagspakkeIdOpprettet = TestUtil.byggGrunnlagspakke().grunnlagspakkeId
    val oppdatertGrunnlagspakke = oppdaterGrunnlagspakkeService.oppdaterGrunnlagspakke(
      grunnlagspakkeIdOpprettet,
      TestUtil.byggOppdaterGrunnlagspakkeRequestBarnetillegg(),
      LocalDateTime.now()
    )

    val barnetilleggListe = barnetilleggBoCaptor.allValues

    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettBarnetillegg(
      GrunnlagspakkeServiceMockTest.MockitoHelper.any(BarnetilleggBo::class.java))

    assertAll(
       { Assertions.assertThat(grunnlagspakkeIdOpprettet).isNotNull() },
       { Assertions.assertThat(grunnlagspakkeIdOpprettet).isNotNull() },

      // sjekk BarnetilleggDto
       { Assertions.assertThat(barnetilleggListe).isNotNull() },
       { Assertions.assertThat(barnetilleggListe.size).isEqualTo(1) },
       { Assertions.assertThat(barnetilleggListe[0].partPersonId).isEqualTo("12345678910") },
       { Assertions.assertThat(barnetilleggListe[0].barnPersonId).isEqualTo("barnIdent") },
       { Assertions.assertThat(barnetilleggListe[0].barnetilleggType)
        .isEqualTo(BarnetilleggType.PENSJON.toString()) },
       { Assertions.assertThat(barnetilleggListe[0].periodeFra).isEqualTo(LocalDate.parse("2021-01-01")) },
       { Assertions.assertThat(barnetilleggListe[0].periodeTil).isEqualTo(LocalDate.parse("2022-01-01")) },
       { Assertions.assertThat(barnetilleggListe[0].belopBrutto).isEqualTo(BigDecimal.valueOf(1000.11)) },
       { Assertions.assertThat(barnetilleggListe[0].barnType).isEqualTo(BarnType.FELLES.toString()) },

      // sjekk oppdatertGrunnlagspakke
       { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagspakkeId).isEqualTo(grunnlagspakkeIdOpprettet) },
       { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe.size).isEqualTo(1) },
       { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].type)
        .isEqualTo(GrunnlagRequestType.BARNETILLEGG) },
       { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].personId)
        .isEqualTo("12345678910") },
       { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].status)
        .isEqualTo(GrunnlagsRequestStatus.HENTET) },
       { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].statusMelding)
        .isEqualTo("Antall perioder funnet: 1") }
    )
  }

  @Test
  fun `skal oppdatere grunnlagspakke med kontantstotte`() {
    //TODO
//    Mockito.`when`(persistenceServiceMock.opprettKontantstotte(GrunnlagspakkeServiceMockTest.MockitoHelper.capture(kontantstotteBoCaptor))).thenReturn(
//      TestUtil.byggKontantstotte()
//    )
    Mockito.`when`(kontantstotteConsumerMock.hentKontantstotte(
      GrunnlagspakkeServiceMockTest.MockitoHelper.any(KontantstotteRequest::class.java)))
      .thenReturn(RestResponse.Success(TestUtil.byggKontantstotteResponse()))

    val grunnlagspakkeIdOpprettet = TestUtil.byggGrunnlagspakke().grunnlagspakkeId
    val oppdatertGrunnlagspakke = oppdaterGrunnlagspakkeService.oppdaterGrunnlagspakke(
      grunnlagspakkeIdOpprettet,
      TestUtil.byggOppdaterGrunnlagspakkeRequestKontantstotte(),
      LocalDateTime.now()
    )

//    val kontantstotteListe = kontantstotteBoCaptor.allValues

//    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettKontantstottre(
//      Mockito.any(KontantstotteBo::class.java))

    assertAll(
      { Assertions.assertThat(grunnlagspakkeIdOpprettet).isNotNull() },
      { Assertions.assertThat(grunnlagspakkeIdOpprettet).isNotNull() },

      // sjekk BarnetilleggDto
//      { Assertions.assertThat(barnetilleggListe).isNotNull() },
//      { Assertions.assertThat(barnetilleggListe.size).isEqualTo(1) },
//      { Assertions.assertThat(barnetilleggListe[0].partPersonId).isEqualTo("12345678910") },
//      { Assertions.assertThat(barnetilleggListe[0].barnPersonId).isEqualTo("barnIdent") },
//      { Assertions.assertThat(barnetilleggListe[0].barnetilleggType)
//        .isEqualTo(BarnetilleggType.PENSJON.toString()) },
//      { Assertions.assertThat(barnetilleggListe[0].periodeFra).isEqualTo(LocalDate.parse("2021-01-01")) },
//      { Assertions.assertThat(barnetilleggListe[0].periodeTil).isEqualTo(LocalDate.parse("2022-01-01")) },
//      { Assertions.assertThat(barnetilleggListe[0].belopBrutto).isEqualTo(BigDecimal.valueOf(1000.11)) },
//      { Assertions.assertThat(barnetilleggListe[0].barnType).isEqualTo(BarnType.FELLES.toString()) },

      // sjekk oppdatertGrunnlagspakke
      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagspakkeId).isEqualTo(grunnlagspakkeIdOpprettet) },
      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe.size).isEqualTo(1) },
      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].type)
        .isEqualTo(GrunnlagRequestType.KONTANTSTOTTE) },
      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].personId)
        .isEqualTo("12345678910") },
      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].status)
        .isEqualTo(GrunnlagsRequestStatus.HENTET) },
//      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].statusMelding)
//        .isEqualTo("Antall perioder funnet: 1") }
    )
  }
}