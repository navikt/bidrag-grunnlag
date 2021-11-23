package no.nav.bidrag.grunnlag.comparator

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate

abstract class AbstractPeriodComparator<T : PeriodComparable<*>> {

  companion object {
    @JvmStatic
    val LOGGER: Logger = LoggerFactory.getLogger(AbstractPeriodComparator::class.java)
  }

  private fun isInsidePeriod(requestedPeriod: IPeriod, existingPeriod: IPeriod): Boolean {
    return existingPeriod.periodeFra.isAfterOrEqual(requestedPeriod.periodeFra) && existingPeriod.periodeTil.isBeforeOrEqual(requestedPeriod.periodeTil)
  }

  fun comparePeriodEntities(
    requestedPeriod: IPeriod,
    newEntities: List<T>,
    existingEntities: List<T>
  ): ComparatorResult<T> {
    val expiredEntities = mutableListOf<T>()
    val updatedEntities = mutableListOf<T>()
    val equalEntities = mutableListOf<T>()

    val existingEntitiesWithinRequestedPeriod = filterEntitiesByPeriod(existingEntities, requestedPeriod, expiredEntities)
    LOGGER.info("${existingEntitiesWithinRequestedPeriod.size} existing entities within requested period (${requestedPeriod.periodeFra} - ${requestedPeriod.periodeTil})")
    LOGGER.info("${expiredEntities.size} expired entities before equality check")

    newEntities.forEach() { newEntity ->
      val existingEntityWithEqualPeriod = findCompareEntityWithEqualPeriod(newEntity, existingEntitiesWithinRequestedPeriod)
      if (existingEntityWithEqualPeriod != null) {
        if (isEntitiesEqual(newEntity, existingEntityWithEqualPeriod)) {
          equalEntities.add(existingEntityWithEqualPeriod)
        } else {
          LOGGER.info(
            "Entities not equal. NewEntity: ${
              ObjectMapper().findAndRegisterModules().writeValueAsString(newEntity)
            }, ExistingEntity: ${ObjectMapper().findAndRegisterModules().writeValueAsString(existingEntityWithEqualPeriod)}"
          )
          expiredEntities.add(existingEntityWithEqualPeriod)
          updatedEntities.add(newEntity)
        }
      } else {
        LOGGER.info("Could not find existing entity within the period (${newEntity.periodEntity.periodeFra} - ${newEntity.periodEntity.periodeTil})")
        updatedEntities.add(newEntity)
      }
    }
    return ComparatorResult(expiredEntities, updatedEntities, equalEntities)
  }

  private fun findCompareEntityWithEqualPeriod(
    newEntity: T,
    existingEntities: List<T>
  ): T? {
    return existingEntities.find { t ->
      t.periodEntity.periodeFra.isEqual(newEntity.periodEntity.periodeFra) && t.periodEntity.periodeTil.isEqual(
        newEntity.periodEntity.periodeTil
      )
    }
  }

  private fun filterEntitiesByPeriod(existingEntities: List<T>, requestedPeriod: IPeriod, expiredEntities: MutableList<T>): List<T> {
    val filteredEntities = mutableListOf<T>()
    existingEntities.forEach() { existingEntity ->
      if (isInsidePeriod(requestedPeriod, existingEntity.periodEntity)) {
        filteredEntities.add(existingEntity)
      } else {
        expiredEntities.add(existingEntity)
      }
    }
    return filteredEntities
  }

  abstract fun isEntitiesEqual(newEntity: T, existingEntity: T): Boolean
}

interface IPeriod {
  val periodeFra: LocalDate
  val periodeTil: LocalDate
}

class Period(override val periodeFra: LocalDate, override val periodeTil: LocalDate) : IPeriod

open class PeriodComparable<PeriodEntity: IPeriod>(val periodEntity: PeriodEntity) {}

class PeriodComparableWithChildren<PeriodEntity: IPeriod, Child>(periodEntity: PeriodEntity, val children: List<Child>) :
  PeriodComparable<PeriodEntity>(periodEntity) {}


class ComparatorResult<T>(
  val expiredEntities: List<T>,
  val updatedEntities: List<T>,
  val equalEntities: List<T>
)

fun LocalDate.isAfterOrEqual(startDate: LocalDate): Boolean {
  return this >= startDate
}

fun LocalDate.isBeforeOrEqual(endDate: LocalDate): Boolean {
  return this <= endDate
}

