package no.nav.bidrag.grunnlag.persistence.repository

import no.nav.bidrag.grunnlag.persistence.entity.Husstand
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface HusstandRepository : JpaRepository<Husstand, Int?> {

  @Query(
    "select hu from Husstand hu where hu.grunnlagspakkeId = :grunnlagspakkeId and hu.aktiv = true"
  )
  fun hentHusstand(grunnlagspakkeId: Int): List<Husstand>

  @Modifying
  @Query(
    "update Husstand hu " +
        "set hu.aktiv = false, hu.brukTil = :timestampOppdatering " +
        "where hu.grunnlagspakkeId = :grunnlagspakkeId and hu.personId = :personId and hu.aktiv = true"
  )
  fun oppdaterEksisterendeHusstandTilInaktiv(grunnlagspakkeId: Int, personId: String, timestampOppdatering: LocalDateTime)

}
