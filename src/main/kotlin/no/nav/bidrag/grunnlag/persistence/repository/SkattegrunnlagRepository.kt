package no.nav.bidrag.grunnlag.persistence.repository

import no.nav.bidrag.grunnlag.persistence.entity.Skattegrunnlag
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface SkattegrunnlagRepository : JpaRepository<Skattegrunnlag, Int?> {

  @Query(
      "select ints from Skattegrunnlag ints where ints.grunnlagspakkeId = :grunnlagspakkeId and ints.aktiv = true"
  )
  fun hentSkattegrunnlag(grunnlagspakkeId: Int): List<Skattegrunnlag>

  @Query(
      "update Skattegrunnlag ints set ints.aktiv = false, ints.brukTil = CURRENT_TIMESTAMP where ints.skattegrunnlagId = :inntektId"
  )
  @Modifying
  fun settSkattegrunnlagSomInaktiv(inntektId: Int)

}