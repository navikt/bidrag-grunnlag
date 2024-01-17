package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.consumer.bidragperson.BidragPersonConsumer
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.transport.behandling.grunnlag.response.SivilstandGrunnlagDto
import no.nav.bidrag.transport.person.SivilstandPdlHistorikkDto

class HentSivilstandService(
    private val bidragPersonConsumer: BidragPersonConsumer,
) : List<SivilstandGrunnlagDto> by listOf() {

    fun hentSivilstand(sivilstandRequestListe: List<PersonIdOgPeriodeRequest>): List<SivilstandGrunnlagDto> {
        val sivilstandListe = mutableListOf<SivilstandGrunnlagDto>()

        sivilstandRequestListe.forEach {
            when (
                val restResponseSivilstand = bidragPersonConsumer.hentSivilstand(Personident(it.personId))
            ) {
                is RestResponse.Success -> {
                    SECURE_LOGGER.info(
                        "Henting av sivilstand sivilstand ga følgende respons for ${it.personId}: ${restResponseSivilstand.body}",
                    )
                    leggTilSivilstand(sivilstandListe, restResponseSivilstand.body, it.personId)
                }

                is RestResponse.Failure -> {
                    SECURE_LOGGER.warn("Feil ved henting av sivilstand for ${it.personId}")
                }
            }
        }

        return sivilstandListe
    }

    private fun leggTilSivilstand(sivilstandListe: MutableList<SivilstandGrunnlagDto>, sivilstandRespons: SivilstandPdlHistorikkDto, ident: String) {
        sivilstandRespons.sivilstandPdlDto.forEach {
            sivilstandListe.add(
                SivilstandGrunnlagDto(
                    personId = ident,
                    type = it.type,
                    gyldigFom = it.gyldigFom,
                    bekreftelsesdato = it.bekreftelsesdato,
                    master = it.master,
                    registrert = it.registrert,
                    historisk = it.historisk,
                ),
            )
        }
    }
}
