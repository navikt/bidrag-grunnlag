package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.domene.enums.grunnlag.GrunnlagRequestType
import no.nav.bidrag.grunnlag.consumer.bidragperson.BidragPersonConsumer
import no.nav.bidrag.grunnlag.consumer.familiebasak.FamilieBaSakConsumer
import no.nav.bidrag.grunnlag.consumer.familieefsak.FamilieEfSakConsumer
import no.nav.bidrag.grunnlag.consumer.familiekssak.FamilieKsSakConsumer
import no.nav.bidrag.grunnlag.consumer.pensjon.PensjonConsumer
import no.nav.bidrag.grunnlag.consumer.skattegrunnlag.SigrunConsumer
import no.nav.bidrag.grunnlag.model.OppdaterAinntekt
import no.nav.bidrag.grunnlag.model.OppdaterBarnetillegg
import no.nav.bidrag.grunnlag.model.OppdaterBarnetilsyn
import no.nav.bidrag.grunnlag.model.OppdaterKontantstotte
import no.nav.bidrag.grunnlag.model.OppdaterOvergangsstønad
import no.nav.bidrag.grunnlag.model.OppdaterRelatertePersoner
import no.nav.bidrag.grunnlag.model.OppdaterSivilstand
import no.nav.bidrag.grunnlag.model.OppdaterSkattegrunnlag
import no.nav.bidrag.grunnlag.model.OppdaterUtvidetBarnetrygdOgSmaabarnstillegg
import no.nav.bidrag.transport.behandling.grunnlag.request.GrunnlagRequestDto
import no.nav.bidrag.transport.behandling.grunnlag.request.OppdaterGrunnlagspakkeRequestDto
import no.nav.bidrag.transport.behandling.grunnlag.response.OppdaterGrunnlagDto
import no.nav.bidrag.transport.behandling.grunnlag.response.OppdaterGrunnlagspakkeDto
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class OppdaterGrunnlagspakkeService(
    private val persistenceService: PersistenceService,
    private val familieBaSakConsumer: FamilieBaSakConsumer,
    private val pensjonConsumer: PensjonConsumer,
    private val inntektskomponentenService: InntektskomponentenService,
    private val sigrunConsumer: SigrunConsumer,
    private val bidragPersonConsumer: BidragPersonConsumer,
    private val familieKsSakConsumer: FamilieKsSakConsumer,
    private val familieEfSakConsumer: FamilieEfSakConsumer,
) {
    fun oppdaterGrunnlagspakke(
        grunnlagspakkeId: Int,
        oppdaterGrunnlagspakkeRequestDto: OppdaterGrunnlagspakkeRequestDto,
        timestampOppdatering: LocalDateTime,
    ): OppdaterGrunnlagspakkeDto {
        val oppdaterGrunnlagDtoListe = OppdaterGrunnlagspakke(
            grunnlagspakkeId,
            timestampOppdatering,
        )
            .oppdaterAinntekt(
                hentRequestListeFor(GrunnlagRequestType.AINNTEKT, oppdaterGrunnlagspakkeRequestDto),
            )
            .oppdaterSkattegrunnlag(
                hentRequestListeFor(GrunnlagRequestType.SKATTEGRUNNLAG, oppdaterGrunnlagspakkeRequestDto),
            )
            .oppdaterUtvidetBarnetrygdOgSmaabarnstillegg(
                hentRequestListeFor(
                    GrunnlagRequestType.UTVIDET_BARNETRYGD_OG_SMÅBARNSTILLEGG,
                    oppdaterGrunnlagspakkeRequestDto,
                ),
            )
            .oppdaterBarnetillegg(
                hentRequestListeFor(GrunnlagRequestType.BARNETILLEGG, oppdaterGrunnlagspakkeRequestDto),
            )
            .oppdaterKontantstotte(
                hentRequestListeFor(GrunnlagRequestType.KONTANTSTØTTE, oppdaterGrunnlagspakkeRequestDto),
            )
            .oppdaterHusstandsmedlemmerOgEgneBarn(
                hentRequestListeFor(GrunnlagRequestType.HUSSTANDSMEDLEMMER_OG_EGNE_BARN, oppdaterGrunnlagspakkeRequestDto),
            )
            .oppdaterSivilstand(
                hentRequestListeFor(GrunnlagRequestType.SIVILSTAND, oppdaterGrunnlagspakkeRequestDto),
            )
            .oppdaterBarnetilsyn(
                hentRequestListeFor(GrunnlagRequestType.BARNETILSYN, oppdaterGrunnlagspakkeRequestDto),
            )
            .oppdaterOvergangsstønad(
                hentRequestListeFor(GrunnlagRequestType.OVERGANGSSTONAD, oppdaterGrunnlagspakkeRequestDto),
            )

        return OppdaterGrunnlagspakkeDto(grunnlagspakkeId, oppdaterGrunnlagDtoListe)
    }

    private fun hentRequestListeFor(
        type: GrunnlagRequestType,
        oppdaterGrunnlagspakkeRequestDto: OppdaterGrunnlagspakkeRequestDto,
    ): List<PersonIdOgPeriodeRequest> {
        val grunnlagRequestListe = mutableListOf<PersonIdOgPeriodeRequest>()
        oppdaterGrunnlagspakkeRequestDto.grunnlagRequestDtoListe.forEach {
            if (it.type == type) {
                grunnlagRequestListe.add(nyPersonIdOgPeriode(it))
            }
        }
        return grunnlagRequestListe
    }

    private fun nyPersonIdOgPeriode(grunnlagRequestDto: GrunnlagRequestDto) = PersonIdOgPeriodeRequest(
        personId = grunnlagRequestDto.personId,
        periodeFra = grunnlagRequestDto.periodeFra,
        periodeTil = grunnlagRequestDto.periodeTil,
    )

    inner class OppdaterGrunnlagspakke(
        private val grunnlagspakkeId: Int,
        private val timestampOppdatering: LocalDateTime,
    ) : MutableList<OppdaterGrunnlagDto> by mutableListOf() {

        fun oppdaterAinntekt(ainntektRequestListe: List<PersonIdOgPeriodeRequest>): OppdaterGrunnlagspakke {
            this.addAll(
                OppdaterAinntekt(
                    grunnlagspakkeId,
                    timestampOppdatering,
                    persistenceService,
                    inntektskomponentenService,
                )
                    .oppdaterAinntekt(ainntektRequestListe),
            )
            return this
        }

        fun oppdaterSkattegrunnlag(skattegrunnlagRequestListe: List<PersonIdOgPeriodeRequest>): OppdaterGrunnlagspakke {
            this.addAll(
                OppdaterSkattegrunnlag(
                    grunnlagspakkeId,
                    timestampOppdatering,
                    persistenceService,
                    sigrunConsumer,
                )
                    .oppdaterSkattegrunnlag(skattegrunnlagRequestListe),
            )
            return this
        }

        fun oppdaterUtvidetBarnetrygdOgSmaabarnstillegg(
            utvidetBarnetrygdOgSmaabarnstilleggRequestListe: List<PersonIdOgPeriodeRequest>,
        ): OppdaterGrunnlagspakke {
            this.addAll(
                OppdaterUtvidetBarnetrygdOgSmaabarnstillegg(
                    grunnlagspakkeId,
                    timestampOppdatering,
                    persistenceService,
                    familieBaSakConsumer,
                )
                    .oppdaterUtvidetBarnetrygdOgSmaabarnstillegg(
                        utvidetBarnetrygdOgSmaabarnstilleggRequestListe,
                    ),
            )
            return this
        }

        fun oppdaterBarnetillegg(barnetilleggRequestListe: List<PersonIdOgPeriodeRequest>): OppdaterGrunnlagspakke {
            this.addAll(
                OppdaterBarnetillegg(
                    grunnlagspakkeId,
                    timestampOppdatering,
                    persistenceService,
                    pensjonConsumer,
                )
                    .oppdaterBarnetillegg(barnetilleggRequestListe),
            )
            return this
        }

        fun oppdaterKontantstotte(kontantstotteRequestListe: List<PersonIdOgPeriodeRequest>): OppdaterGrunnlagspakke {
            this.addAll(
                OppdaterKontantstotte(
                    grunnlagspakkeId,
                    timestampOppdatering,
                    persistenceService,
                    familieKsSakConsumer,
                )
                    .oppdaterKontantstotte(kontantstotteRequestListe),
            )
            return this
        }

        fun oppdaterHusstandsmedlemmerOgEgneBarn(relatertePersonerRequestListe: List<PersonIdOgPeriodeRequest>): OppdaterGrunnlagspakke {
            this.addAll(
                OppdaterRelatertePersoner(
                    grunnlagspakkeId,
                    timestampOppdatering,
                    persistenceService,
                    bidragPersonConsumer,
                )
                    .oppdaterRelatertePersoner(relatertePersonerRequestListe),
            )
            return this
        }

        fun oppdaterSivilstand(sivilstandRequestListe: List<PersonIdOgPeriodeRequest>): OppdaterGrunnlagspakke {
            this.addAll(
                OppdaterSivilstand(
                    grunnlagspakkeId,
                    timestampOppdatering,
                    persistenceService,
                    bidragPersonConsumer,
                )
                    .oppdaterSivilstand(sivilstandRequestListe),
            )
            return this
        }

        fun oppdaterBarnetilsyn(barnetilsynRequestListe: List<PersonIdOgPeriodeRequest>): OppdaterGrunnlagspakke {
            this.addAll(
                OppdaterBarnetilsyn(
                    grunnlagspakkeId,
                    timestampOppdatering,
                    persistenceService,
                    familieEfSakConsumer,
                )
                    .oppdaterBarnetilsyn(barnetilsynRequestListe),
            )
            return this
        }

        fun oppdaterOvergangsstønad(overgangsstønadRequestListe: List<PersonIdOgPeriodeRequest>): OppdaterGrunnlagspakke {
            this.addAll(
                OppdaterOvergangsstønad(
                    grunnlagspakkeId,
                    timestampOppdatering,
                    persistenceService,
                    familieEfSakConsumer,
                )
                    .oppdaterOvergangsstønad(overgangsstønadRequestListe),
            )
            return this
        }
    }
}
