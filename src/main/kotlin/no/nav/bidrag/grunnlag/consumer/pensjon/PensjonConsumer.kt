package no.nav.bidrag.grunnlag.consumer.pensjon

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.consumer.GrunnlagsConsumer
import no.nav.bidrag.grunnlag.consumer.pensjon.api.HentBarnetilleggPensjonRequest
import no.nav.bidrag.grunnlag.consumer.pensjon.api.HentBarnetilleggPensjonResponse
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.exception.tryExchange
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.web.util.UriComponentsBuilder

private const val BARNETILLEGG_URL = "/pen/api/barnetillegg/search"

open class PensjonConsumer(private val restTemplate: HttpHeaderRestTemplate) : GrunnlagsConsumer() {

    companion object {
        @JvmStatic
        val LOGGER: Logger = LoggerFactory.getLogger(PensjonConsumer::class.java)
    }

    open fun hentBarnetilleggPensjon(request: HentBarnetilleggPensjonRequest): RestResponse<HentBarnetilleggPensjonResponse> {
        val uri = UriComponentsBuilder.fromPath(BARNETILLEGG_URL)
            .build()
            .toUriString()

        SECURE_LOGGER.info("HentBarnetillegg uri: {}", uri)
        SECURE_LOGGER.info("HentBarnetilleggRequest: {}", request)

        val restResponse = restTemplate.tryExchange(
            uri,
            HttpMethod.POST,
            initHttpEntity(request),
            HentBarnetilleggPensjonResponse::class.java,
            HentBarnetilleggPensjonResponse(emptyList())
        )

        logResponse(SECURE_LOGGER, restResponse)

        return restResponse
    }
}
