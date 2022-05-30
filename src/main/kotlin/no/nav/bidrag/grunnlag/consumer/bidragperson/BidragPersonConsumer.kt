package no.nav.bidrag.grunnlag.consumer.bidragperson

import no.nav.bidrag.behandling.felles.dto.grunnlag.PersonDto
import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.grunnlag.consumer.GrunnlagsConsumer
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.NavnFoedselDoedResponseDto
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.ForelderBarnRelasjonResponseDto
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.ForelderBarnRequest
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.HusstandsmedlemmerResponseDto
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.HusstandsmedlemmerRequest
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.SivilstandRequest
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.SivilstandResponseDto
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.exception.tryExchange
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import java.time.LocalDate
import java.time.LocalDateTime

private const val BIDRAGPERSON_CONTEXT_FOEDSEL_DOED = "/bidrag-person/foedselogdoed"
private const val BIDRAGPERSON_CONTEXT_FORELDER_BARN_RELASJON = "/bidrag-person/forelderbarnrelasjon"
private const val BIDRAGPERSON_CONTEXT_HUSSTANDSMEDLEMMER = "/bidrag-person/husstandsmedlemmer"
private const val BIDRAGPERSON_CONTEXT_SIVILSTAND = "/bidrag-person/sivilstand"
private const val BIDRAGPERSON_CONTEXT_PERSON = "/bidrag-person/informasjon"

open class BidragPersonConsumer(private val restTemplate: HttpHeaderRestTemplate) :
  GrunnlagsConsumer() {

  companion object {
    @JvmStatic
    val logger: Logger = LoggerFactory.getLogger(BidragPersonConsumer::class.java)
  }

  open fun hentFoedselOgDoed(request: String): RestResponse<NavnFoedselDoedResponseDto> {
    logger.info("Kaller bidrag-person som igjen henter info om fødselsdato og eventuelt død fra PDL")

    val restResponse = restTemplate.tryExchange(
      BIDRAGPERSON_CONTEXT_FOEDSEL_DOED,
      HttpMethod.POST,
      initHttpEntity(request),
      NavnFoedselDoedResponseDto::class.java,
      NavnFoedselDoedResponseDto(null, null,0, null)
    )

    logResponse(logger, restResponse)

    return restResponse
  }

  open fun hentForelderBarnRelasjon(request: ForelderBarnRequest): RestResponse<ForelderBarnRelasjonResponseDto> {
    logger.info("Kaller bidrag-person som igjen henter forelderbarnrelasjoner for angitt person fra PDL")

    val restResponse = restTemplate.tryExchange(
      BIDRAGPERSON_CONTEXT_FORELDER_BARN_RELASJON,
      HttpMethod.POST,
      initHttpEntity(request),
      ForelderBarnRelasjonResponseDto::class.java,
      ForelderBarnRelasjonResponseDto(emptyList())
    )

    logResponse(logger, restResponse)

    return restResponse
  }

  open fun hentHusstandsmedlemmer(request: HusstandsmedlemmerRequest): RestResponse<HusstandsmedlemmerResponseDto> {
    logger.info("Kaller bidrag-person som igjen henter info om en persons bostedsadresser og personer som har bodd på samme adresse på samme tid fra PDL")

    val restResponse = restTemplate.tryExchange(
      BIDRAGPERSON_CONTEXT_HUSSTANDSMEDLEMMER,
      HttpMethod.POST,
      initHttpEntity(request),
      HusstandsmedlemmerResponseDto::class.java,
      HusstandsmedlemmerResponseDto(emptyList())
    )

    logResponse(logger, restResponse)

    return restResponse
  }

  open fun hentSivilstand(request: SivilstandRequest): RestResponse<SivilstandResponseDto> {
    logger.info("Kaller bidrag-person som igjen kaller PDL for å finne en persons sivilstand")

    val restResponse = restTemplate.tryExchange(
      BIDRAGPERSON_CONTEXT_SIVILSTAND,
      HttpMethod.POST,
      initHttpEntity(request),
      SivilstandResponseDto::class.java,
      SivilstandResponseDto(emptyList())
    )

    logResponse(logger, restResponse)

    return restResponse
  }

  open fun hentPerson(request: String): RestResponse<PersonDto> {
    logger.info("Kaller bidrag-person som igjen kaller PDL for å finne informasjon om en person")

    val restResponse = restTemplate.tryExchange(
      BIDRAGPERSON_CONTEXT_PERSON,
      HttpMethod.POST,
      initHttpEntity(request),
      PersonDto::class.java,
      PersonDto("0",null, null, null, null, LocalDateTime.now())
    )

    logResponse(logger, restResponse)

    return restResponse
  }


}
