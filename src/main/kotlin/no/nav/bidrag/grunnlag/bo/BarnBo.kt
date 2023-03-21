package no.nav.bidrag.grunnlag.bo

import java.time.LocalDate

data class BarnBo(
  val personId: String?,
  var navn: String?,
  var fodselsdato: LocalDate?
  )
