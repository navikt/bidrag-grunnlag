package no.nav.bidrag.grunnlag.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.bidrag.gcp.proxy.consumer.inntektskomponenten.response.HentInntektListeResponse
import no.nav.bidrag.grunnlag.ISSUER
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.BidragGcpProxyConsumer
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.HentInntektRequest
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.skatt.HentSkattegrunnlagRequest
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.skatt.HentSkattegrunnlagResponse
import no.nav.bidrag.grunnlag.consumer.familiebasak.FamilieBaSakConsumer
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.FamilieBaSakRequest
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.FamilieBaSakResponse
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@ProtectedWithClaims(issuer = ISSUER)
class IntegrasjonsController(private val bidragGcpProxyConsumer: BidragGcpProxyConsumer, private val familieBaSakConsumer: FamilieBaSakConsumer) {


  @PostMapping(HENT_INNTEKT)
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Henter inntekt")
  fun hentInntekt(@RequestBody hentInntektRequest: HentInntektRequest): ResponseEntity<HentInntektListeResponse> {
    return handleRestResponse(bidragGcpProxyConsumer.hentInntekt(hentInntektRequest))
  }

  @PostMapping(HENT_SKATTEGRUNNLAG)
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Henter skattegrunnlag")
  fun hentSkattegrunnlag(@RequestBody hentSkattegrunnlagRequest: HentSkattegrunnlagRequest): ResponseEntity<HentSkattegrunnlagResponse> {
    return handleRestResponse(bidragGcpProxyConsumer.hentSkattegrunnlag(hentSkattegrunnlagRequest))
  }

  @PostMapping(HENT_FAMILIEBASAK)
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Henter familie ba sak")
  fun hentFamilieBaSak(@RequestBody familieBaSakRequest: FamilieBaSakRequest): ResponseEntity<FamilieBaSakResponse> {
    return handleRestResponse(familieBaSakConsumer.hentFamilieBaSak(familieBaSakRequest))
  }

  private fun <T> handleRestResponse(restResponse: RestResponse<T>): ResponseEntity<T> {
    return when (restResponse) {
      is RestResponse.Success -> ResponseEntity(restResponse.body, HttpStatus.OK)
      is RestResponse.Failure -> throw ResponseStatusException(restResponse.statusCode, restResponse.message)
    }
  }

  companion object {
    const val HENT_INNTEKT = "/integrasjoner/inntekt"
    const val HENT_SKATTEGRUNNLAG = "/integrasjoner/skattegrunnlag"
    const val HENT_FAMILIEBASAK = "/integrasjoner/familiebasak"
  }
}