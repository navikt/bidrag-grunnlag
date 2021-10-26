package no.nav.bidrag.grunnlag.consumer.bidraggcpproxy

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.gcp.proxy.consumer.inntektskomponenten.response.HentInntektListeResponse
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.HentInntektRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType

private const val BIDRAGGCPPROXY_CONTEXT = "/inntekt/hent"

open class BidragGcpProxyConsumer(private val restTemplate: HttpHeaderRestTemplate) {

  companion object {
    @JvmStatic
    private val LOGGER = LoggerFactory.getLogger(BidragGcpProxyConsumer::class.java)
  }

  fun hentInntekt(request: HentInntektRequest): HentInntektListeResponse {
    LOGGER.info("Henter inntekt fra Inntektskomponenten via bidrag-gcp-proxy")

    val response = restTemplate.exchange(
      BIDRAGGCPPROXY_CONTEXT,
      HttpMethod.POST,
      initHttpEntity(request),
      HentInntektListeResponse::class.java
    )

    LOGGER.info("Response: ${response.statusCode}/${response.body}")

    return response.body ?: HentInntektListeResponse(emptyList())
  }

  private fun <T> initHttpEntity(body: T): HttpEntity<T> {
    val httpHeaders = HttpHeaders()
    httpHeaders.contentType = MediaType.APPLICATION_JSON
    return HttpEntity(body, httpHeaders)
  }
}
