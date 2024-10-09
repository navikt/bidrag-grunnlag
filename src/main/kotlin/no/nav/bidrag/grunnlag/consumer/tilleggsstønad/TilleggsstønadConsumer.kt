package no.nav.bidrag.grunnlag.consumer.familiebasak

import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.grunnlag.consumer.GrunnlagsConsumer
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.TilleggsstønadRequest
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.TilleggsstønadResponse
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.exception.tryExchange
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod

private const val TILLEGGSSTØNAD_CONTEXT = "/api/ekstern/vedtak/tilsyn-barn"

open class TilleggsstønadConsumer(private val restTemplate: HttpHeaderRestTemplate) : GrunnlagsConsumer() {

    companion object {
        @JvmStatic
        val logger: Logger = LoggerFactory.getLogger(TilleggsstønadConsumer::class.java)
    }

    open fun hentTilleggsstønad(request: TilleggsstønadRequest): RestResponse<TilleggsstønadResponse> {
        logger.debug("Henter tilleggsstønad for barnetilsyn fra tilleggsstonader-sak")
        secureLogger.debug { "Henter tilleggsstønad for barnetilsyn fra tilleggsstonader-sak for ident: ${request.ident}" }

        val restResponse = restTemplate.tryExchange(
            TILLEGGSSTØNAD_CONTEXT,
            HttpMethod.POST,
            initHttpEntity(request),
            TilleggsstønadResponse::class.java,
            TilleggsstønadResponse(false),
        )

        logResponse("Tilleggsstønad til barnetilsyn", request.ident, null, null, restResponse)

        return restResponse
    }
}
