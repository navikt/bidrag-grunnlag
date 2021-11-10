package no.nav.bidrag.grunnlag

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import no.nav.bidrag.commons.CorrelationId
import no.nav.bidrag.commons.ExceptionLogger
import no.nav.bidrag.commons.web.CorrelationIdFilter
import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.BidragGcpProxyConsumer
import no.nav.bidrag.grunnlag.consumer.familiebasak.FamilieBaSakConsumer
import no.nav.bidrag.grunnlag.service.SecurityTokenService
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RootUriTemplateHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope

const val LIVE_PROFILE = "live"

@Configuration
@OpenAPIDefinition(
  info = Info(title = "bidrag-grunnlag", version = "v1"),
  security = [SecurityRequirement(name = "bearer-key")]
)
@EnableJwtTokenValidation
@SecurityScheme(
  bearerFormat = "JWT",
  name = "bearer-key",
  scheme = "bearer",
  type = SecuritySchemeType.HTTP
)
class BidragGrunnlagConfig {

  companion object {

    @JvmStatic
    private val LOGGER = LoggerFactory.getLogger(BidragGrunnlagConfig::class.java)
  }

  @Bean
  fun exceptionLogger(): ExceptionLogger {
    return ExceptionLogger(BidragGrunnlag::class.java.simpleName)
  }

  @Bean
  fun correlationIdFilter(): CorrelationIdFilter {
    return CorrelationIdFilter()
  }

  @Bean
  @Scope("prototype")
  fun restTemplate(): HttpHeaderRestTemplate {
    val httpHeaderRestTemplate = HttpHeaderRestTemplate()
    httpHeaderRestTemplate.addHeaderGenerator(CorrelationIdFilter.CORRELATION_ID_HEADER) { CorrelationId.fetchCorrelationIdForThread() }
    return httpHeaderRestTemplate
  }

  @Bean
  fun familieBaSakConsumer(
    @Value("\${FAMILIEBASAK_URL}") url: String,
    restTemplate: HttpHeaderRestTemplate,
    securityTokenService: SecurityTokenService,
    exceptionLogger: ExceptionLogger
  ): FamilieBaSakConsumer {
    LOGGER.info("Url satt i config: $url")
    restTemplate.uriTemplateHandler = RootUriTemplateHandler(url)
    restTemplate.interceptors.add(securityTokenService.generateBearerToken("familiebasak"))
    return FamilieBaSakConsumer(restTemplate)
  }

  @Bean
  fun bidragGcpProxyConsumer(
    @Value("\${BIDRAGGCPPROXY_URL}") url: String,
    restTemplate: HttpHeaderRestTemplate,
    securityTokenService: SecurityTokenService,
    exceptionLogger: ExceptionLogger
  ): BidragGcpProxyConsumer {
    LOGGER.info("Url satt i config: $url")
    restTemplate.uriTemplateHandler = RootUriTemplateHandler(url)
    restTemplate.interceptors.add(securityTokenService.generateBearerToken("bidraggcpproxy"))
    return BidragGcpProxyConsumer(restTemplate)
  }
}
