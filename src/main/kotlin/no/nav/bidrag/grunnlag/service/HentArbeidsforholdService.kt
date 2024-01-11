package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.consumer.arbeidsforhold.ArbeidsforholdConsumer
import no.nav.bidrag.grunnlag.consumer.arbeidsforhold.EnhetsregisterConsumer
import no.nav.bidrag.grunnlag.consumer.arbeidsforhold.api.Arbeidsforhold
import no.nav.bidrag.grunnlag.consumer.arbeidsforhold.api.Arbeidssted
import no.nav.bidrag.grunnlag.consumer.arbeidsforhold.api.HentArbeidsforholdRequest
import no.nav.bidrag.grunnlag.consumer.arbeidsforhold.api.HentEnhetsregisterRequest
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.transport.behandling.grunnlag.response.Ansettelsesdetaljer
import no.nav.bidrag.transport.behandling.grunnlag.response.ArbeidsforholdGrunnlagDto
import no.nav.bidrag.transport.behandling.grunnlag.response.Permisjon
import no.nav.bidrag.transport.behandling.grunnlag.response.Permittering

// @Service
class HentArbeidsforholdService(
    private val arbeidsforholdConsumer: ArbeidsforholdConsumer,
    private val enhetsregisterConsumer: EnhetsregisterConsumer,
) : List<ArbeidsforholdGrunnlagDto> by listOf() {

    fun hentArbeidsforhold(arbeidsforholdRequestListe: List<PersonIdOgPeriodeRequest>): List<ArbeidsforholdGrunnlagDto> {
        val arbeidsforholdListe = mutableListOf<ArbeidsforholdGrunnlagDto>()

        arbeidsforholdRequestListe.forEach {
            val hentArbeidsforholdRequest = HentArbeidsforholdRequest(
                arbeidstakerId = it.personId,
            )

            when (
                val restResponseArbeidsforhold =
                    arbeidsforholdConsumer.hentArbeidsforhold(hentArbeidsforholdRequest)
            ) {
                is RestResponse.Success -> {
                    SECURE_LOGGER.info("Aareg hent arbeidsforhold ga følgende respons: ${restResponseArbeidsforhold.body}")
                    leggTilArbeidsforhold(arbeidsforholdListe, restResponseArbeidsforhold.body, it.personId)
                }

                is RestResponse.Failure -> {
                    return emptyList()
                }
            }
        }
        return arbeidsforholdListe
    }

    private fun leggTilArbeidsforhold(
        arbeidsforholdListe: MutableList<ArbeidsforholdGrunnlagDto>,
        arbeidsforholdResponsListe: List<Arbeidsforhold>,
        ident: String,
    ) {
        arbeidsforholdResponsListe.forEach { arbeidsforhold ->

            val ansettelsesdetaljerListe = mutableListOf<Ansettelsesdetaljer>()
            val permisjonListe = mutableListOf<Permisjon>()
            val permitteringListe = mutableListOf<Permittering>()
            val arbeidsgiverinfo = finnArbeidsgiverinfo(arbeidsforhold.arbeidssted)

            arbeidsforhold.ansettelsesdetaljer?.forEach {
                ansettelsesdetaljerListe.add(
                    Ansettelsesdetaljer(
                        periodeFra = it.rapporteringsmaaneder?.fra,
                        periodeTil = it.rapporteringsmaaneder?.til,
                        arbeidsforholdType = it.type,
                        arbeidstidsordningBeskrivelse = it.arbeidstidsordning?.beskrivelse,
                        ansettelsesformBeskrivelse = it.ansettelsesform?.beskrivelse,
                        yrkeBeskrivelse = it.yrke?.beskrivelse,
                        antallTimerPrUke = it.antallTimerPrUke,
                        avtaltStillingsprosent = it.avtaltStillingsprosent,
                        sisteStillingsprosentendringDato = it.sisteStillingsprosentendring,
                        sisteLønnsendringDato = it.sisteLoennsendring,
                    ),
                )
            }

            arbeidsforhold.permisjoner?.forEach {
                permisjonListe.add(
                    Permisjon(
                        startdato = it.startdato,
                        sluttdato = it.sluttdato,
                        beskrivelse = it.type?.beskrivelse,
                        prosent = it.prosent,
                    ),
                )
            }

            arbeidsforhold.permitteringer?.forEach {
                permitteringListe.add(
                    Permittering(
                        startdato = it.startdato,
                        sluttdato = it.sluttdato,
                        beskrivelse = it.type?.beskrivelse,
                        prosent = it.prosent,
                    ),
                )
            }

            arbeidsforholdListe.add(
                ArbeidsforholdGrunnlagDto(
                    partPersonId = ident,
                    startdato = arbeidsforhold.ansettelsesperiode?.startdato,
                    sluttdato = arbeidsforhold.ansettelsesperiode?.sluttdato,
                    arbeidsgiverNavn = arbeidsgiverinfo?.navn,
                    arbeidsgiverOrgnummer = arbeidsgiverinfo?.orgnr,
                    ansettelsesdetaljerListe = ansettelsesdetaljerListe,
                    permisjonListe = permisjonListe,
                    permitteringListe = permitteringListe,
                ),
            )
        }
    }

    private fun finnArbeidsgiverinfo(arbeidssted: Arbeidssted?): Arbeidsgiverinfo? {
        return if (arbeidssted?.type == "Underenhet") {
            val orgnr = arbeidssted.identer?.filter { it.type == "ORGANISASJONSNUMMER" }?.get(0)?.ident
            var navn: String? = null

            if (orgnr != null) {
                when (val restResponseEnhetsregister = enhetsregisterConsumer.hentEnhetsinfo(HentEnhetsregisterRequest(orgnr))) {
                    is RestResponse.Success -> {
                        navn = restResponseEnhetsregister.body.navn?.navnelinje1
                    }

                    else -> {
                        SECURE_LOGGER.info("Feil ved hent av arbeidsgivernavn fra Enhetsregisteret for orgnr: $orgnr")
                    }
                }
            }

            Arbeidsgiverinfo(
                orgnr = orgnr,
                navn = navn,
            )
        } else {
            null
        }
    }
}

data class Arbeidsgiverinfo(
    val orgnr: String?,
    val navn: String?,
)
