package no.nav.bidrag.grunnlag.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import no.nav.bidrag.commons.util.RequestContextAsyncContext
import no.nav.bidrag.commons.util.SecurityCoroutineContext
import no.nav.bidrag.domene.enums.grunnlag.GrunnlagRequestType
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.consumer.arbeidsforhold.ArbeidsforholdConsumer
import no.nav.bidrag.grunnlag.consumer.arbeidsforhold.EnhetsregisterConsumer
import no.nav.bidrag.grunnlag.consumer.bidragperson.BidragPersonConsumer
import no.nav.bidrag.grunnlag.consumer.familiebasak.FamilieBaSakConsumer
import no.nav.bidrag.grunnlag.consumer.familieefsak.FamilieEfSakConsumer
import no.nav.bidrag.grunnlag.consumer.familiekssak.FamilieKsSakConsumer
import no.nav.bidrag.grunnlag.consumer.pensjon.PensjonConsumer
import no.nav.bidrag.grunnlag.consumer.skattegrunnlag.SigrunConsumer
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.transport.behandling.grunnlag.request.GrunnlagRequestDto
import no.nav.bidrag.transport.behandling.grunnlag.request.HentGrunnlagRequestDto
import no.nav.bidrag.transport.behandling.grunnlag.response.AinntektGrunnlagDto
import no.nav.bidrag.transport.behandling.grunnlag.response.ArbeidsforholdGrunnlagDto
import no.nav.bidrag.transport.behandling.grunnlag.response.BarnetilleggGrunnlagDto
import no.nav.bidrag.transport.behandling.grunnlag.response.BarnetilsynGrunnlagDto
import no.nav.bidrag.transport.behandling.grunnlag.response.HentGrunnlagDto
import no.nav.bidrag.transport.behandling.grunnlag.response.KontantstøtteGrunnlagDto
import no.nav.bidrag.transport.behandling.grunnlag.response.RelatertPersonGrunnlagDto
import no.nav.bidrag.transport.behandling.grunnlag.response.SivilstandGrunnlagDto
import no.nav.bidrag.transport.behandling.grunnlag.response.SkattegrunnlagGrunnlagDto
import no.nav.bidrag.transport.behandling.grunnlag.response.UtvidetBarnetrygdOgSmaabarnstilleggGrunnlagDto
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class HentGrunnlagServiceAsynk(
    private val inntektskomponentenService: InntektskomponentenService,
    private val sigrunConsumer: SigrunConsumer,
    private val familieBaSakConsumer: FamilieBaSakConsumer,
    private val pensjonConsumer: PensjonConsumer,
    private val familieKsSakConsumer: FamilieKsSakConsumer,
    private val bidragPersonConsumer: BidragPersonConsumer,
    private val familieEfSakConsumer: FamilieEfSakConsumer,
    private val arbeidsforholdConsumer: ArbeidsforholdConsumer,
    private val enhetsregisterConsumer: EnhetsregisterConsumer,
) {

    companion object {
        @JvmStatic
        val LOGGER: Logger = LoggerFactory.getLogger(HentGrunnlagServiceAsynk::class.java)
    }

    suspend fun hentGrunnlag(hentGrunnlagRequestDto: HentGrunnlagRequestDto): HentGrunnlagDto {
        LOGGER.info("*** Start HENT GRUNNLAG ASYNK ***")

        val scope = CoroutineScope(Dispatchers.IO + SecurityCoroutineContext() + RequestContextAsyncContext())

        val hentetTidspunkt = LocalDateTime.now()

        // Henter aktiv ident for personer i requesten
        val historiskeIdenterMap = hentHistoriskeOgAktiveIdenter(hentGrunnlagRequestDto)

        // Oppdaterer aktiv ident for personer i requesten
        val requestMedNyesteIdenter = byttUtIdentMedAktivIdent(hentGrunnlagRequestDto, historiskeIdenterMap)

        return runBlocking {
            val ainntektListe = scope.async {
                HentAinntektService(
                    inntektskomponentenService = inntektskomponentenService,
                ).hentAinntekt(
                    ainntektRequestListe = hentRequestListeFor(
                        type = GrunnlagRequestType.AINNTEKT,
                        hentGrunnlagRequestDto = requestMedNyesteIdenter,
                    ),
                    formål = hentGrunnlagRequestDto.formaal,
                )
            }

            val skattegrunnlagListe = scope.async {
                HentSkattegrunnlagService(
                    sigrunConsumer = sigrunConsumer,
                ).hentSkattegrunnlag(
                    skattegrunnlagRequestListe = hentRequestListeFor(
                        type = GrunnlagRequestType.SKATTEGRUNNLAG,
                        hentGrunnlagRequestDto = requestMedNyesteIdenter,
                    ),
                )
            }

            val utvidetBarnetrygdOgKontantstøtteListe = scope.async {
                HentUtvidetBarnetrygdOgSmåbarnstilleggService(
                    familieBaSakConsumer = familieBaSakConsumer,
                ).hentUbst(
                    ubstRequestListe = hentRequestListeFor(
                        type = GrunnlagRequestType.UTVIDET_BARNETRYGD_OG_SMÅBARNSTILLEGG,
                        hentGrunnlagRequestDto = requestMedNyesteIdenter,
                    ),
                )
            }

            val barnetilleggPensjonListe = scope.async {
                HentBarnetilleggService(
                    pensjonConsumer = pensjonConsumer,
                ).hentBarnetilleggPensjon(
                    barnetilleggPensjonRequestListe = hentRequestListeFor(
                        type = GrunnlagRequestType.BARNETILLEGG,
                        hentGrunnlagRequestDto = requestMedNyesteIdenter,
                    ),
                )
            }

            val kontantstøtteListe = scope.async {
                HentKontantstøtteService(
                    familieKsSakConsumer = familieKsSakConsumer,
                ).hentKontantstøtte(
                    kontantstøtteRequestListe = hentRequestListeFor(
                        type = GrunnlagRequestType.KONTANTSTØTTE,
                        hentGrunnlagRequestDto = requestMedNyesteIdenter,
                    ),
                    historiskeIdenterMap = historiskeIdenterMap,
                )
            }

            val husstandsmedlemmerOgEgneBarnListe = scope.async {
                HentRelatertePersonerService(
                    bidragPersonConsumer = bidragPersonConsumer,
                ).hentRelatertePersoner(
                    relatertPersonRequestListe = hentRequestListeFor(
                        type = GrunnlagRequestType.HUSSTANDSMEDLEMMER_OG_EGNE_BARN,
                        hentGrunnlagRequestDto = requestMedNyesteIdenter,
                    ),
                )
            }

            val sivilstandListe = scope.async {
                HentSivilstandService(
                    bidragPersonConsumer = bidragPersonConsumer,
                ).hentSivilstand(
                    sivilstandRequestListe = hentRequestListeFor(
                        type = GrunnlagRequestType.SIVILSTAND,
                        hentGrunnlagRequestDto = requestMedNyesteIdenter,
                    ),
                )
            }

            val barnetilsynListe = scope.async {
                HentBarnetilsynService(
                    familieEfSakConsumer = familieEfSakConsumer,
                ).hentBarnetilsyn(
                    barnetilsynRequestListe = hentRequestListeFor(
                        type = GrunnlagRequestType.BARNETILSYN,
                        hentGrunnlagRequestDto = requestMedNyesteIdenter,
                    ),
                )
            }

            val arbeidsforholdListe = scope.async {
                HentArbeidsforholdService(
                    arbeidsforholdConsumer = arbeidsforholdConsumer,
                    enhetsregisterConsumer = enhetsregisterConsumer,
                ).hentArbeidsforhold(
                    arbeidsforholdRequestListe = hentRequestListeFor(
                        type = GrunnlagRequestType.ARBEIDSFORHOLD,
                        hentGrunnlagRequestDto = requestMedNyesteIdenter,
                    ),
                )
            }

            LOGGER.info("*** Slutt HENT GRUNNLAG ASYNK ***")

            HentGrunnlagDto(
                ainntektListe = ainntektListe.await()
                    .sortedWith(compareBy<AinntektGrunnlagDto> { it.personId }.thenBy { it.periodeFra }),
                skattegrunnlagListe = skattegrunnlagListe.await()
                    .sortedWith(compareBy<SkattegrunnlagGrunnlagDto> { it.personId }.thenBy { it.periodeFra }),
                ubstListe = utvidetBarnetrygdOgKontantstøtteListe.await()
                    .sortedWith(
                        compareBy<UtvidetBarnetrygdOgSmaabarnstilleggGrunnlagDto> { it.personId }.thenBy { it.type }.thenBy { it.periodeFra },
                    ),
                barnetilleggListe = barnetilleggPensjonListe.await()
                    .sortedWith(
                        compareBy<BarnetilleggGrunnlagDto> { it.partPersonId }.thenBy { it.barnPersonId }.thenBy { it.barnetilleggType }
                            .thenBy { it.periodeFra },
                    ),
                kontantstøtteListe = kontantstøtteListe.await()
                    .sortedWith(compareBy<KontantstøtteGrunnlagDto> { it.partPersonId }.thenBy { it.barnPersonId }.thenBy { it.periodeFra }),
                husstandsmedlemmerOgEgneBarnListe = husstandsmedlemmerOgEgneBarnListe.await()
                    .sortedWith(compareBy<RelatertPersonGrunnlagDto> { it.partPersonId }.thenBy { it.relatertPersonPersonId })
                    .distinct(),
                sivilstandListe = sivilstandListe.await()
                    .sortedWith(compareBy<SivilstandGrunnlagDto> { it.personId }.thenBy { it.type }.thenBy { it.gyldigFom }),
                barnetilsynListe = barnetilsynListe.await()
                    .sortedWith(compareBy<BarnetilsynGrunnlagDto> { it.partPersonId }.thenBy { it.barnPersonId }.thenBy { it.periodeFra }),
                arbeidsforholdListe = arbeidsforholdListe.await()
                    .sortedWith(compareBy<ArbeidsforholdGrunnlagDto> { it.partPersonId }.thenBy { it.startdato }),
                hentetTidspunkt = hentetTidspunkt,
            )
        }
    }

    // Henter historiske identer for personer i requesten.
    // Returnerer en map hvor key = aktiv ident og  value = liste med historiske identer (inklusiv den aktive identen)
    private fun hentHistoriskeOgAktiveIdenter(request: HentGrunnlagRequestDto): Map<String, List<String>> {
        val historiskeIdenterMap = mutableMapOf<String, List<String>>()

        request.grunnlagRequestDtoListe.forEach { grunnlagDto ->
            // Gjør ikke oppslag for personer som allerede er lagt til i map
            if (!historiskeIdenterMap.values.any { grunnlagDto.personId in it }) {
                val historiskeIdenterListe = hentIdenterFraConsumer(grunnlagDto.personId)
                if (historiskeIdenterListe.size > 1) {
                    SECURE_LOGGER.warn("Hentet historiske identer for personId: ${grunnlagDto.personId} og fikk tilbake: $historiskeIdenterListe")
                }

                val key = historiskeIdenterListe.find { !it.historisk }?.personId
                val values = historiskeIdenterListe.map { it.personId }.sorted()

                key?.let { historiskeIdenterMap[it] = values }
            }
        }

        return historiskeIdenterMap
    }

    // Henter historiske identer for personen. Returnerer en liste med historiske identer (inklusiv den aktive identen)
    private fun hentIdenterFraConsumer(personId: String): List<HistoriskIdent> {
        return when (val response = bidragPersonConsumer.hentPersonidenter(personident = Personident(personId), inkludereHistoriske = true)) {
            is RestResponse.Success -> {
                val personidenterResponse = response.body
                SECURE_LOGGER.info(
                    "Kall til bidrag-person for å hente historiske identer for ident $personId ga følgende respons: $personidenterResponse",
                )
                if (personidenterResponse.isEmpty()) {
                    listOf(HistoriskIdent(personId, false))
                } else {
                    personidenterResponse.map { HistoriskIdent(it.ident, it.historisk) }
                }
            }

            is RestResponse.Failure -> {
                SECURE_LOGGER.warn("Feil ved kall til bidrag-person for å hente historiske identer for ident $personId. Respons = $response")
                listOf(HistoriskIdent(personId, false))
            }
        }
    }

    // Bytter ut identer i requesten med aktiv ident for personen
    private fun byttUtIdentMedAktivIdent(request: HentGrunnlagRequestDto, historiskeIdenterMap: Map<String, List<String>>): HentGrunnlagRequestDto {
        val dtoListe = request.grunnlagRequestDtoListe.map { grunnlagRequestDto ->
            // Søker gjennom value-listen og setter aktivIdent lik tilhørende key-verdi. Hvis personId ikke finnes i lista settes aktivIdent
            // lik personId.
            val aktivIdent = historiskeIdenterMap.entries.find { grunnlagRequestDto.personId in it.value }?.key ?: grunnlagRequestDto.personId
            if (aktivIdent != grunnlagRequestDto.personId) {
                SECURE_LOGGER.info("Hentet nyeste ident for personId: ${grunnlagRequestDto.personId} og fikk tilbake: $aktivIdent")
            }
            grunnlagRequestDto.copy(personId = aktivIdent)
        }
        return HentGrunnlagRequestDto(formaal = request.formaal, grunnlagRequestDtoListe = dtoListe)
    }

    private fun hentRequestListeFor(type: GrunnlagRequestType, hentGrunnlagRequestDto: HentGrunnlagRequestDto): List<PersonIdOgPeriodeRequest> {
        val grunnlagRequestListe = mutableListOf<PersonIdOgPeriodeRequest>()
        hentGrunnlagRequestDto.grunnlagRequestDtoListe.forEach {
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
}
