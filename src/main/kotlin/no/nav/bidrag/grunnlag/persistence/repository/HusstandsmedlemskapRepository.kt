package no.nav.bidrag.grunnlag.persistence.repository

import no.nav.bidrag.grunnlag.persistence.entity.Husstandsmedlemskap
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface HusstandsmedlemskapRepository : JpaRepository<Husstandsmedlemskap, Int?> {

  @Query(
    "select hm from Husstandsmedlemskap hm where hm.grunnlagspakkeId = :grunnlagspakkeId and hm.aktiv = true order by hm.partPersonId, hm.erBarnAvBmBp, hm.husstandsmedlemPersonId, hm.periodeFra"
  )
  fun hentHusstandsmedlemskap(grunnlagspakkeId: Int): List<Husstandsmedlemskap>

  @Modifying
  @Query(
    "update Husstandsmedlemskap hm " +
      "set hm.aktiv = false, hm.brukTil = :timestampOppdatering " +
      "where hm.grunnlagspakkeId = :grunnlagspakkeId and hm.partPersonId = :personId and hm.aktiv = true"
  )
  fun oppdaterEksisterendeHusstandsmedlemskapTilInaktiv(grunnlagspakkeId: Int, personId: String, timestampOppdatering: LocalDateTime)

}