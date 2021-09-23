package no.nav.bidrag.grunnlag.controller

import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.grunnlag.BidragGrunnlagLocal
import no.nav.bidrag.grunnlag.BidragGrunnlagLocal.Companion.TEST_PROFILE
import no.nav.bidrag.grunnlag.TestUtil
import no.nav.bidrag.grunnlag.api.FinnGrunnlagResponse
import no.nav.bidrag.grunnlag.api.NyGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.api.NyGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.dto.GrunnlagspakkeDto
import no.nav.bidrag.grunnlag.persistence.repository.GrunnlagspakkeRepository
import no.nav.bidrag.grunnlag.service.PersistenceService

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
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

@DisplayName("GrunnlagspakkeControllerTest")
@ActiveProfiles(TEST_PROFILE)
@SpringBootTest(classes = [BidragGrunnlagLocal::class], webEnvironment = WebEnvironment.RANDOM_PORT)
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

    // Oppretter ny forekomst av stønad
    val response = securedTestRestTemplate.exchange(
      fullUrlForNyGrunnlagspakke(),
      HttpMethod.POST,
      byggNyGrunnlagspakkeRequest(),
      NyGrunnlagspakkeResponse::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull() },
    )
    grunnlagspakkeRepository.deleteAll()
  }

  @Test
  fun `skal finne data for en grunnlagspakke`() {
    val nyGrunnlagspakkeOpprettet = persistenceService.opprettNyGrunnlagspakke(GrunnlagspakkeDto(
      opprettetAv = "X123456"
    ))

    // Henter forekomst
    val response = securedTestRestTemplate.exchange(
      "${fullUrlForHentGrunnlagspakke()}/${nyGrunnlagspakkeOpprettet.grunnlagspakkeId}",
      HttpMethod.GET,
      null,
      FinnGrunnlagResponse::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull },
      Executable { assertThat(response?.body?.opprettetAv).isEqualTo(nyGrunnlagspakkeOpprettet.opprettetAv) },
    )
    grunnlagspakkeRepository.deleteAll()


  }


  private fun fullUrlForNyGrunnlagspakke(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + GrunnlagspakkeController.GRUNNLAGSPAKKE_NY).toUriString()
  }

  private fun fullUrlForHentGrunnlagspakke(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + GrunnlagspakkeController.GRUNNLAGSPAKKE_HENT).toUriString()
  }

  private fun byggNyGrunnlagspakkeRequest(): HttpEntity<NyGrunnlagspakkeRequest> {
    return initHttpEntity(TestUtil.byggNyGrunnlagspakkeRequest())
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