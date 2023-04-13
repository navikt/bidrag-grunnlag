package no.nav.bidrag.grunnlag.persistence.repository

import no.nav.bidrag.grunnlag.persistence.entity.Overgangsstonad
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface OvergangsstonadRepository : JpaRepository<Overgangsstonad, Int?> {

    @Query(
        "select os from Overgangsstonad os where os.grunnlagspakkeId = :grunnlagspakkeId and os.aktiv = true order by os.partPersonId, os.periodeFra"
    )
    fun hentOvergangsstonad(grunnlagspakkeId: Int): List<Overgangsstonad>

    @Modifying
    @Query(
        "update Overgangsstonad os " +
            "set os.aktiv = false, os.brukTil = :timestampOppdatering " +
            "where os.grunnlagspakkeId = :grunnlagspakkeId and os.partPersonId = :partPersonId and os.aktiv = true"
    )
    fun oppdaterEksisterendeOvergangsstonadTilInaktiv(
        grunnlagspakkeId: Int,
        partPersonId: String,
        timestampOppdatering: LocalDateTime
    )
}
