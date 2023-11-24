package no.nav.bidrag.grunnlag.model

import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.consumer.arbeidsforhold.ArbeidsforholdConsumer
import no.nav.bidrag.grunnlag.consumer.arbeidsforhold.EnhetsregisterConsumer
import no.nav.bidrag.grunnlag.consumer.arbeidsforhold.api.Arbeidssted
import no.nav.bidrag.grunnlag.consumer.arbeidsforhold.api.HentArbeidsforholdRequest
import no.nav.bidrag.grunnlag.consumer.arbeidsforhold.api.HentEnhetsregisterRequest
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.service.PersonIdOgPeriodeRequest
import no.nav.bidrag.transport.behandling.grunnlag.response.ArbeidsforholdDto
import no.nav.bidrag.transport.behandling.grunnlag.response.HentGrunnlagDto
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

class HentArbeidsforhold(
    private val arbeidsforholdConsumer: ArbeidsforholdConsumer,
    private val enhetsregisterConsumer: EnhetsregisterConsumer,
) : MutableList<HentGrunnlagDto> by mutableListOf() {

    companion object {
        @JvmStatic
        private val LOGGER: Logger = LoggerFactory.getLogger(HentArbeidsforhold::class.java)
    }

    fun hentArbeidsforhold(arbeidsforholdRequestListe: List<PersonIdOgPeriodeRequest>): List<ArbeidsforholdDto> {
        val arbeidsforholdListe = mutableListOf<ArbeidsforholdDto>()

        arbeidsforholdRequestListe.forEach { personIdOgPeriode ->
            val hentArbeidsforholdRequest = HentArbeidsforholdRequest(
                arbeidstakerId = personIdOgPeriode.personId,
            )

            LOGGER.info("Kaller Aareg for å hente arbeidsforhold")
            SECURE_LOGGER.info("Kaller Aareg request: $hentArbeidsforholdRequest")

            when (
                val restResponseArbeidsforhold =
                    arbeidsforholdConsumer.hentArbeidsforhold(hentArbeidsforholdRequest)
            ) {
                is RestResponse.Success -> {
                    val arbeidsforholdResponse = restResponseArbeidsforhold.body

                    SECURE_LOGGER.info("Aareg hent arbeidsforhold ga følgende respons: $arbeidsforholdResponse")

                    arbeidsforholdResponse.forEach { arbeidsforhold ->

                        val ansettelsesdetaljerListe = mutableListOf<no.nav.bidrag.transport.behandling.grunnlag.response.Ansettelsesdetaljer>()
                        val permisjonListe = mutableListOf<no.nav.bidrag.transport.behandling.grunnlag.response.Permisjon>()
                        val permitteringListe = mutableListOf<no.nav.bidrag.transport.behandling.grunnlag.response.Permittering>()
                        val arbeidsgiverinfo = finnArbeidsgiverinfo(arbeidsforhold.arbeidssted)
                        val arbeidsgiverNavn = arbeidsgiverinfo?.navn
                        val orgnr = arbeidsgiverinfo?.orgnr

                        arbeidsforhold.ansettelsesdetaljer?.forEach {
                            ansettelsesdetaljerListe.add(
                                no.nav.bidrag.transport.behandling.grunnlag.response.Ansettelsesdetaljer(
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
                                no.nav.bidrag.transport.behandling.grunnlag.response.Permisjon(
                                    startdato = it.startdato,
                                    sluttdato = it.sluttdato,
                                    beskrivelse = it.type?.beskrivelse,
                                    prosent = it.prosent,

                                ),
                            )
                        }

                        arbeidsforhold.permitteringer?.forEach {
                            permitteringListe.add(
                                no.nav.bidrag.transport.behandling.grunnlag.response.Permittering(
                                    startdato = it.startdato,
                                    sluttdato = it.sluttdato,
                                    beskrivelse = it.type?.beskrivelse,
                                    prosent = it.prosent,
                                ),
                            )
                        }

                        arbeidsforholdListe.add(
                            ArbeidsforholdDto(
                                partPersonId = personIdOgPeriode.personId,
                                startdato = arbeidsforhold.ansettelsesperiode?.startdato,
                                sluttdato = arbeidsforhold.ansettelsesperiode?.sluttdato,
                                arbeidsgiverNavn = arbeidsgiverNavn,
                                arbeidsgiverOrgnummer = orgnr,
                                ansettelsesdetaljer = ansettelsesdetaljerListe,
                                permisjoner = permisjonListe,
                                permitteringer = permitteringListe,
                                hentetTidspunkt = LocalDateTime.now(),
                            ),
                        )
                    }
                    return arbeidsforholdListe
                }

                is RestResponse.Failure -> {
                    if (restResponseArbeidsforhold.statusCode == HttpStatus.NOT_FOUND) {
                        SECURE_LOGGER.info("Ingen arbeidsforhold funnet for ${personIdOgPeriode.personId}")
                    }
                    return arbeidsforholdListe
                }
            }
        }
        return arbeidsforholdListe
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
                        SECURE_LOGGER.info("Feil ved hent av arbeidsgrivernavn fra Enhetsregisteret for orgnr: $orgnr")
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
