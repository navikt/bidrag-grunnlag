package no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.skatt

data class HentSkattegrunnlagResponse(
    val grunnlag: List<Skattegrunnlag>?,
    val svalbardGrunnlag: List<Skattegrunnlag>?,
    val skatteoppgjoersdato: String?
)
