package no.nav.bidrag.grunnlag.consumer.bidraggcpproxy

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.grunnlag.consumer.GrunnlagsConsumer
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.barnetillegg.HentBarnetilleggPensjonRequest
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.barnetillegg.HentBarnetilleggPensjonResponse
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.exception.tryExchange
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod

private const val BIDRAGGCPPROXY_BARNETILLEGG_PENSJON_CONTEXT = "/barnetillegg/pensjon/hent"

open class BidragGcpProxyConsumer(private val restTemplate: HttpHeaderRestTemplate) :
    GrunnlagsConsumer() {

    companion object {
        @JvmStatic
        val logger: Logger = LoggerFactory.getLogger(BidragGcpProxyConsumer::class.java)
    }

    open fun hentBarnetilleggPensjon(request: HentBarnetilleggPensjonRequest): RestResponse<HentBarnetilleggPensjonResponse> {
        logger.info("Henter barnetillegg fra Pensjon via bidrag-gcp-proxy")

        val restResponse = restTemplate.tryExchange(
            BIDRAGGCPPROXY_BARNETILLEGG_PENSJON_CONTEXT,
            HttpMethod.POST,
            initHttpEntity(request),
            HentBarnetilleggPensjonResponse::class.java,
            HentBarnetilleggPensjonResponse(emptyList())
        )
        logResponse(logger, restResponse)

        return restResponse
    }
}
