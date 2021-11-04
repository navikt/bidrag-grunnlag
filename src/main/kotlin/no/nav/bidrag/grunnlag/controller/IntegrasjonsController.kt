package no.nav.bidrag.grunnlag.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.bidrag.gcp.proxy.consumer.inntektskomponenten.response.HentInntektListeResponse
import no.nav.bidrag.grunnlag.ISSUER
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.BidragGcpProxyConsumer
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.HentInntektRequest
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.ReceivedResponse
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
    return when (val receivedResponseInntektListe = bidragGcpProxyConsumer.hentInntekt(hentInntektRequest)) {
      is ReceivedResponse.Success -> ResponseEntity(receivedResponseInntektListe.body, HttpStatus.OK)
      is ReceivedResponse.Failure -> ResponseEntity(receivedResponseInntektListe.body, receivedResponseInntektListe.statusCode)
    }
  }

  @PostMapping(HENT_SKATTEGRUNNLAG)
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Henter skattegrunnlag")
  fun hentSkattegrunnlag(@RequestBody hentSkattegrunnlagRequest: HentSkattegrunnlagRequest): ResponseEntity<HentSkattegrunnlagResponse> {
    return when (val restResponseSkattegrunnlag = bidragGcpProxyConsumer.hentSkattegrunnlag(hentSkattegrunnlagRequest)) {
      is RestResponse.Success -> ResponseEntity(restResponseSkattegrunnlag.body, HttpStatus.OK)
      is RestResponse.Failure -> throw ResponseStatusException(restResponseSkattegrunnlag.statusCode, restResponseSkattegrunnlag.message)
    }
  }

  @PostMapping(HENT_FAMILIEBASAK)
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Henter familie ba sak")
  fun hentFamilieBaSak(@RequestBody familieBaSakRequest: FamilieBaSakRequest): ResponseEntity<FamilieBaSakResponse> {
    return when(val receivedResponseFamilieBaSak = familieBaSakConsumer.hentFamilieBaSak(familieBaSakRequest)) {
      is ReceivedResponse.Success -> ResponseEntity(receivedResponseFamilieBaSak.body, HttpStatus.OK)
      is ReceivedResponse.Failure -> ResponseEntity(receivedResponseFamilieBaSak.body, receivedResponseFamilieBaSak.statusCode)
    }
  }
  companion object {
    const val HENT_INNTEKT = "/integrasjoner/inntekt"
    const val HENT_SKATTEGRUNNLAG = "/integrasjoner/skattegrunnlag"
    const val HENT_FAMILIEBASAK = "/integrasjoner/familiebasak"
  }
}