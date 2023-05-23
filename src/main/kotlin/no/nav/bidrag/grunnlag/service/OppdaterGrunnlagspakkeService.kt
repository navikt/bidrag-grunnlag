package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.behandling.felles.dto.grunnlag.GrunnlagRequestDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.OppdaterGrunnlagDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.OppdaterGrunnlagspakkeDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.OppdaterGrunnlagspakkeRequestDto
import no.nav.bidrag.behandling.felles.enums.GrunnlagRequestType
import no.nav.bidrag.behandling.felles.enums.GrunnlagRequestType.AINNTEKT
import no.nav.bidrag.behandling.felles.enums.GrunnlagRequestType.BARNETILLEGG
import no.nav.bidrag.behandling.felles.enums.GrunnlagRequestType.BARNETILSYN
import no.nav.bidrag.behandling.felles.enums.GrunnlagRequestType.HUSSTANDSMEDLEMMER_OG_EGNE_BARN
import no.nav.bidrag.behandling.felles.enums.GrunnlagRequestType.KONTANTSTOTTE
import no.nav.bidrag.behandling.felles.enums.GrunnlagRequestType.OVERGANGSSTONAD
import no.nav.bidrag.behandling.felles.enums.GrunnlagRequestType.SIVILSTAND
import no.nav.bidrag.behandling.felles.enums.GrunnlagRequestType.SKATTEGRUNNLAG
import no.nav.bidrag.behandling.felles.enums.GrunnlagRequestType.UTVIDET_BARNETRYGD_OG_SMAABARNSTILLEGG
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.BidragGcpProxyConsumer
import no.nav.bidrag.grunnlag.consumer.bidragperson.BidragPersonConsumer
import no.nav.bidrag.grunnlag.consumer.familiebasak.FamilieBaSakConsumer
import no.nav.bidrag.grunnlag.consumer.familieefsak.FamilieEfSakConsumer
import no.nav.bidrag.grunnlag.consumer.familiekssak.FamilieKsSakConsumer
import no.nav.bidrag.grunnlag.model.OppdaterAinntekt
import no.nav.bidrag.grunnlag.model.OppdaterBarnetillegg
import no.nav.bidrag.grunnlag.model.OppdaterBarnetilsyn
import no.nav.bidrag.grunnlag.model.OppdaterKontantstotte
import no.nav.bidrag.grunnlag.model.OppdaterOvergangsstønad
import no.nav.bidrag.grunnlag.model.OppdaterRelatertePersoner
import no.nav.bidrag.grunnlag.model.OppdaterSivilstand
import no.nav.bidrag.grunnlag.model.OppdaterSkattegrunnlag
import no.nav.bidrag.grunnlag.model.OppdaterUtvidetBarnetrygdOgSmaabarnstillegg
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class OppdaterGrunnlagspakkeService(
    private val persistenceService: PersistenceService,
    private val familieBaSakConsumer: FamilieBaSakConsumer,
    private val bidragGcpProxyConsumer: BidragGcpProxyConsumer,
    private val inntektskomponentenService: InntektskomponentenService,
    private val bidragPersonConsumer: BidragPersonConsumer,
    private val familieKsSakConsumer: FamilieKsSakConsumer,
    private val familieEfSakConsumer: FamilieEfSakConsumer
) {
    fun oppdaterGrunnlagspakke(
        grunnlagspakkeId: Int,
        oppdaterGrunnlagspakkeRequestDto: OppdaterGrunnlagspakkeRequestDto,
        timestampOppdatering: LocalDateTime
    ): OppdaterGrunnlagspakkeDto {
        val oppdaterGrunnlagDtoListe = OppdaterGrunnlagspakke(
            grunnlagspakkeId,
            timestampOppdatering
        )
            .oppdaterAinntekt(
                hentRequestListeFor(AINNTEKT, oppdaterGrunnlagspakkeRequestDto)
            )
            .oppdaterSkattegrunnlag(
                hentRequestListeFor(SKATTEGRUNNLAG, oppdaterGrunnlagspakkeRequestDto)
            )
            .oppdaterUtvidetBarnetrygdOgSmaabarnstillegg(
                hentRequestListeFor(
                    UTVIDET_BARNETRYGD_OG_SMAABARNSTILLEGG,
                    oppdaterGrunnlagspakkeRequestDto
                )
            )
            .oppdaterBarnetillegg(
                hentRequestListeFor(BARNETILLEGG, oppdaterGrunnlagspakkeRequestDto)
            )
            .oppdaterKontantstotte(
                hentRequestListeFor(KONTANTSTOTTE, oppdaterGrunnlagspakkeRequestDto)
            )
            .oppdaterHusstandsmedlemmerOgEgneBarn(
                hentRequestListeFor(HUSSTANDSMEDLEMMER_OG_EGNE_BARN, oppdaterGrunnlagspakkeRequestDto)
            )
            .oppdaterSivilstand(
                hentRequestListeFor(SIVILSTAND, oppdaterGrunnlagspakkeRequestDto)
            )
            .oppdaterBarnetilsyn(
                hentRequestListeFor(BARNETILSYN, oppdaterGrunnlagspakkeRequestDto)
            )
            .oppdaterOvergangsstønad(
                hentRequestListeFor(OVERGANGSSTONAD, oppdaterGrunnlagspakkeRequestDto)
            )

        return OppdaterGrunnlagspakkeDto(grunnlagspakkeId, oppdaterGrunnlagDtoListe)
    }

    private fun hentRequestListeFor(
        type: GrunnlagRequestType,
        oppdaterGrunnlagspakkeRequestDto: OppdaterGrunnlagspakkeRequestDto
    ): List<PersonIdOgPeriodeRequest> {
        val grunnlagRequestListe = mutableListOf<PersonIdOgPeriodeRequest>()
        oppdaterGrunnlagspakkeRequestDto.grunnlagRequestDtoListe.forEach {
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

    inner class OppdaterGrunnlagspakke(
        private val grunnlagspakkeId: Int,
        private val timestampOppdatering: LocalDateTime
    ) : MutableList<OppdaterGrunnlagDto> by mutableListOf() {

        fun oppdaterAinntekt(ainntektRequestListe: List<PersonIdOgPeriodeRequest>): OppdaterGrunnlagspakke {
            this.addAll(
                OppdaterAinntekt(
                    grunnlagspakkeId,
                    timestampOppdatering,
                    persistenceService,
                    inntektskomponentenService
                )
                    .oppdaterAinntekt(ainntektRequestListe)
            )
            return this
        }

        fun oppdaterSkattegrunnlag(skattegrunnlagRequestListe: List<PersonIdOgPeriodeRequest>): OppdaterGrunnlagspakke {
            this.addAll(
                OppdaterSkattegrunnlag(
                    grunnlagspakkeId,
                    timestampOppdatering,
                    persistenceService,
                    bidragGcpProxyConsumer
                )
                    .oppdaterSkattegrunnlag(skattegrunnlagRequestListe)
            )
            return this
        }

        fun oppdaterUtvidetBarnetrygdOgSmaabarnstillegg(utvidetBarnetrygdOgSmaabarnstilleggRequestListe: List<PersonIdOgPeriodeRequest>): OppdaterGrunnlagspakke {
            this.addAll(
                OppdaterUtvidetBarnetrygdOgSmaabarnstillegg(
                    grunnlagspakkeId,
                    timestampOppdatering,
                    persistenceService,
                    familieBaSakConsumer
                )
                    .oppdaterUtvidetBarnetrygdOgSmaabarnstillegg(
                        utvidetBarnetrygdOgSmaabarnstilleggRequestListe
                    )
            )
            return this
        }

        fun oppdaterBarnetillegg(barnetilleggRequestListe: List<PersonIdOgPeriodeRequest>): OppdaterGrunnlagspakke {
            this.addAll(
                OppdaterBarnetillegg(
                    grunnlagspakkeId,
                    timestampOppdatering,
                    persistenceService,
                    bidragGcpProxyConsumer
                )
                    .oppdaterBarnetillegg(barnetilleggRequestListe)
            )
            return this
        }

        fun oppdaterKontantstotte(kontantstotteRequestListe: List<PersonIdOgPeriodeRequest>): OppdaterGrunnlagspakke {
            this.addAll(
                OppdaterKontantstotte(
                    grunnlagspakkeId,
                    timestampOppdatering,
                    persistenceService,
                    familieKsSakConsumer
                )
                    .oppdaterKontantstotte(kontantstotteRequestListe)
            )
            return this
        }

        fun oppdaterHusstandsmedlemmerOgEgneBarn(relatertePersonerRequestListe: List<PersonIdOgPeriodeRequest>): OppdaterGrunnlagspakke {
            this.addAll(
                OppdaterRelatertePersoner(
                    grunnlagspakkeId,
                    timestampOppdatering,
                    persistenceService,
                    bidragPersonConsumer
                )
                    .oppdaterRelatertePersoner(relatertePersonerRequestListe)
            )
            return this
        }

        fun oppdaterSivilstand(sivilstandRequestListe: List<PersonIdOgPeriodeRequest>): OppdaterGrunnlagspakke {
            this.addAll(
                OppdaterSivilstand(
                    grunnlagspakkeId,
                    timestampOppdatering,
                    persistenceService,
                    bidragPersonConsumer
                )
                    .oppdaterSivilstand(sivilstandRequestListe)
            )
            return this
        }

        fun oppdaterBarnetilsyn(barnetilsynRequestListe: List<PersonIdOgPeriodeRequest>): OppdaterGrunnlagspakke {
            this.addAll(
                OppdaterBarnetilsyn(
                    grunnlagspakkeId,
                    timestampOppdatering,
                    persistenceService,
                    familieEfSakConsumer
                )
                    .oppdaterBarnetilsyn(barnetilsynRequestListe)
            )
            return this
        }
        fun oppdaterOvergangsstønad(overgangsstønadRequestListe: List<PersonIdOgPeriodeRequest>): OppdaterGrunnlagspakke {
            this.addAll(
                OppdaterOvergangsstønad(
                    grunnlagspakkeId,
                    timestampOppdatering,
                    persistenceService,
                    familieEfSakConsumer
                )
                    .oppdaterOvergangsstønad(overgangsstønadRequestListe)
            )
            return this
        }
    }
}
