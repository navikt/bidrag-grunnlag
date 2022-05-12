package no.nav.bidrag.grunnlag.persistence.repository

import no.nav.bidrag.grunnlag.persistence.entity.Husstand
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface HusstandRepository : JpaRepository<Husstand, Int?> {

  @Query(
    "select hu from Husstand hu where hu.grunnlagspakkeId = :grunnlagspakkeId and hu.aktiv = true"
  )
  fun hentHusstand(grunnlagspakkeId: Int): List<Husstand>
}
