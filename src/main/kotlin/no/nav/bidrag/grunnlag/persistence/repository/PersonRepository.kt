package no.nav.bidrag.grunnlag.persistence.repository

import no.nav.bidrag.grunnlag.persistence.entity.Person
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PersonRepository : JpaRepository<Person, Int?> {

  @Query(
    "select pe from Person pe where pe.grunnlagspakkeId = :grunnlagspakkeId and pe.aktiv = true"
  )
  fun hentPerson(grunnlagspakkeId: Int): List<Person>
}
