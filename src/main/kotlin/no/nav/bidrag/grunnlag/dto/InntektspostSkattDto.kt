package no.nav.bidrag.grunnlag.dto

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.grunnlag.persistence.entity.Inntektspost
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.reflect.full.memberProperties

data class InntektspostSkattDto (

  @Schema(description = "Inntektspost-id")
  val inntektspostId: Int = 0,

  @Schema(description = "Inntekt-id")
  val inntektId: Int = 0,

  @Schema(description = "Perioden innteksposten er utbetalt YYYYMM")
  val utbetalingsperiode: String = "",

  @Schema(description = "Type inntekt, Lonnsinntekt, Naeringsinntekt, Pensjon eller trygd, Ytelse fra offentlig")
  val type: String = "",

  @Schema(description = "Belop")
  val belop: BigDecimal = BigDecimal.ZERO
)

fun InntektspostSkattDto.toInntektspostSkattEntity() = with(::InntektspostSkatt) {
  val propertiesByName = InntektspostSkattDto::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      else -> propertiesByName[parameter.name]?.get(this@toInntektspostSkattEntity)
    }
  })

}