package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.consumer.familiebasak.FamilieBaSakConsumer
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.FamilieBaSakRequest
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.FamilieBaSakResponse
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.transport.behandling.grunnlag.response.UtvidetBarnetrygdOgSmaabarnstilleggGrunnlagDto
import java.math.BigDecimal
import java.time.LocalDate

// @Service
class HentUtvidetBarnetrygdOgSmåbarnstilleggService(
    private val familieBaSakConsumer: FamilieBaSakConsumer,
) : List<UtvidetBarnetrygdOgSmaabarnstilleggGrunnlagDto> by listOf() {

    fun hentUbst(ubstRequestListe: List<PersonIdOgPeriodeRequest>): List<UtvidetBarnetrygdOgSmaabarnstilleggGrunnlagDto> {
        val ubstListe = mutableListOf<UtvidetBarnetrygdOgSmaabarnstilleggGrunnlagDto>()

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
                        "Familie-BA-sak hent utvidet barnetrygd og småbarnstillegg ga følgende respons: ${restResponseUbst.body}",
                    )
                    leggTilUbst(ubstListe, restResponseUbst.body, it.personId)
                }

                is RestResponse.Failure -> {
                    return emptyList()
                }
            }
        }
        return ubstListe
    }

    private fun leggTilUbst(
        ubstListe: MutableList<UtvidetBarnetrygdOgSmaabarnstilleggGrunnlagDto>,
        familieBaSakRespons: FamilieBaSakResponse,
        ident: String,
    ) {
        familieBaSakRespons.perioder.forEach {
            ubstListe.add(
                UtvidetBarnetrygdOgSmaabarnstilleggGrunnlagDto(
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
