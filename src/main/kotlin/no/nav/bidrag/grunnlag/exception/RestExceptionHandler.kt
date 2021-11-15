package no.nav.bidrag.grunnlag.exception

import no.nav.bidrag.commons.ExceptionLogger
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.util.function.Consumer


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

  @ResponseBody
  @ExceptionHandler(
    MethodArgumentNotValidException::class
  )
  fun handleValidationExceptions(
    e: MethodArgumentNotValidException
  ): ResponseEntity<*> {
    exceptionLogger.logException(e, "RestExceptionHandler")
    val errors: MutableMap<String, String?> = HashMap()
    e.bindingResult.allErrors.forEach(Consumer { error: ObjectError ->
      val fieldName = (error as FieldError).field
      val errorMessage = error.getDefaultMessage()
      errors[fieldName] = errorMessage
    })
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors)
  }
}

sealed class RestResponse<T> {
  data class Success<T>(val body: T) : RestResponse<T>()
  data class Failure<T>(val message: String?, val statusCode: HttpStatus, val restClientException: RestClientException) : RestResponse<T>()
}

fun <T> RestTemplate.tryExchange(url: String, httpMethod: HttpMethod, httpEntity: HttpEntity<*>, responseType: Class<T>, fallbackBody: T): RestResponse<T> {
  return try {
    val response = exchange(url, httpMethod, httpEntity, responseType)
    RestResponse.Success(response.body ?: fallbackBody)
  } catch (e: HttpClientErrorException) {
    RestResponse.Failure("Message: ${e.message}", e.statusCode, e)
  } catch (e: HttpServerErrorException) {
    RestResponse.Failure("Message: ${e.message}", e.statusCode, e)
  }
}