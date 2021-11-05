package no.nav.bidrag.grunnlag.exception

import no.nav.bidrag.commons.ExceptionLogger
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.HttpStatusCodeException
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
  @ExceptionHandler(HttpClientErrorException::class, HttpServerErrorException::class)
  protected fun handleHttpClientErrorException(e: HttpStatusCodeException): ResponseEntity<*> {
    when (e) {
      is HttpClientErrorException -> exceptionLogger.logException(e, "HttpClientErrorException")
      is HttpServerErrorException -> exceptionLogger.logException(e, "HttpServerErrorException")
    }
    return ResponseEntity(e.message, e.statusCode)
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

sealed class RestResponse<T> {
  data class Success<T>(val body: T) : RestResponse<T>()
  data class Failure<T>(val message: String?, val statusCode: HttpStatus) : RestResponse<T>()
}

fun <T> RestTemplate.tryExchange(url: String, method: HttpMethod, entity: HttpEntity<*>, responseType: Class<T>, fallbackBody: T): RestResponse<T> {
  return try {
    val response = exchange(url, method, entity, responseType)
    RestResponse.Success(response.body ?: fallbackBody)
  } catch (e: HttpClientErrorException) {
    RestResponse.Failure("Message: ${e.message} ResponseHeader: ${e.responseHeaders?.get(HttpHeaders.WARNING)} ResponseBody: ${e.responseBodyAsString}", e.statusCode)
  } catch (e: HttpServerErrorException) {
    RestResponse.Failure("Message: ${e.message} ResponseHeader: ${e.responseHeaders?.get(HttpHeaders.WARNING)} ResponseBody: ${e.responseBodyAsString}", e.statusCode)
  }
}