package no.nav.bidrag.grunnlag.consumer.bidraggcpproxy

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.gcp.proxy.consumer.inntektskomponenten.response.HentAinntektListeResponse
import no.nav.bidrag.grunnlag.consumer.GrunnlagsConsumer
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.HentAinntektRequest
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.skatt.HentSkattegrunnlagRequest
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.skatt.HentSkattegrunnlagResponse
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.exception.tryExchange
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod

private const val BIDRAGGCPPROXY_INNTEKT_CONTEXT = "/inntekt/hent"
private const val BIDRAGGCPPROXY_SKATTEGRUNNLAG_CONTEXT = "/skattegrunnlag/hent"

open class BidragGcpProxyConsumer(private val restTemplate: HttpHeaderRestTemplate) :
  GrunnlagsConsumer() {

  companion object {
    @JvmStatic
    val logger: Logger = LoggerFactory.getLogger(BidragGcpProxyConsumer::class.java)
  }

  fun hentAinntekt(request: HentAinntektRequest): RestResponse<HentAinntektListeResponse> {
    logger.info("Henter inntekt fra Inntektskomponenten via bidrag-gcp-proxy")

    val restResponse = restTemplate.tryExchange(
      BIDRAGGCPPROXY_INNTEKT_CONTEXT,
      HttpMethod.POST,
      initHttpEntity(request),
      HentAinntektListeResponse::class.java,
      HentAinntektListeResponse(emptyList())
    )

    logResponse(logger, restResponse)

    return restResponse
  }

  fun hentSkattegrunnlag(request: HentSkattegrunnlagRequest): RestResponse<HentSkattegrunnlagResponse> {
    logger.info("Henter skattegrunnlag fra Sigrun via bidrag-gcp-proxy")

    val restResponse = restTemplate.tryExchange(
      BIDRAGGCPPROXY_SKATTEGRUNNLAG_CONTEXT,
      HttpMethod.POST,
      initHttpEntity(request),
      HentSkattegrunnlagResponse::class.java,
      HentSkattegrunnlagResponse(emptyList(), emptyList(), null)
    )
    logResponse(logger, restResponse)

    return restResponse;
  }
}
