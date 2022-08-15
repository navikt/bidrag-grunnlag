package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.behandling.felles.dto.grunnlag.OpprettGrunnlagspakkeRequestDto
import no.nav.bidrag.behandling.felles.enums.BarnType
import no.nav.bidrag.behandling.felles.enums.Formaal
import no.nav.bidrag.behandling.felles.enums.SivilstandKode
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggAinntekt
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggAinntektBo
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggAinntektspost
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggAinntektspostBo
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggBarn
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggBarnBo
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggBarnetillegg
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggBarnetilleggBo
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggGrunnlagspakke
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggHusstand
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggHusstandBo
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggHusstandsmedlem
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggHusstandsmedlemBo
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggNyGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggForelder
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggForelderBo
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggSivilstand
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggSivilstandBo
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggSkattegrunnlagSkatt
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggSkattegrunnlagSkattBo
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggSkattegrunnlagspost
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggSkattegrunnlagspostBo
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggUtvidetBarnetrygdOgSmaabarnstillegg
import no.nav.bidrag.grunnlag.TestUtil.Companion.byggUtvidetBarnetrygdOgSmaabarnstilleggBo
import no.nav.bidrag.grunnlag.bo.AinntektBo
import no.nav.bidrag.grunnlag.bo.AinntektspostBo
import no.nav.bidrag.grunnlag.bo.BarnBo
import no.nav.bidrag.grunnlag.bo.BarnetilleggBo
import no.nav.bidrag.grunnlag.bo.HusstandBo
import no.nav.bidrag.grunnlag.bo.HusstandsmedlemBo
import no.nav.bidrag.grunnlag.bo.ForelderBo
import no.nav.bidrag.grunnlag.bo.SivilstandBo
import no.nav.bidrag.grunnlag.bo.SkattegrunnlagBo
import no.nav.bidrag.grunnlag.bo.SkattegrunnlagspostBo
import no.nav.bidrag.grunnlag.bo.UtvidetBarnetrygdOgSmaabarnstilleggBo
import org.assertj.core.api.Assertions.assertThat
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

@DisplayName("GrunnlagspakkeServiceMockTest")
@ExtendWith(MockitoExtension::class)
class GrunnlagspakkeServiceMockTest {

  @InjectMocks
  private lateinit var grunnlagspakkeService: GrunnlagspakkeService

  @Mock
  private lateinit var persistenceServiceMock: PersistenceService
  @Mock
  private lateinit var oppdaterGrunnlagspakkeService: OppdaterGrunnlagspakkeService

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

  @Captor
  private lateinit var forelderBoCaptor: ArgumentCaptor<ForelderBo>

  @Captor
  private lateinit var barnBoCaptor: ArgumentCaptor<BarnBo>

  @Captor
  private lateinit var husstandBoCaptor: ArgumentCaptor<HusstandBo>

  @Captor
  private lateinit var husstandsmedlemBoCaptor: ArgumentCaptor<HusstandsmedlemBo>

  @Captor
  private lateinit var sivilstandBoCaptor: ArgumentCaptor<SivilstandBo>

  @Test
  fun `Skal opprette ny grunnlagspakke`() {
    Mockito.`when`(persistenceServiceMock.opprettNyGrunnlagspakke(MockitoHelper.capture(opprettGrunnlagspakkeRequestDtoCaptor)))
      .thenReturn(byggGrunnlagspakke())
    val nyGrunnlagspakkeIdOpprettet = grunnlagspakkeService.opprettGrunnlagspakke(byggNyGrunnlagspakkeRequest())
    val grunnlagspakke = opprettGrunnlagspakkeRequestDtoCaptor.value
    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettNyGrunnlagspakke(MockitoHelper.any(OpprettGrunnlagspakkeRequestDto::class.java))
    assertAll(
      { assertThat(nyGrunnlagspakkeIdOpprettet).isNotNull() },
      // sjekk GrunnlagspakkeDto
      { assertThat(grunnlagspakke).isNotNull() }
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
    Mockito.`when`(persistenceServiceMock.opprettForelder(MockitoHelper.capture(forelderBoCaptor)))
      .thenReturn(byggForelder())
    Mockito.`when`(persistenceServiceMock.opprettBarn(MockitoHelper.capture(barnBoCaptor)))
      .thenReturn(byggBarn())
    Mockito.`when`(persistenceServiceMock.opprettHusstand(MockitoHelper.capture(husstandBoCaptor)))
      .thenReturn(byggHusstand())
    Mockito.`when`(persistenceServiceMock.opprettHusstandsmedlem(MockitoHelper.capture(husstandsmedlemBoCaptor)))
      .thenReturn(byggHusstandsmedlem())
    Mockito.`when`(persistenceServiceMock.opprettSivilstand(MockitoHelper.capture(sivilstandBoCaptor)))
      .thenReturn(byggSivilstand())

    val grunnlagspakkeIdOpprettet = grunnlagspakkeService.opprettGrunnlagspakke(byggNyGrunnlagspakkeRequest())
    val nyAinntektOpprettet = persistenceServiceMock.opprettAinntekt(byggAinntektBo())
    val nyAinntektspostOpprettet = persistenceServiceMock.opprettAinntektspost(byggAinntektspostBo())
    val nyttSkattegrunnlagOpprettet = persistenceServiceMock.opprettSkattegrunnlag(byggSkattegrunnlagSkattBo())
    val nySkattegrunnlagspostOpprettet = persistenceServiceMock.opprettSkattegrunnlagspost(byggSkattegrunnlagspostBo())
    val nyUtvidetBarnetrygdOgSmaabarnstilleggOpprettet =
      persistenceServiceMock.opprettUtvidetBarnetrygdOgSmaabarnstillegg(byggUtvidetBarnetrygdOgSmaabarnstilleggBo())
    val nyttBarnetilleggOpprettet = persistenceServiceMock.opprettBarnetillegg(byggBarnetilleggBo())
    val nyForelderOpprettet = persistenceServiceMock.opprettForelder(byggForelderBo())
    val nyttBarnOpprettet = persistenceServiceMock.opprettBarn(byggBarnBo())
    val nyHusstandOpprettet = persistenceServiceMock.opprettHusstand(byggHusstandBo())
    val nyHusstandsmedlemOpprettet = persistenceServiceMock.opprettHusstandsmedlem(byggHusstandsmedlemBo())
    val nySivilstandOpprettet = persistenceServiceMock.opprettSivilstand(byggSivilstandBo())

    val opprettGrunnlagspakkeRequestDto = opprettGrunnlagspakkeRequestDtoCaptor.value
    val ainntektBoListe = ainntektBoCaptor.allValues
    val ainntektspostBoListe = ainntektspostBoCaptor.allValues
    val skattegrunnlagBoListe = skattegrunnlagBoCaptor.allValues
    val skattegrunnlagspostBoListe = skattegrunnlagspostBoCaptor.allValues
    val ubstBoListe = utvidetBarnetrygdOgSmaabarnstilleggBoCaptor.allValues
    val barnetilleggBoListe = barnetilleggBoCaptor.allValues
    val forelderBoListe = forelderBoCaptor.allValues
    val barnBoListe = barnBoCaptor.allValues
    val husstandBoListe = husstandBoCaptor.allValues
    val husstandsmedlemBoListe = husstandsmedlemBoCaptor.allValues
    val sivilstandBoListe = sivilstandBoCaptor.allValues

    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettNyGrunnlagspakke(MockitoHelper.any(OpprettGrunnlagspakkeRequestDto::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettAinntekt(MockitoHelper.any(AinntektBo::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettAinntektspost(MockitoHelper.any(AinntektspostBo::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettSkattegrunnlag(MockitoHelper.any(SkattegrunnlagBo::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettSkattegrunnlagspost(MockitoHelper.any(SkattegrunnlagspostBo::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(1))
      .opprettUtvidetBarnetrygdOgSmaabarnstillegg(MockitoHelper.any(UtvidetBarnetrygdOgSmaabarnstilleggBo::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettBarnetillegg(MockitoHelper.any(BarnetilleggBo::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettForelder(MockitoHelper.any(ForelderBo::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettBarn(MockitoHelper.any(BarnBo::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettHusstand(MockitoHelper.any(HusstandBo::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettHusstandsmedlem(MockitoHelper.any(HusstandsmedlemBo::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettSivilstand(MockitoHelper.any(SivilstandBo::class.java))

    assertAll(
      { assertThat(grunnlagspakkeIdOpprettet).isNotNull() },
      { assertThat(grunnlagspakkeIdOpprettet).isNotNull() },

      { assertThat(nyAinntektOpprettet).isNotNull() },
      { assertThat(nyAinntektOpprettet.personId).isNotNull() },

      { assertThat(nyAinntektspostOpprettet).isNotNull() },
      { assertThat(nyAinntektspostOpprettet.inntektId).isNotNull() },

      { assertThat(nyttSkattegrunnlagOpprettet).isNotNull() },
      { assertThat(nyttSkattegrunnlagOpprettet.personId).isNotNull() },

      { assertThat(nySkattegrunnlagspostOpprettet).isNotNull() },
      { assertThat(nySkattegrunnlagspostOpprettet.skattegrunnlagId).isNotNull() },

      { assertThat(nyUtvidetBarnetrygdOgSmaabarnstilleggOpprettet).isNotNull() },
      { assertThat(nyUtvidetBarnetrygdOgSmaabarnstilleggOpprettet.grunnlagspakkeId).isNotNull() },

      { assertThat(nyttBarnetilleggOpprettet).isNotNull() },
      { assertThat(nyttBarnetilleggOpprettet.grunnlagspakkeId).isNotNull() },

      { assertThat(nyForelderOpprettet).isNotNull() },
      { assertThat(nyForelderOpprettet.forelderId).isNotNull() },

      { assertThat(nyttBarnOpprettet).isNotNull() },
      { assertThat(nyttBarnOpprettet.barnId).isNotNull() },

      { assertThat(nyHusstandOpprettet).isNotNull() },
      { assertThat(nyHusstandOpprettet.husstandId).isNotNull() },

      { assertThat(nyHusstandsmedlemOpprettet).isNotNull() },
      { assertThat(nyHusstandsmedlemOpprettet.husstandsmedlemId).isNotNull() },

      { assertThat(nySivilstandOpprettet).isNotNull() },
      { assertThat(nySivilstandOpprettet.sivilstandId).isNotNull() },


      // sjekk GrunnlagspakkeBo
      { assertThat(opprettGrunnlagspakkeRequestDto).isNotNull() },
      { assertThat(opprettGrunnlagspakkeRequestDto.opprettetAv).isNotNull() },
      { assertThat(opprettGrunnlagspakkeRequestDto.opprettetAv).isEqualTo("RTV9999") },
      { assertThat(opprettGrunnlagspakkeRequestDto.formaal).isEqualTo(Formaal.BIDRAG) },

      // sjekk AinntektBo
      { assertThat(ainntektBoListe[0].personId).isEqualTo("1234567") },
      { assertThat(ainntektBoListe[0].aktiv).isTrue },
      { assertThat(ainntektBoListe[0].periodeFra).isEqualTo(LocalDate.parse("2021-07-01")) },
      { assertThat(ainntektBoListe[0].periodeTil).isEqualTo(LocalDate.parse("2021-08-01")) },

      // sjekk AinntektspostBo
      { assertThat(ainntektspostBoListe.size).isEqualTo(1) },
      { assertThat(ainntektspostBoListe[0].utbetalingsperiode).isEqualTo("202108") },
      { assertThat(ainntektspostBoListe[0].opptjeningsperiodeFra).isEqualTo(LocalDate.parse("2021-07-01")) },
      { assertThat(ainntektspostBoListe[0].opptjeningsperiodeTil).isEqualTo(LocalDate.parse("2021-08-01")) },
      { assertThat(ainntektspostBoListe[0].opplysningspliktigId).isEqualTo(("123")) },
      { assertThat(ainntektspostBoListe[0].inntektType).isEqualTo(("Loenn")) },
      { assertThat(ainntektspostBoListe[0].fordelType).isEqualTo(("Kontantytelse")) },
      { assertThat(ainntektspostBoListe[0].beskrivelse).isEqualTo(("Loenn/ferieLoenn")) },
      { assertThat(ainntektspostBoListe[0].belop).isEqualTo(BigDecimal.valueOf(50000)) },

      // sjekk SkattegrunnlagBo
      { assertThat(skattegrunnlagBoListe[0].personId).isEqualTo("7654321") },
      { assertThat(skattegrunnlagBoListe[0].aktiv).isTrue },
      { assertThat(skattegrunnlagBoListe[0].periodeFra).isEqualTo(LocalDate.parse("2021-01-01")) },
      { assertThat(skattegrunnlagBoListe[0].periodeTil).isEqualTo(LocalDate.parse("2022-01-01")) },

      // sjekk SkattegrunnlagspostBo
      { assertThat(skattegrunnlagspostBoListe.size).isEqualTo(1) },
      { assertThat(skattegrunnlagspostBoListe[0].inntektType).isEqualTo(("Loenn")) },
      { assertThat(skattegrunnlagspostBoListe[0].belop).isEqualTo(BigDecimal.valueOf(171717)) },

      // sjekk UtvidetBarnetrygdOgSmaabarnstilleggdBo
      { assertThat(ubstBoListe[0].personId).isEqualTo("1234567") },
      { assertThat(ubstBoListe[0].type).isEqualTo("Utvidet barnetrygd") },
      { assertThat(ubstBoListe[0].periodeFra).isEqualTo(LocalDate.parse("2021-01-01")) },
      { assertThat(ubstBoListe[0].periodeTil).isEqualTo(LocalDate.parse("2021-07-01")) },
      { assertThat(ubstBoListe[0].belop).isEqualTo(BigDecimal.valueOf(12468.01)) },
      { assertThat(ubstBoListe[0].manueltBeregnet).isFalse },
      { assertThat(ubstBoListe[0].deltBosted).isFalse },

      // sjekk BarnetilleggBo
      { assertThat(nyttBarnetilleggOpprettet).isNotNull() },
      { assertThat(nyttBarnetilleggOpprettet.grunnlagspakkeId).isNotNull() },
      { assertThat(barnetilleggBoListe[0].partPersonId).isEqualTo("1234567") },
      { assertThat(barnetilleggBoListe[0].barnPersonId).isEqualTo("0123456") },
      { assertThat(barnetilleggBoListe[0].barnetilleggType).isEqualTo("Utvidet barnetrygd") },
      { assertThat(barnetilleggBoListe[0].periodeFra).isEqualTo(LocalDate.parse("2021-01-01")) },
      { assertThat(barnetilleggBoListe[0].periodeTil).isEqualTo(LocalDate.parse("2021-07-01")) },
      { assertThat(barnetilleggBoListe[0].belopBrutto).isEqualTo(BigDecimal.valueOf(1000)) },
      { assertThat(barnetilleggBoListe[0].barnType).isEqualTo(BarnType.FELLES.toString()) },

      // sjekk ForelderBo
      { assertThat(forelderBoListe[0].personId).isEqualTo("4321") },
      { assertThat(forelderBoListe[0].navn).isEqualTo("navn1") },
      { assertThat(forelderBoListe[0].foedselsdato).isEqualTo(LocalDate.parse("2021-01-01")) },
      { assertThat(forelderBoListe[0].doedsdato).isEqualTo(LocalDate.parse("2021-07-01")) },
      { assertThat(forelderBoListe[0].aktiv).isTrue },
      { assertThat(forelderBoListe[0].brukFra).isNotNull() },
      { assertThat(forelderBoListe[0].brukTil).isNull() },
      { assertThat(forelderBoListe[0].opprettetAv).isNull() },
      { assertThat(forelderBoListe[0].hentetTidspunkt).isNotNull() },

      // sjekk BarnBo
      { assertThat(barnBoListe[0].personId).isEqualTo("1234567") },
      { assertThat(barnBoListe[0].navn).isEqualTo("Svett Elefant") },
      { assertThat(barnBoListe[0].foedselsdato).isEqualTo(LocalDate.parse("2021-01-01")) },
      { assertThat(barnBoListe[0].foedselsaar).isEqualTo(2011) },
      { assertThat(barnBoListe[0].doedsdato).isEqualTo(LocalDate.parse("2021-07-01")) },
      { assertThat(barnBoListe[0].aktiv).isTrue },
      { assertThat(barnBoListe[0].brukFra).isNotNull() },
      { assertThat(barnBoListe[0].brukTil).isNull() },
      { assertThat(barnBoListe[0].opprettetAv).isNull() },
      { assertThat(barnBoListe[0].hentetTidspunkt).isNotNull() },

      // sjekk HusstandBo
      { assertThat(husstandBoListe[0].personId).isEqualTo("1234567") },
      { assertThat(husstandBoListe[0].periodeFra).isEqualTo(LocalDate.parse("2011-01-01")) },
      { assertThat(husstandBoListe[0].periodeTil).isEqualTo(LocalDate.parse("2011-02-01")) },
      { assertThat(husstandBoListe[0].adressenavn).isEqualTo("adressenavn1") },
      { assertThat(husstandBoListe[0].husnummer).isEqualTo("husnummer1") },
      { assertThat(husstandBoListe[0].husbokstav).isEqualTo("husbokstav1") },
      { assertThat(husstandBoListe[0].postnummer).isEqualTo("postnr1") },
      { assertThat(husstandBoListe[0].bydelsnummer).isEqualTo("bydelsnummer1") },
      { assertThat(husstandBoListe[0].kommunenummer).isEqualTo("kommunenummer1") },
      { assertThat(husstandBoListe[0].matrikkelId).isEqualTo("matrikkelId1") },
      { assertThat(husstandBoListe[0].aktiv).isTrue },
      { assertThat(husstandBoListe[0].brukFra).isNotNull() },
      { assertThat(husstandBoListe[0].brukTil).isNull() },
      { assertThat(husstandBoListe[0].opprettetAv).isNull() },
      { assertThat(husstandBoListe[0].hentetTidspunkt).isNotNull() },

      // sjekk HusstandsmedlemBo
      { assertThat(husstandsmedlemBoListe[0].personId).isEqualTo("123") },
      { assertThat(husstandsmedlemBoListe[0].navn).isEqualTo("navn1") },
      { assertThat(husstandsmedlemBoListe[0].periodeFra).isEqualTo(LocalDate.parse("2011-01-01")) },
      { assertThat(husstandsmedlemBoListe[0].periodeTil).isEqualTo(LocalDate.parse("2011-02-01")) },
      { assertThat(husstandsmedlemBoListe[0].opprettetAv).isNull() },
      { assertThat(husstandsmedlemBoListe[0].hentetTidspunkt).isNotNull() },

      // sjekk SivilstandBo
      { assertThat(sivilstandBoListe[0].personId).isEqualTo("1234") },
      { assertThat(sivilstandBoListe[0].periodeFra).isEqualTo(LocalDate.parse("2011-01-01")) },
      { assertThat(sivilstandBoListe[0].periodeTil).isEqualTo(LocalDate.parse("2011-02-01")) },
      { assertThat(sivilstandBoListe[0].sivilstand).isEqualTo(SivilstandKode.SAMBOER) },
      { assertThat(sivilstandBoListe[0].aktiv).isTrue },
      { assertThat(sivilstandBoListe[0].brukFra).isNotNull() },
      { assertThat(sivilstandBoListe[0].brukTil).isNull() },
      { assertThat(sivilstandBoListe[0].opprettetAv).isNull() },
      { assertThat(sivilstandBoListe[0].hentetTidspunkt).isNotNull() }

    )
  }

//  @Test
//  @Suppress("NonAsciiCharacters")
//  fun `Skal oppdatere grunnlagspakke med utvidet barnetrygd`() {
//
//    Mockito.`when`(persistenceServiceMock.opprettNyGrunnlagspakke(MockitoHelper.capture(opprettGrunnlagspakkeRequestDtoCaptor)))
//      .thenReturn(byggGrunnlagspakke())
//    Mockito.`when`(
//      persistenceServiceMock.opprettUtvidetBarnetrygdOgSmaabarnstillegg(
//        MockitoHelper.capture(utvidetBarnetrygdOgSmaabarnstilleggBoCaptor)
//      )
//    ).thenReturn(byggUtvidetBarnetrygdOgSmaabarnstillegg())
//    Mockito.`when`(familieBaSakConsumerMock.hentFamilieBaSak(MockitoHelper.any(FamilieBaSakRequest::class.java)))
//      .thenReturn(RestResponse.Success(TestUtil.byggFamilieBaSakResponse()))
//
//    val grunnlagspakkeIdOpprettet = grunnlagspakkeService.opprettGrunnlagspakke(byggNyGrunnlagspakkeRequest())
//    val oppdatertGrunnlagspakke = grunnlagspakkeService.oppdaterGrunnlagspakke(
//      grunnlagspakkeIdOpprettet,
//      TestUtil.byggOppdaterGrunnlagspakkeRequestUtvidetBarnetrygd()
//    )
//
//    val opprettGrunnlagspakkeRequestDto = opprettGrunnlagspakkeRequestDtoCaptor.value
//    val ubstListe = utvidetBarnetrygdOgSmaabarnstilleggBoCaptor.allValues
//
//    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettNyGrunnlagspakke(MockitoHelper.any(OpprettGrunnlagspakkeRequestDto::class.java))
//    Mockito.verify(persistenceServiceMock, Mockito.times(1))
//      .opprettUtvidetBarnetrygdOgSmaabarnstillegg(MockitoHelper.any(UtvidetBarnetrygdOgSmaabarnstilleggBo::class.java))
//
//    assertAll(
//      Executable { assertThat(grunnlagspakkeIdOpprettet).isNotNull() },
//
//      // sjekk GrunnlagspakkeDto
//      Executable { assertThat(opprettGrunnlagspakkeRequestDto).isNotNull() },
//      Executable { assertThat(opprettGrunnlagspakkeRequestDto.opprettetAv).isEqualTo("RTV9999") },
//      Executable { assertThat(opprettGrunnlagspakkeRequestDto.formaal).isEqualTo(Formaal.BIDRAG) },
//
//      // sjekk UtvidetBarnetrygdOgSmaabarnstilleggDto
//      Executable { assertThat(ubstListe).isNotNull() },
//      Executable { assertThat(ubstListe.size).isEqualTo(1) },
//      Executable { assertThat(ubstListe[0].personId).isEqualTo("12345678910") },
//      Executable { assertThat(ubstListe[0].type).isEqualTo(BisysStønadstype.UTVIDET.toString()) },
//      Executable { assertThat(ubstListe[0].periodeFra).isEqualTo(LocalDate.parse("2021-01-01")) },
//      Executable { assertThat(ubstListe[0].periodeTil).isEqualTo(LocalDate.parse("2022-01-01")) },
//      Executable { assertThat(ubstListe[0].belop).isEqualTo(BigDecimal.valueOf(1000.11)) },
//      Executable { assertThat(ubstListe[0].manueltBeregnet).isFalse },
//      Executable { assertThat(ubstListe[0].deltBosted).isFalse },
//
//      // sjekk oppdatertGrunnlagspakke
//      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagspakkeId).isEqualTo(grunnlagspakkeIdOpprettet) },
//      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe.size).isEqualTo(1) },
//      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].type).isEqualTo(
//        GrunnlagRequestType.UTVIDET_BARNETRYGD_OG_SMAABARNSTILLEGG) },
//      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].personId).isEqualTo("12345678910") },
//      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].status).isEqualTo(
//        GrunnlagsRequestStatus.HENTET) },
//      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].statusMelding).isEqualTo("Antall perioder funnet: 1") }
//    )
//  }

//  @Test
//  @Suppress("NonAsciiCharacters")
//  fun `Skal oppdatere grunnlagspakke med barnetillegg fra Pensjon`() {
//
//    Mockito.`when`(persistenceServiceMock.opprettNyGrunnlagspakke(MockitoHelper.capture(opprettGrunnlagspakkeRequestDtoCaptor)))
//      .thenReturn(byggGrunnlagspakke())
//    Mockito.`when`(persistenceServiceMock.opprettBarnetillegg(MockitoHelper.capture(barnetilleggBoCaptor))).thenReturn(byggBarnetillegg())
//    Mockito.`when`(bidragGcpProxyConsumerMock.hentBarnetilleggPensjon(MockitoHelper.any(HentBarnetilleggPensjonRequest::class.java)))
//      .thenReturn(RestResponse.Success(TestUtil.byggHentBarnetilleggPensjonResponse()))
//
//    val grunnlagspakkeIdOpprettet = grunnlagspakkeService.opprettGrunnlagspakke(byggNyGrunnlagspakkeRequest())
//    val oppdatertGrunnlagspakke = grunnlagspakkeService.oppdaterGrunnlagspakke(
//      grunnlagspakkeIdOpprettet,
//      TestUtil.byggOppdaterGrunnlagspakkeRequestBarnetillegg()
//    )
//
//    val opprettGrunnlagspakkeRequestDto = opprettGrunnlagspakkeRequestDtoCaptor.value
//    val barnetilleggListe = barnetilleggBoCaptor.allValues
//
//    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettNyGrunnlagspakke(MockitoHelper.any(OpprettGrunnlagspakkeRequestDto::class.java))
//    Mockito.verify(persistenceServiceMock, Mockito.times(2)).opprettBarnetillegg(MockitoHelper.any(BarnetilleggBo::class.java))
//
//    assertAll(
//      Executable { assertThat(grunnlagspakkeIdOpprettet).isNotNull() },
//
//      // sjekk GrunnlagspakkeBo
//      Executable { assertThat(opprettGrunnlagspakkeRequestDto).isNotNull() },
//      Executable { assertThat(opprettGrunnlagspakkeRequestDto.opprettetAv).isEqualTo("RTV9999") },
//      Executable { assertThat(opprettGrunnlagspakkeRequestDto.formaal).isEqualTo(Formaal.BIDRAG) },
//
//      // sjekk BarnetilleggBo
//      Executable { assertThat(barnetilleggListe).isNotNull() },
//      Executable { assertThat(barnetilleggListe.size).isEqualTo(2) },
//      Executable { assertThat(barnetilleggListe[0].partPersonId).isEqualTo("12345678910") },
//      Executable { assertThat(barnetilleggListe[0].barnPersonId).isEqualTo("barnIdent") },
//      Executable { assertThat(barnetilleggListe[0].barnetilleggType).isEqualTo(BarnetilleggType.PENSJON.toString()) },
//      Executable { assertThat(barnetilleggListe[0].periodeFra).isEqualTo(LocalDate.parse("2021-01-01")) },
//      Executable { assertThat(barnetilleggListe[0].periodeTil).isEqualTo(LocalDate.parse("2022-01-01")) },
//      Executable { assertThat(barnetilleggListe[0].belopBrutto).isEqualTo(BigDecimal.valueOf(1000.11)) },
//      Executable { assertThat(barnetilleggListe[0].barnType).isEqualTo(BarnType.FELLES.toString()) },
//
//      Executable { assertThat(barnetilleggListe[1].partPersonId).isEqualTo("12345678910") },
//      Executable { assertThat(barnetilleggListe[1].barnPersonId).isEqualTo("barnIdent") },
//      Executable { assertThat(barnetilleggListe[1].barnetilleggType).isEqualTo(BarnetilleggType.PENSJON.toString()) },
//      Executable { assertThat(barnetilleggListe[1].periodeFra).isEqualTo(LocalDate.parse("2022-01-01")) },
//      Executable { assertThat(barnetilleggListe[1].periodeTil).isEqualTo(LocalDate.parse("2023-01-01")) },
//      Executable { assertThat(barnetilleggListe[1].belopBrutto).isEqualTo(BigDecimal.valueOf(2000.22)) },
//      Executable { assertThat(barnetilleggListe[1].barnType).isEqualTo(BarnType.FELLES.toString()) },
//
//      // sjekk oppdatertGrunnlagspakke
//      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagspakkeId).isEqualTo(grunnlagspakkeIdOpprettet) },
//      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe.size).isEqualTo(1) },
//      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].type).isEqualTo(GrunnlagRequestType.BARNETILLEGG) },
//      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].personId).isEqualTo("12345678910") },
//      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].status).isEqualTo(GrunnlagsRequestStatus.HENTET) },
//      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].statusMelding).isEqualTo("Antall perioder funnet: 2") }
//    )
//  }


//  @Test
//  @Suppress("NonAsciiCharacters")
//  fun `Skal oppdatere grunnlagspakke med husstand og husstandsmedlemmer fra PDL via bidrag-person`() {
//
//    Mockito.`when`(persistenceServiceMock.opprettNyGrunnlagspakke(MockitoHelper.capture(opprettGrunnlagspakkeRequestDtoCaptor)))
//      .thenReturn(byggGrunnlagspakke())
//    Mockito.`when`(persistenceServiceMock.opprettHusstand(MockitoHelper.capture(husstandBoCaptor))).thenReturn(byggHusstand())
//    Mockito.`when`(persistenceServiceMock.opprettHusstandsmedlem(MockitoHelper.capture(husstandsmedlemBoCaptor))).thenReturn(byggHusstandsmedlem())
//    Mockito.`when`(bidragPersonConsumerMock.hentHusstandsmedlemmer(MockitoHelper.any(HusstandsmedlemmerRequest::class.java)))
//      .thenReturn(RestResponse.Success(TestUtil.byggHentHusstandsmedlemmerResponse()))
//
//    val grunnlagspakkeIdOpprettet = grunnlagspakkeService.opprettGrunnlagspakke(byggNyGrunnlagspakkeRequest())
//    val oppdatertGrunnlagspakke = grunnlagspakkeService.oppdaterGrunnlagspakke(
//      grunnlagspakkeIdOpprettet,
//      TestUtil.byggOppdaterGrunnlagspakkeRequestHusstandsmedlemmer()
//    )
//
//    val opprettGrunnlagspakkeRequestDto = opprettGrunnlagspakkeRequestDtoCaptor.value
//    val husstandListe = husstandBoCaptor.allValues
//    val husstandsmedlemListe = husstandsmedlemBoCaptor.allValues
//
//    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettNyGrunnlagspakke(MockitoHelper.any(OpprettGrunnlagspakkeRequestDto::class.java))
//    Mockito.verify(persistenceServiceMock, Mockito.times(2)).opprettHusstand(MockitoHelper.any(HusstandBo::class.java))
//    Mockito.verify(persistenceServiceMock, Mockito.times(5)).opprettHusstandsmedlem(MockitoHelper.any(HusstandsmedlemBo::class.java))
//
//    assertAll(
//      Executable { assertThat(grunnlagspakkeIdOpprettet).isNotNull() },
//
//      // sjekk GrunnlagspakkeBo
//      Executable { assertThat(opprettGrunnlagspakkeRequestDto).isNotNull() },
//      Executable { assertThat(opprettGrunnlagspakkeRequestDto.opprettetAv).isEqualTo("RTV9999") },
//      Executable { assertThat(opprettGrunnlagspakkeRequestDto.formaal).isEqualTo(Formaal.BIDRAG) },
//
//      // sjekk HusstandBo
//      Executable { assertThat(husstandListe).isNotNull() },
//      Executable { assertThat(husstandListe.size).isEqualTo(3) },
//
//      Executable { assertThat(husstandListe.size).isEqualTo(2) },
//      Executable { assertThat(husstandListe[0]?.periodeFra).isEqualTo(LocalDate.parse("2011-01-01")) },
//      Executable { assertThat(husstandListe[0]?.periodeTil).isEqualTo(LocalDate.parse("2011-10-01")) },
//      Executable { assertThat(husstandListe[0]?.adressenavn).isEqualTo("adressenavn1") },
//      Executable { assertThat(husstandListe[0]?.husnummer).isEqualTo("husnummer1") },
//      Executable { assertThat(husstandListe[0]?.husbokstav).isEqualTo("husbokstav1") },
//      Executable { assertThat(husstandListe[0]?.bruksenhetsnummer).isEqualTo("bruksenhetsnummer1") },
//      Executable { assertThat(husstandListe[0]?.postnummer).isEqualTo("postnr1") },
//      Executable { assertThat(husstandListe[0]?.bydelsnummer).isEqualTo("bydelsnummer1") },
//      Executable { assertThat(husstandListe[0]?.kommunenummer).isEqualTo("kommunenummer1") },
//      Executable { assertThat(husstandListe[0]?.matrikkelId).isEqualTo(12345) },
//      Executable { assertThat(husstandListe[0]?.aktiv).isTrue() },
//      Executable { assertThat(husstandListe[0]?.brukFra).isNotNull() },
//      Executable { assertThat(husstandListe[0]?.brukTil).isNull() },
//      Executable { assertThat(husstandListe[0]?.opprettetAv).isNull() },
//      Executable { assertThat(husstandListe[0]?.hentetTidspunkt).isNotNull() },
//
//      Executable { assertThat(husstandsmedlemListe?.get(0)?.personId).isEqualTo("123") },
//      Executable { assertThat(husstandsmedlemListe?.get(0)?.navn).isEqualTo("fornavn1 mellomnavn1 etternavn1") },
//      Executable { assertThat(husstandsmedlemListe?.get(0)?.periodeFra).isEqualTo(LocalDate.parse("2011-01-01")) },
//      Executable { assertThat(husstandsmedlemListe?.get(0)?.periodeTil).isEqualTo(LocalDate.parse("2011-02-01")) },
//      Executable { assertThat(husstandsmedlemListe?.get(0)?.opprettetAv).isNull() },
//      Executable { assertThat(husstandsmedlemListe?.get(0)?.hentetTidspunkt).isNotNull() },
//
//      Executable { assertThat(husstandsmedlemListe?.get(1)?.personId).isEqualTo("234") },
//      Executable { assertThat(husstandsmedlemListe?.get(1)?.navn).isEqualTo("fornavn2 mellomnavn2 etternavn2") },
//      Executable { assertThat(husstandsmedlemListe?.get(1)?.periodeFra).isEqualTo(LocalDate.parse("2011-01-01")) },
//      Executable { assertThat(husstandsmedlemListe?.get(1)?.periodeTil).isEqualTo(LocalDate.parse("2011-12-01")) },
//      Executable { assertThat(husstandsmedlemListe?.get(1)?.opprettetAv).isNull() },
//      Executable { assertThat(husstandsmedlemListe?.get(1)?.hentetTidspunkt).isNotNull() },
//
//      Executable { assertThat(husstandsmedlemListe?.get(2)?.personId).isEqualTo("345") },
//      Executable { assertThat(husstandsmedlemListe?.get(2)?.navn).isEqualTo("fornavn3 mellomnavn3 etternavn3") },
//      Executable { assertThat(husstandsmedlemListe?.get(2)?.periodeFra).isEqualTo(LocalDate.parse("2011-05-01")) },
//      Executable { assertThat(husstandsmedlemListe?.get(2)?.periodeTil).isEqualTo(LocalDate.parse("2011-06-01")) },
//      Executable { assertThat(husstandsmedlemListe?.get(2)?.opprettetAv).isNull() },
//      Executable { assertThat(husstandsmedlemListe?.get(2)?.hentetTidspunkt).isNotNull() },
//
//
//
//          // sjekk oppdatertGrunnlagspakke
//      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagspakkeId).isEqualTo(grunnlagspakkeIdOpprettet) },
//      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe.size).isEqualTo(1) },
//      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].type).isEqualTo(GrunnlagRequestType.HUSSTANDSMEDLEMMER) },
//      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].personId).isEqualTo("12345678910") },
//      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].status).isEqualTo(GrunnlagsRequestStatus.HENTET) },
//      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].statusMelding).isEqualTo("Antall husstander funnet: 2") }
//    )
//  }
//
//
//
//  @Test
//  @Suppress("NonAsciiCharacters")
//  fun `Skal oppdatere grunnlagspakke med sivilstand fra PDL via bidrag-person`() {
//
//    Mockito.`when`(persistenceServiceMock.opprettNyGrunnlagspakke(MockitoHelper.capture(opprettGrunnlagspakkeRequestDtoCaptor)))
//      .thenReturn(byggGrunnlagspakke())
//    Mockito.`when`(persistenceServiceMock.opprettSivilstand(MockitoHelper.capture(sivilstandBoCaptor))).thenReturn(byggSivilstand())
//    Mockito.`when`(bidragPersonConsumerMock.hentSivilstand(MockitoHelper.any(SivilstandRequest::class.java)))
//      .thenReturn(RestResponse.Success(TestUtil.byggHentSivilstandResponse()))
//
//    val grunnlagspakkeIdOpprettet = grunnlagspakkeService.opprettGrunnlagspakke(byggNyGrunnlagspakkeRequest())
//    val oppdatertGrunnlagspakke = grunnlagspakkeService.oppdaterGrunnlagspakke(
//      grunnlagspakkeIdOpprettet,
//      TestUtil.byggOppdaterGrunnlagspakkeRequestSivilstand()
//    )
//
//    val opprettGrunnlagspakkeRequestDto = opprettGrunnlagspakkeRequestDtoCaptor.value
//    val sivilstandListe = sivilstandBoCaptor.allValues
//
//    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettNyGrunnlagspakke(MockitoHelper.any(OpprettGrunnlagspakkeRequestDto::class.java))
//    Mockito.verify(persistenceServiceMock, Mockito.times(3)).opprettSivilstand(MockitoHelper.any(SivilstandBo::class.java))
//
//    assertAll(
//      Executable { assertThat(grunnlagspakkeIdOpprettet).isNotNull() },
//
//      // sjekk GrunnlagspakkeBo
//      Executable { assertThat(opprettGrunnlagspakkeRequestDto).isNotNull() },
//      Executable { assertThat(opprettGrunnlagspakkeRequestDto.opprettetAv).isEqualTo("RTV9999") },
//      Executable { assertThat(opprettGrunnlagspakkeRequestDto.formaal).isEqualTo(Formaal.BIDRAG) },
//
//      // sjekk SivilstandBo
//      Executable { assertThat(sivilstandListe).isNotNull() },
//      Executable { assertThat(sivilstandListe.size).isEqualTo(3) },
//      Executable { assertThat(sivilstandListe[0].personId).isEqualTo("12345678910") },
//      Executable { assertThat(sivilstandListe[0].periodeFra).isNull() },
//      Executable { assertThat(sivilstandListe[0].periodeTil).isNull() },
//      Executable { assertThat(sivilstandListe[0].sivilstand).isEqualTo(SivilstandKode.ENSLIG.toString()) },
//
//      Executable { assertThat(sivilstandListe[1].personId).isEqualTo("12345678910") },
//      Executable { assertThat(sivilstandListe[1].periodeFra).isEqualTo(LocalDate.parse("2021-01-01")) },
//      Executable { assertThat(sivilstandListe[1].periodeTil).isNull() },
//      Executable { assertThat(sivilstandListe[1].sivilstand).isEqualTo(SivilstandKode.SAMBOER.toString()) },
//
//      Executable { assertThat(sivilstandListe[2].personId).isEqualTo("12345678910") },
//      Executable { assertThat(sivilstandListe[2].periodeFra).isEqualTo(LocalDate.parse("2021-09-01")) },
//      Executable { assertThat(sivilstandListe[2].periodeTil).isNull() },
//      Executable { assertThat(sivilstandListe[2].sivilstand).isEqualTo(SivilstandKode.GIFT.toString()) },
//
//      // sjekk oppdatertGrunnlagspakke
//      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagspakkeId).isEqualTo(grunnlagspakkeIdOpprettet) },
//      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe.size).isEqualTo(1) },
//      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].type).isEqualTo(GrunnlagRequestType.SIVILSTAND) },
//      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].personId).isEqualTo("12345678910") },
//      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].status).isEqualTo(GrunnlagsRequestStatus.HENTET) },
//      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].statusMelding).isEqualTo("Antall perioder funnet: 3") },
//      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].type).isEqualTo(GrunnlagRequestType.BARNETILLEGG) },
//      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].personId).isEqualTo("12345678910") },
//      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].status).isEqualTo(GrunnlagsRequestStatus.HENTET) },
//      Executable { assertThat(oppdatertGrunnlagspakke.grunnlagTypeResponsListe[0].statusMelding).isEqualTo("Antall perioder funnet: 1") }
//=======
//      // sjekk UtvidetBarnetrygdOgSmaabarnstilleggdDto
//      { assertThat(ubstListe[0].personId).isEqualTo("1234567") },
//      { assertThat(ubstListe[0].type).isEqualTo("Utvidet barnetrygd") },
//      { assertThat(ubstListe[0].periodeFra).isEqualTo(LocalDate.parse("2021-01-01")) },
//      { assertThat(ubstListe[0].periodeTil).isEqualTo(LocalDate.parse("2021-07-01")) },
//      { assertThat(ubstListe[0].belop).isEqualTo(BigDecimal.valueOf(12468.01)) },
//      { assertThat(ubstListe[0].manueltBeregnet).isFalse },
//      { assertThat(ubstListe[0].deltBosted).isFalse },
//
//      // sjekk BarnetilleggDto
//      { assertThat(barnetilleggListe[0].partPersonId).isEqualTo("1234567") },
//      { assertThat(barnetilleggListe[0].barnPersonId).isEqualTo("0123456") },
//      { assertThat(barnetilleggListe[0].barnetilleggType).isEqualTo("Utvidet barnetrygd") },
//      { assertThat(barnetilleggListe[0].periodeFra).isEqualTo(LocalDate.parse("2021-01-01")) },
//      { assertThat(barnetilleggListe[0].periodeTil).isEqualTo(LocalDate.parse("2021-07-01")) },
//      { assertThat(barnetilleggListe[0].belopBrutto).isEqualTo(BigDecimal.valueOf(1000)) },
//      { assertThat(barnetilleggListe[0].barnType).isEqualTo(BarnType.FELLES.toString()) }
//>>>>>>> main
//    )
//  }

  object MockitoHelper {

    // use this in place of captor.capture() if you are trying to capture an argument that is not nullable
    fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()
    fun <T> any(type: Class<T>): T = Mockito.any(type)
  }
}
