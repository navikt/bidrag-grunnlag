package no.nav.bidrag.grunnlag.persistence.repository

import no.nav.bidrag.grunnlag.persistence.entity.Grunnlagspakke
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface GrunnlagspakkeRepository : JpaRepository<Grunnlagspakke, Int?>{

  @Query(
    "update Grunnlagspakke gp set gp.gyldigTil = :gyldigTil where gp.grunnlagspakkeId = :grunnlagspakkeId"
  )
  @Modifying
  fun settGyldigTildatoGrunnlagspakke(grunnlagspakkeId: Int, gyldigTil: LocalDate)

}