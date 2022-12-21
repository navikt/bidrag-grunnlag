package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.behandling.felles.dto.grunnlag.BarnetilsynDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.BorISammeHusstandDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.EgneBarnDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.HentGrunnlagspakkeDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.HusstandDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.KontantstotteDto
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
      emptyList<KontantstotteDto>(),
//      persistenceService.hentKontantstotte(grunnlagspakkeId),
      hentEgneBarnIHusstanden(grunnlagspakkeId),
      hentHusstandsmedlemmer(grunnlagspakkeId),
      persistenceService.hentSivilstand(grunnlagspakkeId),
      emptyList<BarnetilsynDto>()
//      persistenceService.hentBarnetilsyn(grunnlagspakkeId)
    )
  }


  fun hentEgneBarnIHusstanden(grunnlagspakkeId: Int): List<EgneBarnDto> {
    val egneBarnDtoListe = mutableListOf<EgneBarnDto>()

    persistenceService.hentForeldre(grunnlagspakkeId).forEach { forelder ->
      if (forelder.personId != null) {
        val husstandListe = persistenceService.hentHusstandsmedlemmerUnder18Aar(grunnlagspakkeId, forelder.personId)

        persistenceService.hentAlleBarnForForelder(forelder.forelderId).forEach { barn ->
          if (!persistenceService.personHarFyllt18Aar(LocalDate.now(), barn.foedselsdato))
            egneBarnDtoListe.add(
              EgneBarnDto(
                personIdForelder = forelder.personId,
                personIdBarn = barn.personId,
                navn = barn.navn,
                foedselsdato = barn.foedselsdato,
                foedselsaar = barn.foedselsaar,
                doedsdato = barn.doedsdato,
                opprettetAv = barn.opprettetAv,
                hentetTidspunkt = barn.hentetTidspunkt,
                byggBorISammeHusstandDtoListe(husstandListe, barn.personId)
            )
          )
        }
      } else
        return emptyList()
    }
    return egneBarnDtoListe
  }

// Mottar liste over alle husstander og tilhørende husstandsmedlemmer under 18 år for en person pluss personid for barn
// og sjekker om mottatt barn finnes blandt husstandsmedlemmene. Hvis så så skal BorISammeHusstandDto returneres med korrekt periode.
// Hvilken husstand det gjelder er ikke relevant for grunnlag Egne barn i husstanden.
  fun byggBorISammeHusstandDtoListe(
    husstandDtoListe: List<HusstandDto>?,
    personIdBarn: String?
  ): List<BorISammeHusstandDto>? {
    val borISammeHusstandDtoListe = mutableListOf<BorISammeHusstandDto>()

    if (husstandDtoListe == null || personIdBarn == null) return null
    else
      husstandDtoListe.forEach() { husstandDto ->
        husstandDto.husstandsmedlemmerListe?.forEach() { husstandsmedlem ->
          if (husstandsmedlem.personId == personIdBarn)
            borISammeHusstandDtoListe.add(
              BorISammeHusstandDto(
                periodeFra = husstandsmedlem.periodeFra,
                periodeTil = husstandsmedlem.periodeTil,
                opprettetAv = husstandsmedlem.opprettetAv,
                hentetTidspunkt = husstandsmedlem.hentetTidspunkt
              )
            )
        }
      }
    return borISammeHusstandDtoListe
  }





  fun hentHusstandsmedlemmer(grunnlagspakkeId: Int): List<HusstandDto> {

    return persistenceService.hentVoksneHusstandsmedlemmer(grunnlagspakkeId)


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
