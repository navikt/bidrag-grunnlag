package no.nav.bidrag.grunnlag.consumer.familiebasak

import java.time.LocalDate
import java.time.YearMonth

data class FamilieBaSakRequest(
  val personIdent: String,
  val fraDato: LocalDate
)

data class FamilieBaSakResponse(val perioder: List<UtvidetBarnetrygdPeriode>)
data class UtvidetBarnetrygdPeriode(
  val stønadstype: BisysStønadstype,
  val fomMåned: YearMonth,
  val tomMåned: YearMonth?,
  val beløp: Double,
  val manueltBeregnet: Boolean,
)

enum class BisysStønadstype {
  UTVIDET,
  SMÅBARNSTILLEGG
}
