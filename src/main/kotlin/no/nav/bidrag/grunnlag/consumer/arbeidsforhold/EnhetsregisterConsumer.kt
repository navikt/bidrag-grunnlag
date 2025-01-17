package no.nav.bidrag.grunnlag.consumer.arbeidsforhold

import no.nav.bidrag.grunnlag.consumer.GrunnlagConsumer
import no.nav.bidrag.grunnlag.consumer.arbeidsforhold.api.HentEnhetsregisterRequest
import no.nav.bidrag.grunnlag.consumer.arbeidsforhold.api.HentEnhetsregisterResponse
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.exception.tryExchange
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class EnhetsregisterConsumer(
    @Value("\${EREG_URL}") private val eregUrl: URI,
    private val restTemplate: RestTemplate,
    private val grunnlagConsumer: GrunnlagConsumer,
) {

    fun hentEnhetsinfo(request: HentEnhetsregisterRequest): RestResponse<HentEnhetsregisterResponse> {
        val hentEnhetsinfoUri =
            UriComponentsBuilder
                .fromUri(eregUrl)
                .pathSegment(byggEregUrl(request))
                .build()
                .toUriString()

        val restResponse = restTemplate.tryExchange(
            url = hentEnhetsinfoUri,
            httpMethod = HttpMethod.GET,
            httpEntity = grunnlagConsumer.initHttpEntityEreg(request),
            responseType = HentEnhetsregisterResponse::class.java,
            fallbackBody = HentEnhetsregisterResponse(),
        )

        grunnlagConsumer.logResponse(
            type = "Enhetsregister",
            ident = request.organisasjonsnummer,
            fom = null,
            tom = null,
            restResponse = restResponse,
        )

        return restResponse
    }

    private fun byggEregUrl(request: HentEnhetsregisterRequest): String {
        val url = "v2/organisasjon/${request.organisasjonsnummer}/noekkelinfo"
        return if (!request.gyldigDato.isNullOrBlank()) url.plus("?gyldigDato=${request.gyldigDato}") else url
    }
}
