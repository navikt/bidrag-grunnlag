package no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.ainntekt

import java.time.LocalDate

data class TilleggsinformasjonDetaljerIntern(
  val etterbetalingsperiodeFom: LocalDate,
  val etterbetalingsperiodeTom: LocalDate
)
