package no.nav.bidrag.grunnlag.persistence.repository

import no.nav.bidrag.grunnlag.persistence.entity.Barn
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface BarnRepository : JpaRepository<Barn, Int?> {

  @Query(
    "select ba from Barn ba where ba.personDbId = :personDnId"
  )
  fun hentBarn(personDbId: Int): List<Barn>
}
