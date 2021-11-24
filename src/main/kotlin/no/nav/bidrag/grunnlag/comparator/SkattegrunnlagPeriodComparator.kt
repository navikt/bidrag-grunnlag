package no.nav.bidrag.grunnlag.comparator

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.grunnlag.dto.SkattegrunnlagDto
import no.nav.bidrag.grunnlag.dto.SkattegrunnlagspostDto
import java.math.BigDecimal

class SkattegrunnlagPeriodComparator : AbstractPeriodComparator<PeriodComparableWithChildren<SkattegrunnlagDto, SkattegrunnlagspostDto>>() {
  override fun isEntitiesEqual(
    newEntity: PeriodComparableWithChildren<SkattegrunnlagDto, SkattegrunnlagspostDto>,
    existingEntity: PeriodComparableWithChildren<SkattegrunnlagDto, SkattegrunnlagspostDto>
  ): Boolean {
    val newSkattegrunnlagsposter = sortSkattegrunnlagsposter(newEntity.children)
    val existingSkattegrunnlagsposter = sortSkattegrunnlagsposter(existingEntity.children)
    if (newSkattegrunnlagsposter.size != existingSkattegrunnlagsposter.size) {
      return false
    }
    val differences = mutableMapOf<String, String>()
    for (i in newSkattegrunnlagsposter.indices) {
      val newSkattegrunnlagspost = newSkattegrunnlagsposter[i]
      val existingSkattegrunnlagspost = existingSkattegrunnlagsposter[i]

      differences.putAll(compare(newSkattegrunnlagspost.inntektType, existingSkattegrunnlagspost.inntektType, "inntektType"))
      differences.putAll(compare(newSkattegrunnlagspost.skattegrunnlagType, existingSkattegrunnlagspost.skattegrunnlagType, "skattegrunnlagType"))
      differences.putAll(compare(newSkattegrunnlagspost.belop, existingSkattegrunnlagspost.belop, "belop"))
    }
    if (differences.isNotEmpty()) {
      LOGGER.info(ObjectMapper().findAndRegisterModules().writeValueAsString(differences))
    }
    return differences.isEmpty()
  }

  private fun compare(newEntity: Any, existingEntity: Any, fieldName: String): Map<String, String> {
    val differences = mutableMapOf<String, String>()
    val entitiesNotEqual = when (newEntity) {
      is BigDecimal -> newEntity.compareTo(existingEntity as BigDecimal) != 0
      else -> newEntity != existingEntity
    }
    if (entitiesNotEqual){
      differences[fieldName] = "$newEntity != $existingEntity"
    }
    return differences
  }

  private fun sortSkattegrunnlagsposter(ainntektsposter: List<SkattegrunnlagspostDto>): List<SkattegrunnlagspostDto> {
    return ainntektsposter.sortedWith(compareBy({ it.inntektType }, { it.skattegrunnlagType }))
    }
  }
}