package no.nav.bidrag.grunnlag.service

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import no.nav.bidrag.commons.security.utils.TokenUtils
import no.nav.bidrag.domain.enums.Formaal
import no.nav.bidrag.domain.enums.GrunnlagsRequestStatus
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
    private val meterRegistry: MeterRegistry
) {

    fun opprettGrunnlagspakkeCounter(formaal: Formaal) = Counter.builder("opprett_grunnlagspakke")
        .tag("formaal", formaal.name)
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
        oppdaterGrunnlagspakkeRequestDto: OppdaterGrunnlagspakkeRequestDto
    ): OppdaterGrunnlagspakkeDto {
        val timestampOppdatering = LocalDateTime.now()

        // Validerer at grunnlagspakke eksisterer
        persistenceService.validerGrunnlagspakke(grunnlagspakkeId)

        val oppdaterGrunnlagspakkeDto = oppdaterGrunnlagspakkeService.oppdaterGrunnlagspakke(
            grunnlagspakkeId,
            oppdaterGrunnlagspakkeRequestDto,
            timestampOppdatering
        )

        // Oppdaterer endret_timestamp på grunnlagspakke
        if (harOppdatertGrunnlag(oppdaterGrunnlagspakkeDto.grunnlagTypeResponsListe)) {
            persistenceService.oppdaterEndretTimestamp(grunnlagspakkeId, timestampOppdatering)
        }

        return oppdaterGrunnlagspakkeDto
    }

    private fun harOppdatertGrunnlag(grunnlagTypeResponsListe: List<OppdaterGrunnlagDto>): Boolean {
        return grunnlagTypeResponsListe.any { it.status == GrunnlagsRequestStatus.HENTET }
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
            overgangsstonadListe = persistenceService.hentOvergangsstønad(grunnlagspakkeId)
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
    val periodeTil: LocalDate
)
