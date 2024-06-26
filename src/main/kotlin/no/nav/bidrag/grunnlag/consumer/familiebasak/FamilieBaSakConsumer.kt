package no.nav.bidrag.grunnlag.consumer.familiebasak

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.consumer.GrunnlagsConsumer
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.FamilieBaSakRequest
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.FamilieBaSakResponse
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.exception.tryExchange
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import java.net.SocketTimeoutException

private const val FAMILIEBASAK_CONTEXT = "/api/bisys/hent-utvidet-barnetrygd"

open class FamilieBaSakConsumer(private val restTemplate: HttpHeaderRestTemplate) :
    GrunnlagsConsumer() {

    companion object {
        @JvmStatic
        val logger: Logger = LoggerFactory.getLogger(FamilieBaSakConsumer::class.java)
    }

    @Retryable(value = [Exception::class], exclude = [SocketTimeoutException::class], backoff = Backoff(delay = 500))
    open fun hentFamilieBaSak(request: FamilieBaSakRequest): RestResponse<FamilieBaSakResponse> {
        logger.debug("Henter utvidet barnetrygd og småbarnstillegg fra familie-ba-sak")

        val restResponse = restTemplate.tryExchange(
            FAMILIEBASAK_CONTEXT,
            HttpMethod.POST,
            initHttpEntity(request),
            FamilieBaSakResponse::class.java,
            FamilieBaSakResponse(emptyList()),
        )

        logResponse(SECURE_LOGGER, restResponse)

        return restResponse
    }
}
