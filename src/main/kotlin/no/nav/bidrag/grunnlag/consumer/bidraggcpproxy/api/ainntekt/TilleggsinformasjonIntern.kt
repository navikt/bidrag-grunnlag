package no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.ainntekt

data class TilleggsinformasjonIntern(
    val kategori: String,
    val tilleggsinformasjonDetaljer: TilleggsinformasjonDetaljerIntern
)
