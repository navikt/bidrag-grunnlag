package no.nav.bidrag.grunnlag.bo

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.grunnlag.persistence.entity.Barn
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.reflect.full.memberProperties

data class BarnBo(

  @Schema(description = "Grunnlagspakke-id")
  val grunnlagspakkeId: Int = 0,

  @Schema(description = "Person-id til barnet")
  val personIdBarn: String?,

  @Schema(description = "Person-id til forelder til barnet")
  val personIdVoksen: String,

  @Schema(description = "Barnets navn")
  val navn: String?,

  @Schema(description = "Barnets fødselsdato")
  val foedselsdato: LocalDate?,

  @Schema(description = "Barnets fødselsår")
  val foedselsaar: Int?,

  @Schema(description = "Barnets eventuelle dødsdato")
  val doedsdato: LocalDate?,

  @Schema(description = "Angir om en personopplysning er aktiv")
  val aktiv: Boolean = true,

  @Schema(description = "Tidspunkt personopplysning taes i bruk")
  val brukFra: LocalDateTime = LocalDateTime.now(),

  @Schema(description = "Tidspunkt personopplysningen ikke lenger er aktiv. Null betyr at personopplysningen er aktiv")
  val brukTil: LocalDateTime? = null,

  @Schema(description = "Angis hvis barnet er manuelt registrert")
  val opprettetAv: String?,

  @Schema(description = "Opprettet tidspunkt")
  val opprettetTidspunkt: LocalDateTime

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