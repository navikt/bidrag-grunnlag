package no.nav.bidrag.grunnlag.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.bidrag.grunnlag.ISSUER
import no.nav.bidrag.grunnlag.api.grunnlagspakke.HentKomplettGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.api.grunnlagspakke.LukkGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OppdaterGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OppdaterGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OpprettGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OpprettGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.service.GrunnlagspakkeService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = ISSUER)
class GrunnlagspakkeController(private val grunnlagspakkeService: GrunnlagspakkeService) {

  @PostMapping(GRUNNLAGSPAKKE_NY)
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Oppretter grunnlagspakke")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Grunnlagspakke opprettet"),
      ApiResponse(responseCode = "400", description = "Feil opplysinger oppgitt"),
      ApiResponse(responseCode = "401", description = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      ApiResponse(responseCode = "500", description = "Serverfeil"),
      ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig")
    ]
  )

  fun opprettNyGrunnlagspakke(@RequestBody request: OpprettGrunnlagspakkeRequest): ResponseEntity<OpprettGrunnlagspakkeResponse>? {
    val grunnlagspakkeOpprettet = grunnlagspakkeService.opprettGrunnlagspakke(request)
    LOGGER.info("Følgende grunnlagspakke er opprettet: $grunnlagspakkeOpprettet")
    return ResponseEntity(grunnlagspakkeOpprettet, HttpStatus.OK)

  }


  @PostMapping(GRUNNLAGSPAKKE_OPPDATER)
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Trigger innhenting av grunnlag for grunnlagspakke")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Grunnlagspakke oppdatert"),
      ApiResponse(responseCode = "401", description = "Manglende eller utløpt id-token"),
      ApiResponse(responseCode = "403", description = "Saksbehandler mangler tilgang til å lese data for aktuell grunnlagspakke"),
      ApiResponse(responseCode = "404", description = "grunnlagspakke ikke funnet"),
      ApiResponse(responseCode = "500", description = "Serverfeil"),
      ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig")
    ]
  )
  fun oppdaterGrunnlagspakke(@RequestBody request: OppdaterGrunnlagspakkeRequest): ResponseEntity<OppdaterGrunnlagspakkeResponse>? {
    val grunnlagspakkeOppdatert = grunnlagspakkeService.oppdaterGrunnlagspakke(request)
    LOGGER.info("Følgende grunnlagspakke ble oppdatert: ${request.grunnlagspakkeId}")
    return ResponseEntity(grunnlagspakkeOppdatert, HttpStatus.OK)
  }


  @GetMapping("$GRUNNLAGSPAKKE_HENT/{grunnlagspakkeId}")
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Finn alle data for en grunnlagspakke")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Grunnlagspakke funnet"),
      ApiResponse(responseCode = "401", description = "Manglende eller utløpt id-token"),
      ApiResponse(responseCode = "403", description = "Saksbehandler mangler tilgang til å lese data for aktuell grunnlagspakke"),
      ApiResponse(responseCode = "404", description = "grunnlagspakke ikke funnet"),
      ApiResponse(responseCode = "500", description = "Serverfeil"),
      ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig")
    ]
  )

  fun hentGrunnlagspakke(@PathVariable grunnlagspakkeId: Int): ResponseEntity<HentKomplettGrunnlagspakkeResponse>? {
    val grunnlagspakkeFunnet = grunnlagspakkeService.hentKomplettGrunnlagspakke(grunnlagspakkeId)
    LOGGER.info("Følgende grunnlagspakke ble funnet: $grunnlagspakkeFunnet")
    return ResponseEntity(grunnlagspakkeFunnet, HttpStatus.OK)

  }


  @PostMapping(GRUNNLAGSPAKKE_LUKK)
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Setter gyldigTil-dato = dato i input for angitt grunnlagspakke")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Grunnlagspakke oppdatert"),
      ApiResponse(responseCode = "401", description = "Manglende eller utløpt id-token"),
      ApiResponse(responseCode = "403", description = "Saksbehandler mangler tilgang til å lese data for aktuell grunnlagspakke"),
      ApiResponse(responseCode = "404", description = "grunnlagspakke ikke funnet"),
      ApiResponse(responseCode = "500", description = "Serverfeil"),
      ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig")
    ]
  )

  fun lukkGrunnlagspakke(@RequestBody request: LukkGrunnlagspakkeRequest): ResponseEntity<Int>? {
    val oppdatertgrunnlagspakke = grunnlagspakkeService.lukkGrunnlagspakke(request)
    LOGGER.info("Følgende grunnlagspakke ble oppdatert med gyldigTil-dato: $oppdatertgrunnlagspakke")
    return ResponseEntity(request.grunnlagspakkeId, HttpStatus.OK)

  }

  companion object {
    const val GRUNNLAGSPAKKE_NY = "/grunnlagspakke/ny"
    const val GRUNNLAGSPAKKE_OPPDATER = "/grunnlagspakke/oppdater"
    const val GRUNNLAGSPAKKE_HENT = "/grunnlagspakke/hent"
    const val GRUNNLAGSPAKKE_LUKK = "/grunnlagspakke/lukk"
    private val LOGGER = LoggerFactory.getLogger(GrunnlagspakkeController::class.java)
  }
}
