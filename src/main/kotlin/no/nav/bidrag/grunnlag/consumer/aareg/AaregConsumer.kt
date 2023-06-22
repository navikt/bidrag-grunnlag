package no.nav.bidrag.grunnlag.consumer.aareg

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.consumer.GrunnlagsConsumer
import no.nav.bidrag.grunnlag.consumer.aareg.api.Arbeidsforhold
import no.nav.bidrag.grunnlag.consumer.aareg.api.HentArbeidsforholdRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity

private const val AAREG_CONTEXT = "/api/v2/arbeidstaker/arbeidsforhold"

open class AaregConsumer(private val restTemplate: HttpHeaderRestTemplate) :
    GrunnlagsConsumer() {

    companion object {
        @JvmStatic
        val logger: Logger = LoggerFactory.getLogger(AaregConsumer::class.java)
    }

    open fun hentArbeidsforhold(request: HentArbeidsforholdRequest): ResponseEntity<List<Arbeidsforhold>> {
        logger.info("Henter arbeidsforhold fra aareg")
        SECURE_LOGGER.info("Henter arbeidsforhold fra aareg med request: $request")

        val responseType = object : ParameterizedTypeReference<List<Arbeidsforhold>>() {}

        val restResponse = restTemplate.exchange(
            AAREG_CONTEXT,
            HttpMethod.GET,
            initHttpEntityAareg(request, request.arbeidstakerId),
            responseType
        )

        return restResponse
    }
}
