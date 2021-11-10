package no.nav.bidrag.grunnlag.persistence.repository

import no.nav.bidrag.grunnlag.persistence.entity.Grunnlagspakke
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface GrunnlagspakkeRepository : JpaRepository<Grunnlagspakke, Int?> {

  @Query(
    "select gp from Grunnlagspakke gp where gp.grunnlagspakkeId = :grunnlagspakkeId"
  )
  fun hentGrunnlagspakke(grunnlagspakkeId: Int): Grunnlagspakke


  @Query(
    "update Grunnlagspakke gp set gp.gyldigTil = current_date where gp.grunnlagspakkeId = :grunnlagspakkeId"
  )
  @Modifying
  fun lukkGrunnlagspakke(grunnlagspakkeId: Int)


  @Query(
    "select gp.formaal from Grunnlagspakke gp where gp.grunnlagspakkeId = :grunnlagspakkeId"
  )
  fun hentFormaalGrunnlagspakke(grunnlagspakkeId: Int): String

}

