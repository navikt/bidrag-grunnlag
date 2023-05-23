package no.nav.bidrag.grunnlag.consumer

import no.nav.bidrag.grunnlag.exception.RestResponse
import org.slf4j.Logger
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.util.*

open class GrunnlagsConsumer {

    fun <T> logResponse(logger: Logger, restResponse: RestResponse<T>) {
        when (restResponse) {
            is RestResponse.Success -> logger.info("Response: ${HttpStatus.OK}")
            is RestResponse.Failure -> logger.warn("Response: ${restResponse.statusCode}/${restResponse.message}")
        }
    }

    fun <T> initHttpEntity(body: T): HttpEntity<T> {
        val httpHeaders = HttpHeaders()
        httpHeaders.contentType = MediaType.APPLICATION_JSON
        return HttpEntity(body, httpHeaders)
    }

    fun <T> initHttpEntityInntektskomponenten(body: T): HttpEntity<T> {
        val httpHeaders = HttpHeaders()
        httpHeaders.contentType = MediaType.APPLICATION_JSON
        httpHeaders.add("Nav-Call-Id", UUID.randomUUID().toString())
        httpHeaders.add("Nav-Consumer-Id", "BIDRAG-GRUNNLAG")
        return HttpEntity(body, httpHeaders)
    }
}
