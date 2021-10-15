package no.nav.bidrag.grunnlag.persistence.repository

import no.nav.bidrag.grunnlag.persistence.entity.InntektAinntekt
import no.nav.bidrag.grunnlag.persistence.entity.InntektSkatt
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface InntektSkattRepository : JpaRepository<InntektSkatt, Int?> {

  @Query(
    "select ints from InntektSkatt ints where ints.grunnlagspakkeId = :grunnlagspakkeId and ints.aktiv = true"
  )
  fun hentInntekter(grunnlagspakkeId: Int): List<InntektSkatt>

  @Query(
    "update InntektSkatt ints set ints.aktiv = false, ints.brukTil = CURRENT_TIMESTAMP where ints.inntektId = :inntektId"
  )
  @Modifying
  fun settInntektSomInnaktiv(inntektId: Int)

}