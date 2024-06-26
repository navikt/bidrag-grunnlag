package no.nav.bidrag.grunnlag.consumer.familiekssak

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.consumer.GrunnlagsConsumer
import no.nav.bidrag.grunnlag.consumer.familiekssak.api.BisysDto
import no.nav.bidrag.grunnlag.consumer.familiekssak.api.BisysResponsDto
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.exception.tryExchange
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import java.net.SocketTimeoutException

private const val FAMILIEKSSAK_CONTEXT = "/api/bisys/hent-utbetalingsinfo"

open class FamilieKsSakConsumer(private val restTemplate: HttpHeaderRestTemplate) :
    GrunnlagsConsumer() {

    companion object {
        @JvmStatic
        val logger: Logger = LoggerFactory.getLogger(FamilieKsSakConsumer::class.java)
    }

    @Retryable(value = [Exception::class], exclude = [SocketTimeoutException::class], backoff = Backoff(delay = 500))
    open fun hentKontantstotte(request: BisysDto): RestResponse<BisysResponsDto> {
        val restResponse = restTemplate.tryExchange(
            FAMILIEKSSAK_CONTEXT,
            HttpMethod.POST,
            initHttpEntity(request),
            BisysResponsDto::class.java,
            BisysResponsDto(emptyList(), emptyList()),
        )

        logResponse(SECURE_LOGGER, restResponse)

        return restResponse
    }
}
