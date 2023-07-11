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
        httpHeaders.add(NAV_CALL_ID, UUID.randomUUID().toString())
        httpHeaders.add(NAV_CONSUMER_ID, NAV_CONSUMER_ID_VERDI)
        return HttpEntity(body, httpHeaders)
    }

    fun <T> initHttpEntitySkattegrunnlag(body: T, ident: String): HttpEntity<T> {
        val httpHeaders = HttpHeaders()
        httpHeaders.contentType = MediaType.APPLICATION_JSON
        httpHeaders.add(NAV_CALL_ID, UUID.randomUUID().toString())
        httpHeaders.add(NAV_CONSUMER_ID, NAV_CONSUMER_ID_VERDI)
        httpHeaders.add(NAV_PERSONIDENT, ident)
        return HttpEntity(body, httpHeaders)
    }

    fun <T> initHttpEntityAareg(body: T, ident: String): HttpEntity<T> {
        val httpHeaders = HttpHeaders()
        httpHeaders.contentType = MediaType.APPLICATION_JSON
        httpHeaders.add(NAV_CALL_ID, UUID.randomUUID().toString())
        httpHeaders.add(NAV_PERSONIDENT, ident)
        return HttpEntity(body, httpHeaders)
    }

    fun <T> initHttpEntityEreg(body: T): HttpEntity<T> {
        val httpHeaders = HttpHeaders()
        httpHeaders.contentType = MediaType.APPLICATION_JSON
        httpHeaders.add(NAV_CALL_ID, UUID.randomUUID().toString())
        return HttpEntity(body, httpHeaders)
    }

    companion object {
        const val NAV_CALL_ID = "Nav-Call-Id"
        const val NAV_CONSUMER_ID = "Nav-Consumer-Id"
        const val NAV_CONSUMER_ID_VERDI = "BIDRAG-GRUNNLAG"
        const val NAV_PERSONIDENT = "Nav-Personident"
    }
}
