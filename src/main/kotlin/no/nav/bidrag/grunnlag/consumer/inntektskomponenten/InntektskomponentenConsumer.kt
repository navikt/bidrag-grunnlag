package no.nav.bidrag.grunnlag.consumer.inntektskomponenten

import io.github.resilience4j.timelimiter.TimeLimiterRegistry
import no.nav.bidrag.commons.web.client.AbstractRestClient
import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.consumer.GrunnlagConsumer
import no.nav.bidrag.grunnlag.consumer.inntektskomponenten.api.HentInntektListeRequest
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.exception.tryExchange
import no.nav.bidrag.grunnlag.service.InntektskomponentenService
import no.nav.bidrag.grunnlag.util.GrunnlagUtil.Companion.tilJson
import no.nav.tjenester.aordningen.inntektsinformasjon.Aktoer
import no.nav.tjenester.aordningen.inntektsinformasjon.AktoerType
import no.nav.tjenester.aordningen.inntektsinformasjon.response.HentInntektListeResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.concurrent.TimeoutException
import javax.naming.ServiceUnavailableException

@Service
class InntektskomponentenConsumer(
    @Value("\${INNTEKTSKOMPONENTEN_URL}") inntektskomponentenUrl: URI,
    @Qualifier("azureService") private val restTemplate: RestTemplate,
    private val grunnlagConsumer: GrunnlagConsumer,
    circuitBreakerFactory: CircuitBreakerFactory<*, *>,
    timeLimiterRegistry: TimeLimiterRegistry,
) : AbstractRestClient(restTemplate, "inntektskomponenten") {

    private val circuitBreaker = circuitBreakerFactory.create("inntektskomponenten")
    private val timeLimiter = timeLimiterRegistry.timeLimiter("inntektskomponenten")

    private val hentInntekterUri =
        UriComponentsBuilder
            .fromUri(inntektskomponentenUrl)
            .pathSegment("rs/api/v1/hentinntektliste")
            .build()
            .toUriString()

    private val hentAbonnerteInntekterUri =
        UriComponentsBuilder
            .fromUri(inntektskomponentenUrl)
            .pathSegment("rs/api/v1/hentdetaljerteabonnerteinntekter")
            .build()
            .toUriString()

    fun hentInntekter(request: HentInntektListeRequest, abonnerteInntekterRequest: Boolean): RestResponse<HentInntektListeResponse> =
        circuitBreaker.run({
            timeLimiter.executeFutureSupplier {
                CompletableFuture.supplyAsync {
                    val url = if (abonnerteInntekterRequest) hentAbonnerteInntekterUri else hentInntekterUri

                    val restResponse = restTemplate.tryExchange(
                        url = url,
                        httpMethod = HttpMethod.POST,
                        httpEntity = grunnlagConsumer.initHttpEntityInntektskomponenten(request),
                        responseType = HentInntektListeResponse::class.java,
                        fallbackBody = HentInntektListeResponse(emptyList(), Aktoer(request.ident.identifikator, AktoerType.NATURLIG_IDENT)),
                    )
                    when (restResponse) {
                        is RestResponse.Success -> SECURE_LOGGER.info(
                            "Henting av abonnerte ? $abonnerteInntekterRequest inntekter for perioden ${request.maanedFom} - ${request.maanedTom} " +
                                "ga følgende respons for ${request.ident.identifikator}: ${tilJson(restResponse.body)}",
                        )

                        is RestResponse.Failure -> {
                            if (abonnerteInntekterRequest) {
                                // Logger som warning i stedet for feil ved 400 - Bad Request (inntektsabonnement finnes ikke for personen)
                                // og 423 Locked (inntektsabonnementet er ikke aktivt ennå)
                                if (restResponse.statusCode == HttpStatus.BAD_REQUEST ||
                                    restResponse.statusCode == HttpStatus.LOCKED
                                ) {
                                    InntektskomponentenService.LOGGER.warn(
                                        "Mangler abonnement for henting av inntekter fra Inntektskomponenten. " +
                                            "Statuskode ${restResponse.statusCode.value()}/${restResponse.message}",
                                    )
                                    SECURE_LOGGER.warn(
                                        "Mangler abonnement for henting av inntekter fra Inntektskomponenten for ${request.ident.identifikator} " +
                                            "for perioden ${request.maanedFom} - ${request.maanedTom}." +
                                            " Prøver å hente inntekter uten abonnement. ${restResponse.message}",
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

                    grunnlagConsumer.logResponse(logger = SECURE_LOGGER, restResponse = restResponse)

                    restResponse
                }
            }
        }, { throwable ->
            håndtereFeilMedKontekst(throwable, request, abonnerteInntekterRequest)
        })

    private fun håndtereFeilMedKontekst(
        throwable: Throwable,
        request: HentInntektListeRequest,
        abonnerteInntekterRequest: Boolean,
    ): RestResponse<HentInntektListeResponse> {
        val ident = request.ident.identifikator
        val periode = "${request.maanedFom} - ${request.maanedTom}"
        val isTimeout = throwable is TimeoutException ||
            throwable is CancellationException ||
            (throwable is CompletionException && throwable.cause is TimeoutException)

        if (Thread.currentThread().isInterrupted) {
            Thread.interrupted()
            SECURE_LOGGER.warn("Tråd ble avbrutt under kall til inntektskomponenten for: $ident")
        }

        if (isTimeout) {
            InntektskomponentenService.LOGGER.error(
                "Timeout ved kall til inntektskomponenten (${if (abonnerteInntekterRequest) "abonnerte" else "vanlige"} inntekter)",
                throwable,
            )
            SECURE_LOGGER.error(
                "Timeout ved henting av ${if (abonnerteInntekterRequest) "abonnerte" else "vanlige"} inntekter for $ident i perioden $periode",
                throwable,
            )
        } else {
            SECURE_LOGGER.error("Circuitbreaker aktivert for inntektskomponenten: ${throwable.message}", throwable)
        }

        return RestResponse.Failure(
            "Inntektskomponenten svarer ikke" + if (isTimeout) " (timeout)" else "",
            HttpStatus.SERVICE_UNAVAILABLE,
            ServiceUnavailableException(throwable.message),
        )
    }
}
