package no.nav.bidrag.grunnlag.exception

import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import no.nav.bidrag.commons.ExceptionLogger
import no.nav.bidrag.commons.util.LoggingRetryListener
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.retry.RetryContext
import org.springframework.retry.RetryPolicy
import org.springframework.retry.backoff.FixedBackOffPolicy
import org.springframework.retry.context.RetryContextSupport
import org.springframework.retry.support.RetryTemplate
import org.springframework.stereotype.Component
import org.springframework.util.ClassUtils
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.time.format.DateTimeParseException

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
        return ResponseEntity.status(e.statusCode).body(e.responseBodyAsString.ifEmpty { e.message })
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
        MethodArgumentNotValidException::class,
    )
    fun handleArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<*> {
        exceptionLogger.logException(e, "RestExceptionHandler")
        val errors: MutableMap<String, String?> = HashMap()
        e.bindingResult.allErrors.forEach { error: ObjectError ->
            val fieldName = (error as FieldError).field
            val errorMessage = error.getDefaultMessage()
            errors[fieldName] = errorMessage
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors)
    }

    @ResponseBody
    @ExceptionHandler(
        MethodArgumentTypeMismatchException::class,
    )
    fun handleArgumentTypeMismatchException(e: MethodArgumentTypeMismatchException): ResponseEntity<*> {
        exceptionLogger.logException(e, "RestExceptionHandler")
        val errors: MutableMap<String, String?> = HashMap()
        errors[e.name] = when (e.cause) {
            is NumberFormatException -> "Ugyldig tallformat '${e.value}'"
            else -> e.message
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors)
    }

    @ResponseBody
    @ExceptionHandler(
        JacksonException::class,
    )
    fun handleJacksonExceptions(e: JacksonException): ResponseEntity<*> {
        val errors: MutableMap<String, String?> = HashMap()
        when (e) {
            is InvalidFormatException -> {
                errors[extractPath(e.path)] = when (val cause = e.cause) {
                    is DateTimeParseException -> "Ugyldig datoformat på oppgitt dato: '${cause.parsedString}'. Dato må oppgis på formatet yyyy-MM-dd."
                    else -> e.originalMessage
                }
            }
            else -> {
                errors["Feil ved deserialisering"] = e.originalMessage
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors)
    }

    private fun extractPath(paths: List<JsonMappingException.Reference>): String {
        val sb = StringBuilder()
        paths.forEach { jsonMappingException ->
            if (jsonMappingException.index != -1) {
                sb.append("[${jsonMappingException.index}]")
            } else {
                if (sb.isNotEmpty()) {
                    sb.append(".")
                }
                sb.append(jsonMappingException.fieldName)
            }
        }
        return sb.toString()
    }
}

sealed class RestResponse<T> {
    data class Success<T>(val body: T) : RestResponse<T>()
    data class Failure<T>(val message: String?, val statusCode: HttpStatusCode, val restClientException: Exception) : RestResponse<T>()
}
fun httpRetryTemplate(details: String? = null): RetryTemplate {
    val retryTemplate = RetryTemplate()
    val fixedBackOffPolicy = FixedBackOffPolicy()
    fixedBackOffPolicy.backOffPeriod = 500L
    retryTemplate.setBackOffPolicy(fixedBackOffPolicy)
    val retryPolicy = HttpRetryPolicy()
    retryTemplate.setRetryPolicy(retryPolicy)
    retryTemplate.setThrowLastExceptionOnExhausted(true)
    retryTemplate.registerListener(LoggingRetryListener(details))
    return retryTemplate
}

class HttpRetryPolicy(
    private val maxAttempts: Int = 3,
    private val ignoreHttpStatus: List<HttpStatus> = listOf(HttpStatus.NOT_FOUND, HttpStatus.BAD_REQUEST),
) : RetryPolicy {
    internal class HttpRetryContext(parent: RetryContext?) : RetryContextSupport(parent)

    override fun canRetry(context: RetryContext): Boolean {
        val throwable = context.lastThrowable
        val ignoreException =
            throwable != null &&
                (throwable is ResourceAccessException || throwable is HttpStatusCodeException && ignoreHttpStatus.contains(throwable.statusCode))
        val can = context.retryCount < maxAttempts &&
            (
                context.lastThrowable == null || !ignoreException
                )
        if (!can && throwable != null) {
            context.setAttribute(RetryContext.NO_RECOVERY, true)
        } else {
            context.removeAttribute(RetryContext.NO_RECOVERY)
        }
        return can
    }

    override fun close(status: RetryContext) {
    }

    override fun registerThrowable(context: RetryContext, throwable: Throwable?) {
        val httpRetryContext = (context as HttpRetryContext)
        httpRetryContext.registerThrowable(throwable)
    }

    override fun open(parent: RetryContext?): RetryContext = HttpRetryContext(parent)

    override fun toString(): String = ClassUtils.getShortName(javaClass) + "[maxAttempts=$maxAttempts, ignoreHttpStatus=$ignoreHttpStatus]"
}

fun <T> RestTemplate.tryExchange(
    url: String,
    httpMethod: HttpMethod,
    httpEntity: HttpEntity<*>,
    responseType: Class<T>,
    fallbackBody: T,
): RestResponse<T> = try {
    val response = httpRetryTemplate(
        url,
    ).execute<ResponseEntity<T>, HttpClientErrorException> {
        exchange(url, httpMethod, httpEntity, responseType)
    }
    if (response.statusCode == HttpStatus.OK) {
        RestResponse.Success(response.body ?: fallbackBody)
    } else {
        RestResponse.Failure(
            message = response.headers.getOrEmpty(HttpHeaders.WARNING).joinToString(","),
            statusCode = response.statusCode,
            restClientException = HttpClientErrorException(response.statusCode),
        )
    }
} catch (e: HttpClientErrorException) {
    RestResponse.Failure("Message: ${e.message}", e.statusCode, e)
} catch (e: HttpServerErrorException) {
    RestResponse.Failure("Message: ${e.message}", e.statusCode, e)
} catch (e: Exception) {
    RestResponse.Failure("Message: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR, e)
}

// Brukes hvis responseType er en liste
fun <T> RestTemplate.tryExchange(
    url: String,
    httpMethod: HttpMethod,
    httpEntity: HttpEntity<*>,
    responseType: ParameterizedTypeReference<T>,
    fallbackBody: T,
): RestResponse<T> = try {
    val response = httpRetryTemplate(
        url,
    ).execute<ResponseEntity<T>, HttpClientErrorException> {
        exchange(url, httpMethod, httpEntity, responseType)
    }
    if (response.statusCode == HttpStatus.OK) {
        RestResponse.Success(response.body ?: fallbackBody)
    } else {
        RestResponse.Failure(
            message = response.headers.getOrEmpty(HttpHeaders.WARNING).joinToString(","),
            statusCode = response.statusCode,
            restClientException = HttpClientErrorException(response.statusCode),
        )
    }
} catch (e: HttpClientErrorException) {
    RestResponse.Failure("Message: ${e.message}", e.statusCode, e)
} catch (e: HttpServerErrorException) {
    RestResponse.Failure("Message: ${e.message}", e.statusCode, e)
}
