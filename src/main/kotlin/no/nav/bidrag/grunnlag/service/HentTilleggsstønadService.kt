package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.domene.enums.grunnlag.GrunnlagRequestType
import no.nav.bidrag.grunnlag.consumer.familiebasak.TilleggsstønadConsumer
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.TilleggsstønadRequest
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.TilleggsstønadResponse
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.util.GrunnlagUtil.Companion.evaluerFeilmelding
import no.nav.bidrag.grunnlag.util.GrunnlagUtil.Companion.evaluerFeiltype
import no.nav.bidrag.transport.behandling.grunnlag.response.FeilrapporteringDto
import no.nav.bidrag.transport.behandling.grunnlag.response.TilleggsstønadGrunnlagDto

class HentTilleggsstønadService(private val tilleggsstønadConsumer: TilleggsstønadConsumer) {

    fun hentTilleggsstønad(
        tilleggsstønadRequestListe: List<PersonIdOgPeriodeRequest>,
        historiskeIdenterMap: Map<String, List<String>>,
    ): HentGrunnlagGenericDto<TilleggsstønadGrunnlagDto> {
        val tilleggsstønadListe = mutableListOf<TilleggsstønadGrunnlagDto>()
        val feilrapporteringListe = mutableListOf<FeilrapporteringDto>()

        tilleggsstønadRequestListe.forEach {
            val hentTilleggsstønadRequest = TilleggsstønadRequest(
                ident = it.personId,
            )

            when (
                val restResponseTilleggsstønad = tilleggsstønadConsumer.hentTilleggsstønad(hentTilleggsstønadRequest)
            ) {
                is RestResponse.Success -> {
                    leggTilTilleggsstønad(
                        tilleggsstønadResponsListe = tilleggsstønadResponsListe,
                        tilleggsstønadRespons = restResponseTilleggsstønad.body,
                        personIdOgPeriodeRequest = it,
                    )
                }

                is RestResponse.Failure -> {
                    feilrapporteringListe.add(
                        FeilrapporteringDto(
                            grunnlagstype = GrunnlagRequestType.KONTANTSTØTTE,
                            personId = it.personId,
                            periodeFra = hentKontantstøtteRequest.fom,
                            periodeTil = null,
                            feiltype = evaluerFeiltype(
                                melding = restResponseKontantstøtte.message,
                                httpStatuskode = restResponseKontantstøtte.statusCode,
                            ),
                            feilmelding = evaluerFeilmelding(
                                melding = restResponseKontantstøtte.message,
                                grunnlagstype = GrunnlagRequestType.KONTANTSTØTTE,
                            ),
                        ),
                    )
                }
            }
        }

        return HentGrunnlagGenericDto(grunnlagListe = tilleggsstønadListe, feilrapporteringListe = feilrapporteringListe)
    }

    private fun leggTilTilleggsstønad(tilleggsstønadListe: MutableList<TilleggsstønadGrunnlagDto>, tilleggsstønadRespons: TilleggsstønadResponse, ident: String) {
        tilleggsstønadListe.add(
                    TilleggsstønadGrunnlagDto(
                        partPersonId = tilleggsstønadRespons. .ident,
                        harInnvilgetVedtak = tilleggsstønadRespons.harInnvilgetVedtak,
                    ),
                )
            }
        }
    }
}
