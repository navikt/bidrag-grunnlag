package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.domene.enums.grunnlag.GrunnlagRequestType
import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.consumer.familiebasak.FamilieBaSakConsumer
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.FamilieBaSakRequest
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.FamilieBaSakResponse
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.util.GrunnlagUtil.Companion.evaluerFeilmelding
import no.nav.bidrag.grunnlag.util.GrunnlagUtil.Companion.evaluerFeiltype
import no.nav.bidrag.grunnlag.util.GrunnlagUtil.Companion.tilJson
import no.nav.bidrag.transport.behandling.grunnlag.response.FeilrapporteringDto
import java.math.BigDecimal
import java.time.LocalDate

class HentUtvidetBarnetrygdOgSmåbarnstilleggService(
    private val familieBaSakConsumer: FamilieBaSakConsumer,
) {

    fun hentUbst(ubstRequestListe: List<PersonIdOgPeriodeRequest>): HentGrunnlagGenericDto<UtvidetBarnetrygdOgSmåbarnstilleggGrunnlagDto> {
        val ubstListe = mutableListOf<UtvidetBarnetrygdOgSmåbarnstilleggGrunnlagDto>()
        val feilrapporteringListe = mutableListOf<FeilrapporteringDto>()

        ubstRequestListe.forEach {
            val hentUbstRequest = FamilieBaSakRequest(
                personIdent = it.personId,
                fraDato = it.periodeFra,
            )

            when (
                val restResponseUbst = familieBaSakConsumer.hentFamilieBaSak(hentUbstRequest)
            ) {
                is RestResponse.Success -> {
                    SECURE_LOGGER.info(
                        "Henting av utvidet barnetrygd og småbarnstillegg ga følgende respons for ${it.personId}: ${tilJson(restResponseUbst.body)}",
                    )
                    leggTilUbst(ubstListe = ubstListe, familieBaSakRespons = restResponseUbst.body, ident = it.personId)
                }

                is RestResponse.Failure -> {
                    SECURE_LOGGER.warn(
                        "Feil ved henting av utvidet barnetrygd og småbarnstillegg for ${it.personId}. " +
                            "Statuskode ${restResponseUbst.statusCode.value()}",
                    )
                    feilrapporteringListe.add(
                        FeilrapporteringDto(
                            grunnlagstype = GrunnlagRequestType.UTVIDET_BARNETRYGD_OG_SMÅBARNSTILLEGG,
                            personId = hentUbstRequest.personIdent,
                            periodeFra = it.periodeFra,
                            periodeTil = null,
                            feiltype = evaluerFeiltype(
                                melding = restResponseUbst.message,
                                httpStatuskode = restResponseUbst.statusCode,
                            ),
                            feilmelding = evaluerFeilmelding(
                                melding = restResponseUbst.message,
                                grunnlagstype = GrunnlagRequestType.UTVIDET_BARNETRYGD_OG_SMÅBARNSTILLEGG,
                            ),
                        ),
                    )
                }
            }
        }

        return HentGrunnlagGenericDto(grunnlagListe = ubstListe, feilrapporteringListe = feilrapporteringListe)
    }

    private fun leggTilUbst(
        ubstListe: MutableList<UtvidetBarnetrygdOgSmåbarnstilleggGrunnlagDto>,
        familieBaSakRespons: FamilieBaSakResponse,
        ident: String,
    ) {
        familieBaSakRespons.perioder.forEach {
            ubstListe.add(
                UtvidetBarnetrygdOgSmåbarnstilleggGrunnlagDto(
                    personId = ident,
                    type = it.stønadstype.toString(),
                    periodeFra = LocalDate.parse("${it.fomMåned}-01"),
                    periodeTil = LocalDate.parse("${it.tomMåned}-01").plusMonths(1) ?: null,
                    beløp = BigDecimal.valueOf(it.beløp),
                    manueltBeregnet = it.manueltBeregnet,
                ),
            )
        }
    }
}
