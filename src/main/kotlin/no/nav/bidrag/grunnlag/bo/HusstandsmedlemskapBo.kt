package no.nav.bidrag.grunnlag.bo

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.grunnlag.persistence.entity.Husstandsmedlemskap
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.reflect.full.memberProperties

data class HusstandsmedlemskapBo(

  @Schema(description = "Grunnlagspakke-id")
  val grunnlagspakkeId: Int = 0,

  @Schema(description = "Personid til BM eller BP")
  var partPersonId: String?,

  @Schema(description = "Identen til husstandsmedlemmet")
  var husstandsmedlemPersonId: String?,

  @Schema(description = "Navn på husstandsmedlemmet, format <Fornavn, mellomnavn, Etternavn")
  var navn: String?,

  @Schema(description = "Husstandsmedlemmets fødselsdag")
  var fodselsdato: LocalDate?,

  @Schema(description = "Angir om husstandsmedlemmet er barn av BM eller BM, som dette grunnlaget er hentet for")
  var erBarnAvBmBp: Boolean,

  @Schema(description = "Husstandsmedlemmet bor i husstanden fra- og med måned")
  val periodeFra: LocalDate?,

  @Schema(description = "Husstandsmedlemmet bor i husstanden til- og med måned")
  val periodeTil: LocalDate?,

  @Schema(description = "Angir om en sivilstand er aktiv")
  val aktiv: Boolean = true,

  @Schema(description = "Tidspunkt sivilstanden taes i bruk")
  val brukFra: LocalDateTime = LocalDateTime.now(),

  @Schema(description = "Tidspunkt sivilstanden ikke lenger er aktiv. Null betyr at sivilstanden er aktiv")
  val brukTil: LocalDateTime? = null,

  @Schema(description = "Opprettet tidspunkt")
  val hentetTidspunkt: LocalDateTime
)

fun HusstandsmedlemskapBo.toHusstandsmedlemskapEntity() = with(::Husstandsmedlemskap) {
  val propertiesByName = HusstandsmedlemskapBo::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      Husstandsmedlemskap::husstandsmedlemskapId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toHusstandsmedlemskapEntity)
    }
  })

}