package no.nav.bidrag.grunnlag.persistence.repository

import no.nav.bidrag.grunnlag.persistence.entity.Person
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface PersonRepository : JpaRepository<Person, Int?> {

  @Query(
    "select pe from Person pe where pe.grunnlagspakkeId = :grunnlagspakkeId and pe.aktiv = true"
  )
  fun hentPerson(grunnlagspakkeId: Int): List<Person>

  @Modifying
  @Query(
    "update Person per " +
        "set per.aktiv = false, per.brukTil = :timestampOppdatering " +
        "where per.grunnlagspakkeId = :grunnlagspakkeId and per.personId = :personId and per.aktiv = true"
  )
  fun oppdaterEksisterendePersonTilInaktiv(grunnlagspakkeId: Int, personId: String, timestampOppdatering: LocalDateTime)

}