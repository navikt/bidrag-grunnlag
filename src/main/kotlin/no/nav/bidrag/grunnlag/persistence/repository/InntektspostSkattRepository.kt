package no.nav.bidrag.grunnlag.persistence.repository

import no.nav.bidrag.grunnlag.persistence.entity.InntektspostSkatt
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface InntektspostSkattRepository : JpaRepository<InntektspostSkatt, Int?> {

  @Query(
    "select inps from InntektspostSkatt inps where inps.inntektId = :inntektId"
  )
  fun hentInntektsposter(inntektId: Int): List<InntektspostSkatt>

}