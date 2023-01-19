package no.nav.bidrag.grunnlag.consumer.familiekssak.api

import java.time.YearMonth

data class BisysDto(val identer: List<String>)

data class BisysResponsDto(val infotrygdPerioder: List<InfotrygdPeriode>?, val ksSakPerioder: List<KsSakPeriode>?)
data class InfotrygdPeriode(
  val fomMåned: YearMonth,
  val tomMåned: YearMonth?,
  val beløp: Int,
  val barna: List<String>
)

data class KsSakPeriode(
  val fomMåned: YearMonth,
  val tomMåned: YearMonth?,
  val barn: Barn
)

data class Barn(val beløp: Int, val ident: String)