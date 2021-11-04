package no.nav.bidrag.grunnlag.consumer.bidraggcpproxy

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.gcp.proxy.consumer.inntektskomponenten.response.HentInntektListeResponse
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.HentInntektRequest
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.ReceivedResponse
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.handleResponse
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.skatt.HentSkattegrunnlagRequest
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.skatt.HentSkattegrunnlagResponse
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.exception.tryExchange
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

private const val BIDRAGGCPPROXY_INNTEKT_CONTEXT = "/inntekt/hent"
private const val BIDRAGGCPPROXY_SKATTEGRUNNLAG_CONTEXT = "/skattegrunnlag/hent"

open class BidragGcpProxyConsumer(private val restTemplate: HttpHeaderRestTemplate) {

  companion object {
    @JvmStatic
    private val LOGGER = LoggerFactory.getLogger(BidragGcpProxyConsumer::class.java)
  }

  fun hentInntekt(request: HentInntektRequest): ReceivedResponse<HentInntektListeResponse> {
    LOGGER.info("Henter inntekt fra Inntektskomponenten via bidrag-gcp-proxy")

    val response = restTemplate.tryExchange(
      BIDRAGGCPPROXY_INNTEKT_CONTEXT,
      HttpMethod.POST,
      initHttpEntity(request),
      HentInntektListeResponse::class.java
    )

    LOGGER.info("Response: ${response.statusCode}/${response.body}")

    return handleResponse(response, HentInntektListeResponse(emptyList()))
  }

  fun hentSkattegrunnlag(request: HentSkattegrunnlagRequest): RestResponse<HentSkattegrunnlagResponse> {
    LOGGER.info("Henter skattegrunnlag fra Sigrun via bidrag-gcp-proxy")

    val response = restTemplate.tryExchange(
        BIDRAGGCPPROXY_SKATTEGRUNNLAG_CONTEXT,
        HttpMethod.POST,
        initHttpEntity(request),
        HentSkattegrunnlagResponse::class.java,
        HentSkattegrunnlagResponse(emptyList(), emptyList(), null)
    )
    
    when (response) {
      is RestResponse.Success -> LOGGER.info("Response: ${HttpStatus.OK}/${response.body}")
      is RestResponse.Failure -> LOGGER.info("Response: ${response.statusCode}/${response.message}")
    }

    return response;
  }

  private fun <T> initHttpEntity(body: T): HttpEntity<T> {
    val httpHeaders = HttpHeaders()
    httpHeaders.contentType = MediaType.APPLICATION_JSON
    return HttpEntity(body, httpHeaders)
  }
}
