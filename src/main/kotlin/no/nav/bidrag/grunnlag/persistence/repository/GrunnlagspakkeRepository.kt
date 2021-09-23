package no.nav.bidrag.grunnlag.persistence.repository

import no.nav.bidrag.grunnlag.persistence.entity.Grunnlagspakke
import org.springframework.data.jpa.repository.JpaRepository

interface GrunnlagspakkeRepository : JpaRepository<Grunnlagspakke, Int?>{
}