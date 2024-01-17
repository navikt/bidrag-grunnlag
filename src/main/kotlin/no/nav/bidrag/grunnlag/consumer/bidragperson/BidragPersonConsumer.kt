package no.nav.bidrag.grunnlag.consumer.bidragperson

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.consumer.GrunnlagsConsumer
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.exception.tryExchange
import no.nav.bidrag.transport.person.ForelderBarnRelasjonDto
import no.nav.bidrag.transport.person.HentePersonidenterRequest
import no.nav.bidrag.transport.person.HusstandsmedlemmerDto
import no.nav.bidrag.transport.person.Identgruppe
import no.nav.bidrag.transport.person.NavnFødselDødDto
import no.nav.bidrag.transport.person.PersonRequest
import no.nav.bidrag.transport.person.PersonidentDto
import no.nav.bidrag.transport.person.SivilstandPdlHistorikkDto
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable

private const val BIDRAGPERSON_CONTEXT_FOEDSEL_DOED = "/bidrag-person/navnfoedseldoed"
private const val BIDRAGPERSON_CONTEXT_FORELDER_BARN_RELASJON = "/bidrag-person/forelderbarnrelasjon"
private const val BIDRAGPERSON_CONTEXT_HUSSTANDSMEDLEMMER = "/bidrag-person/husstandsmedlemmer"
private const val BIDRAGPERSON_CONTEXT_SIVILSTAND = "/bidrag-person/sivilstand"
private const val BIDRAGPERSON_CONTEXT_PERSONIDENTER = "/bidrag-person/personidenter"

open class BidragPersonConsumer(private val restTemplate: HttpHeaderRestTemplate) :
    GrunnlagsConsumer() {

    companion object {
        @JvmStatic
        val logger: Logger = LoggerFactory.getLogger(BidragPersonConsumer::class.java)
    }

    @Retryable(value = [Exception::class], backoff = Backoff(delay = 500))
    open fun hentNavnFoedselOgDoed(personident: Personident): RestResponse<NavnFødselDødDto> {
        logger.info("Kaller bidrag-person som igjen henter info om fødselsdato og eventuelt død fra PDL")

        val restResponse = restTemplate.tryExchange(
            BIDRAGPERSON_CONTEXT_FOEDSEL_DOED,
            HttpMethod.POST,
            initHttpEntity(PersonRequest(personident)),
            NavnFødselDødDto::class.java,
            NavnFødselDødDto(navn = "", fødselsdato = null, fødselsår = 0, dødsdato = null),
        )

        logResponse(SECURE_LOGGER, restResponse)

        return restResponse
    }

    @Retryable(value = [Exception::class], backoff = Backoff(delay = 500))
    open fun hentForelderBarnRelasjon(personident: Personident): RestResponse<ForelderBarnRelasjonDto> {
        logger.info("Kaller bidrag-person som igjen henter forelderbarnrelasjoner for angitt person fra PDL")

        val restResponse = restTemplate.tryExchange(
            BIDRAGPERSON_CONTEXT_FORELDER_BARN_RELASJON,
            HttpMethod.POST,
            initHttpEntity(PersonRequest(personident)),
            ForelderBarnRelasjonDto::class.java,
            ForelderBarnRelasjonDto(emptyList()),
        )

        logResponse(SECURE_LOGGER, restResponse)

        return restResponse
    }

    @Retryable(value = [Exception::class], backoff = Backoff(delay = 500))
    open fun hentHusstandsmedlemmer(personident: Personident): RestResponse<HusstandsmedlemmerDto> {
        logger.info(
            "Kaller bidrag-person som igjen henter info om en persons bostedsadresser " +
                "og personer som har bodd på samme adresse på samme tid fra PDL",
        )

        val restResponse = restTemplate.tryExchange(
            BIDRAGPERSON_CONTEXT_HUSSTANDSMEDLEMMER,
            HttpMethod.POST,
            initHttpEntity(PersonRequest(personident)),
            HusstandsmedlemmerDto::class.java,
            HusstandsmedlemmerDto(emptyList()),
        )

        logResponse(SECURE_LOGGER, restResponse)

        return restResponse
    }

    @Retryable(value = [Exception::class], backoff = Backoff(delay = 500))
    open fun hentSivilstand(personident: Personident): RestResponse<SivilstandPdlHistorikkDto> {
        logger.info("Kaller bidrag-person som igjen kaller PDL for å finne en persons sivilstand")

        val restResponse = restTemplate.tryExchange(
            BIDRAGPERSON_CONTEXT_SIVILSTAND,
            HttpMethod.POST,
            initHttpEntity(PersonRequest(personident)),
            SivilstandPdlHistorikkDto::class.java,
            SivilstandPdlHistorikkDto(emptyList()),
        )

        logResponse(SECURE_LOGGER, restResponse)

        return restResponse
    }

    @Retryable(value = [Exception::class], backoff = Backoff(delay = 500))
    open fun hentPersonidenter(personident: Personident, inkludereHistoriske: Boolean): RestResponse<List<PersonidentDto>> {
        logger.info("Kaller bidrag-person som igjen kaller PDL for å finne en persons historiske identer")

        val responseType = object : ParameterizedTypeReference<List<PersonidentDto>>() {}

        val restResponse = restTemplate.tryExchange(
            BIDRAGPERSON_CONTEXT_PERSONIDENTER,
            HttpMethod.POST,
            initHttpEntity(HentePersonidenterRequest(personident.verdi, setOf(Identgruppe.FOLKEREGISTERIDENT), inkludereHistoriske)),
            responseType,
            emptyList(),
        )

        logResponse(SECURE_LOGGER, restResponse)

        return restResponse
    }
}
