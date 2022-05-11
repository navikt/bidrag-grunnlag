package no.nav.bidrag.grunnlag.bo

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.behandling.felles.enums.SivilstandKode
import no.nav.bidrag.grunnlag.persistence.entity.Sivilstand
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.reflect.full.memberProperties

data class SivilstandBo(

  @Schema(description = "Person-id til personen sivilstanden gjelder for")
  val personId: Int = 0,

  @Schema(description = "Periode fra- og med måned")
  val periodeFra: LocalDate,

  @Schema(description = "Periode til- og med måned")
  val periodeTil: LocalDate?,

  @Schema(description = "Person-id til barnet")
  val sivilstand: SivilstandKode,

  @Schema(description = "Angis hvis barnet er manuelt registrert")
  val opprettetAv: String? = "",

  @Schema(description = "Lagret tidspunkt")
  val lagretTidspunkt: LocalDateTime = LocalDateTime.now()

)

fun SivilstandBo.toSivilstandEntity() = with(::Sivilstand) {
  val propertiesByName = SivilstandBo::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      Sivilstand::sivilstandId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toSivilstandEntity)
    }
  })

}