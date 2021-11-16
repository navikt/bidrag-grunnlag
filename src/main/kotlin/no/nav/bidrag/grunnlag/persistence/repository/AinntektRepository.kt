package no.nav.bidrag.grunnlag.persistence.repository

import no.nav.bidrag.grunnlag.persistence.entity.Ainntekt
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface AinntektRepository : JpaRepository<Ainntekt, Int?> {

  @Query(
//    "select inta from Ainntekt inta where inta.grunnlagspakkeId = :grunnlagspakkeId"
    "select ain from Ainntekt ain where ain.grunnlagspakkeId = :grunnlagspakkeId and ain.aktiv = true"
  )
  fun hentAinntekter(grunnlagspakkeId: Int): List<Ainntekt>

  @Query(
    "update Ainntekt ain set ain.aktiv = false, ain.brukTil = CURRENT_TIMESTAMP where ain.inntektId = :inntektId"
  )
  @Modifying
  fun settInntektSomInaktiv(inntektId: Int)

}