package no.nav.bidrag.grunnlag.comparator

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import java.time.LocalDate

abstract class AbstractPeriodComparator<Parent : IPeriod, Child> {

  companion object {
    @JvmStatic
    val LOGGER = LoggerFactory.getLogger(AbstractPeriodComparator::class.java)
  }

  private fun isInsidePeriod(requestedPeriod: IPeriod, existingPeriod: IPeriod): Boolean {
    return existingPeriod.periodeFra.isAfterOrEqual(requestedPeriod.periodeFra) && existingPeriod.periodeTil.isBeforeOrEqual(requestedPeriod.periodeTil)
  }

  fun comparePeriodEntities(
    requestedPeriod: IPeriod,
    newEntities: List<PeriodComparable<Parent, Child>>,
    existingEntities: List<PeriodComparable<Parent, Child>>
  ): ComparatorResult<Parent, Child> {
    val expiredEntities = mutableListOf<PeriodComparable<Parent, Child>>()
    val updatedEntities = mutableListOf<PeriodComparable<Parent, Child>>()
    val equalEntities = mutableListOf<PeriodComparable<Parent, Child>>()

    val existingEntitiesWithinRequestedPeriod = filterEntitiesByPeriod(existingEntities, requestedPeriod, expiredEntities)
    LOGGER.info("${existingEntitiesWithinRequestedPeriod.size} existing entities within requested period (${requestedPeriod.periodeFra} - ${requestedPeriod.periodeTil})")
    LOGGER.info("$expiredEntities expired entities before equality check")

    newEntities.forEach() { newEntity ->
      val existingEntityWithEqualPeriod = findCompareEntityWithEqualPeriod(newEntity, existingEntitiesWithinRequestedPeriod)
      if (existingEntityWithEqualPeriod != null) {
        if (isEntitiesEqual(newEntity, existingEntityWithEqualPeriod)) {
          equalEntities.add(existingEntityWithEqualPeriod)
        } else {
          LOGGER.info("Entities not equal. NewEntity: ${ObjectMapper().findAndRegisterModules().writeValueAsString(newEntity)}, ExistingEntity: ${ObjectMapper().findAndRegisterModules().writeValueAsString(existingEntityWithEqualPeriod)}")
          expiredEntities.add(existingEntityWithEqualPeriod)
          updatedEntities.add(newEntity)
        }
      } else {
        LOGGER.info("Could not find existing entity within the period (${newEntity.parent.periodeFra} - ${newEntity.parent.periodeTil})")
        updatedEntities.add(newEntity)
      }
    }
    return ComparatorResult(expiredEntities, updatedEntities, equalEntities)
  }

  private fun findCompareEntityWithEqualPeriod(
    newEntity: PeriodComparable<Parent, Child>,
    existingEntities: List<PeriodComparable<Parent, Child>>
  ): PeriodComparable<Parent, Child>? {
    return existingEntities.find { t -> t.parent.periodeFra.isEqual(newEntity.parent.periodeFra) && t.parent.periodeTil.isEqual(newEntity.parent.periodeTil) }
  }

  private fun filterEntitiesByPeriod(existingEntities: List<PeriodComparable<Parent, Child>>, requestedPeriod: IPeriod, expiredEntities: MutableList<PeriodComparable<Parent, Child>>): List<PeriodComparable<Parent, Child>> {
    val filteredEntities = mutableListOf<PeriodComparable<Parent, Child>>()
    existingEntities.forEach() { existingEntity ->
      if (isInsidePeriod(requestedPeriod, existingEntity.parent)) {
        filteredEntities.add(existingEntity)
      } else {
        expiredEntities.add(existingEntity)
      }
    }
    return filteredEntities
  }

  abstract fun isEntitiesEqual(newEntity: PeriodComparable<Parent, Child>, existingEntity: PeriodComparable<Parent, Child>): Boolean
}

interface IPeriod {
  val periodeFra: LocalDate
  val periodeTil: LocalDate
}

class Period(override val periodeFra: LocalDate, override val periodeTil: LocalDate) : IPeriod

class PeriodComparable<Parent, Child>(val parent: Parent, val children: List<Child>) {}


class ComparatorResult<Parent, Child>(
  val expiredEntities: List<PeriodComparable<Parent, Child>>,
  val updatedEntities: List<PeriodComparable<Parent, Child>>,
  val equalEntities: List<PeriodComparable<Parent, Child>>
)

fun LocalDate.isAfterOrEqual(startDate: LocalDate): Boolean {
  return this >= startDate
}

fun LocalDate.isBeforeOrEqual(endDate: LocalDate): Boolean {
  return this <= endDate
}

