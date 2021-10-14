package no.nav.bidrag.grunnlag.persistence.repository

import no.nav.bidrag.grunnlag.persistence.entity.Stonad
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface StonadRepository : JpaRepository<Stonad, Int?> {

  @Query(
    "select st from Stonad st where st.grunnlagspakkeId = :grunnlagspakkeId"
  )
  fun hentStonader(grunnlagspakkeId: Int): List<Stonad>



}