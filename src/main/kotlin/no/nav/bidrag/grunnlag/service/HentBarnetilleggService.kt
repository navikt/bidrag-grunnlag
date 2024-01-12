package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.domene.enums.barnetillegg.Barnetilleggstype
import no.nav.bidrag.domene.enums.person.BarnType
import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.consumer.pensjon.PensjonConsumer
import no.nav.bidrag.grunnlag.consumer.pensjon.api.BarnetilleggPensjon
import no.nav.bidrag.grunnlag.consumer.pensjon.api.HentBarnetilleggPensjonRequest
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.transport.behandling.grunnlag.response.BarnetilleggGrunnlagDto

// @Service
class HentBarnetilleggService(
    private val pensjonConsumer: PensjonConsumer,
) : List<BarnetilleggGrunnlagDto> by listOf() {

    fun hentBarnetilleggPensjon(barnetilleggPensjonRequestListe: List<PersonIdOgPeriodeRequest>): List<BarnetilleggGrunnlagDto> {
        val barnetilleggPensjonListe = mutableListOf<BarnetilleggGrunnlagDto>()

        barnetilleggPensjonRequestListe.forEach {
            val hentBarnetilleggRequest = HentBarnetilleggPensjonRequest(
                mottaker = it.personId,
                fom = it.periodeFra,
                tom = it.periodeTil.minusDays(1),
            )

            when (
                val restResponseBarnetillegg = pensjonConsumer.hentBarnetilleggPensjon(hentBarnetilleggRequest)
            ) {
                is RestResponse.Success -> {
                    SECURE_LOGGER.info("Barnetillegg pensjon ga følgende respons: ${restResponseBarnetillegg.body}")
                    leggTilBarnetillegg(barnetilleggPensjonListe, restResponseBarnetillegg.body, it.personId)
                }

                is RestResponse.Failure -> {
                    return emptyList()
                }
            }
        }
        return barnetilleggPensjonListe
    }

    private fun leggTilBarnetillegg(
        barnetilleggPensjonListe: MutableList<BarnetilleggGrunnlagDto>,
        barnetilleggPensjonResponsListe: List<BarnetilleggPensjon>,
        ident: String,
    ) {
        barnetilleggPensjonResponsListe.forEach {
            barnetilleggPensjonListe.add(
                BarnetilleggGrunnlagDto(
                    partPersonId = ident,
                    barnPersonId = it.barn,
                    barnetilleggType = Barnetilleggstype.PENSJON.toString(),
                    periodeFra = it.fom,
                    periodeTil = it.tom.plusMonths(1).withDayOfMonth(1) ?: null,
                    beløpBrutto = it.beloep,
                    barnType = if (it.erFellesbarn) BarnType.FELLES.toString() else BarnType.SÆRKULL.toString(),
                ),
            )
        }
    }
}
