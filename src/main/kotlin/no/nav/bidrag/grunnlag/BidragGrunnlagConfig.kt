package no.nav.bidrag.grunnlag

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import no.nav.bidrag.commons.CorrelationId
import no.nav.bidrag.commons.ExceptionLogger
import no.nav.bidrag.commons.cache.EnableUserCache
import no.nav.bidrag.commons.web.CorrelationIdFilter
import no.nav.bidrag.commons.web.DefaultCorsFilter
import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.commons.web.UserMdcFilter
import no.nav.bidrag.grunnlag.consumer.arbeidsforhold.ArbeidsforholdConsumer
import no.nav.bidrag.grunnlag.consumer.arbeidsforhold.EnhetsregisterConsumer
import no.nav.bidrag.grunnlag.consumer.bidragperson.BidragPersonConsumer
import no.nav.bidrag.grunnlag.consumer.familiebasak.FamilieBaSakConsumer
import no.nav.bidrag.grunnlag.consumer.familiebasak.TilleggsstønadConsumer
import no.nav.bidrag.grunnlag.consumer.familieefsak.FamilieEfSakConsumer
import no.nav.bidrag.grunnlag.consumer.familiekssak.FamilieKsSakConsumer
import no.nav.bidrag.grunnlag.consumer.inntektskomponenten.InntektskomponentenConsumer
import no.nav.bidrag.grunnlag.consumer.pensjon.PensjonConsumer
import no.nav.bidrag.grunnlag.consumer.skattegrunnlag.SigrunConsumer
import no.nav.bidrag.grunnlag.service.SecurityTokenService
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder
import org.apache.hc.core5.http.io.SocketConfig
import org.apache.hc.core5.util.Timeout
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RootUriTemplateHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Scope
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.http.client.observation.DefaultClientRequestObservationConvention
import org.springframework.retry.annotation.EnableRetry

const val LIVE_PROFILE = "live"

@Configuration
@OpenAPIDefinition(
    info = Info(title = "bidrag-grunnlag", version = "v1"),
    security = [SecurityRequirement(name = "bearer-key")],
)
@EnableJwtTokenValidation
@SecurityScheme(
    bearerFormat = "JWT",
    name = "bearer-key",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
)
@EnableUserCache
@EnableRetry
@Import(CorrelationIdFilter::class, UserMdcFilter::class, DefaultCorsFilter::class)
class BidragGrunnlagConfig {

    companion object {

        @JvmStatic
        private val LOGGER = LoggerFactory.getLogger(BidragGrunnlagConfig::class.java)
    }

    @Bean
    fun exceptionLogger(): ExceptionLogger = ExceptionLogger(BidragGrunnlag::class.java.simpleName)

    @Bean
    @Scope("prototype")
    fun restTemplate(): HttpHeaderRestTemplate {
        val httpHeaderRestTemplate = HttpHeaderRestTemplate()

        val sc = SocketConfig.custom().setSoTimeout(Timeout.ofSeconds(10)).build()
        val pb = PoolingHttpClientConnectionManagerBuilder.create().setDefaultSocketConfig(sc).build()
        val connectionManager = HttpClientBuilder.create().setConnectionManager(pb).build()
        val requestFactory = HttpComponentsClientHttpRequestFactory(connectionManager)
        httpHeaderRestTemplate.requestFactory = requestFactory

        httpHeaderRestTemplate.addHeaderGenerator(CorrelationIdFilter.CORRELATION_ID_HEADER) { CorrelationId.fetchCorrelationIdForThread() }
        return httpHeaderRestTemplate
    }

    @Bean
    fun clientRequestObservationConvention() = DefaultClientRequestObservationConvention()

    @Bean
    fun familieBaSakConsumer(
        @Value("\${FAMILIEBASAK_URL}") url: String,
        restTemplate: HttpHeaderRestTemplate,
        grunnlagSecurityTokenService: SecurityTokenService,
    ): FamilieBaSakConsumer {
        LOGGER.info("Url satt i config: $url")
        restTemplate.uriTemplateHandler = RootUriTemplateHandler(url)
        restTemplate.interceptors.add(grunnlagSecurityTokenService.generateBearerToken("familiebasak"))
        return FamilieBaSakConsumer(restTemplate)
    }

    @Bean
    fun familieEfSakConsumer(
        @Value("\${FAMILIEEFSAK_URL}") url: String,
        restTemplate: HttpHeaderRestTemplate,
        grunnlagSecurityTokenService: SecurityTokenService,
    ): FamilieEfSakConsumer {
        LOGGER.info("Url satt i config: $url")
        restTemplate.uriTemplateHandler = RootUriTemplateHandler(url)
        restTemplate.interceptors.add(grunnlagSecurityTokenService.generateBearerToken("familieefsak"))
        return FamilieEfSakConsumer(restTemplate)
    }

    @Bean
    fun inntektskomponentenConsumer(
        @Value("\${INNTEKTSKOMPONENTEN_URL}") url: String,
        restTemplate: HttpHeaderRestTemplate,
        grunnlagSecurityTokenService: SecurityTokenService,
    ): InntektskomponentenConsumer {
        LOGGER.info("Url satt i config: $url")
        restTemplate.uriTemplateHandler = RootUriTemplateHandler(url)
        restTemplate.interceptors.add(grunnlagSecurityTokenService.generateBearerToken("inntektskomponenten"))
        return InntektskomponentenConsumer(restTemplate)
    }

    @Bean
    fun sigrunConsumer(
        @Value("\${SIGRUN_URL}") url: String,
        restTemplate: HttpHeaderRestTemplate,
        grunnlagSecurityTokenService: SecurityTokenService,
    ): SigrunConsumer {
        LOGGER.info("Url satt i config: $url")
        restTemplate.uriTemplateHandler = RootUriTemplateHandler(url)
        restTemplate.interceptors.add(grunnlagSecurityTokenService.generateBearerToken("sigrun"))
        return SigrunConsumer(restTemplate)
    }

    @Bean
    fun pensjonConsumer(
        @Value("\${PENSJON_URL}") url: String,
        restTemplate: HttpHeaderRestTemplate,
        grunnlagSecurityTokenService: SecurityTokenService,
    ): PensjonConsumer {
        LOGGER.info("Url satt i config: $url")
        restTemplate.uriTemplateHandler = RootUriTemplateHandler(url)
        restTemplate.interceptors.add(grunnlagSecurityTokenService.generateBearerToken("pensjon"))
        return PensjonConsumer(restTemplate)
    }

    @Bean
    fun bidragPersonConsumer(
        @Value("\${BIDRAGPERSON_URL}") url: String,
        restTemplate: HttpHeaderRestTemplate,
        grunnlagSecurityTokenService: SecurityTokenService,
    ): BidragPersonConsumer {
        LOGGER.info("Url satt i config: $url")
        restTemplate.uriTemplateHandler = RootUriTemplateHandler(url)
        restTemplate.interceptors.add(grunnlagSecurityTokenService.generateBearerToken("bidragperson"))
        return BidragPersonConsumer(restTemplate)
    }

    @Bean
    fun familieKsSakConsumer(
        @Value("\${FAMILIEKSSAK_URL}") url: String,
        restTemplate: HttpHeaderRestTemplate,
        grunnlagSecurityTokenService: SecurityTokenService,
    ): FamilieKsSakConsumer {
        LOGGER.info("Url satt i config: $url")
        restTemplate.uriTemplateHandler = RootUriTemplateHandler(url)
        restTemplate.interceptors.add(grunnlagSecurityTokenService.generateBearerToken("familiekssak"))
        return FamilieKsSakConsumer(restTemplate)
    }

    @Bean
    fun arbeidsforholdConsumer(
        @Value("\${AAREG_URL}") url: String,
        restTemplate: HttpHeaderRestTemplate,
        grunnlagSecurityTokenService: SecurityTokenService,
    ): ArbeidsforholdConsumer {
        LOGGER.info("Url satt i config: $url")
        restTemplate.uriTemplateHandler = RootUriTemplateHandler(url)
        restTemplate.interceptors.add(grunnlagSecurityTokenService.generateBearerToken("aareg"))
        return ArbeidsforholdConsumer(restTemplate)
    }

    @Bean
    fun enhetsregisterConsumer(@Value("\${EREG_URL}") url: String, restTemplate: HttpHeaderRestTemplate): EnhetsregisterConsumer {
        LOGGER.info("Url satt i config: $url")
        restTemplate.uriTemplateHandler = RootUriTemplateHandler(url)
        return EnhetsregisterConsumer(restTemplate)
    }

    @Bean
    fun tilleggsstønadConsumer(
        @Value("\${TILLEGGSSTONADERSAK_URL}") url: String,
        restTemplate: HttpHeaderRestTemplate,
        grunnlagSecurityTokenService: SecurityTokenService,
    ): TilleggsstønadConsumer {
        LOGGER.info("Url satt i config: $url")
        restTemplate.uriTemplateHandler = RootUriTemplateHandler(url)
        restTemplate.interceptors.add(grunnlagSecurityTokenService.generateBearerToken("tilleggsstonadersak"))
        return TilleggsstønadConsumer(restTemplate)
    }
}
