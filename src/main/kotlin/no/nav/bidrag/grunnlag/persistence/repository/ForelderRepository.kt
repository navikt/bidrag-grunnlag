package no.nav.bidrag.grunnlag.persistence.repository

import no.nav.bidrag.grunnlag.persistence.entity.Forelder
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface ForelderRepository : JpaRepository<Forelder, Int?> {

  @Query(
    "select fo from Forelder fo where fo.grunnlagspakkeId = :grunnlagspakkeId and fo.aktiv = true"
  )
  fun hentForeldre(grunnlagspakkeId: Int): List<Forelder>

  @Modifying
  @Query(
    "update Forelder fo " +
        "set fo.aktiv = false, fo.brukTil = :timestampOppdatering " +
        "where fo.grunnlagspakkeId = :grunnlagspakkeId and fo.personId = :personId and fo.aktiv = true"
  )
  fun oppdaterEksisterendeForelderTilInaktiv(grunnlagspakkeId: Int, personId: String, timestampOppdatering: LocalDateTime)

}