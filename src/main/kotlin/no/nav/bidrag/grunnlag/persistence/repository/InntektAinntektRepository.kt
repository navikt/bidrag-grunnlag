package no.nav.bidrag.grunnlag.persistence.repository

import no.nav.bidrag.grunnlag.persistence.entity.InntektAinntekt
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface InntektAinntektRepository : JpaRepository<InntektAinntekt, Int?> {

  @Query(
    "select inta from InntektAinntekt inta where inta.grunnlagspakkeId = :grunnlagspakkeId and inta.aktiv = true"
  )
  fun hentInntekter(grunnlagspakkeId: Int): List<InntektAinntekt>

  @Query(
    "update InntektAinntekt inta set inta.aktiv = false, inta.brukTil = CURRENT_TIMESTAMP where inta.inntektId = :inntektId"
  )
  @Modifying
  fun settInntektSomInnaktiv(inntektId: Int)

}