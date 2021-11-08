package no.nav.bidrag.grunnlag.consumer.bidraggcpproxy

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.gcp.proxy.consumer.inntektskomponenten.response.HentInntektListeResponse
import no.nav.bidrag.grunnlag.exception.RestResponse
import org.hibernate.JDBCException
import org.hibernate.exception.ConstraintViolationException
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertTrue
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.sql.SQLException

class BidragGcpProxyConsumerTest() {

  @Mock
  private var bidragGcpProxyConsumer: BidragGcpProxyConsumer

  private var restTemplate: HttpHeaderRestTemplate = Mockito.mock(HttpHeaderRestTemplate::class.java)

  init {
    bidragGcpProxyConsumer = BidragGcpProxyConsumer(restTemplate)
  }

//  @Test
//  public TestHentInntekt() {
//    Mockito.`when`(restTemplate.exchange(String(), HttpMethod.POST, HttpEntity.EMPTY, HentInntektListeResponse::class.java)).thenThrow(HttpClientErrorException(HttpStatus.NOT_FOUND)).
//  }

  @Test
  fun TestDbActionPerformer() {
    var result: DbResult<List<String>> = performDbAction {
      throw ConstraintViolationException("Feil", SQLException(), "")
    }
    assertTrue(result is DbResult.Failure)

    result = performDbAction {
      emptyList()
    }
    assertTrue(result is DbResult.Success)
  }

  sealed class DbResult<T> {
    data class Success<T>(val result: T) : DbResult<T>()
    data class Failure<T>(val exception: JDBCException) : DbResult<T>()
  }

  fun <T> performDbAction(dbAction: () -> T): DbResult<T> {
    return try {
      val result = dbAction()
      DbResult.Success(result)
    } catch (exception: JDBCException) {
      DbResult.Failure(exception)
    }
  }
}