package no.nav.bidrag.grunnlag.consumer.inntektskomponenten

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.consumer.GrunnlagsConsumer
import no.nav.bidrag.grunnlag.consumer.inntektskomponenten.api.HentInntektListeRequest
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.exception.tryExchange
import no.nav.tjenester.aordningen.inntektsinformasjon.Aktoer
import no.nav.tjenester.aordningen.inntektsinformasjon.AktoerType
import no.nav.tjenester.aordningen.inntektsinformasjon.response.HentInntektListeResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod

private const val INNTEKT_LISTE_CONTEXT = "/rs/api/v1/hentinntektliste"
private const val DETALJERTE_ABONNERTE_INNTEKTER_CONTEXT = "/rs/api/v1/hentdetaljerteabonnerteinntekter"

open class InntektskomponentenConsumer(private val restTemplate: HttpHeaderRestTemplate) : GrunnlagsConsumer() {

    companion object {
        @JvmStatic
        val LOGGER: Logger = LoggerFactory.getLogger(InntektskomponentenConsumer::class.java)
    }

    open fun hentInntekter(request: HentInntektListeRequest, abonnerteInntekterRequest: Boolean): RestResponse<HentInntektListeResponse> {
        if (abonnerteInntekterRequest) {
            SECURE_LOGGER.info("Henter abonnerte inntekter fra Inntektskomponenten.")
        } else {
            SECURE_LOGGER.info("Henter inntekter fra Inntektskomponenten.")
        }
        SECURE_LOGGER.info("HentInntektListeRequest: $request")
        val url = if (abonnerteInntekterRequest) DETALJERTE_ABONNERTE_INNTEKTER_CONTEXT else INNTEKT_LISTE_CONTEXT

        val restResponse = restTemplate.tryExchange(
            url,
            HttpMethod.POST,
            initHttpEntityInntektskomponenten(request),
            HentInntektListeResponse::class.java,
            HentInntektListeResponse(emptyList(), Aktoer(request.ident.identifikator, AktoerType.NATURLIG_IDENT))
        )

        logResponse(SECURE_LOGGER, restResponse)

        return restResponse
    }
}
