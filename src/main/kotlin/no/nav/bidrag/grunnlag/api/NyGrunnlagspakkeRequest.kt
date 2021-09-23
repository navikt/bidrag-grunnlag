package no.nav.bidrag.grunnlag.api

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.grunnlag.dto.GrunnlagspakkeDto
import kotlin.reflect.full.memberProperties

@Schema(description ="Request for å endre mottaker-id på en stønad")
data class NyGrunnlagspakkeRequest (

  @Schema(description = "opprettet av")
  val opprettetAv: String = ""
)

fun NyGrunnlagspakkeRequest.toGrunnlagspakkeDto() = with(::GrunnlagspakkeDto) {
  val propertiesByName = NyGrunnlagspakkeRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      else -> propertiesByName[parameter.name]?.get(this@toGrunnlagspakkeDto)
    }
  })

}