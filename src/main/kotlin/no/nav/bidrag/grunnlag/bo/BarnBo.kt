package no.nav.bidrag.grunnlag.bo

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.grunnlag.persistence.entity.Barn
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.reflect.full.memberProperties

data class BarnBo(

  @Schema(description = "Person-id til forelder til barnet")
  val personId: Int = 0,

  @Schema(description = "Person-id til barnet")
  val personIdBarn: String?,

  @Schema(description = "Barnets navn")
  val navn: String?,

  @Schema(description = "Barnets fødselsdato")
  val foedselsdato: LocalDate,

  @Schema(description = "Barnets fødselsår")
  val foedselsaar: Int?,

  @Schema(description = "Barnets eventuelle dødsdato")
  val doedsdato: LocalDate?,

  @Schema(description = "Angis hvis barnet er manuelt registrert")
  val opprettetAv: String? = "",

  @Schema(description = "Lagret tidspunkt")
  val lagretTidspunkt: LocalDateTime = LocalDateTime.now()

)

fun BarnBo.toBarnEntity() = with(::Barn) {
  val propertiesByName = BarnBo::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      Barn::barnId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toBarnEntity)
    }
  })

}