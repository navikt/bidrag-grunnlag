package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.behandling.felles.enums.BarnType
import no.nav.bidrag.behandling.felles.enums.BarnetilleggType
import no.nav.bidrag.behandling.felles.enums.GrunnlagRequestType
import no.nav.bidrag.behandling.felles.enums.GrunnlagsRequestStatus
import no.nav.bidrag.behandling.felles.enums.SivilstandKode
import no.nav.bidrag.grunnlag.TestUtil
import no.nav.bidrag.grunnlag.bo.BarnBo
import no.nav.bidrag.grunnlag.bo.BarnetilleggBo
import no.nav.bidrag.grunnlag.bo.BarnetilsynBo
import no.nav.bidrag.grunnlag.bo.KontantstotteBo
import no.nav.bidrag.grunnlag.bo.ForelderBarnBo
import no.nav.bidrag.grunnlag.bo.ForelderBo
import no.nav.bidrag.grunnlag.bo.HusstandBo
import no.nav.bidrag.grunnlag.bo.HusstandsmedlemBo
import no.nav.bidrag.grunnlag.bo.SivilstandBo
import no.nav.bidrag.grunnlag.bo.UtvidetBarnetrygdOgSmaabarnstilleggBo
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.BidragGcpProxyConsumer
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.barnetillegg.HentBarnetilleggPensjonRequest
import no.nav.bidrag.grunnlag.consumer.bidragperson.BidragPersonConsumer
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.ForelderBarnRequest
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.HusstandsmedlemmerRequest
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.SivilstandRequest
import no.nav.bidrag.grunnlag.consumer.familiebasak.FamilieBaSakConsumer
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.BisysStønadstype
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.FamilieBaSakRequest
import no.nav.bidrag.grunnlag.consumer.familieefsak.FamilieEfSakConsumer
import no.nav.bidrag.grunnlag.consumer.familieefsak.api.BarnetilsynRequest
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
  private lateinit var bidragPersonConsumerMock: BidragPersonConsumer
  @Mock
  private lateinit var kontantstotteConsumerMock: KontantstotteConsumer
  @Mock
  private lateinit var familieEfSakConsumer: FamilieEfSakConsumer

  @Captor
  private lateinit var utvidetBarnetrygdOgSmaabarnstilleggBoCaptor: ArgumentCaptor<UtvidetBarnetrygdOgSmaabarnstilleggBo>
  @Captor
  private lateinit var barnetilleggBoCaptor: ArgumentCaptor<BarnetilleggBo>
  @Captor
  private lateinit var forelderBoCaptor: ArgumentCaptor<ForelderBo>
  @Captor
  private lateinit var forelderBarnBoCaptor: ArgumentCaptor<ForelderBarnBo>
  @Captor
  private lateinit var barnBoCaptor: ArgumentCaptor<BarnBo>
  @Captor
  private lateinit var husstandBoCaptor: ArgumentCaptor<HusstandBo>
  @Captor
  private lateinit var husstandsmedlemBoCaptor: ArgumentCaptor<HusstandsmedlemBo>
  @Captor
  private lateinit var sivilstandBoCaptor: ArgumentCaptor<SivilstandBo>
  @Captor
  private lateinit var kontantstotteBoCaptor: ArgumentCaptor<KontantstotteBo>
  @Captor
  private lateinit var barnetilsynBoCaptor: ArgumentCaptor<BarnetilsynBo>

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
        Assertions.assertThat(grunnlagspakkeIdOpprettet).isNotNull() },

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

    Mockito.verify(persistenceServiceMock, Mockito.times(2)).opprettBarnetillegg(
      GrunnlagspakkeServiceMockTest.MockitoHelper.any(BarnetilleggBo::class.java))

    assertAll(
       { Assertions.assertThat(grunnlagspakkeIdOpprettet).isNotNull() },
       { Assertions.assertThat(grunnlagspakkeIdOpprettet).isNotNull() },

      // sjekk BarnetilleggDto
       { Assertions.assertThat(barnetilleggListe).isNotNull() },
       { Assertions.assertThat(barnetilleggListe.size).isEqualTo(2) },
       { Assertions.assertThat(barnetilleggListe[0].partPersonId).isEqualTo("12345678910") },
       { Assertions.assertThat(barnetilleggListe[0].barnPersonId).isEqualTo("barnIdent") },
       { Assertions.assertThat(barnetilleggListe[0].barnetilleggType)
        .isEqualTo(BarnetilleggType.PENSJON.toString()) },
       { Assertions.assertThat(barnetilleggListe[0].periodeFra).isEqualTo(LocalDate.parse("2021-01-01")) },
       { Assertions.assertThat(barnetilleggListe[0].periodeTil).isEqualTo(LocalDate.parse("2022-01-01")) },
       { Assertions.assertThat(barnetilleggListe[0].belopBrutto).isEqualTo(BigDecimal.valueOf(1000.11)) },
       { Assertions.assertThat(barnetilleggListe[0].barnType).isEqualTo(BarnType.FELLES.toString()) },

      { Assertions.assertThat(barnetilleggListe[1].partPersonId).isEqualTo("12345678910") },
      { Assertions.assertThat(barnetilleggListe[1].barnPersonId).isEqualTo("barnIdent") },
      { Assertions.assertThat(barnetilleggListe[1].barnetilleggType)
        .isEqualTo(BarnetilleggType.PENSJON.toString()) },
      { Assertions.assertThat(barnetilleggListe[1].periodeFra).isEqualTo(LocalDate.parse("2022-01-01")) },
      { Assertions.assertThat(barnetilleggListe[1].periodeTil).isEqualTo(LocalDate.parse("2023-01-01")) },
      { Assertions.assertThat(barnetilleggListe[1].belopBrutto).isEqualTo(BigDecimal.valueOf(2000.22)) },
      { Assertions.assertThat(barnetilleggListe[1].barnType).isEqualTo(BarnType.FELLES.toString()) },

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
        .isEqualTo("Antall perioder funnet: 2") }
    )
  }

  @Test
  fun `Skal oppdatere grunnlagspakke med egne barn i husstanden fra PDL via bidrag-person`() {

    Mockito.`when`(persistenceServiceMock.opprettForelder(GrunnlagspakkeServiceMockTest.MockitoHelper.capture(forelderBoCaptor)))
      .thenReturn(TestUtil.byggForelder()
      )
    Mockito.`when`(persistenceServiceMock.opprettBarn(GrunnlagspakkeServiceMockTest.MockitoHelper.capture(barnBoCaptor)))
      .thenReturn(TestUtil.byggBarn()
      )
    Mockito.`when`(persistenceServiceMock.opprettForelderBarn(GrunnlagspakkeServiceMockTest.MockitoHelper.capture(forelderBarnBoCaptor)))
      .thenReturn(TestUtil.byggForelderBarn()
      )

    Mockito.`when`(bidragPersonConsumerMock.hentForelderBarnRelasjon(
      GrunnlagspakkeServiceMockTest.MockitoHelper.any(
        ForelderBarnRequest::class.java)))
      .thenReturn(RestResponse.Success(TestUtil.byggHentForelderBarnRelasjonerResponse()))

    Mockito.`when`(bidragPersonConsumerMock.hentNavnFoedselOgDoed(
      GrunnlagspakkeServiceMockTest.MockitoHelper.any(
        String::class.java)))
      .thenReturn(RestResponse.Success(TestUtil.byggHentNavnFoedselOgDoedResponse()))

    val grunnlagspakkeIdOpprettet = TestUtil.byggGrunnlagspakke().grunnlagspakkeId
    val oppdatertGrunnlagspakke = oppdaterGrunnlagspakkeService.oppdaterGrunnlagspakke(
      grunnlagspakkeIdOpprettet,
      TestUtil.byggOppdaterGrunnlagspakkeRequestEgneBarnIHusstanden(),
      LocalDateTime.now()
    )

    val forelderListe = forelderBoCaptor.allValues
    val barnListe = barnBoCaptor.allValues
    val forelderBarnListe = forelderBarnBoCaptor.allValues

    assertAll(
      { Assertions.assertThat(grunnlagspakkeIdOpprettet).isNotNull() },

      // sjekk GrunnlagspakkeBo
      { Assertions.assertThat(oppdatertGrunnlagspakke).isNotNull() },

      // sjekk HusstandBo
      { Assertions.assertThat(forelderListe).isNotNull() },

      { Assertions.assertThat(forelderListe.size).isEqualTo(1) },
      { Assertions.assertThat(forelderListe[0]?.personId).isEqualTo("12345678910") },
      { Assertions.assertThat(forelderListe[0]?.navn).isEqualTo("Dunkel Sol") },
      { Assertions.assertThat(forelderListe[0]?.foedselsdato).isEqualTo(LocalDate.parse("2001-04-17")) },
      { Assertions.assertThat(forelderListe[0]?.doedsdato).isNull() },
      { Assertions.assertThat(forelderListe[0]?.aktiv).isTrue() },
      { Assertions.assertThat(forelderListe[0]?.brukFra).isNotNull() },
      { Assertions.assertThat(forelderListe[0]?.brukTil).isNull() },
      { Assertions.assertThat(forelderListe[0]?.opprettetAv).isNull() },
      { Assertions.assertThat(forelderListe[0]?.hentetTidspunkt).isNotNull() },

      { Assertions.assertThat(barnListe.size).isEqualTo(3) },
      { Assertions.assertThat(barnListe[0]?.personId).isEqualTo("111") },
      { Assertions.assertThat(barnListe[0]?.aktiv).isTrue() },
      { Assertions.assertThat(barnListe[0]?.brukFra).isNotNull() },
      { Assertions.assertThat(barnListe[0]?.brukTil).isNull() },
      { Assertions.assertThat(barnListe[0]?.opprettetAv).isNull() },
      { Assertions.assertThat(barnListe[0]?.hentetTidspunkt).isNotNull() },

      { Assertions.assertThat(barnListe[1]?.personId).isEqualTo("234") },
      { Assertions.assertThat(barnListe[1]?.aktiv).isTrue() },
      { Assertions.assertThat(barnListe[1]?.brukFra).isNotNull() },
      { Assertions.assertThat(barnListe[1]?.brukTil).isNull() },
      { Assertions.assertThat(barnListe[1]?.opprettetAv).isNull() },
      { Assertions.assertThat(barnListe[1]?.hentetTidspunkt).isNotNull() },

      { Assertions.assertThat(barnListe[2]?.personId).isEqualTo("333") },
      { Assertions.assertThat(barnListe[2]?.aktiv).isTrue() },
      { Assertions.assertThat(barnListe[2]?.brukFra).isNotNull() },
      { Assertions.assertThat(barnListe[2]?.brukTil).isNull() },
      { Assertions.assertThat(barnListe[2]?.opprettetAv).isNull() },
      { Assertions.assertThat(barnListe[2]?.hentetTidspunkt).isNotNull() },

      { Assertions.assertThat(forelderBarnListe.size).isEqualTo(3) },

      // sjekk oppdatertGrunnlagspakke
      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagspakkeId).isEqualTo(grunnlagspakkeIdOpprettet) },
      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe.size).isEqualTo(1) },
      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].type).isEqualTo(GrunnlagRequestType.EGNE_BARN_I_HUSSTANDEN) },
      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].personId)
        .isEqualTo("12345678910") },
      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].status)
        .isEqualTo(GrunnlagsRequestStatus.HENTET) },
      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].statusMelding)
        .isEqualTo("Antall barn funnet: 3") }
    )
  }



  @Test
  fun `Skal oppdatere grunnlagspakke med husstand og husstandsmedlemmer fra PDL via bidrag-person`() {

    Mockito.`when`(persistenceServiceMock.opprettHusstand(GrunnlagspakkeServiceMockTest.MockitoHelper.capture(husstandBoCaptor)))
      .thenReturn(TestUtil.byggHusstand()
    )
    Mockito.`when`(persistenceServiceMock.opprettHusstandsmedlem(GrunnlagspakkeServiceMockTest.MockitoHelper.capture(husstandsmedlemBoCaptor)))
      .thenReturn(TestUtil.byggHusstandsmedlem()
    )
    Mockito.`when`(bidragPersonConsumerMock.hentHusstandsmedlemmer(
      GrunnlagspakkeServiceMockTest.MockitoHelper.any(
        HusstandsmedlemmerRequest::class.java)))
      .thenReturn(RestResponse.Success(TestUtil.byggHentHusstandsmedlemmerResponse()))

    val grunnlagspakkeIdOpprettet = TestUtil.byggGrunnlagspakke().grunnlagspakkeId
    val oppdatertGrunnlagspakke = oppdaterGrunnlagspakkeService.oppdaterGrunnlagspakke(
      grunnlagspakkeIdOpprettet,
      TestUtil.byggOppdaterGrunnlagspakkeRequestHusstandsmedlemmer(),
      LocalDateTime.now()
    )

//    val opprettGrunnlagspakkeRequestDto = opprettGrunnlagspakkeRequestDtoCaptor.value
    val husstandListe = husstandBoCaptor.allValues
    val husstandsmedlemListe = husstandsmedlemBoCaptor.allValues

    assertAll(
      { Assertions.assertThat(grunnlagspakkeIdOpprettet).isNotNull() },

      // sjekk GrunnlagspakkeBo
      { Assertions.assertThat(oppdatertGrunnlagspakke).isNotNull() },

      // sjekk HusstandBo
      { Assertions.assertThat(husstandListe).isNotNull() },

      { Assertions.assertThat(husstandListe.size).isEqualTo(2) },
      { Assertions.assertThat(husstandListe[0]?.periodeFra).isEqualTo(LocalDate.parse("2011-01-01")) },
      { Assertions.assertThat(husstandListe[0]?.periodeTil).isEqualTo(LocalDate.parse("2011-10-01")) },
      { Assertions.assertThat(husstandListe[0]?.adressenavn).isEqualTo("adressenavn1") },
      { Assertions.assertThat(husstandListe[0]?.husnummer).isEqualTo("husnummer1") },
      { Assertions.assertThat(husstandListe[0]?.husbokstav).isEqualTo("husbokstav1") },
      { Assertions.assertThat(husstandListe[0]?.bruksenhetsnummer).isEqualTo("bruksenhetsnummer1") },
      { Assertions.assertThat(husstandListe[0]?.postnummer).isEqualTo("postnr1") },
      { Assertions.assertThat(husstandListe[0]?.bydelsnummer).isEqualTo("bydelsnummer1") },
      { Assertions.assertThat(husstandListe[0]?.kommunenummer).isEqualTo("kommunenummer1") },
      { Assertions.assertThat(husstandListe[0]?.matrikkelId).isEqualTo(12345) },
      { Assertions.assertThat(husstandListe[0]?.aktiv).isTrue() },
      { Assertions.assertThat(husstandListe[0]?.brukFra).isNotNull() },
      { Assertions.assertThat(husstandListe[0]?.brukTil).isNull() },
      { Assertions.assertThat(husstandListe[0]?.opprettetAv).isNull() },
      { Assertions.assertThat(husstandListe[0]?.hentetTidspunkt).isNotNull() },

      { Assertions.assertThat(husstandsmedlemListe?.get(0)?.personId).isEqualTo("123") },
      { Assertions.assertThat(husstandsmedlemListe?.get(0)?.navn).isEqualTo("fornavn1 mellomnavn1 etternavn1") },
      { Assertions.assertThat(husstandsmedlemListe?.get(0)?.periodeFra).isEqualTo(LocalDate.parse("2011-01-01")) },
      { Assertions.assertThat(husstandsmedlemListe?.get(0)?.periodeTil).isEqualTo(LocalDate.parse("2011-02-01")) },
      { Assertions.assertThat(husstandsmedlemListe?.get(0)?.opprettetAv).isNull() },
      { Assertions.assertThat(husstandsmedlemListe?.get(0)?.hentetTidspunkt).isNotNull() },

      { Assertions.assertThat(husstandsmedlemListe?.get(1)?.personId).isEqualTo("234") },
      { Assertions.assertThat(husstandsmedlemListe?.get(1)?.navn).isEqualTo("fornavn2 mellomnavn2 etternavn2") },
      { Assertions.assertThat(husstandsmedlemListe?.get(1)?.periodeFra).isEqualTo(LocalDate.parse("2011-01-01")) },
      { Assertions.assertThat(husstandsmedlemListe?.get(1)?.periodeTil).isEqualTo(LocalDate.parse("2011-12-01")) },
      { Assertions.assertThat(husstandsmedlemListe?.get(1)?.opprettetAv).isNull() },
      { Assertions.assertThat(husstandsmedlemListe?.get(1)?.hentetTidspunkt).isNotNull() },

      { Assertions.assertThat(husstandsmedlemListe?.get(2)?.personId).isEqualTo("345") },
      { Assertions.assertThat(husstandsmedlemListe?.get(2)?.navn).isEqualTo("fornavn3 mellomnavn3 etternavn3") },
      { Assertions.assertThat(husstandsmedlemListe?.get(2)?.periodeFra).isEqualTo(LocalDate.parse("2011-05-01")) },
      { Assertions.assertThat(husstandsmedlemListe?.get(2)?.periodeTil).isEqualTo(LocalDate.parse("2011-06-01")) },
      { Assertions.assertThat(husstandsmedlemListe?.get(2)?.opprettetAv).isNull() },
      { Assertions.assertThat(husstandsmedlemListe?.get(2)?.hentetTidspunkt).isNotNull() },

      // sjekk oppdatertGrunnlagspakke
      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagspakkeId).isEqualTo(grunnlagspakkeIdOpprettet) },
      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe.size).isEqualTo(1) },
      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].type).isEqualTo(GrunnlagRequestType.HUSSTANDSMEDLEMMER) },
      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].personId)
        .isEqualTo("12345678910") },
      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].status)
        .isEqualTo(GrunnlagsRequestStatus.HENTET) },
      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].statusMelding)
        .isEqualTo("Antall husstander funnet: 2") }
    )
  }

  @Test
  fun `Skal oppdatere grunnlagspakke med sivilstand fra PDL via bidrag-person`() {

    Mockito.`when`(persistenceServiceMock.opprettSivilstand(GrunnlagspakkeServiceMockTest.MockitoHelper.capture(sivilstandBoCaptor)))
      .thenReturn(TestUtil.byggSivilstand()
      )
    Mockito.`when`(bidragPersonConsumerMock.hentSivilstand(
      GrunnlagspakkeServiceMockTest.MockitoHelper.any(
        SivilstandRequest::class.java)))
      .thenReturn(RestResponse.Success(TestUtil.byggHentSivilstandResponse()))

    val grunnlagspakkeIdOpprettet = TestUtil.byggGrunnlagspakke().grunnlagspakkeId
    val oppdatertGrunnlagspakke = oppdaterGrunnlagspakkeService.oppdaterGrunnlagspakke(
      grunnlagspakkeIdOpprettet,
      TestUtil.byggOppdaterGrunnlagspakkeRequestSivilstand(),
      LocalDateTime.now()
    )

//    val opprettGrunnlagspakkeRequestDto = opprettGrunnlagspakkeRequestDtoCaptor.value
    val sivilstandListe = sivilstandBoCaptor.allValues

    assertAll(
      { Assertions.assertThat(grunnlagspakkeIdOpprettet).isNotNull() },

      // sjekk GrunnlagspakkeBo
      { Assertions.assertThat(oppdatertGrunnlagspakke).isNotNull() },

      // sjekk SivilstandBo
      { Assertions.assertThat(sivilstandListe).isNotNull() },
      { Assertions.assertThat(sivilstandListe.size).isEqualTo(3) },
      { Assertions.assertThat(sivilstandListe[0].personId).isEqualTo("12345678910") },
      { Assertions.assertThat(sivilstandListe[0].periodeFra).isNull() },
      { Assertions.assertThat(sivilstandListe[0].periodeTil).isNull() },
      { Assertions.assertThat(sivilstandListe[0].sivilstand).isEqualTo(SivilstandKode.ENSLIG.toString()) },

      { Assertions.assertThat(sivilstandListe[1].personId).isEqualTo("12345678910") },
      { Assertions.assertThat(sivilstandListe[1].periodeFra).isEqualTo(LocalDate.parse("2021-01-01")) },
      { Assertions.assertThat(sivilstandListe[1].periodeTil).isNull() },
      { Assertions.assertThat(sivilstandListe[1].sivilstand).isEqualTo(SivilstandKode.SAMBOER.toString()) },

      { Assertions.assertThat(sivilstandListe[2].personId).isEqualTo("12345678910") },
      { Assertions.assertThat(sivilstandListe[2].periodeFra).isEqualTo(LocalDate.parse("2021-09-01")) },
      { Assertions.assertThat(sivilstandListe[2].periodeTil).isNull() },
      { Assertions.assertThat(sivilstandListe[2].sivilstand).isEqualTo(SivilstandKode.GIFT.toString()) },

      // sjekk oppdatertGrunnlagspakke
      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagspakkeId).isEqualTo(grunnlagspakkeIdOpprettet) },
      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe.size).isEqualTo(1) },
      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].type).isEqualTo(GrunnlagRequestType.SIVILSTAND) },
      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].personId).isEqualTo("12345678910") },
      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].status).isEqualTo(GrunnlagsRequestStatus.HENTET) },
      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].statusMelding).isEqualTo("Antall perioder funnet: 3") },
      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].type).isEqualTo(GrunnlagRequestType.SIVILSTAND) },
      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].personId).isEqualTo("12345678910") },
      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].status).isEqualTo(GrunnlagsRequestStatus.HENTET) },
      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].statusMelding).isEqualTo("Antall perioder funnet: 3") }


    )
  }


  @Test
  fun `skal oppdatere grunnlagspakke med kontantstotte`() {
    Mockito.`when`(persistenceServiceMock.opprettKontantstotte(GrunnlagspakkeServiceMockTest.MockitoHelper.capture(kontantstotteBoCaptor))).thenReturn(
      TestUtil.byggKontantstotte()
    )
    Mockito.`when`(kontantstotteConsumerMock.hentKontantstotte(
      GrunnlagspakkeServiceMockTest.MockitoHelper.any(KontantstotteRequest::class.java)))
      .thenReturn(RestResponse.Success(TestUtil.byggKontantstotteResponse()))

    val grunnlagspakkeIdOpprettet = TestUtil.byggGrunnlagspakke().grunnlagspakkeId
    val oppdatertGrunnlagspakke = oppdaterGrunnlagspakkeService.oppdaterGrunnlagspakke(
      grunnlagspakkeIdOpprettet,
      TestUtil.byggOppdaterGrunnlagspakkeRequestKontantstotte(),
      LocalDateTime.now()
    )

    val kontantstotteListe = kontantstotteBoCaptor.allValues

    Mockito.verify(persistenceServiceMock, Mockito.times(2)).opprettKontantstotte(
      GrunnlagspakkeServiceMockTest.MockitoHelper.any(KontantstotteBo::class.java))

    assertAll(
      { Assertions.assertThat(grunnlagspakkeIdOpprettet).isNotNull() },
      { Assertions.assertThat(grunnlagspakkeIdOpprettet).isNotNull() },

      // sjekk KontantstotteDto
      { Assertions.assertThat(kontantstotteListe.size).isEqualTo(2) },
      { Assertions.assertThat(kontantstotteListe[0].partPersonId).isEqualTo("12345678910") },
      { Assertions.assertThat(kontantstotteListe[0].barnPersonId).isEqualTo("11223344551") },
      { Assertions.assertThat(kontantstotteListe[0].periodeFra).isEqualTo(LocalDate.parse("2022-01-01")) },
      { Assertions.assertThat(kontantstotteListe[0].periodeTil).isEqualTo(LocalDate.parse("2022-08-01")) },
      { Assertions.assertThat(kontantstotteListe[0].belop).isEqualTo(7500) },
      { Assertions.assertThat(kontantstotteListe[1].partPersonId).isEqualTo("12345678910") },
      { Assertions.assertThat(kontantstotteListe[1].barnPersonId).isEqualTo("15544332211") },
      { Assertions.assertThat(kontantstotteListe[1].periodeFra).isEqualTo(LocalDate.parse("2022-01-01")) },
      { Assertions.assertThat(kontantstotteListe[1].periodeTil).isEqualTo(LocalDate.parse("2022-08-01")) },
      { Assertions.assertThat(kontantstotteListe[1].belop).isEqualTo(7501) },

      // sjekk oppdatertGrunnlagspakke
      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagspakkeId).isEqualTo(grunnlagspakkeIdOpprettet) },
      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe.size).isEqualTo(1) },
      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].type)
        .isEqualTo(GrunnlagRequestType.KONTANTSTOTTE) },
      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].personId)
        .isEqualTo("12345678910") },
      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].status)
        .isEqualTo(GrunnlagsRequestStatus.HENTET) },
      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].statusMelding)
        .isEqualTo("Antall perioder funnet: 1") }
    )
  }

  @Test
  fun `skal oppdatere grunnlagspakke med barnetilsyn`() {
    Mockito.`when`(persistenceServiceMock.opprettBarnetilsyn(GrunnlagspakkeServiceMockTest.MockitoHelper.capture(barnetilsynBoCaptor))).thenReturn(
      TestUtil.byggBarnetilsyn()
    )
    Mockito.`when`(familieEfSakConsumer.hentBarnetilsyn(
      GrunnlagspakkeServiceMockTest.MockitoHelper.any(BarnetilsynRequest::class.java)))
      .thenReturn(RestResponse.Success(TestUtil.byggBarnetilsynResponse()))

    val grunnlagspakkeIdOpprettet = TestUtil.byggGrunnlagspakke().grunnlagspakkeId
    val oppdatertGrunnlagspakke = oppdaterGrunnlagspakkeService.oppdaterGrunnlagspakke(
      grunnlagspakkeIdOpprettet,
      TestUtil.byggOppdaterGrunnlagspakkeRequestBarnetilsyn(),
      LocalDateTime.now()
    )

    val barnetilsynListe = barnetilsynBoCaptor.allValues

    Mockito.verify(persistenceServiceMock, Mockito.times(2)).opprettBarnetilsyn(
      GrunnlagspakkeServiceMockTest.MockitoHelper.any(BarnetilsynBo::class.java))

    assertAll(
      { Assertions.assertThat(grunnlagspakkeIdOpprettet).isNotNull() },
      { Assertions.assertThat(grunnlagspakkeIdOpprettet).isNotNull() },

      // sjekk BarnetilsynDto
      { Assertions.assertThat(barnetilsynListe.size).isEqualTo(2) },
      { Assertions.assertThat(barnetilsynListe[0].partPersonId).isEqualTo("12345678910") },
      { Assertions.assertThat(barnetilsynListe[0].barnPersonId).isEqualTo("01012212345") },
      { Assertions.assertThat(barnetilsynListe[0].periodeFra).isEqualTo(LocalDate.parse("2021-01-01")) },
      { Assertions.assertThat(barnetilsynListe[0].periodeTil).isEqualTo(LocalDate.parse("2021-08-01")) },
      { Assertions.assertThat(barnetilsynListe[0].belop).isNull() },
      { Assertions.assertThat(barnetilsynListe[1].partPersonId).isEqualTo("12345678910") },
      { Assertions.assertThat(barnetilsynListe[1].barnPersonId).isEqualTo("01011034543") },
      { Assertions.assertThat(barnetilsynListe[1].periodeFra).isEqualTo(LocalDate.parse("2021-01-01")) },
      { Assertions.assertThat(barnetilsynListe[1].periodeTil).isEqualTo(LocalDate.parse("2021-08-01")) },
      { Assertions.assertThat(barnetilsynListe[1].belop).isNull() },

      // sjekk oppdatertGrunnlagspakke
      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagspakkeId).isEqualTo(grunnlagspakkeIdOpprettet) },
      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe.size).isEqualTo(1) },
      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].type)
        .isEqualTo(GrunnlagRequestType.BARNETILSYN) },
      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].personId)
        .isEqualTo("12345678910") },
      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].status)
        .isEqualTo(GrunnlagsRequestStatus.HENTET) },
      { Assertions.assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].statusMelding)
        .isEqualTo("Antall perioder funnet: 1") }
    )
  }
}