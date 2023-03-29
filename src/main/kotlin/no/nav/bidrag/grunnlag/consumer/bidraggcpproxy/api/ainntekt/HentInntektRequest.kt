package no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.ainntekt

import java.time.LocalDate

data class HentInntektRequest(
    val ident: String,
    val innsynHistoriskeInntekterDato: LocalDate?,
    val maanedFom: String,
    val maanedTom: String,
    val ainntektsfilter: String,
    val formaal: String
)
