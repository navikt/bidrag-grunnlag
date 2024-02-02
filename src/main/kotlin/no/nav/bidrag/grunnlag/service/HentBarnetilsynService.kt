package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.domene.enums.barnetilsyn.Skolealder
import no.nav.bidrag.domene.enums.barnetilsyn.Tilsynstype
import no.nav.bidrag.domene.enums.grunnlag.GrunnlagRequestType
import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.consumer.familieefsak.FamilieEfSakConsumer
import no.nav.bidrag.grunnlag.consumer.familieefsak.api.BarnetilsynRequest
import no.nav.bidrag.grunnlag.consumer.familieefsak.api.BarnetilsynResponse
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.util.GrunnlagUtil.Companion.evaluerFeilmelding
import no.nav.bidrag.grunnlag.util.GrunnlagUtil.Companion.evaluerFeiltype
import no.nav.bidrag.transport.behandling.grunnlag.response.BarnetilsynGrunnlagDto
import no.nav.bidrag.transport.behandling.grunnlag.response.FeilrapporteringDto

class HentBarnetilsynService(
    private val familieEfSakConsumer: FamilieEfSakConsumer,
) {

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
                    SECURE_LOGGER.info("Henting av barnetilsyn ga følgende respons for ${it.personId}: ${restResponseBarnetilsyn.body}")
                    leggTilBarnetilsyn(barnetilsynListe = barnetilsynListe, barnetilsynRespons = restResponseBarnetilsyn.body, ident = it.personId)
                }

                is RestResponse.Failure -> {
                    SECURE_LOGGER.warn("Feil ved henting av barnetilsyn for ${it.personId}")
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

    private fun leggTilBarnetilsyn(barnetilsynListe: MutableList<BarnetilsynGrunnlagDto>, barnetilsynRespons: BarnetilsynResponse, ident: String) {
        barnetilsynRespons.barnetilsynBisysPerioder.forEach { bts ->
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
}
