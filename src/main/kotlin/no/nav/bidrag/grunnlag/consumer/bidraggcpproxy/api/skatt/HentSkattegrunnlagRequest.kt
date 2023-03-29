package no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.skatt

data class HentSkattegrunnlagRequest(
    val inntektsAar: String,
    val inntektsFilter: String,
    val personId: String
)
