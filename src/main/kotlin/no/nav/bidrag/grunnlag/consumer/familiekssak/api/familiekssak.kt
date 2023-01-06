package no.nav.bidrag.grunnlag.consumer.familiekssak.api

import java.time.YearMonth

data class BisysDto(val barnIdenter: List<String>)

data class BisysResponsDto(val utbetalingsinfo: Map<String, List<UtbetalingsinfoDto>>)

data class UtbetalingsinfoDto(
  val fomMåned: YearMonth,
  val tomMåned: YearMonth?,
  val beløp: Int
)