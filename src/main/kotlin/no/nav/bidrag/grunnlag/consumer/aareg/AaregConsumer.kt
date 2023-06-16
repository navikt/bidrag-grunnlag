package no.nav.bidrag.grunnlag.consumer.aareg

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.grunnlag.consumer.GrunnlagsConsumer
import no.nav.bidrag.grunnlag.consumer.aareg.api.HentArbeidsforholdRequest
import no.nav.bidrag.grunnlag.consumer.aareg.api.HentArbeidsforholdResponse
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.exception.tryExchange
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod

private const val AAREG_CONTEXT = "/api/v2/arbeidstaker/arbeidsforhold"

open class AaregConsumer(private val restTemplate: HttpHeaderRestTemplate) :
    GrunnlagsConsumer() {

    companion object {
        @JvmStatic
        val logger: Logger = LoggerFactory.getLogger(AaregConsumer::class.java)
    }

    open fun hentArbeidsforhold(request: HentArbeidsforholdRequest): RestResponse<HentArbeidsforholdResponse> {
        logger.info("Henter arbeidsforhold fra aareg")
//        SECURE_LOGGER.info("Henter arbeidsforhold fra aareg med request: $request")

        val restResponse = restTemplate.tryExchange(
            AAREG_CONTEXT,
            HttpMethod.GET,
            initHttpEntity(request),
            HentArbeidsforholdResponse::class.java,
            HentArbeidsforholdResponse(emptyList())
        )
        logResponse(logger, restResponse)

        return restResponse
    }
}
