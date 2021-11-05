package no.nav.bidrag.grunnlag.consumer.bidraggcpproxy

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.gcp.proxy.consumer.inntektskomponenten.response.HentInntektListeResponse
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.HentInntektRequest
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.skatt.HentSkattegrunnlagRequest
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.skatt.HentSkattegrunnlagResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType

private const val BIDRAGGCPPROXY_INNTEKT_CONTEXT = "/inntekt/hent"
private const val BIDRAGGCPPROXY_SKATTEGRUNNLAG_CONTEXT = "/skattegrunnlag/hent"

open class BidragGcpProxyConsumer(private val restTemplate: HttpHeaderRestTemplate) {

  companion object {
    @JvmStatic
    private val LOGGER = LoggerFactory.getLogger(BidragGcpProxyConsumer::class.java)
  }

  fun hentAinntekt(request: HentInntektRequest): HentInntektListeResponse {
    LOGGER.info("Henter inntekt fra Inntektskomponenten via bidrag-gcp-proxy")

    val response = restTemplate.exchange(
      BIDRAGGCPPROXY_INNTEKT_CONTEXT,
      HttpMethod.POST,
      initHttpEntity(request),
      HentInntektListeResponse::class.java
    )

    LOGGER.info("Response: ${response.statusCode}/${response.body}")

    return response.body ?: HentInntektListeResponse(emptyList())
  }

  fun hentSkattegrunnlag(request: HentSkattegrunnlagRequest): HentSkattegrunnlagResponse {
    LOGGER.info("Henter skattegrunnlag fra Sigrun via bidrag-gcp-proxy")

    val response = restTemplate.exchange(
        BIDRAGGCPPROXY_SKATTEGRUNNLAG_CONTEXT,
        HttpMethod.POST,
        initHttpEntity(request),
        HentSkattegrunnlagResponse::class.java
    )

    LOGGER.info("Response: ${response.statusCode}/${response.body}")

    return response.body ?: HentSkattegrunnlagResponse(emptyList(), emptyList(), null);
  }

  private fun <T> initHttpEntity(body: T): HttpEntity<T> {
    val httpHeaders = HttpHeaders()
    httpHeaders.contentType = MediaType.APPLICATION_JSON
    return HttpEntity(body, httpHeaders)
  }
}
