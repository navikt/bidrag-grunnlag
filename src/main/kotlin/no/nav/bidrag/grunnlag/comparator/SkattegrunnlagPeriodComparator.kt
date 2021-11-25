package no.nav.bidrag.grunnlag.comparator

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.grunnlag.dto.SkattegrunnlagDto
import no.nav.bidrag.grunnlag.dto.SkattegrunnlagspostDto

class SkattegrunnlagPeriodComparator : AbstractPeriodComparator<PeriodComparable<SkattegrunnlagDto, SkattegrunnlagspostDto>>() {
  override fun isEntitiesEqual(
    newEntity: PeriodComparable<SkattegrunnlagDto, SkattegrunnlagspostDto>,
    existingEntity: PeriodComparable<SkattegrunnlagDto, SkattegrunnlagspostDto>
  ): Boolean {
    val newSkattegrunnlagsposter = sortSkattegrunnlagsposter(newEntity.children!!)
    val existingSkattegrunnlagsposter = sortSkattegrunnlagsposter(existingEntity.children!!)
    if (newSkattegrunnlagsposter.size != existingSkattegrunnlagsposter.size) {
      return false
    }
    val differences = mutableMapOf<String, String>()
    for (i in newSkattegrunnlagsposter.indices) {
      val newSkattegrunnlagspost = newSkattegrunnlagsposter[i]
      val existingSkattegrunnlagspost = existingSkattegrunnlagsposter[i]

      differences.putAll(compareFields(newSkattegrunnlagspost.inntektType, existingSkattegrunnlagspost.inntektType, "inntektType"))
      differences.putAll(compareFields(newSkattegrunnlagspost.skattegrunnlagType, existingSkattegrunnlagspost.skattegrunnlagType, "skattegrunnlagType"))
      differences.putAll(compareFields(newSkattegrunnlagspost.belop, existingSkattegrunnlagspost.belop, "belop"))
    }
    if (differences.isNotEmpty()) {
      LOGGER.debug(ObjectMapper().findAndRegisterModules().writeValueAsString(differences))
    }
    return differences.isEmpty()
  }

  private fun sortSkattegrunnlagsposter(ainntektsposter: List<SkattegrunnlagspostDto>): List<SkattegrunnlagspostDto> {
    return ainntektsposter.sortedWith(compareBy({ it.inntektType }, { it.skattegrunnlagType }))
  }
}
