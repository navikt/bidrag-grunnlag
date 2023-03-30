package no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.barnetillegg

import java.time.LocalDate

data class HentBarnetilleggPensjonRequest(
    val mottaker: String,
    val fom: LocalDate,
    val tom: LocalDate,
    val returnerFellesbarn: Boolean = true,
    val returnerSaerkullsbarn: Boolean = true
)
