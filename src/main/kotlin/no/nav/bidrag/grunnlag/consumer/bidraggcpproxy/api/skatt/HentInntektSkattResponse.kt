package no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.skatt

data class HentInntektSkattResponse (
    val grunnlag: List<Skattegrunnlag>?,
    val svalbardGrunnlag: List<Skattegrunnlag>?,
    val skatteoppgjoersdato: String?
)

data class Skattegrunnlag(
    val beloep: String,
    val tekniskNavn: String
)