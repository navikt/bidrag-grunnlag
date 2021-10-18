package no.nav.bidrag.grunnlag.consumer

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.FamilieBaSakRequest
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.FamilieBaSakResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType

private const val FAMILIEBASAK_CONTEXT = "/api/bisys/hent-utvidet-barnetrygd"

open class FamilieBaSakConsumer(private val restTemplate: HttpHeaderRestTemplate) {

  companion object {
    @JvmStatic
    private val LOGGER = LoggerFactory.getLogger(FamilieBaSakConsumer::class.java)
  }

  fun hentFamilieBaSak(request: FamilieBaSakRequest): FamilieBaSakResponse {
    LOGGER.info("Henter utvidet barnetrygd eller småbarnstillegg fra familie-ba-sak")

    val response = restTemplate.exchange(
      FAMILIEBASAK_CONTEXT,
      HttpMethod.POST,
      initHttpEntity(request),
      FamilieBaSakResponse::class.java
    )

    LOGGER.info("Response: ${response.statusCode}/${response.body}")

    return response.body ?: FamilieBaSakResponse(emptyList())
  }

  private fun <T> initHttpEntity(body: T): HttpEntity<T> {
    val httpHeaders = HttpHeaders()
    httpHeaders.contentType = MediaType.APPLICATION_JSON
    return HttpEntity(body, httpHeaders)
  }
}
