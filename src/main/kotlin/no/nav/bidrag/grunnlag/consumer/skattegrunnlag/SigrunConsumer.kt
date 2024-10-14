package no.nav.bidrag.grunnlag.consumer.skattegrunnlag

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.consumer.GrunnlagsConsumer
import no.nav.bidrag.grunnlag.consumer.skattegrunnlag.api.HentSummertSkattegrunnlagRequest
import no.nav.bidrag.grunnlag.consumer.skattegrunnlag.api.HentSummertSkattegrunnlagResponse
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.exception.tryExchange
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.web.util.UriComponentsBuilder
import java.time.LocalDate

private const val SUMMERT_SKATTEGRUNNLAG_URL = "/api/v2/summertskattegrunnlag"
private const val INNTEKTSAAR = "inntektsaar"
private const val RETTIGHETSPAKKE = "rettighetspakke"
private const val PERSONIDENTIFIKATOR = "personidentifikator"
private const val STADIE = "stadie"

open class SigrunConsumer(private val restTemplate: HttpHeaderRestTemplate) : GrunnlagsConsumer() {

    companion object {
        @JvmStatic
        val LOGGER: Logger = LoggerFactory.getLogger(SigrunConsumer::class.java)
    }

    open fun hentSummertSkattegrunnlag(request: HentSummertSkattegrunnlagRequest): RestResponse<HentSummertSkattegrunnlagResponse> {
        val uri = UriComponentsBuilder.fromPath(SUMMERT_SKATTEGRUNNLAG_URL)
            .queryParam(RETTIGHETSPAKKE, "navBidrag")
            .queryParam(INNTEKTSAAR, request.inntektsAar)
            .queryParam(PERSONIDENTIFIKATOR, request.personId)
            .queryParam(STADIE, "oppgjoer")
            .build()
            .toUriString()

        SECURE_LOGGER.info("HentSummertSkattegrunnlag uri: {}", uri)
        SECURE_LOGGER.info("HentSummertSkattegrunnlagRequest: {}", request)

        val restResponse = restTemplate.tryExchange(
            uri,
            HttpMethod.GET,
            initHttpEntitySkattegrunnlag(request),
            HentSummertSkattegrunnlagResponse::class.java,
            HentSummertSkattegrunnlagResponse(emptyList(), emptyList(), null),
        )

        logResponse("Skattegrunnlag", request.personId, LocalDate.of(request.inntektsAar.toInt(), 1, 1), null, restResponse)

        return restResponse
    }
}
