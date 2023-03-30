package no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.ainntekt

import java.math.BigDecimal
import java.time.LocalDate

data class InntektIntern(
    val inntektType: String,
    val beloep: BigDecimal,
    val fordel: String?,
    val inntektsperiodetype: String?,
    val opptjeningsperiodeFom: LocalDate?,
    val opptjeningsperiodeTom: LocalDate?,
    val utbetaltIMaaned: String?,
    val opplysningspliktig: OpplysningspliktigIntern?,
    val virksomhet: VirksomhetIntern?,
    val tilleggsinformasjon: TilleggsinformasjonIntern?,
    val beskrivelse: String?
)
