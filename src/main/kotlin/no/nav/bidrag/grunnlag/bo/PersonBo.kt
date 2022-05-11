package no.nav.bidrag.grunnlag.bo

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.grunnlag.persistence.entity.Person
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.reflect.full.memberProperties

data class PersonBo(

  @Schema(description = "Person-id til BM/BP")
  val personId: Int = 0,

  @Schema(description = "Personens navn")
  val navn: String?,

  @Schema(description = "Personens fødselsdato")
  val foedselsdato: LocalDate?,

  @Schema(description = "Personens eventuelle dødsdato")
  val doedsdato: LocalDate?,

  @Schema(description = "Angis hvis barnet er manuelt registrert")
  val opprettetAv: String? = "",

  @Schema(description = "Lagret tidspunkt")
  val lagretTidspunkt: LocalDateTime = LocalDateTime.now()

)

fun PersonBo.toPersonEntity() = with(::Person) {
  val propertiesByName = PersonBo::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      Person::personnDbId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toPersonEntity)
    }
  })

}