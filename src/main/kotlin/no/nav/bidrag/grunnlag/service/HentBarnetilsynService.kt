package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.domene.enums.barnetilsyn.Skolealder
import no.nav.bidrag.domene.enums.barnetilsyn.Tilsynstype
import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.consumer.familieefsak.FamilieEfSakConsumer
import no.nav.bidrag.grunnlag.consumer.familieefsak.api.BarnetilsynRequest
import no.nav.bidrag.grunnlag.consumer.familieefsak.api.BarnetilsynResponse
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.transport.behandling.grunnlag.response.BarnetilsynGrunnlagDto
import org.slf4j.Logger
import org.slf4j.LoggerFactory

// @Service
class HentBarnetilsynService(
    private val familieEfSakConsumer: FamilieEfSakConsumer,
) : List<BarnetilsynGrunnlagDto> by listOf() {

    companion object {
        @JvmStatic
        val LOGGER: Logger = LoggerFactory.getLogger(HentBarnetilsynService::class.java)
    }

    fun hentBarnetilsyn(barnetilsynRequestListe: List<PersonIdOgPeriodeRequest>): List<BarnetilsynGrunnlagDto> {
        LOGGER.info("Start BARNETILSYN")

        val barnetilsynListe = mutableListOf<BarnetilsynGrunnlagDto>()

        barnetilsynRequestListe.forEach {
            val hentBarnetilsynRequest = BarnetilsynRequest(
                ident = it.personId,
                fomDato = it.periodeFra,
            )

            when (
                val restResponseBarnetilsyn = familieEfSakConsumer.hentBarnetilsyn(hentBarnetilsynRequest)
            ) {
                is RestResponse.Success -> {
                    SECURE_LOGGER.info("Barnetilsyn enslig forsørger ga følgende respons: ${restResponseBarnetilsyn.body}")
                    leggTilBarnetilsyn(barnetilsynListe, restResponseBarnetilsyn.body, it.personId)
                }

                is RestResponse.Failure -> {
                    SECURE_LOGGER.warn("Feil ved henting av barnetilsyn enslig forsørger for ${it.personId}")
                }
            }
        }

        LOGGER.info("Slutt BARNETILSYN")
        return barnetilsynListe
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
