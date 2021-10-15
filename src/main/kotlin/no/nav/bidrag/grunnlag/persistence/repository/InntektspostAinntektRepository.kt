package no.nav.bidrag.grunnlag.persistence.repository

import no.nav.bidrag.grunnlag.persistence.entity.InntektspostAinntekt
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface InntektspostAinntektRepository : JpaRepository<InntektspostAinntekt, Int?> {

  @Query(
    "select inpa from InntektspostAinntekt inpa where inpa.inntektId = :inntektId"
  )
  fun hentInntektsposter(inntektId: Int): List<InntektspostAinntekt>

}