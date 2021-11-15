package no.nav.bidrag.grunnlag.dto

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.grunnlag.persistence.entity.Skattegrunnlagspost
import java.math.BigDecimal
import kotlin.reflect.full.memberProperties

data class SkattegrunnlagspostDto(

  @Schema(description = "Skattegrunnlagspost-id")
  val skattegrunnlagspostId: Int = 0,

  @Schema(description = "Skattegrunnlag-id")
  val skattegrunnlagId: Int = 0,

  @Schema(description = "Ordinær eller Svalbard")
  val skattegrunnlagType: String = "",

  @Schema(description = "Type inntekt, Lonnsinntekt, Naeringsinntekt, Pensjon eller trygd, Ytelse fra offentlig")
  val inntektType: String = "",

  @Schema(description = "Belop")
  val belop: BigDecimal = BigDecimal.ZERO
)

fun SkattegrunnlagspostDto.toSkattegrunnlagspostEntity() = with(::Skattegrunnlagspost) {
  val propertiesByName = SkattegrunnlagspostDto::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      else -> propertiesByName[parameter.name]?.get(this@toSkattegrunnlagspostEntity)
    }
  })

}