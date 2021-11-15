package no.nav.bidrag.grunnlag.api.grunnlagspakke

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.grunnlag.dto.GrunnlagspakkeDto
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import kotlin.reflect.full.memberProperties

@Schema(description ="Request for å opprette ny grunnlagspakke, uten annet innhold")
data class OpprettGrunnlagspakkeRequest (

  @Schema(description = "opprettet av")
  @field:NotBlank(message = "Kan ikke være null eller blank")
  val opprettetAv: String
)

fun OpprettGrunnlagspakkeRequest.toGrunnlagspakkeDto(opprettetAv: String) = with(::GrunnlagspakkeDto) {
  val propertiesByName = OpprettGrunnlagspakkeRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      GrunnlagspakkeDto::opprettetAv.name -> opprettetAv
      else -> propertiesByName[parameter.name]?.get(this@toGrunnlagspakkeDto)
    }
  })

}