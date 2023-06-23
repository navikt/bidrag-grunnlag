package no.nav.bidrag.grunnlag

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import no.nav.bidrag.commons.CorrelationId
import no.nav.bidrag.commons.ExceptionLogger
import no.nav.bidrag.commons.web.CorrelationIdFilter
import no.nav.bidrag.commons.web.DefaultCorsFilter
import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.commons.web.UserMdcFilter
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.BidragGcpProxyConsumer
import no.nav.bidrag.grunnlag.consumer.bidragperson.BidragPersonConsumer
import no.nav.bidrag.grunnlag.consumer.familiebasak.FamilieBaSakConsumer
import no.nav.bidrag.grunnlag.consumer.familieefsak.FamilieEfSakConsumer
import no.nav.bidrag.grunnlag.consumer.familiekssak.FamilieKsSakConsumer
import no.nav.bidrag.grunnlag.consumer.inntektskomponenten.InntektskomponentenConsumer
import no.nav.bidrag.grunnlag.service.SecurityTokenService
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RootUriTemplateHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
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
@Import(CorrelationIdFilter::class, UserMdcFilter::class, DefaultCorsFilter::class)
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
    fun familieEfSakConsumer(
        @Value("\${FAMILIEEFSAK_URL}") url: String,
        restTemplate: HttpHeaderRestTemplate,
        securityTokenService: SecurityTokenService,
        exceptionLogger: ExceptionLogger
    ): FamilieEfSakConsumer {
        LOGGER.info("Url satt i config: $url")
        restTemplate.uriTemplateHandler = RootUriTemplateHandler(url)
        restTemplate.interceptors.add(securityTokenService.generateBearerToken("familieefsak"))
        return FamilieEfSakConsumer(restTemplate)
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

    @Bean
    fun inntektskomponentenConsumer(
        @Value("\${INNTEKTSKOMPONENTEN_URL}") url: String,
        restTemplate: HttpHeaderRestTemplate,
        securityTokenService: SecurityTokenService,
        exceptionLogger: ExceptionLogger
    ): InntektskomponentenConsumer {
        LOGGER.info("Url satt i config: $url")
        restTemplate.uriTemplateHandler = RootUriTemplateHandler(url)
        restTemplate.interceptors.add(securityTokenService.generateBearerToken("inntektskomponenten"))
        return InntektskomponentenConsumer(restTemplate)
    }

    @Bean
    fun bidragPersonConsumer(
        @Value("\${BIDRAGPERSON_URL}") url: String,
        restTemplate: HttpHeaderRestTemplate,
        securityTokenService: SecurityTokenService,
        exceptionLogger: ExceptionLogger
    ): BidragPersonConsumer {
        LOGGER.info("Url satt i config: $url")
        restTemplate.uriTemplateHandler = RootUriTemplateHandler(url)
        restTemplate.interceptors.add(securityTokenService.generateBearerToken("bidragperson"))
        return BidragPersonConsumer(restTemplate)
    }

    @Bean
    fun familieKsSakConsumer(
        @Value("\${FAMILIEKSSAK_URL}") url: String,
        restTemplate: HttpHeaderRestTemplate,
        securityTokenService: SecurityTokenService,
        exceptionLogger: ExceptionLogger
    ): FamilieKsSakConsumer {
        LOGGER.info("Url satt i config: $url")
        restTemplate.uriTemplateHandler = RootUriTemplateHandler(url)
        restTemplate.interceptors.add(securityTokenService.generateBearerToken("familiekssak"))
        return FamilieKsSakConsumer(restTemplate)
    }

/*  @Bean
  fun kontantstotteConsumer(
    @Value("\${KONTANTSTOTTE_URL}") url: String,
    restTemplate: HttpHeaderRestTemplate,
    securityTokenService: SecurityTokenService,
    exceptionLogger: ExceptionLogger
  ): KontantstotteConsumer {
    LOGGER.info("Url satt i config: $url")
    restTemplate.uriTemplateHandler = RootUriTemplateHandler(url)
    restTemplate.interceptors.add(securityTokenService.generateBearerToken("kontantstotte"))
    return KontantstotteConsumer(restTemplate)
  }*/
}
