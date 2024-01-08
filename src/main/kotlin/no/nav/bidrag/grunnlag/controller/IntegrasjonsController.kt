package no.nav.bidrag.grunnlag.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.grunnlag.ISSUER
import no.nav.bidrag.grunnlag.consumer.arbeidsforhold.ArbeidsforholdConsumer
import no.nav.bidrag.grunnlag.consumer.arbeidsforhold.EnhetsregisterConsumer
import no.nav.bidrag.grunnlag.consumer.arbeidsforhold.api.Arbeidsforhold
import no.nav.bidrag.grunnlag.consumer.arbeidsforhold.api.HentArbeidsforholdRequest
import no.nav.bidrag.grunnlag.consumer.arbeidsforhold.api.HentEnhetsregisterRequest
import no.nav.bidrag.grunnlag.consumer.arbeidsforhold.api.HentEnhetsregisterResponse
import no.nav.bidrag.grunnlag.consumer.bidragperson.BidragPersonConsumer
import no.nav.bidrag.grunnlag.consumer.familiebasak.FamilieBaSakConsumer
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.FamilieBaSakRequest
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.FamilieBaSakResponse
import no.nav.bidrag.grunnlag.consumer.familieefsak.FamilieEfSakConsumer
import no.nav.bidrag.grunnlag.consumer.familieefsak.api.BarnetilsynRequest
import no.nav.bidrag.grunnlag.consumer.familieefsak.api.BarnetilsynResponse
import no.nav.bidrag.grunnlag.consumer.familiekssak.FamilieKsSakConsumer
import no.nav.bidrag.grunnlag.consumer.familiekssak.api.BisysDto
import no.nav.bidrag.grunnlag.consumer.familiekssak.api.BisysResponsDto
import no.nav.bidrag.grunnlag.consumer.inntektskomponenten.InntektskomponentenConsumer
import no.nav.bidrag.grunnlag.consumer.inntektskomponenten.api.HentInntektListeRequest
import no.nav.bidrag.grunnlag.consumer.pensjon.PensjonConsumer
import no.nav.bidrag.grunnlag.consumer.pensjon.api.BarnetilleggPensjon
import no.nav.bidrag.grunnlag.consumer.pensjon.api.HentBarnetilleggPensjonRequest
import no.nav.bidrag.grunnlag.consumer.skattegrunnlag.SigrunConsumer
import no.nav.bidrag.grunnlag.consumer.skattegrunnlag.api.HentSummertSkattegrunnlagRequest
import no.nav.bidrag.grunnlag.consumer.skattegrunnlag.api.HentSummertSkattegrunnlagResponse
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.transport.person.ForelderBarnRelasjonDto
import no.nav.bidrag.transport.person.HusstandsmedlemmerDto
import no.nav.bidrag.transport.person.NavnFødselDødDto
import no.nav.bidrag.transport.person.SivilstandPdlHistorikkDto
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
    private val pensjonConsumer: PensjonConsumer,
    private val inntektskomponentenConsumer: InntektskomponentenConsumer,
    private val sigrunConsumer: SigrunConsumer,
    private val familieBaSakConsumer: FamilieBaSakConsumer,
    private val bidragPersonConsumer: BidragPersonConsumer,
    private val familieKsSakConsumer: FamilieKsSakConsumer,
    private val familieEfSakConsumer: FamilieEfSakConsumer,
    private val arbeidsforholdConsumer: ArbeidsforholdConsumer,
    private val enhetsregisterConsumer: EnhetsregisterConsumer,
) {

    @PostMapping(HENT_AINNTEKT)
    @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Henter A-inntekt")
    fun hentAinntekt(@RequestBody hentInntektListeRequest: HentInntektListeRequest): ResponseEntity<HentInntektListeResponse> {
        return handleRestResponse(inntektskomponentenConsumer.hentInntekter(hentInntektListeRequest, false))
    }

    @PostMapping(HENT_AINNTEKT_ABONNEMENT)
    @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Henter A-inntekt med abonnement")
    fun hentAinntektAbonnement(@RequestBody hentInntektListeRequest: HentInntektListeRequest): ResponseEntity<HentInntektListeResponse> {
        return handleRestResponse(inntektskomponentenConsumer.hentInntekter(hentInntektListeRequest, true))
    }

    @PostMapping(HENT_SKATTEGRUNNLAG)
    @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Henter skattegrunnlag")
    fun hentSkattegrunnlag(
        @RequestBody hentSkattegrunnlagRequest: HentSummertSkattegrunnlagRequest,
    ): ResponseEntity<HentSummertSkattegrunnlagResponse> {
        return handleRestResponse(sigrunConsumer.hentSummertSkattegrunnlag(hentSkattegrunnlagRequest))
    }

    @PostMapping(HENT_BARNETILLEGG_PENSJON)
    @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Henter barnetillegg fra pensjon")
    fun hentBarnetilleggPensjon(
        @RequestBody hentBarnetilleggPensjonRequest: HentBarnetilleggPensjonRequest,
    ): ResponseEntity<List<BarnetilleggPensjon>> {
        return handleRestResponse(pensjonConsumer.hentBarnetilleggPensjon(hentBarnetilleggPensjonRequest))
    }

    @PostMapping(HENT_FAMILIEBASAK)
    @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Henter utvidet barnetrygd og småbarnstillegg")
    fun hentFamilieBaSak(@RequestBody familieBaSakRequest: FamilieBaSakRequest): ResponseEntity<FamilieBaSakResponse> {
        return handleRestResponse(familieBaSakConsumer.hentFamilieBaSak(familieBaSakRequest))
    }

    @PostMapping(HENT_FOEDSEL_DOED)
    @Operation(
        security = [SecurityRequirement(name = "bearer-key")],
        summary = "Kaller bidrag-person som igjen henter info om fødselsdato og eventuell død fra PDL",
    )
    fun hentFoedselOgDoed(@RequestBody bidragPersonident: Personident): ResponseEntity<NavnFødselDødDto> {
        return handleRestResponse(bidragPersonConsumer.hentNavnFoedselOgDoed(bidragPersonident))
    }

    @PostMapping(HENT_FORELDER_BARN_RELASJON)
    @Operation(
        security = [SecurityRequirement(name = "bearer-key")],
        summary = "Kaller bidrag-person som igjen henter forelderbarnrelasjoner for angitt person fra PDL",
    )
    fun hentForelderbarnrelasjon(@RequestBody bidragPersonident: Personident): ResponseEntity<ForelderBarnRelasjonDto> {
        return handleRestResponse(bidragPersonConsumer.hentForelderBarnRelasjon(bidragPersonident))
    }

    @PostMapping(HENT_HUSSTANDSMEDLEMMER)
    @Operation(
        security = [SecurityRequirement(name = "bearer-key")],
        summary = "Kaller bidrag-person som igjen henter info om en persons bostedsadresser og personer som har bodd på samme adresse på samme tid " +
            "fra PDL",
    )
    fun hentHusstandsmedlemmer(@RequestBody husstandsmedlemmerRequest: Personident): ResponseEntity<HusstandsmedlemmerDto> {
        return handleRestResponse(bidragPersonConsumer.hentHusstandsmedlemmer(husstandsmedlemmerRequest))
    }

    @PostMapping(HENT_SIVILSTAND)
    @Operation(
        security = [SecurityRequirement(name = "bearer-key")],
        summary = "Kaller bidrag-person som igjen kaller PDL for å finne en persons sivilstand",
    )
    fun hentSivilstand(@RequestBody sivilstandRequest: Personident): ResponseEntity<SivilstandPdlHistorikkDto> {
        return handleRestResponse(bidragPersonConsumer.hentSivilstand(sivilstandRequest))
    }

    @PostMapping(HENT_KONTANTSTOTTE)
    @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Kaller familie-ks-sak for å hente kontantstotte")
    fun hentKontantstotte(@RequestBody innsynRequest: BisysDto): ResponseEntity<BisysResponsDto> {
        return handleRestResponse(familieKsSakConsumer.hentKontantstotte(innsynRequest))
    }

    @PostMapping(HENT_BARNETILSYN)
    @Operation(
        security = [SecurityRequirement(name = "bearer-key")],
        summary = "Kaller familie-ef-sak/hentPerioderBarnetilsyn for å hente barnetilsyn",
    )
    fun hentBarnetilsyn(@RequestBody barnetilsynRequest: BarnetilsynRequest): ResponseEntity<BarnetilsynResponse> {
        return handleRestResponse(familieEfSakConsumer.hentBarnetilsyn(barnetilsynRequest))
    }

    @PostMapping(HENT_ARBEIDSFORHOLD)
    @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Kaller Aareg og henter arbeidsforhold")
    fun hentArbeidsforhold(@RequestBody hentArbeidsforholdRequest: HentArbeidsforholdRequest): ResponseEntity<List<Arbeidsforhold>> {
        return handleRestResponse(arbeidsforholdConsumer.hentArbeidsforhold(hentArbeidsforholdRequest))
    }

    @PostMapping(HENT_ENHETSINFO)
    @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Kaller Ereg og henter info fra enhetsregister")
    fun hentEnhetsinfo(@RequestBody hentEnhetsregisterRequest: HentEnhetsregisterRequest): ResponseEntity<HentEnhetsregisterResponse> {
        return handleRestResponse(enhetsregisterConsumer.hentEnhetsinfo(hentEnhetsregisterRequest))
    }

    private fun <T> handleRestResponse(restResponse: RestResponse<T>): ResponseEntity<T> {
        return when (restResponse) {
            is RestResponse.Success -> ResponseEntity(restResponse.body, HttpStatus.OK)
            is RestResponse.Failure -> throw ResponseStatusException(restResponse.statusCode, restResponse.message)
        }
    }

    companion object {
        const val HENT_AINNTEKT = "/integrasjoner/ainntekt"
        const val HENT_AINNTEKT_ABONNEMENT = "/integrasjoner/ainntekt/abonnement"
        const val HENT_SKATTEGRUNNLAG = "/integrasjoner/skattegrunnlag"
        const val HENT_BARNETILLEGG_PENSJON = "/integrasjoner/barnetillegg"
        const val HENT_FAMILIEBASAK = "/integrasjoner/familiebasak"
        const val HENT_FORELDER_BARN_RELASJON = "/integrasjoner/forelderbarnrelasjon"
        const val HENT_FOEDSEL_DOED = "/integrasjoner/navnfoedseldoed"
        const val HENT_HUSSTANDSMEDLEMMER = "/integrasjoner/husstandsmedlemmer"
        const val HENT_SIVILSTAND = "/integrasjoner/sivilstand"
        const val HENT_KONTANTSTOTTE = "/integrasjoner/kontantstotte"
        const val HENT_BARNETILSYN = "/integrasjoner/barnetilsyn"
        const val HENT_ARBEIDSFORHOLD = "/integrasjoner/arbeidsforhold"
        const val HENT_ENHETSINFO = "/integrasjoner/enhetsinfo"
    }
}
