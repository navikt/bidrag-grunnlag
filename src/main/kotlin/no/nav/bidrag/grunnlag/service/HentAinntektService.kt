package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.domene.enums.vedtak.Formål
import no.nav.bidrag.grunnlag.consumer.inntektskomponenten.api.Aktoer
import no.nav.bidrag.grunnlag.consumer.inntektskomponenten.api.ArbeidsInntektMaanedIntern
import no.nav.bidrag.grunnlag.consumer.inntektskomponenten.api.HentInntektListeRequest
import no.nav.bidrag.grunnlag.consumer.inntektskomponenten.api.HentInntektRequest
import no.nav.bidrag.grunnlag.exception.custom.UgyldigInputException
import no.nav.bidrag.grunnlag.util.GrunnlagUtil.Companion.JANUAR2015
import no.nav.bidrag.grunnlag.util.GrunnlagUtil.Companion.finnFilter
import no.nav.bidrag.grunnlag.util.GrunnlagUtil.Companion.finnFormaal
import no.nav.bidrag.transport.behandling.grunnlag.response.AinntektGrunnlagDto
import no.nav.bidrag.transport.behandling.grunnlag.response.AinntektspostDto
import no.nav.tjenester.aordningen.inntektsinformasjon.AktoerType
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.YearMonth

class HentAinntektService(
    private val inntektskomponentenService: InntektskomponentenService,
) : List<AinntektGrunnlagDto> by listOf() {

    companion object {
        @JvmStatic
        val LOGGER: Logger = LoggerFactory.getLogger(HentAinntektService::class.java)
    }

    fun hentAinntekt(ainntektRequestListe: List<PersonIdOgPeriodeRequest>, formål: Formål): List<AinntektGrunnlagDto> {
        val ainntektListe = mutableListOf<AinntektGrunnlagDto>()

        ainntektRequestListe.forEach {
            val periodeFra = kalkulerPeriodeFra(it)
            val hentInntektRequestListe = lagInntektListeRequest(
                HentInntektRequest(
                    ident = it.personId,
                    maanedFom = periodeFra,
                    maanedTom = it.periodeTil.minusDays(1).toString().substring(0, 7),
                    ainntektsfilter = finnFilter(formål.name),
                    formaal = finnFormaal(formål.name),
                ),
            )

            hentInntektRequestListe.forEach { hentInntektListeRequest ->
                hentInntekter(hentInntektListeRequest, ainntektListe)
            }
        }

        return ainntektListe
    }

    private fun kalkulerPeriodeFra(personIdOgPeriode: PersonIdOgPeriodeRequest): String {
        return if (personIdOgPeriode.periodeFra.isBefore(LocalDate.parse("2015-01-01"))) {
            LOGGER.info("Ikke tillatt med periodeFra tidligere enn 2015 i request til Ainntekt, overstyres til januar 2015")
            JANUAR2015
        } else {
            personIdOgPeriode.periodeFra.toString().substring(0, 7)
        }
    }

    private fun hentInntekter(hentInntektListeRequest: HentInntektListeRequest, ainntektListe: MutableList<AinntektGrunnlagDto>) {
        val hentInntektListeResponseIntern = inntektskomponentenService.hentInntekt(hentInntektListeRequest)
        if (hentInntektListeResponseIntern.httpStatus.is2xxSuccessful && !hentInntektListeResponseIntern.arbeidsInntektMaanedIntern.isNullOrEmpty()) {
            hentInntektListeResponseIntern.arbeidsInntektMaanedIntern.forEach {
                if (!it.arbeidsInntektInformasjonIntern.inntektIntern.isNullOrEmpty()) {
                    leggTilInntekt(it, ainntektListe, hentInntektListeRequest.ident.identifikator)
                }
            }
        }
    }

    private fun leggTilInntekt(inntektPeriode: ArbeidsInntektMaanedIntern, ainntektListe: MutableList<AinntektGrunnlagDto>, ident: String) {
        val inntektspostListe = mutableListOf<AinntektspostDto>()
        inntektPeriode.arbeidsInntektInformasjonIntern.inntektIntern?.forEach {
            inntektspostListe.add(
                AinntektspostDto(
                    utbetalingsperiode = it.utbetaltIMaaned,
                    opptjeningsperiodeFra = it.opptjeningsperiodeFom,
                    opptjeningsperiodeTil = if (it.opptjeningsperiodeTom != null) {
                        it.opptjeningsperiodeTom.plusMonths(1).withDayOfMonth(1)
                    } else {
                        null
                    },
                    opplysningspliktigId = it.opplysningspliktig?.identifikator,
                    virksomhetId = it.virksomhet?.identifikator,
                    inntektType = it.inntektType,
                    fordelType = it.fordel,
                    beskrivelse = it.beskrivelse,
                    belop = it.beloep,
                    etterbetalingsperiodeFra = it.tilleggsinformasjon?.tilleggsinformasjonDetaljer?.etterbetalingsperiodeFom,
                    etterbetalingsperiodeTil = it.tilleggsinformasjon?.tilleggsinformasjonDetaljer?.etterbetalingsperiodeTom,
                ),
            )
        }

        val inntekt = AinntektGrunnlagDto(
            personId = ident,
            periodeFra = LocalDate.parse(inntektPeriode.aarMaaned + "-01"),
            periodeTil = LocalDate.parse(inntektPeriode.aarMaaned + "-01").plusMonths(1),
            ainntektspostListe = inntektspostListe,
        )

        ainntektListe.add(inntekt)
    }

    private fun lagInntektListeRequest(hentInntektRequest: HentInntektRequest): List<HentInntektListeRequest> {
        var maanedFom = lagYearMonth(hentInntektRequest.maanedFom)
        var aarFom = maanedFom.year
        val aarTom = lagYearMonth(hentInntektRequest.maanedTom).year
        val requestListe = ArrayList<HentInntektListeRequest>()
        while (aarFom < aarTom) {
            requestListe.add(
                HentInntektListeRequest(
                    ident = Aktoer(identifikator = hentInntektRequest.ident, aktoerType = AktoerType.NATURLIG_IDENT.name),
                    maanedFom = maanedFom,
                    maanedTom = YearMonth.of(maanedFom.year, 12),
                    ainntektsfilter = hentInntektRequest.ainntektsfilter,
                    formaal = hentInntektRequest.formaal,
                ),
            )
            aarFom++
            maanedFom = YearMonth.of(aarFom, 1)
        }
        requestListe.add(
            HentInntektListeRequest(
                ident = Aktoer(identifikator = hentInntektRequest.ident, aktoerType = AktoerType.NATURLIG_IDENT.name),
                maanedFom = maanedFom,
                maanedTom = lagYearMonth(hentInntektRequest.maanedTom),
                ainntektsfilter = hentInntektRequest.ainntektsfilter,
                formaal = hentInntektRequest.formaal,
            ),
        )
        return requestListe
    }

    private fun lagYearMonth(aarMaanedString: String): YearMonth {
        if (aarMaanedString.length != 7) {
            throw UgyldigInputException("Ugyldig input i aarMaaned (må være på format ÅÅÅÅ-MM): $aarMaanedString")
        }
        return if (StringUtils.isNumeric(aarMaanedString.substring(0, 4)) && StringUtils.isNumeric(aarMaanedString.substring(5, 7))) {
            YearMonth.of(aarMaanedString.substring(0, 4).toInt(), aarMaanedString.substring(5, 7).toInt())
        } else {
            throw UgyldigInputException("Ugyldig input i aarMaaned (må være på format ÅÅÅÅ-MM): $aarMaanedString")
        }
    }
}
