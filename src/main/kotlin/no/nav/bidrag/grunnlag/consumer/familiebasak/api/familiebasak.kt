package no.nav.bidrag.grunnlag.consumer.familiebasak.api

import java.time.LocalDate
import java.time.YearMonth

data class FamilieBaSakRequest(
  val personIdent: String,
  val fraDato: LocalDate
)

data class FamilieBaSakResponse(val perioder: List<UtvidetBarnetrygdOgSmaabarnstilleggPeriode>)

data class UtvidetBarnetrygdOgSmaabarnstilleggPeriode(
//  val stonadstype: BisysStonadstype,
  val stonadstype: String,
  val fomMaaned: YearMonth,
  val tomMaaned: YearMonth?,
  val belop: Double,
  val manueltBeregnet: Boolean,
)

enum class BisysStonadstype {
  UTVIDET,
  SMÅBARNSTILLEGG
}
