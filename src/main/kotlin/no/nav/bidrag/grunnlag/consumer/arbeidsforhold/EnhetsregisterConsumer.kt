package no.nav.bidrag.grunnlag.consumer.arbeidsforhold

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.consumer.GrunnlagsConsumer
import no.nav.bidrag.grunnlag.consumer.arbeidsforhold.api.HentEnhetsregisterRequest
import no.nav.bidrag.grunnlag.consumer.arbeidsforhold.api.HentEnhetsregisterResponse
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.exception.tryExchange
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable

open class EnhetsregisterConsumer(private val restTemplate: HttpHeaderRestTemplate) :
    GrunnlagsConsumer() {

    companion object {
        @JvmStatic
        val LOGGER: Logger = LoggerFactory.getLogger(EnhetsregisterConsumer::class.java)
    }

    @Retryable(value = [Exception::class], backoff = Backoff(delay = 500))
    open fun hentEnhetsinfo(request: HentEnhetsregisterRequest): RestResponse<HentEnhetsregisterResponse> {
        LOGGER.info("Henter info om en organisasjon fra Ereg")
        SECURE_LOGGER.info("Henter info om en organisasjon fra Ereg med request: $request")

        val restResponse = restTemplate.tryExchange(
            byggEregUrl(request),
            HttpMethod.GET,
            initHttpEntityEreg(request),
            HentEnhetsregisterResponse::class.java,
            HentEnhetsregisterResponse(),
        )

        logResponse(SECURE_LOGGER, restResponse)

        return restResponse
    }

    private fun byggEregUrl(request: HentEnhetsregisterRequest): String {
        val url = "/v2/organisasjon/${request.organisasjonsnummer}/noekkelinfo"
        return if (!request.gyldigDato.isNullOrBlank()) url.plus("?gyldigDato=${request.gyldigDato}") else url
    }
}
