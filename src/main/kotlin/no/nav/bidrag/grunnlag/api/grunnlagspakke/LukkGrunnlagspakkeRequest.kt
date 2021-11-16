package no.nav.bidrag.grunnlag.api.grunnlagspakke

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.grunnlag.api.deserialization.IntDeserializer

data class LukkGrunnlagspakkeRequest(

  @Schema(description = "Grunnlagspakke-id. GyldigTil-dato settes lik dagens dato for angitt gunnlagspakke")
  @JsonDeserialize(using = IntDeserializer::class)
  val grunnlagspakkeId: Int

)