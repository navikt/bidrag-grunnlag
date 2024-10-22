package no.nav.bidrag.grunnlag.consumer.inntektskomponenten

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.consumer.GrunnlagsConsumer
import no.nav.bidrag.grunnlag.consumer.inntektskomponenten.api.HentInntektListeRequest
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.exception.tryExchange
import no.nav.bidrag.grunnlag.service.InntektskomponentenService
import no.nav.bidrag.grunnlag.util.GrunnlagUtil.Companion.tilJson
import no.nav.tjenester.aordningen.inntektsinformasjon.Aktoer
import no.nav.tjenester.aordningen.inntektsinformasjon.AktoerType
import no.nav.tjenester.aordningen.inntektsinformasjon.response.HentInntektListeResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

private const val INNTEKT_LISTE_CONTEXT = "/rs/api/v1/hentinntektliste"
private const val DETALJERTE_ABONNERTE_INNTEKTER_CONTEXT = "/rs/api/v1/hentdetaljerteabonnerteinntekter"

open class InntektskomponentenConsumer(private val restTemplate: HttpHeaderRestTemplate) : GrunnlagsConsumer() {

    companion object {
        @JvmStatic
        val LOGGER: Logger = LoggerFactory.getLogger(InntektskomponentenConsumer::class.java)
    }

    open fun hentInntekter(request: HentInntektListeRequest, abonnerteInntekterRequest: Boolean): RestResponse<HentInntektListeResponse> {
        if (abonnerteInntekterRequest) {
            LOGGER.debug("Henter abonnerte inntekter fra Inntektskomponenten.")
        } else {
            LOGGER.debug("Henter inntekter fra Inntektskomponenten.")
        }
        SECURE_LOGGER.info("HentInntektListeRequest: $request")
        val url = if (abonnerteInntekterRequest) DETALJERTE_ABONNERTE_INNTEKTER_CONTEXT else INNTEKT_LISTE_CONTEXT

        val restResponse = restTemplate.tryExchange(
            url,
            HttpMethod.POST,
            initHttpEntityInntektskomponenten(request),
            HentInntektListeResponse::class.java,
            HentInntektListeResponse(emptyList(), Aktoer(request.ident.identifikator, AktoerType.NATURLIG_IDENT)),
        )

        when (restResponse) {
            is RestResponse.Success -> SECURE_LOGGER.info(
                "Henting av abonnerte ? $abonnerteInntekterRequest inntekter for perioden ${request.maanedFom} - ${request.maanedTom} " +
                    "ga følgende respons for ${request.ident.identifikator}: ${tilJson(restResponse.body)}",
            )

            is RestResponse.Failure -> {
                if (abonnerteInntekterRequest) {
                    // Utelater feillogging ved 400 - Bad Request (inntektsabonnement finnes ikke for personen)
                    // og 423 og 500 (inntektsabonnementet er ikke aktivt ennå)
                    if (restResponse.statusCode == HttpStatus.NOT_FOUND ||
                        restResponse.statusCode == HttpStatus.INTERNAL_SERVER_ERROR ||
                        restResponse.statusCode == HttpStatus.BAD_REQUEST ||
                        restResponse.statusCode == HttpStatus.LOCKED
                    ) {
                        InntektskomponentenService.LOGGER.warn(
                            "Mangler abonnement for henting av inntekter fra Inntektskomponenten. " +
                                "Statuskode ${restResponse.statusCode.value()}/${restResponse.message}",
                        )
                        SECURE_LOGGER.warn(
                            "Mangler abonnement for henting av inntekter fra Inntektskomponenten for ${request.ident.identifikator} for perioden " +
                                "${request.maanedFom} - ${request.maanedTom}. Prøver å hente inntekter uten abonnement. ${restResponse.message}",
                        )
                    } else {
                        InntektskomponentenService.LOGGER.error(
                            "Feil ved henting av inntekter med abonnement fra Inntektskomponenten. " +
                                "Statuskode ${restResponse.statusCode.value()}/${restResponse.message}",
                        )
                        SECURE_LOGGER.error(
                            "Feil ved henting av inntekter med abonnement for ${request.ident.identifikator} for perioden " +
                                "${request.maanedFom} - ${request.maanedTom}. Prøver å hente inntekter uten abonnement. /${restResponse.message}",
                        )
                    }
                } else {
                    InntektskomponentenService.LOGGER.error(
                        "Feil ved henting av inntekter uten abonnement fra Inntektskomponenten. " +
                            "Statuskode ${restResponse.statusCode.value()} /${restResponse.message}",
                    )
                    SECURE_LOGGER.error(
                        "Feil ved henting av inntekter uten abonnement for ${request.ident.identifikator} for perioden " +
                            "${request.maanedFom} - ${request.maanedTom}. Prøver å hente inntekter uten abonnement. /${restResponse.message}",
                    )
                }
            }
        }

        logResponse(SECURE_LOGGER, restResponse)

        return restResponse
    }
}
