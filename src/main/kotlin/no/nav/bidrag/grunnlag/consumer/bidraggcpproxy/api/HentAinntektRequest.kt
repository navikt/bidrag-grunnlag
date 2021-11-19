package no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api

data class HentAinntektRequest(
  val ident: String,
  val innsynHistoriskeInntekterDato: String?,
  val maanedFom: String,
  val maanedTom: String,
  val ainntektsfilter: String,
  val formaal: String
)
