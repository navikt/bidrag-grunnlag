package no.nav.bidrag.grunnlag.consumer.bidragperson

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.grunnlag.consumer.GrunnlagsConsumer
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.NavnFoedselDoedResponseDto
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.ForelderBarnRelasjonDto
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.HusstandsmedlemmerDto
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.SivilstandDto
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.exception.tryExchange
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod

private const val BIDRAGPERSON_CONTEXT_FOEDSEL_DOED = "/bidrag-person/foedselogdoed"
private const val BIDRAGPERSON_CONTEXT_FORELDER_BARN_RELASJON = "/bidrag-person/forelderbarnrelasjon"
private const val BIDRAGPERSON_CONTEXT_HUSSTANDSMEDLEMMER = "/bidrag-person/husstandsmedlemmer"
private const val BIDRAGPERSON_CONTEXT_SIVILSTAND = "/bidrag-person/sivilstand"

open class BidragPersonConsumer(private val restTemplate: HttpHeaderRestTemplate) :
  GrunnlagsConsumer() {

  companion object {
    @JvmStatic
    val logger: Logger = LoggerFactory.getLogger(BidragPersonConsumer::class.java)
  }

  open fun hentNavnFoedselOgDoed(request: String): RestResponse<NavnFoedselDoedResponseDto> {
    logger.info("Kaller bidrag-person som igjen henter info om fødselsdato og eventuelt død fra PDL")

    val restResponse = restTemplate.tryExchange(
      BIDRAGPERSON_CONTEXT_FOEDSEL_DOED,
      HttpMethod.POST,
      initHttpEntity(request),
      NavnFoedselDoedResponseDto::class.java,
      NavnFoedselDoedResponseDto("", null,0, null)
    )

    logResponse(logger, restResponse)

    return restResponse
  }

  open fun hentForelderBarnRelasjon(request: String): RestResponse<ForelderBarnRelasjonDto> {
    logger.info("Kaller bidrag-person som igjen henter forelderbarnrelasjoner for angitt person fra PDL")

    val restResponse = restTemplate.tryExchange(
      BIDRAGPERSON_CONTEXT_FORELDER_BARN_RELASJON,
      HttpMethod.POST,
      initHttpEntity(request),
      ForelderBarnRelasjonDto::class.java,
      ForelderBarnRelasjonDto(emptyList())
    )

    logResponse(logger, restResponse)

    return restResponse
  }

  open fun hentHusstandsmedlemmer(request: String): RestResponse<HusstandsmedlemmerDto> {
    logger.info("Kaller bidrag-person som igjen henter info om en persons bostedsadresser og personer som har bodd på samme adresse på samme tid fra PDL")

    val restResponse = restTemplate.tryExchange(
      BIDRAGPERSON_CONTEXT_HUSSTANDSMEDLEMMER,
      HttpMethod.POST,
      initHttpEntity(request),
      HusstandsmedlemmerDto::class.java,
      HusstandsmedlemmerDto(emptyList())
    )

    logResponse(logger, restResponse)

    return restResponse
  }

  open fun hentSivilstand(request: String): RestResponse<SivilstandDto> {
    logger.info("Kaller bidrag-person som igjen kaller PDL for å finne en persons sivilstand")

    val restResponse = restTemplate.tryExchange(
      BIDRAGPERSON_CONTEXT_SIVILSTAND,
      HttpMethod.POST,
      initHttpEntity(request),
      SivilstandDto::class.java,
      SivilstandDto(emptyList())
    )

    logResponse(logger, restResponse)

    return restResponse
  }
}
