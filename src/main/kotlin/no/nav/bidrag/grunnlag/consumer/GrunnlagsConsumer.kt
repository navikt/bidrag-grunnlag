package no.nav.bidrag.grunnlag.consumer

import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.grunnlag.consumer.bidragperson.BidragPersonConsumer
import no.nav.bidrag.grunnlag.exception.RestResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.time.LocalDate
import java.util.*

open class GrunnlagsConsumer {

    fun <T> logResponse(logger: Logger, restResponse: RestResponse<T>) {
        when (restResponse) {
            is RestResponse.Success -> logger.debug("Response: {}", HttpStatus.OK)
            is RestResponse.Failure -> logger.warn("Response: ${restResponse.statusCode}/${restResponse.message}")
        }
    }

    fun <T> logResponse(type: String, ident: String, fom: LocalDate?, tom: LocalDate?, restResponse: RestResponse<T>) {
        when (restResponse) {
            is RestResponse.Success -> {
                logger.info("Henting av grunnlag $type utført ok")
                secureLogger.info { "Hent av grunnlag $type for $ident for perioden $fom - $tom ga følgende respons: ${restResponse.body}" }
            }

            is RestResponse.Failure -> {
                // Logger som warning i stedet for error hvis status er not found
                if (restResponse.statusCode == HttpStatus.NOT_FOUND) {
                    logger.warn(
                        "Feil ved hent av grunnlag $type ${restResponse.statusCode}/${restResponse.message}",
                    )
                    secureLogger.warn {
                        "Feil ved hent av grunnlag $type for $ident for perioden $fom - $tom. " +
                            "${restResponse.statusCode}/${restResponse.message}"
                    }
                } else {
                    logger.error(
                        "Feil ved hent av grunnlag $type ${restResponse.statusCode}/${restResponse.message}",
                    )
                    secureLogger.error {
                        "Feil ved hent av grunnlag $type for $ident for perioden $fom - $tom. " +
                            "${restResponse.statusCode}/${restResponse.message}"
                    }
                }
            }
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

        @JvmStatic
        val logger: Logger = LoggerFactory.getLogger(BidragPersonConsumer::class.java)
    }
}
