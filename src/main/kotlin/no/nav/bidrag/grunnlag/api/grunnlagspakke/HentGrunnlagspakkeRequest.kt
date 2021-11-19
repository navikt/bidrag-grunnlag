package no.nav.bidrag.grunnlag.api.grunnlagspakke

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.grunnlag.api.deserialization.IntDeserializer

data class HentGrunnlagspakkeRequest(

  @Schema(description = "grunnlagspakke-id")
  @JsonDeserialize(using = IntDeserializer::class)
  val grunnlagspakkeId: Int
)