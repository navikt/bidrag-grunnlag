package no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api

data class HentInntektRequest(
  val ident: String,
  val maanedFom: String,
  val maanedTom: String,
  val ainntektsfilter: String,
  val formaal: String
)
