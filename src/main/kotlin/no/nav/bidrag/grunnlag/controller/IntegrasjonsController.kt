package no.nav.bidrag.grunnlag.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.bidrag.grunnlag.ISSUER
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.BidragGcpProxyConsumer
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.ainntekt.HentInntektRequest
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.barnetillegg.HentBarnetilleggPensjonRequest
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.barnetillegg.HentBarnetilleggPensjonResponse
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.skatt.HentSkattegrunnlagRequest
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.skatt.HentSkattegrunnlagResponse
import no.nav.bidrag.grunnlag.consumer.bidragperson.BidragPersonConsumer
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.FoedselOgDoedDto
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.ForelderBarnRelasjonDto
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.HusstandsmedlemmerDto
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.HusstandsmedlemmerRequest
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.SivilstandDto
import no.nav.bidrag.grunnlag.consumer.bidragperson.api.SivilstandRequest
import no.nav.bidrag.grunnlag.consumer.familiebasak.FamilieBaSakConsumer
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.FamilieBaSakRequest
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.FamilieBaSakResponse
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tjenester.aordningen.inntektsinformasjon.response.HentInntektListeResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@ProtectedWithClaims(issuer = ISSUER)
class IntegrasjonsController(
  private val bidragGcpProxyConsumer: BidragGcpProxyConsumer,
  private val familieBaSakConsumer: FamilieBaSakConsumer,
  private val bidragPersonConsumer: BidragPersonConsumer) {


  @PostMapping(HENT_AINNTEKT)
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Henter A-inntekt")
  fun hentAinntekt(@RequestBody hentAinntektRequest: HentInntektRequest): ResponseEntity<HentInntektListeResponse> {
    return handleRestResponse(bidragGcpProxyConsumer.hentAinntekt(hentAinntektRequest))
  }

  @PostMapping(HENT_SKATTEGRUNNLAG)
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Henter skattegrunnlag")
  fun hentSkattegrunnlag(@RequestBody hentSkattegrunnlagRequest: HentSkattegrunnlagRequest): ResponseEntity<HentSkattegrunnlagResponse> {
    return handleRestResponse(bidragGcpProxyConsumer.hentSkattegrunnlag(hentSkattegrunnlagRequest))
  }

  @PostMapping(HENT_BARNETILLEGG_PENSJON)
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Henter barnetillegg fra pensjon")
  fun hentBarnetilleggPensjon(@RequestBody hentBarnetilleggPensjonRequest: HentBarnetilleggPensjonRequest): ResponseEntity<HentBarnetilleggPensjonResponse> {
    return handleRestResponse(bidragGcpProxyConsumer.hentBarnetilleggPensjon(hentBarnetilleggPensjonRequest))
  }

  @PostMapping(HENT_FAMILIEBASAK)
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Henter utvidet barnetrygd og småbarnstillegg")
  fun hentFamilieBaSak(@RequestBody familieBaSakRequest: FamilieBaSakRequest): ResponseEntity<FamilieBaSakResponse> {
    return handleRestResponse(familieBaSakConsumer.hentFamilieBaSak(familieBaSakRequest))
  }

  @PostMapping(HENT_FOEDSEL_DOED)
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Kaller bidrag-person som igjen henter info om fødselsdato og eventuell død fra PDL")
  fun hentFoedselOgDoed(@RequestBody bidragPersonRequest: String): ResponseEntity<FoedselOgDoedDto> {
    return handleRestResponse(bidragPersonConsumer.hentFoedselOgDoed(bidragPersonRequest))
  }

  @PostMapping(HENT_FORELDER_BARN_RELASJON)
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Kaller bidrag-person som igjen henter forelderbarnrelasjoner for angitt person fra PDL")
  fun hentForelderbarnrelasjon(@RequestBody bidragPersonRequest: String): ResponseEntity<ForelderBarnRelasjonDto> {
    return handleRestResponse(bidragPersonConsumer.hentForelderbarnrelasjon(bidragPersonRequest))
  }

  @PostMapping(HENT_HUSSTANDSMEDLEMMER)
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Kaller bidrag-person som igjen henter info om en persons bostedsadresser og personer som har bodd på samme adresse på samme tid fra PDL")
  fun hentHusstandsmedlemmer(@RequestBody husstandsmedlemmerRequest: HusstandsmedlemmerRequest): ResponseEntity<HusstandsmedlemmerDto> {
    return handleRestResponse(bidragPersonConsumer.hentHusstandsmedlemmer(husstandsmedlemmerRequest))
  }

  @PostMapping(HENT_SIVILSTAND)
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Kaller bidrag-person som igjen kaller PDL for å finne en persons sivilstand")
  fun hentSivilstand(@RequestBody sivilstandRequest: SivilstandRequest): ResponseEntity<SivilstandDto> {
    return handleRestResponse(bidragPersonConsumer.hentSivilstand(sivilstandRequest))
  }





  private fun <T> handleRestResponse(restResponse: RestResponse<T>): ResponseEntity<T> {
    return when (restResponse) {
      is RestResponse.Success -> ResponseEntity(restResponse.body, HttpStatus.OK)
      is RestResponse.Failure -> throw ResponseStatusException(restResponse.statusCode, restResponse.message)
    }
  }

  companion object {
    const val HENT_AINNTEKT = "/integrasjoner/ainntekt"
    const val HENT_SKATTEGRUNNLAG = "/integrasjoner/skattegrunnlag"
    const val HENT_BARNETILLEGG_PENSJON = "/integrasjoner/barnetillegg"
    const val HENT_FAMILIEBASAK = "/integrasjoner/familiebasak"
    const val HENT_FORELDER_BARN_RELASJON = "/integrasjoner/foedselogdoed"
    const val HENT_FOEDSEL_DOED = "/integrasjoner/forelderbarnrelasjon"
    const val HENT_HUSSTANDSMEDLEMMER = "/integrasjoner/husstandsmedlemmer"
    const val HENT_SIVILSTAND = "/integrasjoner/sivilstand"
  }
}