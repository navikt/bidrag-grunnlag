package no.nav.bidrag.grunnlag.service

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import no.nav.bidrag.commons.security.utils.TokenUtils
import no.nav.bidrag.domene.enums.grunnlag.GrunnlagRequestStatus
import no.nav.bidrag.domene.enums.vedtak.Formål
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.grunnlag.SECURE_LOGGER
import no.nav.bidrag.grunnlag.consumer.bidragperson.BidragPersonConsumer
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.transport.behandling.grunnlag.request.OppdaterGrunnlagspakkeRequestDto
import no.nav.bidrag.transport.behandling.grunnlag.request.OpprettGrunnlagspakkeRequestDto
import no.nav.bidrag.transport.behandling.grunnlag.response.HentGrunnlagspakkeDto
import no.nav.bidrag.transport.behandling.grunnlag.response.OppdaterGrunnlagDto
import no.nav.bidrag.transport.behandling.grunnlag.response.OppdaterGrunnlagspakkeDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
@Transactional
class GrunnlagspakkeService(
    private val persistenceService: PersistenceService,
    private val oppdaterGrunnlagspakkeService: OppdaterGrunnlagspakkeService,
    private val meterRegistry: MeterRegistry,
    private val bidragPersonConsumer: BidragPersonConsumer,
) {

    fun opprettGrunnlagspakkeCounter(formål: Formål) = Counter.builder("opprett_grunnlagspakke")
        .tag("formaal", formål.name)
        .tag("opprettetAvApp", TokenUtils.hentApplikasjonsnavn() ?: "UKJENT")
        .register(meterRegistry)

    fun opprettGrunnlagspakke(opprettGrunnlagspakkeRequestDto: OpprettGrunnlagspakkeRequestDto): Int {
        val opprettetGrunnlagspakke =
            persistenceService.opprettNyGrunnlagspakke(opprettGrunnlagspakkeRequestDto)
        opprettGrunnlagspakkeCounter(opprettGrunnlagspakkeRequestDto.formaal).increment()
        return opprettetGrunnlagspakke.grunnlagspakkeId
    }

    fun oppdaterGrunnlagspakke(
        grunnlagspakkeId: Int,
        oppdaterGrunnlagspakkeRequestDto: OppdaterGrunnlagspakkeRequestDto,
    ): OppdaterGrunnlagspakkeDto {
        val timestampOppdatering = LocalDateTime.now()

        // Validerer at grunnlagspakke eksisterer
        persistenceService.validerGrunnlagspakke(grunnlagspakkeId)

        // Henter aktiv ident for personer i grunnlagspakken
        val requestMedNyesteIdent = oppdaterAktivIdent(oppdaterGrunnlagspakkeRequestDto)

        val oppdaterGrunnlagspakkeDto = oppdaterGrunnlagspakkeService.oppdaterGrunnlagspakke(
            grunnlagspakkeId = grunnlagspakkeId,
            oppdaterGrunnlagspakkeRequestDto = requestMedNyesteIdent,
            timestampOppdatering = timestampOppdatering,
        )

        // Oppdaterer endret_timestamp på grunnlagspakke
        if (harOppdatertGrunnlag(oppdaterGrunnlagspakkeDto.grunnlagTypeResponsListe)) {
            persistenceService.oppdaterEndretTimestamp(grunnlagspakkeId = grunnlagspakkeId, timestampOppdatering = timestampOppdatering)
        }

        return oppdaterGrunnlagspakkeDto
    }

    private fun harOppdatertGrunnlag(grunnlagTypeResponsListe: List<OppdaterGrunnlagDto>): Boolean {
        return grunnlagTypeResponsListe.any { it.status == GrunnlagRequestStatus.HENTET }
    }

    // Henter aktiv ident for personer i requesten og bytter evt. ut innsendt ident med aktiv ident
    private fun oppdaterAktivIdent(request: OppdaterGrunnlagspakkeRequestDto): OppdaterGrunnlagspakkeRequestDto {
        val dtoListe = request.grunnlagRequestDtoListe.map {
            val aktivIdent = hentAktivIdentFraConsumer(it.personId)
            SECURE_LOGGER.info("Hentet nyeste ident for personId: ${it.personId} og fikk tilbake: $aktivIdent")
            it.copy(personId = aktivIdent)
        }
        return OppdaterGrunnlagspakkeRequestDto(gyldigTil = request.gyldigTil, grunnlagRequestDtoListe = dtoListe)
    }

    private fun hentAktivIdentFraConsumer(personId: String): String {
        return when (val response = bidragPersonConsumer.hentPersonidenter(personident = Personident(personId), inkludereHistoriske = false)) {
            is RestResponse.Success -> {
                val personidenterResponse = response.body
                SECURE_LOGGER.info(
                    "Kall til bidrag-person for å hente aktiv personident for ident $personId ga følgende respons: $personidenterResponse",
                )

                personidenterResponse.firstOrNull()?.ident ?: personId
            }
            is RestResponse.Failure -> {
                SECURE_LOGGER.warn("Feil ved kall til bidrag-person for å hente aktiv personident for ident $personId. Respons = $response")
                personId
            }
        }
    }

    fun hentGrunnlagspakke(grunnlagspakkeId: Int): HentGrunnlagspakkeDto {
        // Validerer at grunnlagspakke eksisterer
        persistenceService.validerGrunnlagspakke(grunnlagspakkeId)
        return HentGrunnlagspakkeDto(
            grunnlagspakkeId = grunnlagspakkeId,
            ainntektListe = persistenceService.hentAinntekt(grunnlagspakkeId),
            skattegrunnlagListe = persistenceService.hentSkattegrunnlag(grunnlagspakkeId),
            ubstListe = persistenceService.hentUtvidetBarnetrygdOgSmaabarnstillegg(grunnlagspakkeId),
            barnetilleggListe = persistenceService.hentBarnetillegg(grunnlagspakkeId),
            kontantstotteListe = persistenceService.hentKontantstotte(grunnlagspakkeId),
            husstandmedlemmerOgEgneBarnListe = persistenceService.hentHusstandsmedlemmerOgEgneBarn(grunnlagspakkeId),

            sivilstandListe = persistenceService.hentSivilstand(grunnlagspakkeId),
            barnetilsynListe = persistenceService.hentBarnetilsyn(grunnlagspakkeId),
            overgangsstonadListe = persistenceService.hentOvergangsstønad(grunnlagspakkeId),
        )
    }

    fun lukkGrunnlagspakke(grunnlagspakkeId: Int): Int {
        // Validerer at grunnlagspakke eksisterer
        persistenceService.validerGrunnlagspakke(grunnlagspakkeId)
        return persistenceService.lukkGrunnlagspakke(grunnlagspakkeId)
    }
}

data class PersonIdOgPeriodeRequest(
    val personId: String,
    val periodeFra: LocalDate,
    val periodeTil: LocalDate,
)
