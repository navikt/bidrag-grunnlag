package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.domene.enums.inntekt.Skattegrunnlagstype
import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.consumer.skattegrunnlag.SigrunConsumer
import no.nav.bidrag.grunnlag.consumer.skattegrunnlag.api.HentSummertSkattegrunnlagRequest
import no.nav.bidrag.grunnlag.consumer.skattegrunnlag.api.HentSummertSkattegrunnlagResponse
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.transport.behandling.grunnlag.response.SkattegrunnlagGrunnlagDto
import no.nav.bidrag.transport.behandling.grunnlag.response.SkattegrunnlagspostDto
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.LocalDate

// @Service
class HentSkattegrunnlagService(
    private val sigrunConsumer: SigrunConsumer,
) : List<SkattegrunnlagGrunnlagDto> by listOf() {

    companion object {
        @JvmStatic
        val LOGGER: Logger = LoggerFactory.getLogger(HentSkattegrunnlagService::class.java)
    }

    fun hentSkattegrunnlag(skattegrunnlagRequestListe: List<PersonIdOgPeriodeRequest>): List<SkattegrunnlagGrunnlagDto> {
        LOGGER.info("Start SKATTEGRUNNLAG")

        val skattegrunnlagListe = mutableListOf<SkattegrunnlagGrunnlagDto>()

        skattegrunnlagRequestListe.forEach {
            var inntektÅr = it.periodeFra.year
            val sluttÅr = it.periodeTil.year

            while (inntektÅr < sluttÅr) {
                val hentSkattegrunnlagRequest = HentSummertSkattegrunnlagRequest(
                    inntektsAar = inntektÅr.toString(),
                    inntektsFilter = "SummertSkattegrunnlagBidrag",
                    personId = it.personId,
                )

                when (
                    val restResponseSkattegrunnlag = sigrunConsumer.hentSummertSkattegrunnlag(hentSkattegrunnlagRequest)
                ) {
                    is RestResponse.Success -> {
                        SECURE_LOGGER.info(
                            "Sigrun (skattegrunnlag) ga følgende respons for ${it.personId} og år $inntektÅr: ${restResponseSkattegrunnlag.body}",
                        )
                        leggTilSkattegrunnlag(skattegrunnlagListe, restResponseSkattegrunnlag.body, it.personId, inntektÅr)
                    }

                    is RestResponse.Failure -> {
                        SECURE_LOGGER.warn("Feil ved henting av skattegrunnlag for ${it.personId} og år $inntektÅr")
                    }
                }
                inntektÅr++
            }
        }

        LOGGER.info("Slutt SKATTEGRUNNLAG")
        return skattegrunnlagListe
    }

    private fun leggTilSkattegrunnlag(
        skattegrunnlagListe: MutableList<SkattegrunnlagGrunnlagDto>,
        skattegrunnlagRespons: HentSummertSkattegrunnlagResponse,
        ident: String,
        inntektAar: Int,
    ) {
        val skattegrunnlagspostListe = mutableListOf<SkattegrunnlagspostDto>()

        // Ordinære grunnlagsposter
        skattegrunnlagRespons.grunnlag?.forEach {
            skattegrunnlagspostListe.add(
                SkattegrunnlagspostDto(
                    skattegrunnlagType = Skattegrunnlagstype.ORDINÆR.toString(),
                    inntektType = it.tekniskNavn,
                    belop = BigDecimal(it.beloep),
                ),
            )
        }

        // Svalbard grunnlagsposter
        skattegrunnlagRespons.svalbardGrunnlag?.forEach {
            skattegrunnlagspostListe.add(
                SkattegrunnlagspostDto(
                    skattegrunnlagType = Skattegrunnlagstype.SVALBARD.toString(),
                    inntektType = it.tekniskNavn,
                    belop = BigDecimal(it.beloep),
                ),
            )
        }

        val skattegrunnlag = SkattegrunnlagGrunnlagDto(
            personId = ident,
            periodeFra = LocalDate.parse("$inntektAar-01-01"),
            periodeTil = LocalDate.parse("$inntektAar-01-01").plusYears(1),
            skattegrunnlagspostListe = skattegrunnlagspostListe,
        )

        skattegrunnlagListe.add(skattegrunnlag)
    }
}
