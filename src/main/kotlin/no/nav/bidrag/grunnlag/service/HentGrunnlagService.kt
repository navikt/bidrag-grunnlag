package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.behandling.felles.dto.grunnlag.GrunnlagRequestDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.HentGrunnlagDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.HentGrunnlagRequestDto
import no.nav.bidrag.behandling.felles.enums.GrunnlagRequestType
import no.nav.bidrag.behandling.felles.enums.GrunnlagRequestType.ARBEIDSFORHOLD
import no.nav.bidrag.grunnlag.consumer.arbeidsforhold.ArbeidsforholdConsumer
import no.nav.bidrag.grunnlag.model.HentArbeidsforhold
import org.springframework.stereotype.Service

@Service
class HentGrunnlagService(
    private val arbeidsforholdConsumer: ArbeidsforholdConsumer
) {
    fun hentGrunnlag(hentGrunnlagRequestDto: HentGrunnlagRequestDto): HentGrunnlagDto {
        val hentGrunnlagDtoliste = HentGrunnlag()
            .hentArbeidsforhold(
                hentRequestListeFor(ARBEIDSFORHOLD, hentGrunnlagRequestDto)
            )

        return HentGrunnlagDto(hentGrunnlagDtoliste)
    }

    private fun hentRequestListeFor(
        type: GrunnlagRequestType,
        hentGrunnlagRequestDto: HentGrunnlagRequestDto
    ): List<PersonIdOgPeriodeRequest> {
        val grunnlagRequestListe = mutableListOf<PersonIdOgPeriodeRequest>()
        hentGrunnlagRequestDto.grunnlagRequestDtoListe.forEach {
            if (it.type == type) {
                grunnlagRequestListe.add(nyPersonIdOgPeriode(it))
            }
        }
        return grunnlagRequestListe
    }

    private fun nyPersonIdOgPeriode(grunnlagRequestDto: GrunnlagRequestDto) =
        PersonIdOgPeriodeRequest(
            personId = grunnlagRequestDto.personId,
            periodeFra = grunnlagRequestDto.periodeFra,
            periodeTil = grunnlagRequestDto.periodeTil
        )

    inner class HentGrunnlag() : MutableList<HentGrunnlagDto> by mutableListOf() {
        fun hentArbeidsforhold(arbeidsforholdRequestListe: List<PersonIdOgPeriodeRequest>): HentGrunnlag {
            this.addAll(
                HentArbeidsforhold(
                    arbeidsforholdConsumer
                )
                    .hentArbeidsforhold(arbeidsforholdRequestListe)
            )
            return this
        }
    }
}

