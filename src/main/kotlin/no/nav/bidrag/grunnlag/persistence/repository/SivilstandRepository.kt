package no.nav.bidrag.grunnlag.persistence.repository

import no.nav.bidrag.grunnlag.persistence.entity.Sivilstand
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface SivilstandRepository : JpaRepository<Sivilstand, Int?> {

  @Query(
    "select si from Sivilstand si where si.personDbId = :personDnId"
  )
  fun hentSivilstand(personnDbId: Int): List<Sivilstand>
}
