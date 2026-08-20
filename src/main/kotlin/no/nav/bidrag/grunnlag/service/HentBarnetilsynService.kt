package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.domene.enums.barnetilsyn.Skolealder
import no.nav.bidrag.domene.enums.barnetilsyn.Tilsynstype
import no.nav.bidrag.domene.enums.grunnlag.GrunnlagRequestType
import no.nav.bidrag.grunnlag.consumer.familieefsak.FamilieEfSakConsumer
import no.nav.bidrag.grunnlag.consumer.familieefsak.api.BarnetilsynRequest
import no.nav.bidrag.grunnlag.consumer.familieefsak.api.BarnetilsynResponse
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.util.GrunnlagUtil.Companion.evaluerFeilmelding
import no.nav.bidrag.grunnlag.util.GrunnlagUtil.Companion.evaluerFeiltype
import no.nav.bidrag.transport.behandling.grunnlag.response.BarnetilsynGrunnlagDto
import no.nav.bidrag.transport.behandling.grunnlag.response.FeilrapporteringDto

class HentBarnetilsynService(private val familieEfSakConsumer: FamilieEfSakConsumer) {

    fun hentBarnetilsyn(barnetilsynRequestListe: List<PersonIdOgPeriodeRequest>): HentGrunnlagGenericDto<BarnetilsynGrunnlagDto> {
        val barnetilsynListe = mutableListOf<BarnetilsynGrunnlagDto>()
        val feilrapporteringListe = mutableListOf<FeilrapporteringDto>()

        barnetilsynRequestListe.forEach {
            val hentBarnetilsynRequest = BarnetilsynRequest(
                ident = it.personId,
                fomDato = it.periodeFra,
            )

            when (
                val restResponseBarnetilsyn = familieEfSakConsumer.hentBarnetilsyn(hentBarnetilsynRequest)
            ) {
                is RestResponse.Success -> {
                    leggTilBarnetilsyn(
                        barnetilsynListe = barnetilsynListe,
                        barnetilsynRespons = restResponseBarnetilsyn.body,
                        ident = it.personId,
                        requestedPeriodeFra = it.periodeFra,
                        requestedPeriodeTil = it.periodeTil,
                    )
                }

                is RestResponse.Failure -> {
                    feilrapporteringListe.add(
                        FeilrapporteringDto(
                            grunnlagstype = GrunnlagRequestType.BARNETILSYN,
                            personId = hentBarnetilsynRequest.ident,
                            periodeFra = hentBarnetilsynRequest.fomDato,
                            periodeTil = null,
                            feiltype = evaluerFeiltype(
                                melding = restResponseBarnetilsyn.message,
                                httpStatuskode = restResponseBarnetilsyn.statusCode,
                            ),
                            feilmelding = evaluerFeilmelding(
                                melding = restResponseBarnetilsyn.message,
                                grunnlagstype = GrunnlagRequestType.BARNETILSYN,
                            ),
                        ),
                    )
                }
            }
        }

        return HentGrunnlagGenericDto(grunnlagListe = barnetilsynListe, feilrapporteringListe = feilrapporteringListe)
    }

    private fun leggTilBarnetilsyn(
        barnetilsynListe: MutableList<BarnetilsynGrunnlagDto>,
        barnetilsynRespons: BarnetilsynResponse,
        ident: String,
        requestedPeriodeFra: java.time.LocalDate,
        requestedPeriodeTil: java.time.LocalDate,
    ) {
        barnetilsynRespons.barnetilsynBisysPerioder
            .filter { bts -> perioderOverlapper(bts.periode, requestedPeriodeFra, requestedPeriodeTil) }
            .forEach { bts ->
                for (barnIdent in bts.barnIdenter) {
                    barnetilsynListe.add(
                        BarnetilsynGrunnlagDto(
                            partPersonId = ident,
                            barnPersonId = barnIdent,
                            periodeFra = bts.periode.fom,
                            periodeTil = bts.periode.tom.plusMonths(1).withDayOfMonth(1) ?: null,
                            beløp = null,
                            tilsynstype = Tilsynstype.IKKE_ANGITT,
                            skolealder = Skolealder.IKKE_ANGITT,
                        ),
                    )
                }
            }
    }

    private fun perioderOverlapper(
        responsPeriode: no.nav.bidrag.grunnlag.consumer.familieefsak.api.Periode,
        requestedPeriodeFra: java.time.LocalDate,
        requestedPeriodeTil: java.time.LocalDate,
    ): Boolean {
        // Two periods overlap if: fom1 <= tom2 AND tom1 >= fom2
        return responsPeriode.fom <= requestedPeriodeTil && responsPeriode.tom >= requestedPeriodeFra
    }
}
