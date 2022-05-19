package no.nav.bidrag.grunnlag.persistence.repository

import no.nav.bidrag.grunnlag.persistence.entity.ForelderBarn
import no.nav.bidrag.grunnlag.persistence.entity.ForelderBarnPK
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface ForelderBarnRepository : CrudRepository<ForelderBarn, ForelderBarnPK?>{

  @Query(
    "select fb from ForelderBarn fb " +
        "where fb.forelder.forelderId = :forelderId and fb.barn.barnId = :barnId"
  )
  fun hentForelderBarn(forelderId: Int, barnId: Int): ForelderBarn

  @Query(
    "select fa from ForelderBarn fa where fa.forelder.forelderId = :forelderId"
  )
  fun hentAlleBarnForForelder(forelderId: Int): List<ForelderBarn>

}
