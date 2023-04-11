package no.nav.bidrag.grunnlag.persistence.repository

import no.nav.bidrag.grunnlag.persistence.entity.Barnetillegg
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface BarnetilleggRepository : JpaRepository<Barnetillegg, Int?> {

    @Query(
        "select bt from Barnetillegg bt where bt.grunnlagspakkeId = :grunnlagspakkeId and bt.aktiv = true order by bt.partPersonId, bt.periodeFra, bt.barnPersonId"
    )
    fun hentBarnetillegg(grunnlagspakkeId: Int): List<Barnetillegg>

    @Modifying
    @Query(
        "update Barnetillegg bt " +
            "set bt.aktiv = false, bt.brukTil = :timestampOppdatering " +
            "where bt.grunnlagspakkeId = :grunnlagspakkeId and bt.partPersonId = :partPersonId and bt.barnetilleggType = :barnetilleggType and bt.aktiv = true"
    )
    fun oppdaterEksisterendeBarnetilleggTilInaktiv(
        grunnlagspakkeId: Int,
        partPersonId: String,
        timestampOppdatering: LocalDateTime,
        barnetilleggType: String
    )
}
