package no.nav.bidrag.grunnlag.api.ubst

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.grunnlag.dto.UtvidetBarnetrygdOgSmaabarnstilleggDto
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.reflect.full.memberProperties

data class OpprettUtvidetBarnetrygdOgSmaabarnstilleggRequest(

  @Schema(description = "Id til personen ubst er rapport for")
  val personId: String = "",

  @Schema(description = "Type stønad, utvidet barnetrygd eller småbarnstillegg")
  val type: String = "",

  @Schema(description = "Periode fra- og med måned")
  val periodeFra: LocalDate = LocalDate.now(),

  @Schema(description = "Periode til- og med måned")
  val periodeTil: LocalDate = LocalDate.now(),

  @Schema(description = "Beløp")
  val belop: BigDecimal = BigDecimal.ZERO,

  @Schema(description = "Angir om stønaden er manuelt beregnet")
  val manueltBeregnet: Boolean = false
)

fun OpprettUtvidetBarnetrygdOgSmaabarnstilleggRequest.toUtvidetBarnetrygdOgSmaabarnstilleggDto(grunnlagspakkeId: Int) =
  with(::UtvidetBarnetrygdOgSmaabarnstilleggDto) {
  val propertiesByName =
    OpprettUtvidetBarnetrygdOgSmaabarnstilleggRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      UtvidetBarnetrygdOgSmaabarnstilleggDto::grunnlagspakkeId.name -> grunnlagspakkeId
      else -> propertiesByName[parameter.name]?.get(this@toUtvidetBarnetrygdOgSmaabarnstilleggDto)
    }
  })
}

fun OpprettUtvidetBarnetrygdOgSmaabarnstilleggRequest.toUtvidetBarnetrygdOgSmaabarnstilleggDto() =
  with(::UtvidetBarnetrygdOgSmaabarnstilleggDto) {
    val propertiesByName =
      OpprettUtvidetBarnetrygdOgSmaabarnstilleggRequest::class.memberProperties.associateBy { it.name }
    callBy(parameters.associateWith { parameter ->
      when (parameter.name) {
        UtvidetBarnetrygdOgSmaabarnstilleggDto::ubstId.name -> 0
        else -> propertiesByName[parameter.name]?.get(this@toUtvidetBarnetrygdOgSmaabarnstilleggDto)
      }
    })
  }
