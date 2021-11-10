package no.nav.bidrag.grunnlag.controller

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.gcp.proxy.consumer.inntektskomponenten.response.HentAinntektListeResponse
import no.nav.bidrag.grunnlag.BidragGrunnlagLocal
import no.nav.bidrag.grunnlag.BidragGrunnlagLocal.Companion.TEST_PROFILE
import no.nav.bidrag.grunnlag.TestUtil
import no.nav.bidrag.grunnlag.api.grunnlagspakke.HentKomplettGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OppdaterGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OpprettGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OpprettGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.BidragGcpProxyConsumer
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.skatt.HentSkattegrunnlagResponse
import no.nav.bidrag.grunnlag.consumer.familiebasak.FamilieBaSakConsumer
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.FamilieBaSakResponse
import no.nav.bidrag.grunnlag.dto.GrunnlagspakkeDto
import no.nav.bidrag.grunnlag.persistence.repository.GrunnlagspakkeRepository
import no.nav.bidrag.grunnlag.service.GrunnlagspakkeService
import no.nav.bidrag.grunnlag.service.PersistenceService
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.util.UriComponentsBuilder
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException

@DisplayName("GrunnlagspakkeControllerTest")
@ActiveProfiles(TEST_PROFILE)
@SpringBootTest(classes = [BidragGrunnlagLocal::class], webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableMockOAuth2Server
@AutoConfigureWireMock(port = 0)
class GrunnlagspakkeControllerTest {

  @Autowired
  private lateinit var securedTestRestTemplate: HttpHeaderTestRestTemplate

  @Autowired
  private lateinit var grunnlagspakkeRepository: GrunnlagspakkeRepository

  @Autowired
  private lateinit var persistenceService: PersistenceService

  @LocalServerPort
  private val port = 0

  @Value("\${server.servlet.context-path}")
  private val contextPath: String? = null

  @BeforeEach
  fun `init`() {
    // Sletter alle forekomster
    grunnlagspakkeRepository.deleteAll()

  }

  @Test
  fun `skal mappe til context path med random port`() {
    assertThat(makeFullContextPath()).isEqualTo("http://localhost:$port/bidrag-grunnlag")
  }

  @Test
  fun `skal opprette ny grunnlagspakke`() {

    // Oppretter ny forekomst av grunnlagspakke
    val response = securedTestRestTemplate.exchange(
      fullUrlForNyGrunnlagspakke(),
      HttpMethod.POST,
      byggNyGrunnlagspakkeRequest(),
      OpprettGrunnlagspakkeResponse::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull() },
    )
    grunnlagspakkeRepository.deleteAll()
  }


  @Test
  fun `skal oppdatere en grunnlagspakke`() {

    val restTemplate = Mockito.mock(HttpHeaderRestTemplate::class.java)
    val bidragGcpProxyConsumer = BidragGcpProxyConsumer(restTemplate)
    val familieBaSakConsumer = FamilieBaSakConsumer(restTemplate)
    val grunnlagspakkeService = GrunnlagspakkeService(persistenceService, familieBaSakConsumer, bidragGcpProxyConsumer)
    val grunnlagspakkeController = GrunnlagspakkeController(grunnlagspakkeService)


    val nyGrunnlagspakkeOpprettetResponse = grunnlagspakkeController.opprettNyGrunnlagspakke(
      OpprettGrunnlagspakkeRequest(
        opprettetAv = "X123456"
      )
    )

    assertAll(
      Executable { assertThat(nyGrunnlagspakkeOpprettetResponse).isNotNull },
      Executable { assertThat(nyGrunnlagspakkeOpprettetResponse?.body).isNotNull }
    )

    Mockito.`when`(restTemplate.exchange(eq("/inntekt/hent"), eq(HttpMethod.POST), any(), any<Class<HentAinntektListeResponse>>())).thenReturn(
      ResponseEntity(HentAinntektListeResponse(emptyList()), HttpStatus.OK)
    )

    Mockito.`when`(restTemplate.exchange(eq("/skattegrunnlag/hent"), eq(HttpMethod.POST), any(), any<Class<HentSkattegrunnlagResponse>>()))
      .thenReturn(
        ResponseEntity(HentSkattegrunnlagResponse(emptyList(), emptyList(), ""), HttpStatus.OK)
      )

    Mockito.`when`(restTemplate.exchange(eq("/api/bisys/hent-utvidet-barnetrygd"), eq(HttpMethod.POST), any(), any<Class<FamilieBaSakResponse>>()))
      .thenReturn(
        ResponseEntity(FamilieBaSakResponse(emptyList()), HttpStatus.OK)
      )

    val response =
      grunnlagspakkeController.oppdaterGrunnlagspakke(TestUtil.byggOppdaterGrunnlagspakkeRequest(nyGrunnlagspakkeOpprettetResponse!!.body!!.grunnlagspakkeId))

    assertAll(
      Executable { assertThat(response).isNotNull },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull },
      Executable { assertThat(response?.body?.grunnlagtypeResponsListe?.size).isEqualTo(3) }
    )

    response?.body?.grunnlagtypeResponsListe?.forEach() { grunnlagstypeResponse ->
      grunnlagstypeResponse.hentGrunnlagkallResponseListe.forEach() { hentGrunnlagkallResponse ->
        assertEquals(hentGrunnlagkallResponse.statuskode, HttpStatus.OK.value())
      }
    }

    grunnlagspakkeRepository.deleteAll()
  }

  @Test
  fun `skal oppdatere grunnlagspakke og håndtere rest-kall feil`() {
    val restTemplate = Mockito.mock(HttpHeaderRestTemplate::class.java)
    val bidragGcpProxyConsumer = BidragGcpProxyConsumer(restTemplate)
    val familieBaSakConsumer = FamilieBaSakConsumer(restTemplate)
    val grunnlagspakkeService = GrunnlagspakkeService(persistenceService, familieBaSakConsumer, bidragGcpProxyConsumer)
    val grunnlagspakkeController = GrunnlagspakkeController(grunnlagspakkeService)


    val nyGrunnlagspakkeOpprettetResponse = grunnlagspakkeController.opprettNyGrunnlagspakke(
      OpprettGrunnlagspakkeRequest(
        opprettetAv = "X123456"
      )
    )

    assertAll(
      Executable { assertThat(nyGrunnlagspakkeOpprettetResponse).isNotNull },
      Executable { assertThat(nyGrunnlagspakkeOpprettetResponse?.body).isNotNull }
    )

    Mockito.`when`(restTemplate.exchange(eq("/inntekt/hent"), eq(HttpMethod.POST), any(), any<Class<HentAinntektListeResponse>>())).thenThrow(
      HttpClientErrorException(HttpStatus.NOT_FOUND)
    )

    Mockito.`when`(restTemplate.exchange(eq("/skattegrunnlag/hent"), eq(HttpMethod.POST), any(), any<Class<HentSkattegrunnlagResponse>>())).thenThrow(
      HttpClientErrorException(HttpStatus.NOT_FOUND)
    )

    Mockito.`when`(restTemplate.exchange(eq("/api/bisys/hent-utvidet-barnetrygd"), eq(HttpMethod.POST), any(), any<Class<FamilieBaSakResponse>>()))
      .thenThrow(
        HttpClientErrorException(HttpStatus.NOT_FOUND)
      )

    val response =
      grunnlagspakkeController.oppdaterGrunnlagspakke(TestUtil.byggOppdaterGrunnlagspakkeRequest(nyGrunnlagspakkeOpprettetResponse!!.body!!.grunnlagspakkeId))

    assertAll(
      Executable { assertThat(response).isNotNull },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull },
      Executable { assertThat(response?.body?.grunnlagtypeResponsListe?.size).isEqualTo(3) }
    )

    response?.body?.grunnlagtypeResponsListe?.forEach() { grunnlagstypeResponse ->
      grunnlagstypeResponse.hentGrunnlagkallResponseListe.forEach() { hentGrunnlagkallResponse ->
        assertEquals(hentGrunnlagkallResponse.statuskode, HttpStatus.NOT_FOUND.value())
      }
    }

  }


  @Test
  fun `skal finne data for en grunnlagspakke`() {
    val nyGrunnlagspakkeOpprettet = persistenceService.opprettNyGrunnlagspakke(
      GrunnlagspakkeDto(
        opprettetAv = "X123456"
      )
    )

    // Henter forekomst
    val response = securedTestRestTemplate.exchange(
      "${fullUrlForHentGrunnlagspakke()}/${nyGrunnlagspakkeOpprettet.grunnlagspakkeId}",
      HttpMethod.GET,
      null,
      HentKomplettGrunnlagspakkeResponse::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull },
      Executable { assertThat(response?.body?.grunnlagspakkeId).isEqualTo(nyGrunnlagspakkeOpprettet.grunnlagspakkeId) },
    )
    grunnlagspakkeRepository.deleteAll()


  }

  private fun fullUrlForNyGrunnlagspakke(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + GrunnlagspakkeController.GRUNNLAGSPAKKE_NY).toUriString()
  }

  private fun fullUrlForOppdaterGrunnlagspakke(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + GrunnlagspakkeController.GRUNNLAGSPAKKE_OPPDATER).toUriString()
  }

  private fun fullUrlForHentGrunnlagspakke(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + GrunnlagspakkeController.GRUNNLAGSPAKKE_HENT).toUriString()
  }

  private fun byggNyGrunnlagspakkeRequest(): HttpEntity<OpprettGrunnlagspakkeRequest> {
    return initHttpEntity(TestUtil.byggNyGrunnlagspakkeRequest())
  }

  private fun byggOppdaterGrunnlagspakkeRequest(grunnlagspakkeId: Int): HttpEntity<OppdaterGrunnlagspakkeRequest> {
    return initHttpEntity(TestUtil.byggOppdaterGrunnlagspakkeRequest(grunnlagspakkeId))
  }

  private fun makeFullContextPath(): String {
    return "http://localhost:$port$contextPath"
  }

  private fun <T> initHttpEntity(body: T): HttpEntity<T> {
    val httpHeaders = HttpHeaders()
    httpHeaders.contentType = MediaType.APPLICATION_JSON
    return HttpEntity(body, httpHeaders)
  }
}
