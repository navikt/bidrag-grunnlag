package no.nav.bidrag.grunnlag.persistence.repository

import no.nav.bidrag.grunnlag.persistence.entity.Inntekt
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface InntektRepository : JpaRepository<Inntekt, Int?> {

  @Query(
    "select int from Inntekt int where int.grunnlagspakkeId = :grunnlagspakkeId and int.aktiv = true"
  )
  fun hentInntekter(grunnlagspakkeId: Int): List<Inntekt>

  @Query(
    "update Inntekt int set int.aktiv = false, int.brukTil = CURRENT_TIMESTAMP where int.inntektId = :inntektId"
  )
  @Modifying
  fun settInntektSomInnaktiv(inntektId: Int)

}