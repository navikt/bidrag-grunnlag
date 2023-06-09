package no.nav.bidrag.grunnlag.consumer.aareg.api

import java.time.LocalDate

data class HentArbeidsforholdRequest(
    val mottaker: String,
    val fom: LocalDate,
    val tom: LocalDate,
    val returnerFellesbarn: Boolean = true,
    val returnerSaerkullsbarn: Boolean = true
)
