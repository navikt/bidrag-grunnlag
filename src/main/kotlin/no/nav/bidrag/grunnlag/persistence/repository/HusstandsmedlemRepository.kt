package no.nav.bidrag.grunnlag.persistence.repository

import no.nav.bidrag.grunnlag.persistence.entity.Husstand
import no.nav.bidrag.grunnlag.persistence.entity.Husstandsmedlem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface HusstandsmedlemRepository : JpaRepository<Husstandsmedlem, Int?> {

  @Query(
    "select hum from Husstandsmedlem hum where hum.husstandId = :husstandId order by hum.periodeFra"
  )
  fun hentHusstandsmedlem(husstandId: Int): List<Husstandsmedlem>
}
