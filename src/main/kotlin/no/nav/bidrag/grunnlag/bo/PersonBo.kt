package no.nav.bidrag.grunnlag.bo

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.grunnlag.persistence.entity.Person
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.reflect.full.memberProperties

data class PersonBo(

  @Schema(description = "Person-id til BM/BP")
  val personId: Int,

  @Schema(description = "Personens navn")
  val navn: String?,

  @Schema(description = "Personens fødselsdato")
  val foedselsdato: LocalDate?,

  @Schema(description = "Personens eventuelle dødsdato")
  val doedsdato: LocalDate?,

  @Schema(description = "Angir om en personopplysning er aktiv")
  val aktiv: Boolean = true,

  @Schema(description = "Tidspunkt personopplysning taes i bruk")
  val brukFra: LocalDateTime = LocalDateTime.now(),

  @Schema(description = "Tidspunkt personopplysningen ikke lenger aktiv. Null betyr at personopplysningen er aktiv")
  val brukTil: LocalDateTime? = null,

  @Schema(description = "Hentet tidspunkt")
  val hentetTidspunkt: LocalDateTime = LocalDateTime.now()

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