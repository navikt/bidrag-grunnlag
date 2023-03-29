package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.behandling.felles.dto.grunnlag.HentGrunnlagspakkeDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.OppdaterGrunnlagDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.OppdaterGrunnlagspakkeDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.OppdaterGrunnlagspakkeRequestDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.OpprettGrunnlagspakkeRequestDto
import no.nav.bidrag.behandling.felles.enums.GrunnlagsRequestStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
@Transactional
class GrunnlagspakkeService(
    private val persistenceService: PersistenceService,
    private val oppdaterGrunnlagspakkeService: OppdaterGrunnlagspakkeService
) {

    fun opprettGrunnlagspakke(opprettGrunnlagspakkeRequestDto: OpprettGrunnlagspakkeRequestDto): Int {
        val opprettetGrunnlagspakke =
            persistenceService.opprettNyGrunnlagspakke(opprettGrunnlagspakkeRequestDto)
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
            grunnlagspakkeId,
            persistenceService.hentAinntekt(grunnlagspakkeId),
            persistenceService.hentSkattegrunnlag(grunnlagspakkeId),
            persistenceService.hentUtvidetBarnetrygdOgSmaabarnstillegg(grunnlagspakkeId),
            persistenceService.hentBarnetillegg(grunnlagspakkeId),
            persistenceService.hentKontantstotte(grunnlagspakkeId),
            persistenceService.hentEgneBarnIHusstanden(grunnlagspakkeId),
            persistenceService.hentVoksneHusstandsmedlemmer(grunnlagspakkeId),
            persistenceService.hentSivilstand(grunnlagspakkeId),
            persistenceService.hentBarnetilsyn(grunnlagspakkeId)
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
