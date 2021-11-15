package no.nav.bidrag.grunnlag.dto

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.grunnlag.persistence.entity.Grunnlagspakke
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.reflect.full.memberProperties

data class GrunnlagspakkeDto (

  @Schema(description = "grunnlagspakke-id")
  val grunnlagspakkeId: Int = 0,

  @Schema(description = "opprettet av")
  val opprettetAv: String = "",

  @Schema(description = "opprettet timestamp")
  val opprettetTimestamp: LocalDateTime = LocalDateTime.now(),

  @Schema(description = "Endret timestamp")
  val endretTimestamp: LocalDateTime? = null,

  @Schema(description = "Gyldig til-dato")
  val gyldigTil: LocalDate? = null,

  @Schema(description = "Til hvilket formål skal grunnlagspakken benyttes. Bidrag, Forskudd, Særtilskudd")
  val formaal: String = "",

  )

fun GrunnlagspakkeDto.toGrunnlagspakkeEntity() = with(::Grunnlagspakke) {
  val propertiesByName = GrunnlagspakkeDto::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      else -> propertiesByName[parameter.name]?.get(this@toGrunnlagspakkeEntity)
    }
  })

}