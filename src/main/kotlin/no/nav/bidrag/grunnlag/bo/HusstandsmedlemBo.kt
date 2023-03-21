package no.nav.bidrag.grunnlag.bo

import java.time.LocalDate

data class HusstandsmedlemBo(
  val personId: String?,
  var navn: String?,
  var fodselsdato: LocalDate?,
  val gyldigFraOgMed: LocalDate? = null,
  val gyldigTilOgMed: LocalDate? = null
  )
