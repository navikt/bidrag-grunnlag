package no.nav.bidrag.grunnlag.consumer.infotrygdkontantstottev2

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.grunnlag.consumer.GrunnlagsConsumer
import no.nav.bidrag.grunnlag.consumer.infotrygdkontantstottev2.api.KonstantstotteRequest
import no.nav.bidrag.grunnlag.consumer.infotrygdkontantstottev2.api.KontantstotteResponse
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.exception.tryExchange
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod

private const val KONTANTSTOTTE_CONTEXT = "/hentPerioder"

open class KontantstotteConsumer(private val restTemplate: HttpHeaderRestTemplate) :
  GrunnlagsConsumer() {

  companion object {
    @JvmStatic
    val logger: Logger = LoggerFactory.getLogger(KontantstotteConsumer::class.java)
  }

  open fun hentKontantstotte(request: KonstantstotteRequest): RestResponse<KontantstotteResponse> {
    logger.info("Henter kontantstøtte")

    val restResponse = restTemplate.tryExchange(
      KONTANTSTOTTE_CONTEXT,
      HttpMethod.POST,
      initHttpEntity(request),
      KontantstotteResponse::class.java,
      KontantstotteResponse(emptyList())
    )

    logResponse(logger, restResponse)

    return restResponse
  }
}
