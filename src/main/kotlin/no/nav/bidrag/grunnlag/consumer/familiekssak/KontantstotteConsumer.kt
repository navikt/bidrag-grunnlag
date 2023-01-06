package no.nav.bidrag.grunnlag.consumer.familiekssak

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.grunnlag.consumer.GrunnlagsConsumer
import no.nav.bidrag.grunnlag.consumer.familiekssak.api.BisysDto
import no.nav.bidrag.grunnlag.consumer.familiekssak.api.BisysResponsDto
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.exception.tryExchange
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod

private const val KONTANTSTOTTE_CONTEXT = "/api/bisys"

open class KontantstotteConsumer(private val restTemplate: HttpHeaderRestTemplate) :
  GrunnlagsConsumer() {

  companion object {
    @JvmStatic
    val logger: Logger = LoggerFactory.getLogger(KontantstotteConsumer::class.java)
  }

  open fun hentKontantstotte(request: BisysDto): RestResponse<BisysResponsDto> {
    logger.info("Henter kontantstøtte")

    val restResponse = restTemplate.tryExchange(
      KONTANTSTOTTE_CONTEXT,
      HttpMethod.POST,
      initHttpEntity(request),
      BisysResponsDto::class.java,
      BisysResponsDto(emptyMap())
    )

    logResponse(logger, restResponse)

    return restResponse
  }
}
