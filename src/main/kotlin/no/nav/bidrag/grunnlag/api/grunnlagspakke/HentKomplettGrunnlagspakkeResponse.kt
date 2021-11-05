package no.nav.bidrag.grunnlag.api.grunnlagspakke

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.grunnlag.api.ainntekt.HentInntektAinntektResponse
import no.nav.bidrag.grunnlag.api.skatt.HentSkattegrunnlagResponse
import no.nav.bidrag.grunnlag.api.ubst.HentUtvidetBarnetrygdOgSmaabarnstilleggResponse

data class HentKomplettGrunnlagspakkeResponse(

  @Schema(description = "grunnlagspakke-id")
  val grunnlagspakkeId: Int,

  @Schema(description = "Liste over innhentede inntekter fra a-inntekt og underliggende poster")
  val inntektAinntektListe: List<HentInntektAinntektResponse> = emptyList(),

  @Schema(description = "Liste over innhentede fra skatt og underliggende poster")
  val skattegrunnlagListe: List<HentSkattegrunnlagResponse> = emptyList(),

  @Schema(description = "Liste over innhentet utvidet barnetrygd og småbarnstillegg")
  val ubstListe: List<HentUtvidetBarnetrygdOgSmaabarnstilleggResponse> = emptyList()

)