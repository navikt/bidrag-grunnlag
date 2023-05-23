package no.nav.bidrag.grunnlag.model

import no.nav.bidrag.behandling.felles.dto.grunnlag.OppdaterGrunnlagDto
import no.nav.bidrag.behandling.felles.enums.Formaal
import no.nav.bidrag.behandling.felles.enums.GrunnlagRequestType
import no.nav.bidrag.behandling.felles.enums.GrunnlagsRequestStatus
import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.bo.AinntektBo
import no.nav.bidrag.grunnlag.bo.AinntektspostBo
import no.nav.bidrag.grunnlag.comparator.PeriodComparable
import no.nav.bidrag.grunnlag.consumer.inntektskomponenten.api.Aktoer
import no.nav.bidrag.grunnlag.consumer.inntektskomponenten.api.HentInntektListeRequest
import no.nav.bidrag.grunnlag.consumer.inntektskomponenten.api.HentInntektRequest
import no.nav.bidrag.grunnlag.exception.custom.UgyldigInputException
import no.nav.bidrag.grunnlag.service.InntektskomponentenService
import no.nav.bidrag.grunnlag.service.PersistenceService
import no.nav.bidrag.grunnlag.service.PersonIdOgPeriodeRequest
import no.nav.tjenester.aordningen.inntektsinformasjon.AktoerType
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

class OppdaterAinntekt(
    private val grunnlagspakkeId: Int,
    private val timestampOppdatering: LocalDateTime,
    private val persistenceService: PersistenceService,
    private val inntektskomponentenService: InntektskomponentenService
) : MutableList<OppdaterGrunnlagDto> by mutableListOf() {

    companion object {

        @JvmStatic
        val LOGGER: Logger = LoggerFactory.getLogger(OppdaterAinntekt::class.java)

        const val BIDRAG_FILTER = "BidragA-Inntekt"
        const val FORSKUDD_FILTER = "BidragsforskuddA-Inntekt"
        const val BIDRAG_FORMAAL = "Bidrag"
        const val FORSKUDD_FORMAAL = "Bidragsforskudd"
        const val JANUAR2015 = "2015-01"
    }

    fun oppdaterAinntekt(ainntektRequestListe: List<PersonIdOgPeriodeRequest>): OppdaterAinntekt {
        val formaal = persistenceService.hentFormaalGrunnlagspakke(grunnlagspakkeId)

        ainntektRequestListe.forEach { personIdOgPeriode ->

            // Inntektskomponenten returner Bad request ved spørring på inntekter tidligere enn 2015, overstyrer derfor periodeFra til
            // 2015.01 hvis periodeFra er tidligere enn det. Hvis periodeTil er før januar 2015 så gjøres det ikke et kall.
            //
            if (personIdOgPeriode.periodeTil.isBefore(LocalDate.of(2015, 1, 1))) {
                LOGGER.info("Ugyldig periode angitt i HentInntektRequest (Inntektskomponenten). PeriodeTil må være januar 2015 eller senere")
                SECURE_LOGGER.info("Ugyldig periode angitt i HentInntektRequest (Inntektskomponenten). PeriodeTil må være januar 2015 eller senere: $personIdOgPeriode")
            } else {
                val periodeFra: String
                if (personIdOgPeriode.periodeFra.isBefore(LocalDate.parse("2015-01-01"))) {
                    periodeFra = JANUAR2015
                    LOGGER.info("For gammel periodeFra angitt i HentInntektRequest (Inntektskomponenten), overstyres til januar 2015")
                    SECURE_LOGGER.info("For gammel periodeFra angitt i HentInntektRequest (Inntektskomponenten), overstyres til januar 2015: $personIdOgPeriode")
                } else {
                    periodeFra = personIdOgPeriode.periodeFra.toString().substring(0, 7)
                }

                val hentInntektRequest = HentInntektRequest(
                    ident = personIdOgPeriode.personId,
                    maanedFom = periodeFra,
                    maanedTom = personIdOgPeriode.periodeTil.minusDays(1).toString().substring(0, 7),
                    ainntektsfilter = finnFilter(formaal),
                    formaal = finnFormaal(formaal)
                )

                val hentInntektListeRequestListe = lagInntektListeRequest(hentInntektRequest)
                val nyeAinntekter = mutableListOf<PeriodComparable<AinntektBo, AinntektspostBo>>()

                hentInntektListeRequestListe.forEach { hentInntektListeRequest ->
                    // Henter inntekter for ett og ett år (litt uvisst hvorfor det er løst slik)

                    SECURE_LOGGER.info("Kaller InntektskomponentenService med request: $hentInntektListeRequest")

                    val hentInntektListeResponseIntern = inntektskomponentenService.hentInntekt(hentInntektListeRequest)
                    SECURE_LOGGER.info("Inntektskomponenten ga følgende respons: $hentInntektListeResponseIntern")

                    if (hentInntektListeResponseIntern.httpStatus.is2xxSuccessful) {
                        var antallPerioderFunnet = 0

                        if (hentInntektListeResponseIntern.arbeidsInntektMaanedIntern.isNullOrEmpty()) {
                            this.add(
                                OppdaterGrunnlagDto(
                                    GrunnlagRequestType.AINNTEKT,
                                    hentInntektListeRequest.ident.identifikator,
                                    GrunnlagsRequestStatus.HENTET,
                                    "Ingen inntekter funnet for periode ${hentInntektListeRequest.maanedFom} - ${hentInntektListeRequest.maanedTom}. Evt. eksisterende perioder vil bli satt til inaktive."
                                )
                            )
                        } else {
                            hentInntektListeResponseIntern.arbeidsInntektMaanedIntern.forEach { inntektPeriode ->

                                if (!inntektPeriode.arbeidsInntektInformasjonIntern.inntektIntern.isNullOrEmpty()) {
                                    antallPerioderFunnet++
                                    val inntekt = AinntektBo(
                                        grunnlagspakkeId = grunnlagspakkeId,
                                        personId = hentInntektListeRequest.ident.identifikator,
                                        periodeFra = LocalDate.parse(inntektPeriode.aarMaaned + "-01"),
                                        // justerer frem tildato med én dag for å ha lik logikk som resten av appen. Tildato skal angis som til, men ikke inkludert, dato.
                                        periodeTil = LocalDate.parse(inntektPeriode.aarMaaned + "-01").plusMonths(1),
                                        aktiv = true,
                                        brukFra = timestampOppdatering,
                                        brukTil = null,
                                        hentetTidspunkt = timestampOppdatering
                                    )

                                    val inntektsposter = mutableListOf<AinntektspostBo>()
                                    inntektPeriode.arbeidsInntektInformasjonIntern.inntektIntern?.forEach { inntektspost ->
                                        inntektsposter.add(
                                            AinntektspostBo(
                                                utbetalingsperiode = inntektspost.utbetaltIMaaned,
                                                opptjeningsperiodeFra = if (inntektspost.opptjeningsperiodeFom != null) inntektspost.opptjeningsperiodeFom else null,
                                                opptjeningsperiodeTil =
                                                if (inntektspost.opptjeningsperiodeTom != null) {
                                                    inntektspost.opptjeningsperiodeTom
                                                        .plusMonths(1)
                                                } else {
                                                    null
                                                },
                                                opplysningspliktigId = inntektspost.opplysningspliktig?.identifikator,
                                                virksomhetId = inntektspost.virksomhet?.identifikator,
                                                inntektType = inntektspost.inntektType,
                                                fordelType = inntektspost.fordel,
                                                beskrivelse = inntektspost.beskrivelse,
                                                belop = inntektspost.beloep,
                                                etterbetalingsperiodeFra = inntektspost.tilleggsinformasjon?.tilleggsinformasjonDetaljer?.etterbetalingsperiodeFom,
                                                etterbetalingsperiodeTil = inntektspost.tilleggsinformasjon?.tilleggsinformasjonDetaljer?.etterbetalingsperiodeTom

                                            )
                                        )
                                    }
                                    nyeAinntekter.add(PeriodComparable(inntekt, inntektsposter))
                                }
                            }
                            if (antallPerioderFunnet.equals(0)) {
                                this.add(
                                    OppdaterGrunnlagDto(
                                        GrunnlagRequestType.AINNTEKT,
                                        hentInntektListeRequest.ident.identifikator,
                                        GrunnlagsRequestStatus.HENTET,
                                        "Ingen inntekter funnet for periode ${hentInntektListeRequest.maanedFom} - ${hentInntektListeRequest.maanedTom}. Evt. eksisterende perioder vil bli satt til inaktive."
                                    )
                                )
                            } else {
                                this.add(
                                    OppdaterGrunnlagDto(
                                        GrunnlagRequestType.AINNTEKT,
                                        hentInntektListeRequest.ident.identifikator,
                                        GrunnlagsRequestStatus.HENTET,
                                        "Antall inntekter funnet for periode ${hentInntektListeRequest.maanedFom} - ${hentInntektListeRequest.maanedTom}: $antallPerioderFunnet"
                                    )
                                )
                            }
                        }
                    } else {
                        this.add(
                            OppdaterGrunnlagDto(
                                GrunnlagRequestType.AINNTEKT,
                                hentInntektListeRequest.ident.identifikator,
                                if (hentInntektListeResponseIntern.httpStatus == HttpStatus.NOT_FOUND) GrunnlagsRequestStatus.IKKE_FUNNET else GrunnlagsRequestStatus.FEILET,
                                "Feil ved henting av inntekter for periode ${hentInntektListeRequest.maanedFom} - ${hentInntektListeRequest.maanedTom}."
                            )
                        )
                    }
                }

                // Evt. nye perioder opprettes og evt. eksisterende perioder som ikke finnes i den nye responsen vil bli satt til aktiv=false
                persistenceService.oppdaterAinntektForGrunnlagspakke(
                    grunnlagspakkeId,
                    nyeAinntekter,
                    personIdOgPeriode.periodeFra,
                    personIdOgPeriode.periodeTil,
                    personIdOgPeriode.personId,
                    timestampOppdatering
                )
            }
        }
        return this
    }

    private fun finnFilter(formaal: String): String {
        return if (formaal == Formaal.FORSKUDD.toString()) FORSKUDD_FILTER else BIDRAG_FILTER
    }

    private fun finnFormaal(formaal: String): String {
        return if (formaal == Formaal.FORSKUDD.toString()) FORSKUDD_FORMAAL else BIDRAG_FORMAAL
    }

    fun lagInntektListeRequest(hentInntektRequest: HentInntektRequest): List<HentInntektListeRequest> {
        var maanedFom = lagYearMonth(hentInntektRequest.maanedFom)
        var aarFom = maanedFom.year
        val aarTom = lagYearMonth(hentInntektRequest.maanedTom).year
        val requestListe = ArrayList<HentInntektListeRequest>()
        while (aarFom < aarTom) {
            requestListe.add(
                HentInntektListeRequest(
                    Aktoer(hentInntektRequest.ident, AktoerType.NATURLIG_IDENT.name),
                    maanedFom,
                    YearMonth.of(maanedFom.year, 12),
                    hentInntektRequest.ainntektsfilter,
                    hentInntektRequest.formaal
                )
            )
            aarFom++
            maanedFom = YearMonth.of(aarFom, 1)
        }
        requestListe.add(
            HentInntektListeRequest(
                Aktoer(hentInntektRequest.ident, AktoerType.NATURLIG_IDENT.name),
                maanedFom,
                lagYearMonth(hentInntektRequest.maanedTom),
                hentInntektRequest.ainntektsfilter,
                hentInntektRequest.formaal
            )
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
