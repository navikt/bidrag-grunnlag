package no.nav.bidrag.grunnlag.api.grunnlagspakke

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.grunnlag.api.ainntekt.AinntektDto
import no.nav.bidrag.grunnlag.api.barnetillegg.BarnetilleggDto
import no.nav.bidrag.grunnlag.api.skatt.SkattegrunnlagDto
import no.nav.bidrag.grunnlag.api.ubst.UtvidetBarnetrygdOgSmaabarnstilleggDto

data class HentGrunnlagspakkeDto(

  @Schema(description = "grunnlagspakke-id")
  val grunnlagspakkeId: Int = 0,

  @Schema(description = "Liste over innhentede inntekter fra a-inntekt og underliggende poster")
  val ainntektListe: List<AinntektDto> = emptyList(),

  @Schema(description = "Liste over innhentede fra skatt og underliggende poster")
  val skattegrunnlagListe: List<SkattegrunnlagDto> = emptyList(),

  @Schema(description = "Liste over innhentet utvidet barnetrygd og småbarnstillegg")
  val ubstListe: List<UtvidetBarnetrygdOgSmaabarnstilleggDto> = emptyList(),

  @Schema(description = "Liste over innhentet barnetillegg")
  val barnetilleggListe: List<BarnetilleggDto> = emptyList()
)