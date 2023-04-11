package no.nav.bidrag.grunnlag.consumer.familieefsak

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.grunnlag.consumer.GrunnlagsConsumer
import no.nav.bidrag.grunnlag.consumer.familieefsak.api.BarnetilsynRequest
import no.nav.bidrag.grunnlag.consumer.familieefsak.api.BarnetilsynResponse
import no.nav.bidrag.grunnlag.consumer.familieefsak.api.EksternePerioderMedBeløpResponse
import no.nav.bidrag.grunnlag.consumer.familieefsak.api.EksternePerioderRequest
import no.nav.bidrag.grunnlag.consumer.familieefsak.api.Ressurs
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.exception.tryExchange
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod

private const val BARNETILSYN_CONTEXT = "/api/ekstern/bisys/perioder-barnetilsyn"
private const val OVERGANGSSTØNAD_CONTEXT = "/api/ekstern/perioder/overgangsstonad/med-belop"

open class FamilieEfSakConsumer(
    private val restTemplate: HttpHeaderRestTemplate
) : GrunnlagsConsumer() {

    companion object {
        @JvmStatic
        private val logger: Logger = LoggerFactory.getLogger(FamilieEfSakConsumer::class.java)
    }

    open fun hentBarnetilsyn(request: BarnetilsynRequest): RestResponse<BarnetilsynResponse> {
        logger.info("Henter barnetilsyn")

        val restResponse = restTemplate.tryExchange(
            BARNETILSYN_CONTEXT,
            HttpMethod.POST,
            initHttpEntity(request),
            BarnetilsynResponse::class.java,
            BarnetilsynResponse(emptyList())
        )

        logResponse(logger, restResponse)

        return restResponse
    }

    open fun hentOvergangsstønad(request: EksternePerioderRequest): RestResponse<Ressurs> {
        logger.info("Henter overgangsstønad")

        val restResponse = restTemplate.tryExchange(
            OVERGANGSSTØNAD_CONTEXT,
            HttpMethod.POST,
            initHttpEntity(request),
            Ressurs::class.java,
            Ressurs(EksternePerioderMedBeløpResponse(emptyList()))
        )

        logResponse(logger, restResponse)

        return restResponse
    }
}
