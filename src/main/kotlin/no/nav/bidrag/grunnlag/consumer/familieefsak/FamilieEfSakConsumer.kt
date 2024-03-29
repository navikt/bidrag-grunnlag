package no.nav.bidrag.grunnlag.consumer.familieefsak

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.consumer.GrunnlagsConsumer
import no.nav.bidrag.grunnlag.consumer.familieefsak.api.BarnetilsynRequest
import no.nav.bidrag.grunnlag.consumer.familieefsak.api.BarnetilsynResponse
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.exception.tryExchange
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable

private const val BARNETILSYN_CONTEXT = "/api/ekstern/bisys/perioder-barnetilsyn"

open class FamilieEfSakConsumer(
    private val restTemplate: HttpHeaderRestTemplate,
) : GrunnlagsConsumer() {

    companion object {
        @JvmStatic
        private val logger: Logger = LoggerFactory.getLogger(FamilieEfSakConsumer::class.java)
    }

    @Retryable(value = [Exception::class], backoff = Backoff(delay = 500))
    open fun hentBarnetilsyn(request: BarnetilsynRequest): RestResponse<BarnetilsynResponse> {
        logger.info("Henter barnetilsyn")

        val restResponse = restTemplate.tryExchange(
            BARNETILSYN_CONTEXT,
            HttpMethod.POST,
            initHttpEntity(request),
            BarnetilsynResponse::class.java,
            BarnetilsynResponse(emptyList()),
        )

        logResponse(SECURE_LOGGER, restResponse)

        return restResponse
    }
}
