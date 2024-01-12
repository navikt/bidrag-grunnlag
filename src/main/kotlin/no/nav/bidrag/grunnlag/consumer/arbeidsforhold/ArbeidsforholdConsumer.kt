package no.nav.bidrag.grunnlag.consumer.arbeidsforhold

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.consumer.GrunnlagsConsumer
import no.nav.bidrag.grunnlag.consumer.arbeidsforhold.api.Arbeidsforhold
import no.nav.bidrag.grunnlag.consumer.arbeidsforhold.api.HentArbeidsforholdRequest
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.exception.tryExchange
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod

private const val AAREG_CONTEXT = "/api/v2/arbeidstaker/arbeidsforhold"

open class ArbeidsforholdConsumer(private val restTemplate: HttpHeaderRestTemplate) :
    GrunnlagsConsumer() {

    companion object {
        @JvmStatic
        val LOGGER: Logger = LoggerFactory.getLogger(ArbeidsforholdConsumer::class.java)
    }

    open fun hentArbeidsforhold(request: HentArbeidsforholdRequest): RestResponse<List<Arbeidsforhold>> {
        SECURE_LOGGER.info("Henter arbeidsforhold fra Aareg med request: $request")

        val responseType = object : ParameterizedTypeReference<List<Arbeidsforhold>>() {}

        val restResponse = restTemplate.tryExchange(
            AAREG_CONTEXT,
            HttpMethod.GET,
            initHttpEntityAareg(request, request.arbeidstakerId),
            responseType,
            emptyList(),
        )

        logResponse(SECURE_LOGGER, restResponse)

        return restResponse
    }
}
