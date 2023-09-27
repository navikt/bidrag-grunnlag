package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.domain.enums.GrunnlagRequestType
import no.nav.bidrag.grunnlag.consumer.arbeidsforhold.ArbeidsforholdConsumer
import no.nav.bidrag.grunnlag.consumer.arbeidsforhold.EnhetsregisterConsumer
import no.nav.bidrag.grunnlag.model.HentArbeidsforhold
import no.nav.bidrag.transport.behandling.grunnlag.request.GrunnlagRequestDto
import no.nav.bidrag.transport.behandling.grunnlag.request.HentGrunnlagRequestDto
import no.nav.bidrag.transport.behandling.grunnlag.response.ArbeidsforholdDto
import no.nav.bidrag.transport.behandling.grunnlag.response.HentGrunnlagDto
import org.springframework.stereotype.Service

@Service
class HentGrunnlagService(
    private val arbeidsforholdConsumer: ArbeidsforholdConsumer,
    private val enhetsregisterConsumer: EnhetsregisterConsumer
) {
    fun hentGrunnlag(hentGrunnlagRequestDto: HentGrunnlagRequestDto): HentGrunnlagDto {
        val hentGrunnlagDtoliste = HentGrunnlag()
            .hentArbeidsforhold(
                hentRequestListeFor(GrunnlagRequestType.ARBEIDSFORHOLD, hentGrunnlagRequestDto)
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

    inner class HentGrunnlag() : MutableList<ArbeidsforholdDto> by mutableListOf() {
        fun hentArbeidsforhold(arbeidsforholdRequestListe: List<PersonIdOgPeriodeRequest>): HentGrunnlag {
            this.addAll(
                HentArbeidsforhold(
                    arbeidsforholdConsumer,
                    enhetsregisterConsumer
                )
                    .hentArbeidsforhold(arbeidsforholdRequestListe)
            )
            return this
        }
    }
}
