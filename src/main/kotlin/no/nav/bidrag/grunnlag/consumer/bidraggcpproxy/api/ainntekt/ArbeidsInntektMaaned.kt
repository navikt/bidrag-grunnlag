package no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.ainntekt

data class ArbeidsInntektMaaned(
  val aarMaaned: String,
  var arbeidsInntektInformasjon: ArbeidsInntektInformasjon
)
