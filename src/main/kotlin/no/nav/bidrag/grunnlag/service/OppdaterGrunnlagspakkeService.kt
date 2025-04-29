package no.nav.bidrag.grunnlag.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import no.nav.bidrag.commons.util.RequestContextAsyncContext
import no.nav.bidrag.commons.util.SecurityCoroutineContext
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
    suspend fun oppdaterGrunnlagspakke(
        grunnlagspakkeId: Int,
        oppdaterGrunnlagspakkeRequestDto: OppdaterGrunnlagspakkeRequestDto,
        timestampOppdatering: LocalDateTime,
        historiskeIdenterMap: Map<String, List<String>>,
        oppdaterGrunnlagDtoListe: OppdaterGrunnlagspakke = OppdaterGrunnlagspakke(grunnlagspakkeId, timestampOppdatering),
    ): OppdaterGrunnlagspakkeDto {
        val scope = CoroutineScope(Dispatchers.IO + SecurityCoroutineContext() + RequestContextAsyncContext())

        return runBlocking {
            val ainntektListe = scope.async {
                oppdaterGrunnlagDtoListe.oppdaterAinntekt(
                    ainntektRequestListe = hentRequestListeFor(
                        type = GrunnlagRequestType.AINNTEKT,
                        oppdaterGrunnlagspakkeRequestDto = oppdaterGrunnlagspakkeRequestDto,
                    ),
                    historiskeIdenterMap = historiskeIdenterMap,
                )
            }

            val skattegrunnlagListe = scope.async {
                oppdaterGrunnlagDtoListe.oppdaterSkattegrunnlag(
                    skattegrunnlagRequestListe = hentRequestListeFor(
                        type = GrunnlagRequestType.SKATTEGRUNNLAG,
                        oppdaterGrunnlagspakkeRequestDto = oppdaterGrunnlagspakkeRequestDto,
                    ),
                    historiskeIdenterMap = historiskeIdenterMap,
                )
            }

            val utvidetBarnetrygdListe = scope.async {
                oppdaterGrunnlagDtoListe.oppdaterUtvidetBarnetrygdOgSmaabarnstillegg(
                    utvidetBarnetrygdOgSmaabarnstilleggRequestListe = hentRequestListeFor(
                        type = GrunnlagRequestType.UTVIDET_BARNETRYGD_OG_SMÅBARNSTILLEGG,
                        oppdaterGrunnlagspakkeRequestDto = oppdaterGrunnlagspakkeRequestDto,
                    ),
                    historiskeIdenterMap = historiskeIdenterMap,
                )
            }

            val barnetilleggListe = scope.async {
                oppdaterGrunnlagDtoListe.oppdaterBarnetillegg(
                    barnetilleggRequestListe = hentRequestListeFor(
                        type = GrunnlagRequestType.BARNETILLEGG,
                        oppdaterGrunnlagspakkeRequestDto = oppdaterGrunnlagspakkeRequestDto,
                    ),
                    historiskeIdenterMap = historiskeIdenterMap,
                )
            }

            val kontantstotteListe = scope.async {
                oppdaterGrunnlagDtoListe.oppdaterKontantstotte(
                    kontantstotteRequestListe = hentRequestListeFor(
                        type = GrunnlagRequestType.KONTANTSTØTTE,
                        oppdaterGrunnlagspakkeRequestDto = oppdaterGrunnlagspakkeRequestDto,
                    ),
                    historiskeIdenterMap = historiskeIdenterMap,
                )
            }

            val husstandsmedlemmerListe = scope.async {
                oppdaterGrunnlagDtoListe.oppdaterHusstandsmedlemmerOgEgneBarn(
                    relatertePersonerRequestListe = hentRequestListeFor(
                        type = GrunnlagRequestType.HUSSTANDSMEDLEMMER_OG_EGNE_BARN,
                        oppdaterGrunnlagspakkeRequestDto = oppdaterGrunnlagspakkeRequestDto,
                    ),
                    historiskeIdenterMap = historiskeIdenterMap,
                )
            }

            val sivilstandListe = scope.async {
                oppdaterGrunnlagDtoListe.oppdaterSivilstand(
                    sivilstandRequestListe = hentRequestListeFor(
                        type = GrunnlagRequestType.SIVILSTAND,
                        oppdaterGrunnlagspakkeRequestDto = oppdaterGrunnlagspakkeRequestDto,
                    ),
                    historiskeIdenterMap = historiskeIdenterMap,
                )
            }

            val barnetilsynListe = scope.async {
                oppdaterGrunnlagDtoListe.oppdaterBarnetilsyn(
                    barnetilsynRequestListe = hentRequestListeFor(
                        type = GrunnlagRequestType.BARNETILSYN,
                        oppdaterGrunnlagspakkeRequestDto = oppdaterGrunnlagspakkeRequestDto,
                    ),
                    historiskeIdenterMap = historiskeIdenterMap,
                )
            }

            // Await all deferred results to ensure completion
            ainntektListe.await()
            skattegrunnlagListe.await()
            utvidetBarnetrygdListe.await()
            barnetilleggListe.await()
            kontantstotteListe.await()
            husstandsmedlemmerListe.await()
            sivilstandListe.await()
            barnetilsynListe.await()

            OppdaterGrunnlagspakkeDto(grunnlagspakkeId = grunnlagspakkeId, grunnlagTypeResponsListe = oppdaterGrunnlagDtoListe)
        }
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

    inner class OppdaterGrunnlagspakke(private val grunnlagspakkeId: Int, private val timestampOppdatering: LocalDateTime) :
        MutableList<OppdaterGrunnlagDto> by mutableListOf() {

        fun oppdaterAinntekt(
            ainntektRequestListe: List<PersonIdOgPeriodeRequest>,
            historiskeIdenterMap: Map<String, List<String>>,
        ): OppdaterGrunnlagspakke {
            this.addAll(
                OppdaterAinntekt(
                    grunnlagspakkeId = grunnlagspakkeId,
                    timestampOppdatering = timestampOppdatering,
                    persistenceService = persistenceService,
                    inntektskomponentenService = inntektskomponentenService,
                )
                    .oppdaterAinntekt(ainntektRequestListe = ainntektRequestListe, historiskeIdenterMap = historiskeIdenterMap),
            )
            return this
        }

        fun oppdaterSkattegrunnlag(
            skattegrunnlagRequestListe: List<PersonIdOgPeriodeRequest>,
            historiskeIdenterMap: Map<String, List<String>>,
        ): OppdaterGrunnlagspakke {
            this.addAll(
                OppdaterSkattegrunnlag(
                    grunnlagspakkeId = grunnlagspakkeId,
                    timestampOppdatering = timestampOppdatering,
                    persistenceService = persistenceService,
                    sigrunConsumer = sigrunConsumer,
                )
                    .oppdaterSkattegrunnlag(skattegrunnlagRequestListe = skattegrunnlagRequestListe, historiskeIdenterMap = historiskeIdenterMap),
            )
            return this
        }

        fun oppdaterUtvidetBarnetrygdOgSmaabarnstillegg(
            utvidetBarnetrygdOgSmaabarnstilleggRequestListe: List<PersonIdOgPeriodeRequest>,
            historiskeIdenterMap: Map<String, List<String>>,
        ): OppdaterGrunnlagspakke {
            this.addAll(
                OppdaterUtvidetBarnetrygdOgSmaabarnstillegg(
                    grunnlagspakkeId = grunnlagspakkeId,
                    timestampOppdatering = timestampOppdatering,
                    persistenceService = persistenceService,
                    familieBaSakConsumer = familieBaSakConsumer,
                )
                    .oppdaterUtvidetBarnetrygdOgSmaabarnstillegg(
                        utvidetBarnetrygdOgSmaabarnstilleggRequestListe = utvidetBarnetrygdOgSmaabarnstilleggRequestListe,
                        historiskeIdenterMap = historiskeIdenterMap,
                    ),
            )
            return this
        }

        fun oppdaterBarnetillegg(
            barnetilleggRequestListe: List<PersonIdOgPeriodeRequest>,
            historiskeIdenterMap: Map<String, List<String>>,
        ): OppdaterGrunnlagspakke {
            this.addAll(
                OppdaterBarnetillegg(
                    grunnlagspakkeId = grunnlagspakkeId,
                    timestampOppdatering = timestampOppdatering,
                    persistenceService = persistenceService,
                    pensjonConsumer = pensjonConsumer,
                )
                    .oppdaterBarnetillegg(barnetilleggRequestListe = barnetilleggRequestListe, historiskeIdenterMap = historiskeIdenterMap),
            )
            return this
        }

        fun oppdaterKontantstotte(
            kontantstotteRequestListe: List<PersonIdOgPeriodeRequest>,
            historiskeIdenterMap: Map<String, List<String>>,
        ): OppdaterGrunnlagspakke {
            this.addAll(
                OppdaterKontantstotte(
                    grunnlagspakkeId = grunnlagspakkeId,
                    timestampOppdatering = timestampOppdatering,
                    persistenceService = persistenceService,
                    familieKsSakConsumer = familieKsSakConsumer,
                )
                    .oppdaterKontantstotte(kontantstotteRequestListe = kontantstotteRequestListe, historiskeIdenterMap = historiskeIdenterMap),
            )
            return this
        }

        fun oppdaterHusstandsmedlemmerOgEgneBarn(
            relatertePersonerRequestListe: List<PersonIdOgPeriodeRequest>,
            historiskeIdenterMap: Map<String, List<String>>,
        ): OppdaterGrunnlagspakke {
            this.addAll(
                OppdaterRelatertePersoner(
                    grunnlagspakkeId = grunnlagspakkeId,
                    timestampOppdatering = timestampOppdatering,
                    persistenceService = persistenceService,
                    bidragPersonConsumer = bidragPersonConsumer,
                )
                    .oppdaterRelatertePersoner(
                        relatertePersonerRequestListe = relatertePersonerRequestListe,
                        historiskeIdenterMap = historiskeIdenterMap,
                    ),
            )
            return this
        }

        fun oppdaterSivilstand(
            sivilstandRequestListe: List<PersonIdOgPeriodeRequest>,
            historiskeIdenterMap: Map<String, List<String>>,
        ): OppdaterGrunnlagspakke {
            this.addAll(
                OppdaterSivilstand(
                    grunnlagspakkeId = grunnlagspakkeId,
                    timestampOppdatering = timestampOppdatering,
                    persistenceService = persistenceService,
                    bidragPersonConsumer = bidragPersonConsumer,
                )
                    .oppdaterSivilstand(sivilstandRequestListe = sivilstandRequestListe, historiskeIdenterMap = historiskeIdenterMap),
            )
            return this
        }

        fun oppdaterBarnetilsyn(
            barnetilsynRequestListe: List<PersonIdOgPeriodeRequest>,
            historiskeIdenterMap: Map<String, List<String>>,
        ): OppdaterGrunnlagspakke {
            this.addAll(
                OppdaterBarnetilsyn(
                    grunnlagspakkeId = grunnlagspakkeId,
                    timestampOppdatering = timestampOppdatering,
                    persistenceService = persistenceService,
                    familieEfSakConsumer = familieEfSakConsumer,
                )
                    .oppdaterBarnetilsyn(barnetilsynRequestListe = barnetilsynRequestListe, historiskeIdenterMap = historiskeIdenterMap),
            )
            return this
        }
    }
}
