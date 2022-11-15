package no.nav.bidrag.grunnlag.consumer.familieefsak

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.grunnlag.TestUtil
import no.nav.bidrag.grunnlag.consumer.familieefsak.api.BarnetilsynResponse
import no.nav.bidrag.grunnlag.consumer.familieefsak.api.Datakilde
import no.nav.bidrag.grunnlag.exception.RestResponse
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
internal class FamilieEfSakConsumerTest {

  companion object {
    private const val BARNETILSYN_CONTEXT = "/api/ekstern/bisys/perioder-barnetilsyn"
  }

  @InjectMocks
  private lateinit var familieEfSakConsumer: FamilieEfSakConsumer

  @Mock
  private lateinit var restTemplateMock: HttpHeaderRestTemplate

  @Test
  fun `Sjekk at ok respons fra Barnetilsyn-endepunkt mappes korrekt`() {
    val request = TestUtil.byggBarnetilsynRequest()

    Mockito.`when`(
      restTemplateMock.exchange(
        eq(BARNETILSYN_CONTEXT),
        eq(HttpMethod.POST),
        eq(initHttpEntity(request)),
        ArgumentMatchers.any<Class<BarnetilsynResponse>>()
      )
    )
      .thenReturn(ResponseEntity(TestUtil.byggBarnetilsynResponse(), HttpStatus.OK))

    when (val restResponseBarnetilsyn = familieEfSakConsumer.hentBarnetilsyn(request)) {
      is RestResponse.Success -> {
        val hentBarnetilsynResponse = restResponseBarnetilsyn.body
        assertAll(
          { Assertions.assertThat(hentBarnetilsynResponse).isNotNull },
          { Assertions.assertThat(hentBarnetilsynResponse.barnetilsynBisysPerioder.size).isEqualTo(1) },
          { Assertions.assertThat(hentBarnetilsynResponse.barnetilsynBisysPerioder[0].periode.fom).isEqualTo(LocalDate.parse("2021-01-01"))},
          { Assertions.assertThat(hentBarnetilsynResponse.barnetilsynBisysPerioder[0].periode.tom).isEqualTo(LocalDate.parse("2021-07-31"))},
          { Assertions.assertThat(hentBarnetilsynResponse.barnetilsynBisysPerioder[0].månedsbeløp).isEqualTo(3500) },
          { Assertions.assertThat(hentBarnetilsynResponse.barnetilsynBisysPerioder[0].barnIdenter[0]).isEqualTo("01012212345") },
          { Assertions.assertThat(hentBarnetilsynResponse.barnetilsynBisysPerioder[0].barnIdenter[1]).isEqualTo("01011034543") },
          { Assertions.assertThat(hentBarnetilsynResponse.barnetilsynBisysPerioder[0].datakilde).isEqualTo(Datakilde.EF) }
        )
      }
      else -> {
        Assertions.fail("Test returnerte med RestResponse.Failure, som ikke var forventet")
      }
    }
  }

  @Test
  @Suppress("NonAsciiCharacters")
  fun `Sjekk at exception fra Barnetilsyn-endepunkt håndteres korrekt`() {
    val request = TestUtil.byggBarnetilsynRequest()

    Mockito.`when`(
      restTemplateMock.exchange(
        eq(BARNETILSYN_CONTEXT),
        eq(HttpMethod.POST),
        eq(initHttpEntity(request)),
        ArgumentMatchers.any<Class<BarnetilsynResponse>>()
      )
    )
      .thenThrow(HttpClientErrorException(HttpStatus.BAD_REQUEST))

    when (val restResponseBarnetilsyn = familieEfSakConsumer.hentBarnetilsyn(request)) {
      is RestResponse.Failure -> {
        assertAll(
          { Assertions.assertThat(restResponseBarnetilsyn.statusCode).isEqualTo(HttpStatus.BAD_REQUEST) },
          { Assertions.assertThat(restResponseBarnetilsyn.restClientException)
            .isInstanceOf(HttpClientErrorException::class.java) }
        )
      }
      else -> {
        Assertions.fail("Test returnerte med RestResponse.Success, som ikke var forventet")
      }
    }
  }

  fun <T> initHttpEntity(body: T): HttpEntity<T> {
    val httpHeaders = HttpHeaders()
    httpHeaders.contentType = MediaType.APPLICATION_JSON
    return HttpEntity(body, httpHeaders)
  }
}