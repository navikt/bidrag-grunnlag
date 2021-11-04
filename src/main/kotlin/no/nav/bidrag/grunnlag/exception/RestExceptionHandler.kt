package no.nav.bidrag.grunnlag.exception

import no.nav.bidrag.commons.ExceptionLogger
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate


@RestControllerAdvice
@Component
class RestExceptionHandler(private val exceptionLogger: ExceptionLogger) {

  @ResponseBody
  @ExceptionHandler(RestClientException::class)
  protected fun handleRestClientException(e: RestClientException): ResponseEntity<*> {
    exceptionLogger.logException(e, "RestExceptionHandler")
    val feilmelding = "Restkall feilet!"
    val headers = HttpHeaders()
    headers.add(HttpHeaders.WARNING, feilmelding)
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ResponseEntity(e.message, headers, HttpStatus.SERVICE_UNAVAILABLE))
  }

  @ResponseBody
  @ExceptionHandler(IllegalArgumentException::class)
  protected fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<*> {
    exceptionLogger.logException(e, "RestExceptionHandler")
    val feilmelding = if (e.message == null || e.message!!.isBlank()) "Restkall feilet!" else e.message!!
    val headers = HttpHeaders()
    headers.add(HttpHeaders.WARNING, feilmelding)
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseEntity(feilmelding, headers, HttpStatus.BAD_REQUEST))
  }
}

fun <T> RestTemplate.tryExchange(url: String, method: HttpMethod, entity: HttpEntity<*>, responseType: Class<T>): ResponseEntity<T> {
  return try {
    exchange(url, method, entity, responseType)
  } catch (e: HttpClientErrorException) {
    // Might need to add more status codes that should be allowed to pass through in the future
    if (e.statusCode == HttpStatus.NOT_FOUND) {
      ResponseEntity(null, e.statusCode)
    }
    throw e
  }
}