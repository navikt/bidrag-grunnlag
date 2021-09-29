package no.nav.bidrag.grunnlag.persistence.repository

import no.nav.bidrag.grunnlag.api.OppdaterGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.persistence.entity.Inntekt
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface InntektRepository : JpaRepository<Inntekt, Int?> {

  @Query(
    "select int from Inntekt int where int.grunnlagspakkeId = :grunnlagspakkeId"
  )
  fun hentInntekter(oppdaterGrunnlagspakkeRequest: OppdaterGrunnlagspakkeRequest): List<Inntekt>

}