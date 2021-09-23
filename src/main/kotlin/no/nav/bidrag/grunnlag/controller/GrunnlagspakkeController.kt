package no.nav.bidrag.grunnlag.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.bidrag.grunnlag.ISSUER
import no.nav.bidrag.grunnlag.api.FinnGrunnlagResponse
import no.nav.bidrag.grunnlag.api.NyGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.api.NyGrunnlagspakkeResponse
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

  fun opprettNyGrunnlagspakke(@RequestBody request: NyGrunnlagspakkeRequest): ResponseEntity<NyGrunnlagspakkeResponse>? {
    val grunnlagspakkeOpprettet = grunnlagspakkeService.opprettGrunnlagspakke(request)
    LOGGER.info("Følgende grunnlagspakke er opprettet: $grunnlagspakkeOpprettet")
    return ResponseEntity(grunnlagspakkeOpprettet, HttpStatus.OK)

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

  fun finnGrunnlagspakke(@PathVariable grunnlagspakkeId: Int): ResponseEntity<FinnGrunnlagResponse>? {
    val grunnlagspakkeFunnet = grunnlagspakkeService.finnGrunnlag(grunnlagspakkeId)
    LOGGER.info("Følgende grunnlagspakke ble funnet: $grunnlagspakkeFunnet")
    return ResponseEntity(grunnlagspakkeFunnet, HttpStatus.OK)

  }


  companion object {
    const val GRUNNLAGSPAKKE_NY = "/grunnlagspakke/ny"
    const val GRUNNLAGSPAKKE_HENT = "/grunnlagspakke"
    private val LOGGER = LoggerFactory.getLogger(GrunnlagspakkeController::class.java)

  }

}