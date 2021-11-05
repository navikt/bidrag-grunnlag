package no.nav.bidrag.grunnlag.consumer

import no.nav.bidrag.grunnlag.consumer.familiebasak.FamilieBaSakConsumer
import no.nav.bidrag.grunnlag.exception.RestResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

open class GrunnlagsConsumer {

  companion object {
    @JvmStatic
    val LOGGER: Logger = LoggerFactory.getLogger(FamilieBaSakConsumer::class.java)
  }

  fun <T> logResponse(restResponse: RestResponse<T>) {
    when (restResponse) {
      is RestResponse.Success -> LOGGER.info("Response: ${HttpStatus.OK}/${restResponse.body}")
      is RestResponse.Failure -> LOGGER.info("Response: ${restResponse.statusCode}/${restResponse.message}")
    }
  }

  fun <T> initHttpEntity(body: T): HttpEntity<T> {
    val httpHeaders = HttpHeaders()
    httpHeaders.contentType = MediaType.APPLICATION_JSON
    return HttpEntity(body, httpHeaders)
  }
}