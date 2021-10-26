package no.nav.bidrag.gcp.proxy.consumer.inntektskomponenten.response

data class ArbeidsInntektMaaned(
  val aarMaaned: String,
  var arbeidsInntektInformasjon: ArbeidsInntektInformasjon
)
