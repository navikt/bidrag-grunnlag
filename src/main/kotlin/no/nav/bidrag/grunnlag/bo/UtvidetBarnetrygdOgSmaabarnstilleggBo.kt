package no.nav.bidrag.grunnlag.bo

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.grunnlag.persistence.entity.UtvidetBarnetrygdOgSmaabarnstillegg
import java.time.LocalDateTime

import java.math.BigDecimal
import java.time.LocalDate
import kotlin.reflect.full.memberProperties

data class UtvidetBarnetrygdOgSmaabarnstilleggBo(

  @Schema(description = "ubst-id")
  val ubstId: Int = 0,

  @Schema(description = "Grunnlagspakke-id")
  val grunnlagspakkeId: Int = 0,

  @Schema(description = "Id til personen inntekten er rapport for")
  val personId: String = "",

  @Schema(description = "Type stønad")
  val type: String = "",

  @Schema(description = "Periode fra- og med måned")
  val periodeFra: LocalDate = LocalDate.now(),

  @Schema(description = "Periode til- og med måned")
  val periodeTil: LocalDate? = LocalDate.now(),

  @Schema(description = "Angir om en inntektsopplysning er aktiv")
  val aktiv: Boolean = true,

  @Schema(description = "Tidspunkt inntekten taes i bruk")
  val brukFra: LocalDateTime = LocalDateTime.now(),

  @Schema(description = "Tidspunkt inntekten ikke lenger aktiv. Null betyr at inntekten er aktiv")
  val brukTil: LocalDateTime? = null,

  @Schema(description = "Belop")
  val belop: BigDecimal = BigDecimal.ZERO,

  @Schema(description = "Angir om stønaden er manuelt beregnet")
  val manueltBeregnet: Boolean = false,

  @Schema(description = "Angir om barnet har delt bosted")
  val deltBosted: Boolean = false,

  @Schema(description = "Hentet tidspunkt")
  val hentetTidspunkt: LocalDateTime = LocalDateTime.now()
  )

fun UtvidetBarnetrygdOgSmaabarnstilleggBo.toUtvidetBarnetrygdOgSmaabarnstilleggEntity() = with(::UtvidetBarnetrygdOgSmaabarnstillegg) {
  val propertiesByName = UtvidetBarnetrygdOgSmaabarnstilleggBo::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      else -> propertiesByName[parameter.name]?.get(this@toUtvidetBarnetrygdOgSmaabarnstilleggEntity)
    }
  })

}