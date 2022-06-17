package no.nav.bidrag.grunnlag.consumer.infotrygdkontantstottev2.api

import java.time.YearMonth

data class InnsynRequest(
  val fnr: List<String>
)

data class InnsynResponse(
  val data: List<StonadDto>
)

data class StonadDto(
  val fnr: Foedselsnummer,
  val utbetalinger: List<UtbetalingDto>,
  val barn: List<BarnDto>
)

data class UtbetalingDto(
  val fom: YearMonth?,
  val tom: YearMonth?,
  val belop: Int
)

data class BarnDto(
  val fnr: Foedselsnummer
)

data class Foedselsnummer(
  val fnr: String
)