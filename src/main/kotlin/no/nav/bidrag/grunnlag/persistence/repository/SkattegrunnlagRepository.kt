package no.nav.bidrag.grunnlag.persistence.repository

import no.nav.bidrag.grunnlag.persistence.entity.Skattegrunnlag
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface SkattegrunnlagRepository : JpaRepository<Skattegrunnlag, Int?> {

  @Query(
      "select sg from Skattegrunnlag sg where sg.grunnlagspakkeId = :grunnlagspakkeId and sg.aktiv = true"
  )
  fun hentAktivtSkattegrunnlag(grunnlagspakkeId: Int): List<Skattegrunnlag>

  @Query(
      "update Skattegrunnlag sg set sg.aktiv = false, sg.brukTil = CURRENT_TIMESTAMP where sg.skattegrunnlagId = :inntektId"
  )
  @Modifying
  fun settSkattegrunnlagSomInaktiv(inntektId: Int)

}