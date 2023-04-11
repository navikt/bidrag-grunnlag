package no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.barnetillegg

import java.math.BigDecimal
import java.time.LocalDate

data class HentBarnetilleggPensjonResponse(
    val barnetilleggPensjonListe: List<BarnetilleggPensjon>?
)

data class BarnetilleggPensjon(
    val barn: String,
    val beloep: BigDecimal,
    val fom: LocalDate,
    val tom: LocalDate?,
    val erFellesbarn: Boolean
)
