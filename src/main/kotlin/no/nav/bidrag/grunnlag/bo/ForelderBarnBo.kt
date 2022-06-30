package no.nav.bidrag.grunnlag.bo

import no.nav.bidrag.grunnlag.persistence.entity.ForelderBarn
import kotlin.reflect.full.memberProperties

data class ForelderBarnBo(
  val forelderId: Int,
  val barnId: Int
)

fun ForelderBarnBo.toForelderBarnEntity() = with(::ForelderBarn) {
  val propertiesByName = ForelderBarnBo::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      ForelderBarn::forelder.name -> 0
      ForelderBarn::barn.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toForelderBarnEntity)
    }
  })
}