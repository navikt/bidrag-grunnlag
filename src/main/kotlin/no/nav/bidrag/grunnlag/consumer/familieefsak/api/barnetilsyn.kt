package no.nav.bidrag.grunnlag.consumer.familieefsak.api

import java.time.LocalDate

data class BarnetilsynRequest(
  val ident: String,
  val fomDato: LocalDate
)

data class BarnetilsynResponse(
  val barnetilsynBisysPerioder: List<BarnetilsynBisysPerioder>
)

data class BarnetilsynBisysPerioder (
  val periode: Periode,
  val barnIdenter: List<String>,
  val månedsbeløp: Int,
  val datakilde: Datakilde
)

data class Periode(
  val fom: LocalDate,
  val tom: LocalDate
)

enum class Datakilde {
  EF,
  INFOTRYGD
}
