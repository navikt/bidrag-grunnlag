package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.domene.enums.grunnlag.GrunnlagRequestType
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.consumer.bidragperson.BidragPersonConsumer
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.service.InntektskomponentenService.Companion.LOGGER
import no.nav.bidrag.grunnlag.util.GrunnlagUtil.Companion.evaluerFeilmelding
import no.nav.bidrag.grunnlag.util.GrunnlagUtil.Companion.evaluerFeiltype
import no.nav.bidrag.grunnlag.util.GrunnlagUtil.Companion.tilJson
import no.nav.bidrag.transport.behandling.grunnlag.response.FeilrapporteringDto
import no.nav.bidrag.transport.behandling.grunnlag.response.SivilstandGrunnlagDto
import no.nav.bidrag.transport.person.SivilstandPdlHistorikkDto
import org.springframework.http.HttpStatus

class HentSivilstandService(private val bidragPersonConsumer: BidragPersonConsumer) {

    fun hentSivilstand(sivilstandRequestListe: List<PersonIdOgPeriodeRequest>): HentGrunnlagGenericDto<SivilstandGrunnlagDto> {
        val sivilstandListe = mutableListOf<SivilstandGrunnlagDto>()
        val feilrapporteringListe = mutableListOf<FeilrapporteringDto>()

        sivilstandRequestListe.forEach {
            when (
                val restResponseSivilstand = bidragPersonConsumer.hentSivilstand(Personident(it.personId))
            ) {
                is RestResponse.Success -> {
                    SECURE_LOGGER.info(
                        "Henting av sivilstand ga følgende respons for ${it.personId}: ${tilJson(restResponseSivilstand.body)}",
                    )
                    leggTilSivilstand(sivilstandListe = sivilstandListe, sivilstandRespons = restResponseSivilstand.body, ident = it.personId)
                }

                is RestResponse.Failure -> {
                    if (restResponseSivilstand.statusCode == HttpStatus.NOT_FOUND) {
                        SECURE_LOGGER.warn("Sivilstand ikke funnet for ${it.personId}. Statuskode ${restResponseSivilstand.statusCode.value()}")
                    } else {
                        LOGGER.error(
                            "Feil ved henting av sivilstanda bidrag-person/PDL. Statuskode ${restResponseSivilstand.statusCode.value()}",
                        )
                        SECURE_LOGGER.error(
                            "Feil ved henting av sivilstand for ${it.personId}. Statuskode ${restResponseSivilstand.statusCode.value()}",
                        )
                    }

                    feilrapporteringListe.add(
                        FeilrapporteringDto(
                            grunnlagstype = GrunnlagRequestType.SIVILSTAND,
                            personId = it.personId,
                            periodeFra = null,
                            periodeTil = null,
                            feiltype = evaluerFeiltype(
                                melding = restResponseSivilstand.message,
                                httpStatuskode = restResponseSivilstand.statusCode,
                            ),
                            feilmelding = evaluerFeilmelding(
                                melding = restResponseSivilstand.message,
                                grunnlagstype = GrunnlagRequestType.SIVILSTAND,
                            ),
                        ),
                    )
                }
            }
        }

        return HentGrunnlagGenericDto(grunnlagListe = sivilstandListe, feilrapporteringListe = feilrapporteringListe)
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
