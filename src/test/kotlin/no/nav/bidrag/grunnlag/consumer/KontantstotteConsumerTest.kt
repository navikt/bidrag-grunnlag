package no.nav.bidrag.grunnlag.consumer

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.grunnlag.TestUtil
import no.nav.bidrag.grunnlag.consumer.infotrygdkontantstottev2.KontantstotteConsumer
import no.nav.bidrag.grunnlag.consumer.infotrygdkontantstottev2.api.InnsynResponse
import no.nav.bidrag.grunnlag.exception.RestResponse
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
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
import java.time.YearMonth

@ExtendWith(MockitoExtension::class)
@DisplayName("KontantstotteConsumerTest")
internal class KontantstotteConsumerTest {

  @InjectMocks
  private val kontantstotteConsumer: KontantstotteConsumer? = null

  @Mock
  private val restTemplateMock: HttpHeaderRestTemplate? = null

  @Test
  fun `Sjekk at ok respons fra Kontantstotte-endepunkt mappes korrekt`() {
    val request = TestUtil.byggKontantstotteRequest()

    Mockito.`when`(
      restTemplateMock?.exchange(
        eq(KONTANTSTOTTE_CONTEXT),
        eq(HttpMethod.POST),
        eq(initHttpEntity(request)),
        any<Class<InnsynResponse>>()
      )
    )
      .thenReturn(ResponseEntity(TestUtil.byggKontantstotteResponse(), HttpStatus.OK))

    when (val restResponseKontantstotte = kontantstotteConsumer!!.hentKontantstotte(request)) {
      is RestResponse.Success -> {
        val hentKontantstotteResponse = restResponseKontantstotte.body
        assertAll(
          { assertThat(hentKontantstotteResponse).isNotNull },
          { assertThat(hentKontantstotteResponse.data.size).isEqualTo(1) },
          { assertThat(hentKontantstotteResponse.data[0].fnr).isEqualTo("12345678910")},
          { assertThat(hentKontantstotteResponse.data[0].fom).isEqualTo(YearMonth.parse("2022-01"))},
          { assertThat(hentKontantstotteResponse.data[0].tom).isEqualTo(YearMonth.parse("2022-07"))},
          { assertThat(hentKontantstotteResponse.data[0].belop).isEqualTo(15001) },
          { assertThat(hentKontantstotteResponse.data[0].barn[0].fnr).isEqualTo("11223344551") },
          { assertThat(hentKontantstotteResponse.data[0].barn[1].fnr).isEqualTo("15544332211") }
        )
      }
      else -> {
        fail("Test returnerte med RestResponse.Failure, som ikke var forventet")
      }
    }
  }

  @Test
  @Suppress("NonAsciiCharacters")
  fun `Sjekk at exception fra Kontantstotte-endepunkt håndteres korrekt`() {
    val request = TestUtil.byggKontantstotteRequest()

    Mockito.`when`(
      restTemplateMock?.exchange(
        eq(KONTANTSTOTTE_CONTEXT),
        eq(HttpMethod.POST),
        eq(initHttpEntity(request)),
        any<Class<InnsynResponse>>()
      )
    )
      .thenThrow(HttpClientErrorException(HttpStatus.BAD_REQUEST))

    when (val restResponseKontantstotte = kontantstotteConsumer!!.hentKontantstotte(request)) {
      is RestResponse.Failure -> {
        assertAll(
          { assertThat(restResponseKontantstotte.statusCode).isEqualTo(HttpStatus.BAD_REQUEST) },
          { assertThat(restResponseKontantstotte.restClientException).isInstanceOf(HttpClientErrorException::class.java) }
        )
      }
      else -> {
        fail("Test returnerte med RestResponse.Success, som ikke var forventet")
      }
    }
  }

  fun <T> initHttpEntity(body: T): HttpEntity<T> {
    val httpHeaders = HttpHeaders()
    httpHeaders.contentType = MediaType.APPLICATION_JSON
    return HttpEntity(body, httpHeaders)
  }

  companion object {
    private const val KONTANTSTOTTE_CONTEXT = "/hentPerioder"
  }
}