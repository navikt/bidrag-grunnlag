package no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.skatt

data class HentInntektSkattRequest (
    val inntektsAar: String,
    val inntektsFilter: String,
    val personId: String
)