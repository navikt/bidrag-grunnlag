package no.nav.bidrag.grunnlag.persistence.repository

import no.nav.bidrag.grunnlag.persistence.entity.Husstandsmedlemskap
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface HusstandsmedlemRepository : JpaRepository<Husstandsmedlemskap, Int?> {

  @Query(
    "select hum from Husstandsmedlemskap hum where hum.husstandId = :husstandId order by hum.periodeFra"
  )
  fun hentHusstandsmedlem(husstandId: Int): List<Husstandsmedlemskap>
}
