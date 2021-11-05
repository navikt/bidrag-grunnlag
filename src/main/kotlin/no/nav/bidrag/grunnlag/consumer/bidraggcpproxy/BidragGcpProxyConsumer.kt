package no.nav.bidrag.grunnlag.consumer.bidraggcpproxy

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.gcp.proxy.consumer.inntektskomponenten.response.HentInntektListeResponse
import no.nav.bidrag.grunnlag.consumer.GrunnlagsConsumer
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.HentInntektRequest
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.skatt.HentSkattegrunnlagRequest
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.skatt.HentSkattegrunnlagResponse
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.exception.tryExchange
import org.springframework.http.HttpMethod

private const val BIDRAGGCPPROXY_INNTEKT_CONTEXT = "/inntekt/hent"
private const val BIDRAGGCPPROXY_SKATTEGRUNNLAG_CONTEXT = "/skattegrunnlag/hent"

open class BidragGcpProxyConsumer(private val restTemplate: HttpHeaderRestTemplate) : GrunnlagsConsumer() {

  fun hentInntekt(request: HentInntektRequest): RestResponse<HentInntektListeResponse> {
    LOGGER.info("Henter inntekt fra Inntektskomponenten via bidrag-gcp-proxy")

    val restResponse = restTemplate.tryExchange(
      BIDRAGGCPPROXY_INNTEKT_CONTEXT,
      HttpMethod.POST,
      initHttpEntity(request),
      HentInntektListeResponse::class.java,
      HentInntektListeResponse(emptyList())
    )

    logResponse(restResponse)

    return restResponse
  }

  fun hentSkattegrunnlag(request: HentSkattegrunnlagRequest): RestResponse<HentSkattegrunnlagResponse> {
    LOGGER.info("Henter skattegrunnlag fra Sigrun via bidrag-gcp-proxy")

    val restResponse = restTemplate.tryExchange(
        BIDRAGGCPPROXY_SKATTEGRUNNLAG_CONTEXT,
        HttpMethod.POST,
        initHttpEntity(request),
        HentSkattegrunnlagResponse::class.java,
        HentSkattegrunnlagResponse(emptyList(), emptyList(), null)
    )
    logResponse(restResponse)

    return restResponse;
  }
}
