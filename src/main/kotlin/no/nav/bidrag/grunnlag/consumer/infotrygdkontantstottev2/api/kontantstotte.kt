package no.nav.bidrag.grunnlag.consumer.infotrygdkontantstottev2.api

import java.time.YearMonth

data class KontantstotteRequest(
  val fnr: List<String>
)

data class KontantstotteResponse(
  val data: List<StonadDto>
)

data class StonadDto(
  val fnr: String,
  val fom: YearMonth,
  val tom: YearMonth?,
  val belop: Int,
  val barn: List<BarnDto>
)

data class BarnDto(
  val fnr: String
)