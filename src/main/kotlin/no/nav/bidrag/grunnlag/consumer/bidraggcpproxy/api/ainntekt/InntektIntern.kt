package no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.ainntekt

import java.math.BigDecimal
import java.time.LocalDate

data class InntektIntern(
  val inntektType: String,
  val beloep: BigDecimal,
  val fordel: String?,
  val inntektskilde: String?,
  val inntektsperiodetype: String?,
  val inntektsstatus: String?,
  val leveringstidspunkt: String?,
  val opptjeningsland: String?,
  val opptjeningsperiodeFom: LocalDate?,
  val opptjeningsperiodeTom: LocalDate?,
  val utbetaltIMaaned: String?,
  val opplysningspliktig: OpplysningspliktigIntern?,
  val virksomhet: VirksomhetIntern?,
  val tilleggsinformasjon: TilleggsinformasjonIntern?,
  val inntektsmottaker: InntektsmottakerIntern?,
  val inngaarIGrunnlagForTrekk: Boolean,
  val utloeserArbeidsgiveravgift: Boolean,
  val informasjonsstatus: String?,
  val beskrivelse: String?,
  val skatteOgAvgiftsregel: String?,
  val antall: Int?
)
