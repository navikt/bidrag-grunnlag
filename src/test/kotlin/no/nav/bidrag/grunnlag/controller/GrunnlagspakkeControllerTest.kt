package no.nav.bidrag.grunnlag.controller

import no.nav.bidrag.commons.ExceptionLogger
import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.grunnlag.BidragGrunnlagTest
import no.nav.bidrag.grunnlag.BidragGrunnlagTest.Companion.TEST_PROFILE
import no.nav.bidrag.grunnlag.TestUtil
import no.nav.bidrag.grunnlag.api.grunnlagspakke.GrunnlagRequest
import no.nav.bidrag.grunnlag.api.grunnlagspakke.HentKomplettGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OppdaterGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OppdaterGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OpprettGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OpprettGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.BidragGcpProxyConsumer
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.ainntekt.HentInntektListeResponse
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.skatt.HentSkattegrunnlagResponse
import no.nav.bidrag.grunnlag.consumer.familiebasak.FamilieBaSakConsumer
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.FamilieBaSakResponse
import no.nav.bidrag.grunnlag.exception.HibernateExceptionHandler
import no.nav.bidrag.grunnlag.exception.RestExceptionHandler
import no.nav.bidrag.grunnlag.exception.custom.CustomExceptionHandler
import no.nav.bidrag.grunnlag.persistence.repository.GrunnlagspakkeRepository
import no.nav.bidrag.grunnlag.service.Formaal
import no.nav.bidrag.grunnlag.service.GrunnlagspakkeService
import no.nav.bidrag.grunnlag.service.Grunnlagstype
import no.nav.bidrag.grunnlag.service.PersistenceService
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
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
import org.springframework.web.client.HttpClientErrorException
import java.time.LocalDate

@DisplayName("GrunnlagspakkeControllerTest")
@ActiveProfiles(TEST_PROFILE)
@SpringBootTest(classes = [BidragGrunnlagTest::class], webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableMockOAuth2Server
@AutoConfigureWireMock(port = 0)
class GrunnlagspakkeControllerTest(
  @Autowired val grunnlagspakkeRepository: GrunnlagspakkeRepository,
  @Autowired val persistenceService: PersistenceService,
  @Autowired val exceptionLogger: ExceptionLogger
) {

  private val restTemplate: HttpHeaderRestTemplate = Mockito.mock(HttpHeaderRestTemplate::class.java)
  private val bidragGcpProxyConsumer: BidragGcpProxyConsumer = BidragGcpProxyConsumer(restTemplate)
  private val familieBaSakConsumer: FamilieBaSakConsumer = FamilieBaSakConsumer(restTemplate)
  private val grunnlagspakkeService: GrunnlagspakkeService = GrunnlagspakkeService(persistenceService, familieBaSakConsumer, bidragGcpProxyConsumer)
  private val grunnlagspakkeController: GrunnlagspakkeController = GrunnlagspakkeController(grunnlagspakkeService)
  private val mockMvc: MockMvc = MockMvcBuilders.standaloneSetup(grunnlagspakkeController)
    .setControllerAdvice(RestExceptionHandler(exceptionLogger), CustomExceptionHandler(exceptionLogger), HibernateExceptionHandler(exceptionLogger))
    .build()

  @BeforeEach
  fun `init`() {
    grunnlagspakkeRepository.deleteAll()
  }

  @Test
  fun `skal opprette ny grunnlagspakke`() {
    opprettGrunnlagspakke(OpprettGrunnlagspakkeRequest(Formaal.FORSKUDD, "X123456"))
  }

  @Test
  fun `skal oppdatere en grunnlagspakke`() {

    val nyGrunnlagspakkeOpprettetResponse = opprettGrunnlagspakke(OpprettGrunnlagspakkeRequest(Formaal.FORSKUDD, "X123456"))

    Mockito.`when`(restTemplate.exchange(eq("/inntekt/hent"), eq(HttpMethod.POST), any(), any<Class<HentInntektListeResponse>>())).thenReturn(
      ResponseEntity(HentInntektListeResponse(emptyList()), HttpStatus.OK)
    )

    Mockito.`when`(restTemplate.exchange(eq("/skattegrunnlag/hent"), eq(HttpMethod.POST), any(), any<Class<HentSkattegrunnlagResponse>>()))
      .thenReturn(
        ResponseEntity(HentSkattegrunnlagResponse(emptyList(), emptyList(), ""), HttpStatus.OK)
      )

    Mockito.`when`(restTemplate.exchange(eq("/api/bisys/hent-utvidet-barnetrygd"), eq(HttpMethod.POST), any(), any<Class<FamilieBaSakResponse>>()))
      .thenReturn(
        ResponseEntity(FamilieBaSakResponse(emptyList()), HttpStatus.OK)
      )

    val oppdaterGrunnlagspakkeResponse = oppdaterGrunnlagspakke(
      nyGrunnlagspakkeOpprettetResponse.grunnlagspakkeId,
      TestUtil.byggOppdaterGrunnlagspakkeRequest(),
      OppdaterGrunnlagspakkeResponse::class.java
    ) { isOk() }

    assertThat(oppdaterGrunnlagspakkeResponse.grunnlagtypeResponsListe.size).isEqualTo(3)

    oppdaterGrunnlagspakkeResponse.grunnlagtypeResponsListe.forEach { grunnlagstypeResponse ->
      grunnlagstypeResponse.hentGrunnlagkallResponseListe.forEach { hentGrunnlagkallResponse ->
        assertEquals(hentGrunnlagkallResponse.statuskode, HttpStatus.OK.value())
      }
    }
  }

  @Test
  fun `skal oppdatere grunnlagspakke og håndtere rest-kall feil`() {

    val nyGrunnlagspakkeOpprettetResponse = opprettGrunnlagspakke(OpprettGrunnlagspakkeRequest(Formaal.FORSKUDD, "X123456"))

    Mockito.`when`(restTemplate.exchange(eq("/inntekt/hent"), eq(HttpMethod.POST), any(), any<Class<HentInntektListeResponse>>())).thenThrow(
      HttpClientErrorException(HttpStatus.NOT_FOUND)
    )

    Mockito.`when`(restTemplate.exchange(eq("/skattegrunnlag/hent"), eq(HttpMethod.POST), any(), any<Class<HentSkattegrunnlagResponse>>())).thenThrow(
      HttpClientErrorException(HttpStatus.NOT_FOUND)
    )

    Mockito.`when`(restTemplate.exchange(eq("/api/bisys/hent-utvidet-barnetrygd"), eq(HttpMethod.POST), any(), any<Class<FamilieBaSakResponse>>()))
      .thenThrow(
        HttpClientErrorException(HttpStatus.NOT_FOUND)
      )

    val oppdaterGrunnlagspakkeResponse = oppdaterGrunnlagspakke(
      nyGrunnlagspakkeOpprettetResponse.grunnlagspakkeId,
      TestUtil.byggOppdaterGrunnlagspakkeRequest(),
      OppdaterGrunnlagspakkeResponse::class.java
    ) { isOk() }

    assertThat(oppdaterGrunnlagspakkeResponse).isNotNull
    assertThat(oppdaterGrunnlagspakkeResponse.grunnlagtypeResponsListe.size).isEqualTo(3)

    oppdaterGrunnlagspakkeResponse.grunnlagtypeResponsListe.forEach { grunnlagstypeResponse ->
      grunnlagstypeResponse.hentGrunnlagkallResponseListe.forEach { hentGrunnlagkallResponse ->
        assertEquals(hentGrunnlagkallResponse.statuskode, HttpStatus.NOT_FOUND.value())
      }
    }
  }

  @Test
  fun `skal finne data for en grunnlagspakke`() {

    val nyGrunnlagspakkeOpprettetResponse = opprettGrunnlagspakke(OpprettGrunnlagspakkeRequest(Formaal.FORSKUDD, "X123456"))

    val hentGrunnlagspakkeResponse = TestUtil.performRequest(
      mockMvc,
      HttpMethod.GET,
      "/grunnlagspakke/${nyGrunnlagspakkeOpprettetResponse.grunnlagspakkeId}",
      null,
      HentKomplettGrunnlagspakkeResponse::class.java
    ) { isOk() }

    assertNotNull(hentGrunnlagspakkeResponse)
    assertThat(hentGrunnlagspakkeResponse.grunnlagspakkeId).isEqualTo(nyGrunnlagspakkeOpprettetResponse.grunnlagspakkeId)
  }

  @Test
  fun `skal fange opp og håndtere Hibernate feil`() {
    val grunnlagspakkeService = Mockito.mock(GrunnlagspakkeService::class.java)
    val grunnlagspakkeController = GrunnlagspakkeController(grunnlagspakkeService)
    val mockMvc = MockMvcBuilders.standaloneSetup(grunnlagspakkeController).setControllerAdvice(HibernateExceptionHandler(exceptionLogger)).build()

    val nyGrunnlagspakkeOpprettetResponse = opprettGrunnlagspakke(OpprettGrunnlagspakkeRequest(Formaal.FORSKUDD, "X123456"))

    Mockito.`when`(
      grunnlagspakkeService.oppdaterGrunnlagspakke(
        nyGrunnlagspakkeOpprettetResponse.grunnlagspakkeId,
        TestUtil.byggOppdaterGrunnlagspakkeRequest()
      )
    )
      .thenThrow(HibernateException("Test-melding"))

    val oppdaterGrunnlagspakkeResponse =
      oppdaterGrunnlagspakke(
        nyGrunnlagspakkeOpprettetResponse.grunnlagspakkeId,
        TestUtil.byggOppdaterGrunnlagspakkeRequest(),
        String::class.java,
        mockMvc
      ) { isInternalServerError() }

    assertNotNull(oppdaterGrunnlagspakkeResponse)
  }

  @Test
  fun `skal fange opp og håndtere forespørsler på grunnlagspakker som ikke eksisterer`() {

    val oppdaterGrunnlagspakkeResponse = oppdaterGrunnlagspakke(1, TestUtil.byggOppdaterGrunnlagspakkeRequest(), String::class.java) { isNotFound() }

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
  fun `skal håndtere feil eller manglende felter i input ved opprett grunnlagspakke kall`() {

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
      OpprettGrunnlagspakkeResponse::class.java
    ) { isOk() }

    assertNotNull(okResult)
  }

  @Test
  fun `skal håndtere feil eller manglende felter i input ved oppdater grunnlagspakke kall`() {

    var errorResult = performExpectedFailingRequest("/requests/oppdaterGrunnlagspakke1.json", "/grunnlagspakke/null/oppdater")

    assertNotNull(errorResult)
    assertNotNull(errorResult["grunnlagspakkeId"])

    errorResult = performExpectedFailingRequest("/requests/oppdaterGrunnlagspakke2.json", "/grunnlagspakke/Test/oppdater")

    assertNotNull(errorResult)
    assertNotNull(errorResult["grunnlagspakkeId"])

    errorResult = performExpectedFailingRequest("/requests/oppdaterGrunnlagspakke3.json", "/grunnlagspakke/1/oppdater")

    assertNotNull(errorResult)
    assertNotNull(errorResult["grunnlagRequestListe"])

    errorResult = performExpectedFailingRequest("/requests/oppdaterGrunnlagspakke4.json", "/grunnlagspakke/1/oppdater")

    assertNotNull(errorResult)
    assertNotNull(errorResult["gyldigTil"])

    errorResult = performExpectedFailingRequest("/requests/oppdaterGrunnlagspakke5.json", "/grunnlagspakke/1/oppdater")

    assertNotNull(errorResult)
    assertNotNull(errorResult["grunnlagRequestListe[0].grunnlagstype"])

    errorResult = performExpectedFailingRequest("/requests/oppdaterGrunnlagspakke6.json", "/grunnlagspakke/1/oppdater")

    assertNotNull(errorResult)
    assertNotNull(errorResult["grunnlagRequestListe[0].personId"])

    errorResult = performExpectedFailingRequest("/requests/oppdaterGrunnlagspakke7.json", "/grunnlagspakke/1/oppdater")

    assertNotNull(errorResult)
    assertNotNull(errorResult["grunnlagRequestListe[0].personId"])

    errorResult = performExpectedFailingRequest("/requests/oppdaterGrunnlagspakke8.json", "/grunnlagspakke/1/oppdater")

    assertNotNull(errorResult)
    assertNotNull(errorResult["grunnlagRequestListe[0].periodeFra"])

    errorResult = performExpectedFailingRequest("/requests/oppdaterGrunnlagspakke9.json", "/grunnlagspakke/1/oppdater")

    assertNotNull(errorResult)
    assertNotNull(errorResult["grunnlagRequestListe[0].periodeTil"])

    val grunnlagspakkeService = Mockito.mock(GrunnlagspakkeService::class.java)
    val grunnlagspakkeController = GrunnlagspakkeController(grunnlagspakkeService)
    val mockMvc = MockMvcBuilders.standaloneSetup(grunnlagspakkeController).setControllerAdvice(RestExceptionHandler(exceptionLogger)).build()

    Mockito.`when`(
      grunnlagspakkeService.oppdaterGrunnlagspakke(1,
        OppdaterGrunnlagspakkeRequest(
          gyldigTil = LocalDate.parse("2022-01-01"),
          grunnlagRequestListe = listOf(
            GrunnlagRequest(
              grunnlagstype = Grunnlagstype.UTVIDETBARNETRYGDOGSMAABARNSTILLEGG,
              personId = "12345678901",
              periodeFra = LocalDate.parse("2021-11-01"),
              periodeTil = LocalDate.parse("2021-11-15")))
            )
          )
        )
      .thenReturn(OppdaterGrunnlagspakkeResponse())

    val fileContent = getFileContent("/requests/oppdaterGrunnlagspakke10.json")
    val okResult = TestUtil.performRequest(
      mockMvc,
      HttpMethod.POST,
      "/grunnlagspakke/1/oppdater",
      fileContent,
      OppdaterGrunnlagspakkeResponse::class.java
    ) { isOk() }

    assertNotNull(okResult)
  }

  @Test
  fun `skal håndtere feil eller manglende felter i input ved lukk grunnlagspakke kall`() {

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
  fun `skal håndtere feil eller manglende felter i input ved hent grunnlagspakke kall`() {
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

    Mockito.`when`(grunnlagspakkeService.hentKomplettGrunnlagspakke(1))
      .thenReturn(HentKomplettGrunnlagspakkeResponse())

    val okResult = TestUtil.performRequest(
      mockMvc,
      HttpMethod.GET,
      "/grunnlagspakke/1",
      null,
      HentKomplettGrunnlagspakkeResponse::class.java
    ) { isOk() }

    assertNotNull(okResult)
  }

  private fun opprettGrunnlagspakke(opprettGrunnlagspakkeRequest: OpprettGrunnlagspakkeRequest): OpprettGrunnlagspakkeResponse {
    val nyGrunnlagspakkeOpprettetResponse = TestUtil.performRequest(
      mockMvc,
      HttpMethod.POST,
      GrunnlagspakkeController.GRUNNLAGSPAKKE_NY,
      opprettGrunnlagspakkeRequest,
      OpprettGrunnlagspakkeResponse::class.java
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
    oppdaterGrunnlagspakkeRequest: OppdaterGrunnlagspakkeRequest,
    responseType: Class<Response>,
    customMockMvc: MockMvc? = null,
    expectedStatus: StatusResultMatchersDsl.() -> Unit
  ): Response {
    return TestUtil.performRequest(
      customMockMvc ?: mockMvc,
      HttpMethod.POST,
      "/grunnlagspakke/${grunnlagspakkeId}/oppdater",
      oppdaterGrunnlagspakkeRequest,
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
