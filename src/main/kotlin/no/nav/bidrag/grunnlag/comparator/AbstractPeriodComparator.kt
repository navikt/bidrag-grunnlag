package no.nav.bidrag.grunnlag.comparator

import java.time.LocalDate

abstract class AbstractPeriodComparator<Parent : IPeriod, Child> {

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

    newEntities.forEach() { newEntity ->
      val existingEntityWithEqualPeriod = findCompareEntityWithEqualPeriod(newEntity, existingEntitiesWithinRequestedPeriod)
      if (existingEntityWithEqualPeriod != null) {
        if (isEntitiesEqual(newEntity, existingEntityWithEqualPeriod)) {
          equalEntities.add(existingEntityWithEqualPeriod)
        } else {
          expiredEntities.add(existingEntityWithEqualPeriod)
          updatedEntities.add(newEntity)
        }
      } else {
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

