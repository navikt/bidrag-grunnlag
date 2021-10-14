package no.nav.bidrag.grunnlag.dto

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.grunnlag.persistence.entity.Grunnlagspakke
import no.nav.bidrag.grunnlag.persistence.entity.Inntekt
import no.nav.bidrag.grunnlag.persistence.entity.Stonad
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.reflect.full.memberProperties

data class StonadDto (

  @Schema(description = "Stonad-id")
  val stonadId: Int = 0,

  @Schema(description = "Grunnlagspakke-id")
  val grunnlagspakkeId: Int = 0,

  @Schema(description = "Id til personen inntekten er rapport for")
  val personId: Int = 0,

  @Schema(description = "Type stønad")
  val type: String = "",

  @Schema(description = "Periode fra- og med måned")
  val periodeFra: LocalDate = LocalDate.now(),

  @Schema(description = "Periode til- og med måned")
  val periodeTil: LocalDate = LocalDate.now(),

  @Schema(description = "Belop")
  val belop: BigDecimal = BigDecimal.ZERO,

  @Schema(description = "Angir om stønaden er manuelt beregnet")
  val manueltBeregnet: Boolean = false,

  @Schema(description = "Hentet tidspunkt")
  val hentetTidspunkt: LocalDateTime = LocalDateTime.now(),

)

fun StonadDto.toStonadEntity() = with(::Stonad) {
  val propertiesByName = StonadDto::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      else -> propertiesByName[parameter.name]?.get(this@toStonadEntity)
    }
  })

}