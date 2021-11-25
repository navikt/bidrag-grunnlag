package no.nav.bidrag.grunnlag.comparator

import no.nav.bidrag.grunnlag.service.ProcessedComparatorResult
import no.nav.bidrag.grunnlag.service.UpdatedEntity
import no.nav.bidrag.grunnlag.util.toJsonString
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

abstract class AbstractPeriodComparator<T : PeriodComparable<*, *>> {

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

    val existingEntitiesWithinRequestedPeriod = filterEntitiesByPeriod(existingEntities, newEntities, requestedPeriod, expiredEntities)
    LOGGER.debug("${existingEntitiesWithinRequestedPeriod.size} eksisterende entiteter innenfor forespurt periode (${requestedPeriod.periodeFra} - ${requestedPeriod.periodeTil}).")
    LOGGER.debug("${expiredEntities.size} utløpte entiteter før sammenligning.")

    newEntities.forEach() { newEntity ->
      val existingEntityWithEqualPeriod = findEntityWithEqualPeriod(newEntity, existingEntitiesWithinRequestedPeriod)
      if (existingEntityWithEqualPeriod != null) {
        if (isEntitiesEqual(newEntity, existingEntityWithEqualPeriod)) {
          equalEntities.add(existingEntityWithEqualPeriod)
        } else {
          LOGGER.debug(
            "Ny og eksisterende entitet er ulike. Ny: ${
              toJsonString(newEntity)
            }, Eksisterende: ${toJsonString(existingEntityWithEqualPeriod)}."
          )
          expiredEntities.add(existingEntityWithEqualPeriod)
          updatedEntities.add(newEntity)
        }
      } else {
        LOGGER.debug("Kunne ikke finne eksisterede entitet for perioden (${newEntity.periodEntity.periodeFra} - ${newEntity.periodEntity.periodeTil}).")
        updatedEntities.add(newEntity)
      }
    }
    return ComparatorResult(expiredEntities, updatedEntities, equalEntities)
  }

  fun <Entity, ChildEntity, Dto : IComparable<Entity>, ChildDto : IComparableChild<ChildEntity, Entity>, Comparable : PeriodComparable<Dto, ChildDto>> processComparatorResult(
    comparatorResult: ComparatorResult<Comparable>
  ): ProcessedComparatorResult<Entity, ChildEntity> {
    val now = LocalDateTime.now()
    val expiredOrUnchanged = mutableListOf<Entity>()
    val updated = mutableListOf<UpdatedEntity<Entity, ChildEntity>>()

    LOGGER.debug("Setter ${comparatorResult.expiredEntities.size} entiteter til utløpt.")
    comparatorResult.expiredEntities.forEach() { expiredEntity ->
      expiredOrUnchanged.add(expiredEntity.periodEntity.expire(now))
    }
    LOGGER.debug("Oppdaterer ${comparatorResult.equalEntities.size} uendrede eksisterende entiteter med nytt hentet tidspunkt.")
    comparatorResult.equalEntities.forEach() { equalEntity ->
      expiredOrUnchanged.add(equalEntity.periodEntity.update(now))
    }
    LOGGER.debug("Oppretter ${comparatorResult.updatedEntities.size} nye entiteter.")
    comparatorResult.updatedEntities.forEach() { updatedEntity ->
      val entity = updatedEntity.periodEntity.create()
      val children = mutableListOf<ChildEntity>()
      if (updatedEntity.children != null) {
        LOGGER.debug("Oppretter ${updatedEntity.children.size} nye barne-entiteter.")
      }
      updatedEntity.children?.forEach() { skattegrunnlagspostDto ->
        children.add(skattegrunnlagspostDto.create(entity))
      }
      updated.add(UpdatedEntity(entity, children))
    }
    return ProcessedComparatorResult(expiredOrUnchanged, updated)
  }

  fun compareFields(newEntity: Any?, existingEntity: Any?, fieldName: String): Map<String, String> {
    val differences = mutableMapOf<String, String>()
    val entitiesNotEqual = when (newEntity) {
      is BigDecimal -> newEntity.compareTo(existingEntity as BigDecimal) != 0
      else -> newEntity != existingEntity
    }
    if (entitiesNotEqual) {
      differences[fieldName] = "$newEntity != $existingEntity"
    }
    return differences
  }

  private fun findEntityWithEqualPeriod(
    periodEntity: T,
    periodEntities: List<T>
  ): T? {
    return periodEntities.find { t ->
      t.periodEntity.periodeFra.isEqual(periodEntity.periodEntity.periodeFra) && t.periodEntity.periodeTil.isEqual(
        periodEntity.periodEntity.periodeTil
      )
    }
  }

  private fun filterEntitiesByPeriod(
    existingEntities: List<T>,
    newEntities: List<T>,
    requestedPeriod: IPeriod,
    expiredEntities: MutableList<T>
  ): List<T> {
    val filteredEntities = mutableListOf<T>()
    existingEntities.forEach() { existingEntity ->
      if (isInsidePeriod(requestedPeriod, existingEntity.periodEntity) && periodEntityStillExists(existingEntity, newEntities)) {
        filteredEntities.add(existingEntity)
      } else {
        expiredEntities.add(existingEntity)
      }
    }
    return filteredEntities
  }

  private fun periodEntityStillExists(existingEntity: T, newEntities: List<T>): Boolean {
    return findEntityWithEqualPeriod(existingEntity, newEntities) != null
  }

  abstract fun isEntitiesEqual(newEntity: T, existingEntity: T): Boolean
}

interface IPeriod {
  val periodeFra: LocalDate
  val periodeTil: LocalDate
}

interface IComparableChild<T, Parent> {
  fun create(parent: Parent): T
}

interface IComparable<T> : IPeriod {
  fun expire(brukTil: LocalDateTime): T
  fun update(hentetTidspunkt: LocalDateTime): T
  fun create(): T
}

class Period(override val periodeFra: LocalDate, override val periodeTil: LocalDate) : IPeriod

open class PeriodComparable<PeriodEntity: IComparable<*>, Child: IComparableChild<*,*>>(val periodEntity: PeriodEntity, val children: List<Child>?) {}

class ComparatorResult<T: PeriodComparable<*, *>>(
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

