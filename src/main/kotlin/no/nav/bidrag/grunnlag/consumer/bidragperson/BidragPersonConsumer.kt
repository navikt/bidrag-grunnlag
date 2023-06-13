package no.nav.bidrag.grunnlag.consumer.bidragperson

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.domain.ident.PersonIdent
import no.nav.bidrag.domain.number.Fødselsår
import no.nav.bidrag.domain.string.FulltNavn
import no.nav.bidrag.grunnlag.consumer.GrunnlagsConsumer
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.exception.tryExchange
import no.nav.bidrag.transport.person.ForelderBarnRelasjonDto
import no.nav.bidrag.transport.person.HusstandsmedlemmerDto
import no.nav.bidrag.transport.person.NavnFødselDødDto
import no.nav.bidrag.transport.person.PersonRequest
import no.nav.bidrag.transport.person.Sivilstandshistorikk
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod

private const val BIDRAGPERSON_CONTEXT_FOEDSEL_DOED = "/bidrag-person/navnfoedseldoed"
private const val BIDRAGPERSON_CONTEXT_FORELDER_BARN_RELASJON = "/bidrag-person/forelderbarnrelasjon"
private const val BIDRAGPERSON_CONTEXT_HUSSTANDSMEDLEMMER = "/bidrag-person/husstandsmedlemmer"
private const val BIDRAGPERSON_CONTEXT_SIVILSTAND = "/bidrag-person/sivilstand"

open class BidragPersonConsumer(private val restTemplate: HttpHeaderRestTemplate) :
    GrunnlagsConsumer() {

    companion object {
        @JvmStatic
        val logger: Logger = LoggerFactory.getLogger(BidragPersonConsumer::class.java)
    }

    open fun hentNavnFoedselOgDoed(personIdent: PersonIdent): RestResponse<NavnFødselDødDto> {
        logger.info("Kaller bidrag-person som igjen henter info om fødselsdato og eventuelt død fra PDL")

        val restResponse = restTemplate.tryExchange(
            BIDRAGPERSON_CONTEXT_FOEDSEL_DOED,
            HttpMethod.POST,
            initHttpEntity(PersonRequest(personIdent)),
            NavnFødselDødDto::class.java,
            NavnFødselDødDto(FulltNavn(""), null, Fødselsår(0), null)
        )

        logResponse(logger, restResponse)

        return restResponse
    }

    open fun hentForelderBarnRelasjon(personIdent: PersonIdent): RestResponse<ForelderBarnRelasjonDto> {
        logger.info("Kaller bidrag-person som igjen henter forelderbarnrelasjoner for angitt person fra PDL")

        val restResponse = restTemplate.tryExchange(
            BIDRAGPERSON_CONTEXT_FORELDER_BARN_RELASJON,
            HttpMethod.POST,
            initHttpEntity(PersonRequest(personIdent)),
            ForelderBarnRelasjonDto::class.java,
            ForelderBarnRelasjonDto(emptyList())
        )

        logResponse(logger, restResponse)

        return restResponse
    }

    open fun hentHusstandsmedlemmer(personIdent: PersonIdent): RestResponse<HusstandsmedlemmerDto> {
        logger.info(
            "Kaller bidrag-person som igjen henter info om en persons bostedsadresser " +
                "og personer som har bodd på samme adresse på samme tid fra PDL"
        )

        val restResponse = restTemplate.tryExchange(
            BIDRAGPERSON_CONTEXT_HUSSTANDSMEDLEMMER,
            HttpMethod.POST,
            initHttpEntity(PersonRequest(personIdent)),
            HusstandsmedlemmerDto::class.java,
            HusstandsmedlemmerDto(emptyList())
        )

        logResponse(logger, restResponse)

        return restResponse
    }

    open fun hentSivilstand(personIdent: PersonIdent): RestResponse<Sivilstandshistorikk> {
        logger.info("Kaller bidrag-person som igjen kaller PDL for å finne en persons sivilstand")

        val restResponse = restTemplate.tryExchange(
            BIDRAGPERSON_CONTEXT_SIVILSTAND,
            HttpMethod.POST,
            initHttpEntity(PersonRequest(personIdent)),
            Sivilstandshistorikk::class.java,
            Sivilstandshistorikk(emptyList())
        )

        logResponse(logger, restResponse)

        return restResponse
    }
}
