package no.nav.bidrag.grunnlag.persistence.repository

import no.nav.bidrag.grunnlag.persistence.entity.Barn
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface BarnRepository : JpaRepository<Barn, Int?> {

  @Query(
    "select ba from Barn ba where ba.grunnlagspakkeId = :grunnlagspakkeId and ba.aktiv = true"
  )
  fun hentBarn(grunnlagspakkeId: Int): List<Barn>

  @Modifying
  @Query(
    "update Barn ba " +
        "set ba.aktiv = false, ba.brukTil = :timestampOppdatering " +
        "where ba.grunnlagspakkeId = :grunnlagspakkeId and ba.personIdBarn = :personId and ba.aktiv = true"
  )
  fun oppdaterEksisterendeBarnTilInaktiv(grunnlagspakkeId: Int, personId: String, timestampOppdatering: LocalDateTime)


}
