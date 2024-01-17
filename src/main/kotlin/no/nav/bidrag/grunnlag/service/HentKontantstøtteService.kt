package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.consumer.familiekssak.FamilieKsSakConsumer
import no.nav.bidrag.grunnlag.consumer.familiekssak.api.BisysDto
import no.nav.bidrag.grunnlag.consumer.familiekssak.api.BisysResponsDto
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.transport.behandling.grunnlag.response.KontantstøtteGrunnlagDto
import java.time.LocalDate
import java.time.YearMonth

class HentKontantstøtteService(
    private val familieKsSakConsumer: FamilieKsSakConsumer,
) : List<KontantstøtteGrunnlagDto> by listOf() {

    fun hentKontantstøtte(
        kontantstøtteRequestListe: List<PersonIdOgPeriodeRequest>,
        historiskeIdenterMap: Map<String, List<String>>,
    ): List<KontantstøtteGrunnlagDto> {
        val kontantstøtteListe = mutableListOf<KontantstøtteGrunnlagDto>()

        kontantstøtteRequestListe.forEach {
            val personIdListe = historiskeIdenterMap[it.personId] ?: listOf(it.personId)

            val hentKontantstøtteRequest = BisysDto(
                fom = it.periodeFra,
                identer = personIdListe,
            )

            when (
                val restResponseKontantstøtte = familieKsSakConsumer.hentKontantstotte(hentKontantstøtteRequest)
            ) {
                is RestResponse.Success -> {
                    SECURE_LOGGER.info("Henting av kontantstøtte ga følgende respons for $personIdListe: ${restResponseKontantstøtte.body}")
                    leggTilKontantstøtte(kontantstøtteListe, restResponseKontantstøtte.body, it)
                }

                is RestResponse.Failure -> {
                    SECURE_LOGGER.warn("Feil ved henting av kontantstøtte for $personIdListe")
                }
            }
        }

        return kontantstøtteListe
    }

    private fun leggTilKontantstøtte(
        kontantstøtteListe: MutableList<KontantstøtteGrunnlagDto>,
        kontantstøtteRespons: BisysResponsDto,
        personIdOgPeriodeRequest: PersonIdOgPeriodeRequest,
    ) {
        // Kontantstøtte fra Infotrygd
        kontantstøtteRespons.infotrygdPerioder.forEach { ks ->
            val beløpPerParn = ks.beløp.div(ks.barna.size)
            ks.barna.forEach { barnPersonId ->
                kontantstøtteListe.add(
                    KontantstøtteGrunnlagDto(
                        partPersonId = personIdOgPeriodeRequest.personId,
                        barnPersonId = barnPersonId,
                        periodeFra = LocalDate.parse("${ks.fomMåned}-01"),
                        periodeTil = LocalDate.parse("${ks.tomMåned}-01").plusMonths(1) ?: null,
                        beløp = beløpPerParn,
                    ),
                )
            }
        }

        // Kontantstøtte fra ks-sak
        kontantstøtteRespons.ksSakPerioder.forEach { ks ->
            if (ks.fomMåned.isBefore(YearMonth.of(personIdOgPeriodeRequest.periodeTil.year, personIdOgPeriodeRequest.periodeTil.month))) {
                kontantstøtteListe.add(
                    KontantstøtteGrunnlagDto(
                        partPersonId = personIdOgPeriodeRequest.personId,
                        barnPersonId = ks.barn.ident,
                        periodeFra = LocalDate.parse("${ks.fomMåned}-01"),
                        periodeTil = LocalDate.parse("${ks.tomMåned}-01").plusMonths(1) ?: null,
                        beløp = ks.barn.beløp,
                    ),
                )
            }
        }
    }
}
