package no.nav.bidrag.grunnlag.consumer.aareg.api

import java.math.BigDecimal
import java.time.LocalDate

data class HentArbeidsforholdResponse(
    val barnetilleggPensjonListe: List<Arbeidsforhold>?
)

data class Arbeidsforhold(
    val barn: String,
    val beloep: BigDecimal,
    val fom: LocalDate,
    val tom: LocalDate?,
    val erFellesbarn: Boolean
)
