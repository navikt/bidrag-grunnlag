package no.nav.bidrag.grunnlag.persistence.repository

import no.nav.bidrag.grunnlag.persistence.entity.Barnetillegg
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface BarnetilleggRepository : JpaRepository<Barnetillegg, Int?> {

  @Query(
    "select bt from Barnetillegg bt where bt.grunnlagspakkeId = :grunnlagspakkeId and bt.aktiv = true"
  )

  fun hentBarnetillegg(grunnlagspakkeId: Int): List<Barnetillegg>
}