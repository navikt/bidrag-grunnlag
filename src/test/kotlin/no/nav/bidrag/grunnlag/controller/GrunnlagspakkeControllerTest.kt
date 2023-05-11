package no.nav.bidrag.grunnlag.controller

import no.nav.bidrag.behandling.felles.dto.grunnlag.GrunnlagRequestDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.HentGrunnlagspakkeDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.OppdaterGrunnlagDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.OppdaterGrunnlagspakkeDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.OppdaterGrunnlagspakkeRequestDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.OpprettGrunnlagspakkeRequestDto
import no.nav.bidrag.behandling.felles.enums.Formaal
import no.nav.bidrag.behandling.felles.enums.GrunnlagRequestType
import no.nav.bidrag.behandling.felles.enums.GrunnlagsRequestStatus
import no.nav.bidrag.commons.ExceptionLogger
import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.grunnlag.BidragGrunnlagTest
import no.nav.bidrag.grunnlag.BidragGrunnlagTest.Companion.TEST_PROFILE
import no.nav.bidrag.grunnlag.TestUtil
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.BidragGcpProxyConsumer
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.barnetillegg.HentBarnetilleggPensjonResponse
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.skatt.HentSkattegrunnlagResponse
import no.nav.bidrag.grunnlag.consumer.bidragperson.BidragPersonConsumer
import no.nav.bidrag.grunnlag.consumer.familiebasak.FamilieBaSakConsumer
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.FamilieBaSakResponse
import no.nav.bidrag.grunnlag.consumer.familieefsak.FamilieEfSakConsumer
import no.nav.bidrag.grunnlag.consumer.familieefsak.api.BarnetilsynResponse
import no.nav.bidrag.grunnlag.consumer.familieefsak.api.Ressurs
import no.nav.bidrag.grunnlag.consumer.familiekssak.FamilieKsSakConsumer
import no.nav.bidrag.grunnlag.consumer.familiekssak.api.BisysResponsDto
import no.nav.bidrag.grunnlag.consumer.inntektskomponenten.InntektskomponentenConsumer
import no.nav.bidrag.grunnlag.exception.HibernateExceptionHandler
import no.nav.bidrag.grunnlag.exception.RestExceptionHandler
import no.nav.bidrag.grunnlag.exception.custom.CustomExceptionHandler
import no.nav.bidrag.grunnlag.persistence.repository.GrunnlagspakkeRepository
import no.nav.bidrag.grunnlag.service.GrunnlagspakkeService
import no.nav.bidrag.grunnlag.service.InntektskomponentenService
import no.nav.bidrag.grunnlag.service.OppdaterGrunnlagspakkeService
import no.nav.bidrag.grunnlag.service.PersistenceService
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import no.nav.tjenester.aordningen.inntektsinformasjon.response.HentInntektListeResponse
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.HibernateException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.StatusResultMatchersDsl
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.HttpClientErrorException
import java.time.LocalDate

@DisplayName("GrunnlagspakkeControllerTest")
@ActiveProfiles(TEST_PROFILE)
@SpringBootTest(classes = [BidragGrunnlagTest::class], webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableMockOAuth2Server
@AutoConfigureWireMock(port = 0)
@Transactional
class GrunnlagspakkeControllerTest(
    @Autowired val grunnlagspakkeRepository: GrunnlagspakkeRepository,
    @Autowired val persistenceService: PersistenceService,
    @Autowired val exceptionLogger: ExceptionLogger
) {

    private val restTemplate: HttpHeaderRestTemplate = Mockito.mock(HttpHeaderRestTemplate::class.java)
    private val bidragGcpProxyConsumer: BidragGcpProxyConsumer = BidragGcpProxyConsumer(restTemplate)
    private val inntektskomponentenConsumer: InntektskomponentenConsumer = InntektskomponentenConsumer(restTemplate)
    private val inntektskomponentenService: InntektskomponentenService = InntektskomponentenService(inntektskomponentenConsumer)
    private val familieBaSakConsumer: FamilieBaSakConsumer = FamilieBaSakConsumer(restTemplate)
    private val bidragPersonConsumer: BidragPersonConsumer = BidragPersonConsumer(restTemplate)
    private val familieKsSakConsumer: FamilieKsSakConsumer = FamilieKsSakConsumer(restTemplate)
    private val familieEfSakConsumer: FamilieEfSakConsumer = FamilieEfSakConsumer(restTemplate)
    private val oppdaterGrunnlagspakkeService: OppdaterGrunnlagspakkeService = OppdaterGrunnlagspakkeService(
        persistenceService,
        familieBaSakConsumer,
        bidragGcpProxyConsumer,
        inntektskomponentenService,
        bidragPersonConsumer,
        familieKsSakConsumer,
        familieEfSakConsumer
    )
    private val grunnlagspakkeService: GrunnlagspakkeService = GrunnlagspakkeService(persistenceService, oppdaterGrunnlagspakkeService)
    private val grunnlagspakkeController: GrunnlagspakkeController = GrunnlagspakkeController(grunnlagspakkeService)
    private val mockMvc: MockMvc = MockMvcBuilders.standaloneSetup(grunnlagspakkeController)
        .setControllerAdvice(
            RestExceptionHandler(exceptionLogger),
            CustomExceptionHandler(exceptionLogger),
            HibernateExceptionHandler(exceptionLogger)
        )
        .build()

    @BeforeEach
    fun `init`() {
        grunnlagspakkeRepository.deleteAll()
    }

    @Test
    fun `skal opprette ny grunnlagspakke`() {
        opprettGrunnlagspakke(OpprettGrunnlagspakkeRequestDto(Formaal.FORSKUDD, "X123456"))
    }

    @Test
    fun `skal oppdatere en grunnlagspakke`() {
        val grunnlagspakkeIdOpprettet = opprettGrunnlagspakke(OpprettGrunnlagspakkeRequestDto(Formaal.FORSKUDD, "X123456"))

        Mockito.`when`(
            restTemplate.exchange(
                eq("/rs/api/v1/hentdetaljerteabonnerteinntekter"),
                eq(HttpMethod.POST),
                any(),
                any<Class<HentInntektListeResponse>>()
            )
        ).thenReturn(
            ResponseEntity(TestUtil.byggHentInntektListeResponse(), HttpStatus.OK)
        )

        Mockito.`when`(restTemplate.exchange(eq("/skattegrunnlag/hent"), eq(HttpMethod.POST), any(), any<Class<HentSkattegrunnlagResponse>>()))
            .thenReturn(
                ResponseEntity(TestUtil.byggHentSkattegrunnlagResponse(), HttpStatus.OK)
            )

        Mockito.`when`(
            restTemplate.exchange(
                eq("/barnetillegg/pensjon/hent"),
                eq(HttpMethod.POST),
                any(),
                any<Class<HentBarnetilleggPensjonResponse>>()
            )
        )
            .thenReturn(
                ResponseEntity(TestUtil.byggHentBarnetilleggPensjonResponse(), HttpStatus.OK)
            )

        Mockito.`when`(
            restTemplate.exchange(
                eq("/api/bisys/hent-utvidet-barnetrygd"),
                eq(HttpMethod.POST),
                any(),
                any<Class<FamilieBaSakResponse>>()
            )
        )
            .thenReturn(
                ResponseEntity(TestUtil.byggFamilieBaSakResponse(), HttpStatus.OK)
            )

        Mockito.`when`(
            restTemplate.exchange(
                eq("/api/ekstern/bisys/perioder-barnetilsyn"),
                eq(HttpMethod.POST),
                any(),
                any<Class<BarnetilsynResponse>>()
            )
        )
            .thenReturn(
                ResponseEntity(TestUtil.byggBarnetilsynResponse(), HttpStatus.OK)
            )

        Mockito.`when`(restTemplate.exchange(eq("/api/bisys/hent-utbetalingsinfo"), eq(HttpMethod.POST), any(), any<Class<BisysResponsDto>>()))
            .thenReturn(
                ResponseEntity(TestUtil.byggKontantstotteResponse(), HttpStatus.OK)
            )

        Mockito.`when`(
            restTemplate.exchange(
                eq("/api/ekstern/perioder/overgangsstonad/med-belop"),
                eq(HttpMethod.POST),
                any(),
                any<Class<Ressurs>>()
            )
        )
            .thenReturn(
                ResponseEntity(TestUtil.byggOvergangsstønadResponse(), HttpStatus.OK)
            )

        val oppdaterGrunnlagspakkeDto = oppdaterGrunnlagspakke(
            grunnlagspakkeIdOpprettet,
            TestUtil.byggOppdaterGrunnlagspakkeRequestKomplett(),
            OppdaterGrunnlagspakkeDto::class.java
        ) { isOk() }

        assertThat(oppdaterGrunnlagspakkeDto.grunnlagTypeResponsListe.size).isEqualTo(7)

        oppdaterGrunnlagspakkeDto.grunnlagTypeResponsListe.forEach { grunnlagstypeResponse ->
            assertEquals(GrunnlagsRequestStatus.HENTET, grunnlagstypeResponse.status)
        }
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal oppdatere grunnlagspakke og håndtere rest-kall-feil`() {
        val grunnlagspakkeIdOpprettet = opprettGrunnlagspakke(OpprettGrunnlagspakkeRequestDto(Formaal.FORSKUDD, "X123456"))

        Mockito.`when`(
            restTemplate.exchange(
                eq("/rs/api/v1/hentdetaljerteabonnerteinntekter"),
                eq(HttpMethod.POST),
                any(),
                any<Class<HentInntektListeResponse>>()
            )
        ).thenThrow(
            HttpClientErrorException(HttpStatus.NOT_FOUND)
        )

        Mockito.`when`(
            restTemplate.exchange(
                eq("/rs/api/v1/hentinntektliste"),
                eq(HttpMethod.POST),
                any(),
                any<Class<HentInntektListeResponse>>()
            )
        ).thenThrow(
            HttpClientErrorException(HttpStatus.NOT_FOUND)
        )

        Mockito.`when`(restTemplate.exchange(eq("/skattegrunnlag/hent"), eq(HttpMethod.POST), any(), any<Class<HentSkattegrunnlagResponse>>()))
            .thenThrow(
                HttpClientErrorException(HttpStatus.NOT_FOUND)
            )

        Mockito.`when`(
            restTemplate.exchange(
                eq("/barnetillegg/pensjon/hent"),
                eq(HttpMethod.POST),
                any(),
                any<Class<HentBarnetilleggPensjonResponse>>()
            )
        )
            .thenThrow(
                HttpClientErrorException(HttpStatus.NOT_FOUND)
            )

        Mockito.`when`(
            restTemplate.exchange(
                eq("/api/bisys/hent-utvidet-barnetrygd"),
                eq(HttpMethod.POST),
                any(),
                any<Class<FamilieBaSakResponse>>()
            )
        )
            .thenThrow(
                HttpClientErrorException(HttpStatus.NOT_FOUND)
            )

        Mockito.`when`(restTemplate.exchange(eq("/api/bisys/hent-utbetalingsinfo"), eq(HttpMethod.POST), any(), any<Class<BisysResponsDto>>()))
            .thenThrow(
                HttpClientErrorException(HttpStatus.NOT_FOUND)
            )

        Mockito.`when`(
            restTemplate.exchange(
                eq("/api/ekstern/bisys/perioder-barnetilsyn"),
                eq(HttpMethod.POST),
                any(),
                any<Class<BarnetilsynResponse>>()
            )
        )
            .thenThrow(
                HttpClientErrorException(HttpStatus.NOT_FOUND)
            )

        Mockito.`when`(
            restTemplate.exchange(
                eq("/api/ekstern/perioder/overgangsstonad/med-belop"),
                eq(HttpMethod.POST),
                any(),
                any<Class<Ressurs>>()
            )
        )
            .thenThrow(
                HttpClientErrorException(HttpStatus.NOT_FOUND)
            )

        val oppdaterGrunnlagspakkeDto = oppdaterGrunnlagspakke(
            grunnlagspakkeIdOpprettet,
            TestUtil.byggOppdaterGrunnlagspakkeRequestKomplett(),
            OppdaterGrunnlagspakkeDto::class.java
        ) { isOk() }

        assertThat(oppdaterGrunnlagspakkeDto).isNotNull
        assertThat(oppdaterGrunnlagspakkeDto.grunnlagTypeResponsListe.size).isEqualTo(7)

        oppdaterGrunnlagspakkeDto.grunnlagTypeResponsListe.forEach { grunnlagstypeResponse ->
            assertEquals(grunnlagstypeResponse.status, GrunnlagsRequestStatus.IKKE_FUNNET)
        }
    }

    @Test
    fun `skal finne data for en grunnlagspakke`() {
        val grunnlagspakkeIdOpprettet = opprettGrunnlagspakke(OpprettGrunnlagspakkeRequestDto(Formaal.FORSKUDD, "X123456"))

        val hentGrunnlagspakkeResponse = TestUtil.performRequest(
            mockMvc,
            HttpMethod.GET,
            "/grunnlagspakke/$grunnlagspakkeIdOpprettet",
            null,
            HentGrunnlagspakkeDto::class.java
        ) { isOk() }

        assertNotNull(hentGrunnlagspakkeResponse)
        assertThat(hentGrunnlagspakkeResponse.grunnlagspakkeId).isEqualTo(grunnlagspakkeIdOpprettet)
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal fange opp og håndtere Hibernate-feil`() {
        val grunnlagspakkeService = Mockito.mock(GrunnlagspakkeService::class.java)
        val grunnlagspakkeController = GrunnlagspakkeController(grunnlagspakkeService)
        val mockMvc =
            MockMvcBuilders.standaloneSetup(grunnlagspakkeController).setControllerAdvice(HibernateExceptionHandler(exceptionLogger)).build()

        val grunnlagspakkeIdOpprettet = opprettGrunnlagspakke(OpprettGrunnlagspakkeRequestDto(Formaal.FORSKUDD, "X123456"))

        Mockito.`when`(
            grunnlagspakkeService.oppdaterGrunnlagspakke(
                grunnlagspakkeIdOpprettet,
                TestUtil.byggOppdaterGrunnlagspakkeRequestKomplett()
            )
        )
            .thenThrow(HibernateException("Test-melding"))

        val oppdaterGrunnlagspakkeResponse =
            oppdaterGrunnlagspakke(
                grunnlagspakkeIdOpprettet,
                TestUtil.byggOppdaterGrunnlagspakkeRequestKomplett(),
                String::class.java,
                mockMvc
            ) { isInternalServerError() }

        assertNotNull(oppdaterGrunnlagspakkeResponse)
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal fange opp og håndtere forespørsler på grunnlagspakker som ikke eksisterer`() {
        val oppdaterGrunnlagspakkeResponse =
            oppdaterGrunnlagspakke(1, TestUtil.byggOppdaterGrunnlagspakkeRequestKomplett(), String::class.java) { isNotFound() }

        assertNotNull(oppdaterGrunnlagspakkeResponse)

        val hentGrunnlagspakkeResponse = TestUtil.performRequest(
            mockMvc,
            HttpMethod.GET,
            "/grunnlagspakke/1",
            null,
            String::class.java
        ) { isNotFound() }

        assertNotNull(hentGrunnlagspakkeResponse)

        val lukkGrunnlagspakkeResponse = TestUtil.performRequest(
            mockMvc,
            HttpMethod.POST,
            "/grunnlagspakke/1/lukk",
            null,
            String::class.java
        ) { isNotFound() }

        assertNotNull(lukkGrunnlagspakkeResponse)
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal håndtere feil eller manglende felter i input ved opprett grunnlagspakke-kall`() {
        var errorResult = performExpectedFailingRequest("/requests/opprettGrunnlagspakke1.json", GrunnlagspakkeController.GRUNNLAGSPAKKE_NY)

        assertNotNull(errorResult)
        assertNotNull(errorResult["formaal"])

        errorResult = performExpectedFailingRequest("/requests/opprettGrunnlagspakke2.json", GrunnlagspakkeController.GRUNNLAGSPAKKE_NY)

        assertNotNull(errorResult)
        assertNotNull(errorResult["opprettetAv"])

        errorResult = performExpectedFailingRequest("/requests/opprettGrunnlagspakke3.json", GrunnlagspakkeController.GRUNNLAGSPAKKE_NY)

        assertNotNull(errorResult)
        assertNotNull(errorResult["opprettetAv"])

        val fileContent = getFileContent("/requests/opprettGrunnlagspakke4.json")
        val okResult = TestUtil.performRequest(
            mockMvc,
            HttpMethod.POST,
            GrunnlagspakkeController.GRUNNLAGSPAKKE_NY,
            fileContent,
            Int::class.java
        ) { isOk() }

        assertNotNull(okResult)
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal håndtere feil eller manglende felter i input ved oppdater grunnlagspakke-kall`() {
        var errorResult = performExpectedFailingRequest("/requests/oppdaterGrunnlagspakke1.json", "/grunnlagspakke/null/oppdater")

        assertNotNull(errorResult)
        assertNotNull(errorResult["grunnlagspakkeId"])

        errorResult = performExpectedFailingRequest("/requests/oppdaterGrunnlagspakke1.json", "/grunnlagspakke/Test/oppdater")

        assertNotNull(errorResult)
        assertNotNull(errorResult["grunnlagspakkeId"])

        errorResult = performExpectedFailingRequest("/requests/oppdaterGrunnlagspakke1.json", "/grunnlagspakke/1/oppdater")

        assertNotNull(errorResult)
        assertNotNull(errorResult["grunnlagRequestDtoListe"])

        errorResult = performExpectedFailingRequest("/requests/oppdaterGrunnlagspakke5.json", "/grunnlagspakke/1/oppdater")

        assertNotNull(errorResult)
        assertNotNull(errorResult["grunnlagRequestDtoListe[0].type"])

        errorResult = performExpectedFailingRequest("/requests/oppdaterGrunnlagspakke6.json", "/grunnlagspakke/1/oppdater")

        assertNotNull(errorResult)
        assertNotNull(errorResult["grunnlagRequestDtoListe[0].personId"])

        errorResult = performExpectedFailingRequest("/requests/oppdaterGrunnlagspakke7.json", "/grunnlagspakke/1/oppdater")

        assertNotNull(errorResult)
        assertNotNull(errorResult["grunnlagRequestDtoListe[0].personId"])

        errorResult = performExpectedFailingRequest("/requests/oppdaterGrunnlagspakke8.json", "/grunnlagspakke/1/oppdater")

        assertNotNull(errorResult)
        assertNotNull(errorResult["grunnlagRequestDtoListe[0].periodeFra"])

        errorResult = performExpectedFailingRequest("/requests/oppdaterGrunnlagspakke9.json", "/grunnlagspakke/1/oppdater")

        assertNotNull(errorResult)
        assertNotNull(errorResult["grunnlagRequestDtoListe[0].periodeTil"])

        val grunnlagspakkeService = Mockito.mock(GrunnlagspakkeService::class.java)
        val grunnlagspakkeController = GrunnlagspakkeController(grunnlagspakkeService)
        val mockMvc = MockMvcBuilders.standaloneSetup(grunnlagspakkeController).setControllerAdvice(RestExceptionHandler(exceptionLogger)).build()

        Mockito.`when`(
            grunnlagspakkeService.oppdaterGrunnlagspakke(
                1,
                OppdaterGrunnlagspakkeRequestDto(
                    grunnlagRequestDtoListe = listOf(
                        GrunnlagRequestDto(
                            type = GrunnlagRequestType.UTVIDET_BARNETRYGD_OG_SMAABARNSTILLEGG,
                            personId = "12345678901",
                            periodeFra = LocalDate.parse("2021-11-01"),
                            periodeTil = LocalDate.parse("2021-11-15")
                        )
                    )
                )
            )
        )
            .thenReturn(
                OppdaterGrunnlagspakkeDto(
                    grunnlagspakkeId = 1,
                    grunnlagTypeResponsListe =
                    listOf(
                        OppdaterGrunnlagDto(
                            type = GrunnlagRequestType.UTVIDET_BARNETRYGD_OG_SMAABARNSTILLEGG,
                            personId = "12345678901",
                            status = GrunnlagsRequestStatus.HENTET,
                            statusMelding = "Ok"
                        )
                    )
                )
            )

        val fileContent = getFileContent("/requests/oppdaterGrunnlagspakke10.json")
        val okResult = TestUtil.performRequest(
            mockMvc,
            HttpMethod.POST,
            "/grunnlagspakke/1/oppdater",
            fileContent,
            OppdaterGrunnlagspakkeDto::class.java
        ) { isOk() }

        assertNotNull(okResult)
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal håndtere feil eller manglende felter i input ved lukk grunnlagspakke-kall`() {
        val errorResult = performExpectedFailingRequest(null, "/grunnlagspakke/null/lukk")

        assertNotNull(errorResult)
        assertNotNull(errorResult["grunnlagspakkeId"])

        val grunnlagspakkeService = Mockito.mock(GrunnlagspakkeService::class.java)
        val grunnlagspakkeController = GrunnlagspakkeController(grunnlagspakkeService)
        val mockMvc = MockMvcBuilders.standaloneSetup(grunnlagspakkeController).setControllerAdvice(RestExceptionHandler(exceptionLogger)).build()

        Mockito.`when`(grunnlagspakkeService.lukkGrunnlagspakke(1)).thenReturn(1)

        val okResult = TestUtil.performRequest(
            mockMvc,
            HttpMethod.POST,
            "/grunnlagspakke/1/lukk",
            null,
            Int::class.java
        ) { isOk() }

        assertNotNull(okResult)
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal håndtere feil eller manglende felter i input ved hent grunnlagspakke-kall`() {
        val errorResult = TestUtil.performRequest(
            mockMvc,
            HttpMethod.GET,
            "/grunnlagspakke/null",
            null,
            MutableMap::class.java
        ) { isBadRequest() }

        assertNotNull(errorResult)
        assertNotNull(errorResult["grunnlagspakkeId"])

        val grunnlagspakkeService = Mockito.mock(GrunnlagspakkeService::class.java)
        val grunnlagspakkeController = GrunnlagspakkeController(grunnlagspakkeService)
        val mockMvc = MockMvcBuilders.standaloneSetup(grunnlagspakkeController).setControllerAdvice(RestExceptionHandler(exceptionLogger)).build()

        Mockito.`when`(grunnlagspakkeService.hentGrunnlagspakke(1))
            .thenReturn(
                HentGrunnlagspakkeDto(
                    grunnlagspakkeId = 1,
                    ainntektListe = emptyList(),
                    skattegrunnlagListe = emptyList(),
                    ubstListe = emptyList(),
                    barnetilleggListe = emptyList(),
                    kontantstotteListe = emptyList(),
                    husstandmedlemmerOgEgneBarnListe = emptyList(),
                    sivilstandListe = emptyList(),
                    barnetilsynListe = emptyList(),
                    overgangsstonadListe = emptyList()
                )
            )

        val okResult = TestUtil.performRequest(
            mockMvc,
            HttpMethod.GET,
            "/grunnlagspakke/1",
            null,
            HentGrunnlagspakkeDto::class.java
        ) { isOk() }

        assertNotNull(okResult)
    }

    private fun opprettGrunnlagspakke(opprettGrunnlagspakkeRequestDto: OpprettGrunnlagspakkeRequestDto): Int {
        val nyGrunnlagspakkeOpprettetResponse = TestUtil.performRequest(
            mockMvc,
            HttpMethod.POST,
            GrunnlagspakkeController.GRUNNLAGSPAKKE_NY,
            opprettGrunnlagspakkeRequestDto,
            Int::class.java
        ) { isOk() }

        assertNotNull(nyGrunnlagspakkeOpprettetResponse)

        return nyGrunnlagspakkeOpprettetResponse
    }

    private fun getFileContent(filpath: String): String {
        var json = ""
        val url = this::class.java.getResource(filpath)
        if (url != null) {
            json = url.readText()
        } else {
            fail("Klarte ikke å lese fil fra: $filpath")
        }
        return json
    }

    private fun <Response> oppdaterGrunnlagspakke(
        grunnlagspakkeId: Int,
        oppdaterGrunnlagspakkeRequestDto: OppdaterGrunnlagspakkeRequestDto,
        responseType: Class<Response>,
        customMockMvc: MockMvc? = null,
        expectedStatus: StatusResultMatchersDsl.() -> Unit
    ): Response {
        return TestUtil.performRequest(
            customMockMvc ?: mockMvc,
            HttpMethod.POST,
            "/grunnlagspakke/$grunnlagspakkeId/oppdater",
            oppdaterGrunnlagspakkeRequestDto,
            responseType
        ) { expectedStatus() }
    }

    private fun performExpectedFailingRequest(filepath: String?, url: String): MutableMap<*, *> {
        val json = if (filepath != null) getFileContent(filepath) else null
        return TestUtil.performRequest(
            mockMvc,
            HttpMethod.POST,
            url,
            json,
            MutableMap::class.java
        ) { isBadRequest() }
    }
}
