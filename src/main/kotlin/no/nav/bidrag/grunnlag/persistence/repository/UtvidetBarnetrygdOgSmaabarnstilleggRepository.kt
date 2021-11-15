package no.nav.bidrag.grunnlag.persistence.repository

import no.nav.bidrag.grunnlag.persistence.entity.UtvidetBarnetrygdOgSmaabarnstillegg
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UtvidetBarnetrygdOgSmaabarnstilleggRepository : JpaRepository<UtvidetBarnetrygdOgSmaabarnstillegg, Int?> {

  @Query(
    "select ubst from UtvidetBarnetrygdOgSmaabarnstillegg ubst where ubst.grunnlagspakkeId = :grunnlagspakkeId and ubst.aktiv = true"
  )
  fun hentUbst(grunnlagspakkeId: Int): List<UtvidetBarnetrygdOgSmaabarnstillegg>



}