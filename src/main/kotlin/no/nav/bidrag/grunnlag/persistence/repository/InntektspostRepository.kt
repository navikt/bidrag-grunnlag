package no.nav.bidrag.grunnlag.persistence.repository

import no.nav.bidrag.grunnlag.persistence.entity.Inntektspost
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface InntektspostRepository : JpaRepository<Inntektspost, Int?> {

  @Query(
    "select inp from Inntektspost inp where inp.inntektId = :inntektId"
  )
  fun hentInntektsposter(inntektId: Int): List<Inntektspost>

}