package no.nav.bidrag.grunnlag.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import no.nav.bidrag.behandling.felles.dto.grunnlag.HentGrunnlagspakkeDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.OppdaterGrunnlagspakkeDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.OppdaterGrunnlagspakkeRequestDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.OpprettGrunnlagspakkeRequestDto
import no.nav.bidrag.grunnlag.ISSUER
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
    fun opprettNyGrunnlagspakke(
        @Valid @RequestBody
        request: OpprettGrunnlagspakkeRequestDto
    ): ResponseEntity<Int>? {
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
            ApiResponse(responseCode = "404", description = "Grunnlagspakke ikke funnet"),
            ApiResponse(responseCode = "500", description = "Serverfeil"),
            ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig")
        ]
    )
    fun oppdaterGrunnlagspakke(
        @PathVariable @NotNull
        grunnlagspakkeId: Int,
        @Valid @RequestBody
        request: OppdaterGrunnlagspakkeRequestDto
    ):
        ResponseEntity<OppdaterGrunnlagspakkeDto>? {
        val grunnlagspakkeOppdatert = grunnlagspakkeService.oppdaterGrunnlagspakke(grunnlagspakkeId, request)
        LOGGER.info("Følgende grunnlagspakke ble oppdatert: $grunnlagspakkeId")
        return ResponseEntity(grunnlagspakkeOppdatert, HttpStatus.OK)
    }

    @GetMapping(GRUNNLAGSPAKKE_HENT)
    @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Finn alle data for en grunnlagspakke")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Grunnlagspakke funnet"),
            ApiResponse(responseCode = "401", description = "Manglende eller utløpt id-token"),
            ApiResponse(responseCode = "403", description = "Saksbehandler mangler tilgang til å lese data for aktuell grunnlagspakke"),
            ApiResponse(responseCode = "404", description = "Grunnlagspakke ikke funnet"),
            ApiResponse(responseCode = "500", description = "Serverfeil"),
            ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig")
        ]
    )
    fun hentGrunnlagspakke(
        @PathVariable @NotNull
        grunnlagspakkeId: Int
    ): ResponseEntity<HentGrunnlagspakkeDto>? {
        val grunnlagspakkeFunnet = grunnlagspakkeService.hentGrunnlagspakke(grunnlagspakkeId)
        LOGGER.info("Følgende grunnlagspakke ble hentet: ${grunnlagspakkeFunnet.grunnlagspakkeId}")
        return ResponseEntity(grunnlagspakkeFunnet, HttpStatus.OK)
    }

    @PostMapping(GRUNNLAGSPAKKE_LUKK)
    @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Setter gyldigTil-dato = dagens dato for angitt grunnlagspakke")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Grunnlagspakke oppdatert"),
            ApiResponse(responseCode = "401", description = "Manglende eller utløpt id-token"),
            ApiResponse(responseCode = "403", description = "Saksbehandler mangler tilgang til å lese data for aktuell grunnlagspakke"),
            ApiResponse(responseCode = "404", description = "Grunnlagspakke ikke funnet"),
            ApiResponse(responseCode = "500", description = "Serverfeil"),
            ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig")
        ]
    )
    fun lukkGrunnlagspakke(
        @PathVariable @NotNull
        grunnlagspakkeId: Int
    ): ResponseEntity<Int>? {
        val oppdatertgrunnlagspakke = grunnlagspakkeService.lukkGrunnlagspakke(grunnlagspakkeId)
        LOGGER.info("Følgende grunnlagspakke ble oppdatert med gyldigTil-dato: $oppdatertgrunnlagspakke")
        return ResponseEntity(grunnlagspakkeId, HttpStatus.OK)
    }

    companion object {

        const val GRUNNLAGSPAKKE_NY = "/grunnlagspakke"
        const val GRUNNLAGSPAKKE_OPPDATER = "/grunnlagspakke/{grunnlagspakkeId}/oppdater"
        const val GRUNNLAGSPAKKE_HENT = "/grunnlagspakke/{grunnlagspakkeId}"
        const val GRUNNLAGSPAKKE_LUKK = "/grunnlagspakke/{grunnlagspakkeId}/lukk"
        private val LOGGER = LoggerFactory.getLogger(GrunnlagspakkeController::class.java)
    }
}
