package no.nav.bidrag.grunnlag.comparator

import no.nav.bidrag.grunnlag.bo.AinntektBo
import no.nav.bidrag.grunnlag.bo.AinntektspostBo
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class PeriodComparatorTest {

  @Test
  fun `skal filtrere bort ainntekter som faller utenfor oppgitt periode`() {
    var newEntities = createPeriodEntities(Period(LocalDate.of(2021, 8, 1), LocalDate.of(2021, 11, 1)))
    var existingEntities = createPeriodEntities(Period(LocalDate.of(2021, 7, 1), LocalDate.of(2021, 12, 1)))

    val ainntektPeriodComparator = AinntektPeriodComparator()
    var ainntektPeriod = Period(LocalDate.of(2021, 8, 1), LocalDate.of(2021, 11, 1))
    var comparatorResult = ainntektPeriodComparator.comparePeriodEntities(ainntektPeriod, newEntities, existingEntities)

    assertTrue(comparatorResult.expiredEntities.size == 2)
    assertTrue(comparatorResult.equalEntities.size == 3)
    assertTrue(comparatorResult.updatedEntities.isEmpty())

    newEntities = createPeriodEntities(Period(LocalDate.of(2021, 9, 1), LocalDate.of(2021, 11, 1)))

    ainntektPeriod = Period(LocalDate.of(2021, 9, 1), LocalDate.of(2021, 11, 1))
    comparatorResult = ainntektPeriodComparator.comparePeriodEntities(ainntektPeriod, newEntities, existingEntities)

    assertTrue(comparatorResult.expiredEntities.size == 3)
    assertTrue(comparatorResult.equalEntities.size == 2)
    assertTrue(comparatorResult.updatedEntities.isEmpty())

    // Et helt år med nye inntekter. Den nye perioden starter tidligere og slutter senere enn ekisiterede perioder.
    newEntities = createPeriodEntities(Period(LocalDate.of(2020, 1, 1), LocalDate.of(2021, 1, 1)))
    existingEntities = createPeriodEntities(Period(LocalDate.of(2020, 2, 1), LocalDate.of(2020, 12, 1)))

    ainntektPeriod = Period(LocalDate.of(2020, 1, 1), LocalDate.of(2021, 1, 1))
    comparatorResult = ainntektPeriodComparator.comparePeriodEntities(ainntektPeriod, newEntities, existingEntities)

    assertTrue(comparatorResult.expiredEntities.isEmpty())
    assertTrue(comparatorResult.equalEntities.size == 10)
    assertTrue(comparatorResult.updatedEntities.size == 2)

    // Nye inntekter krysser fra ett år til et annet. Eksisterende inntekter finnes for en lengre periode i begge ender.
    newEntities = createPeriodEntities(Period(LocalDate.of(2020, 5, 1), LocalDate.of(2021, 7, 1)))
    existingEntities = createPeriodEntities(Period(LocalDate.of(2020, 3, 1), LocalDate.of(2021, 10, 1)))

    ainntektPeriod = Period(LocalDate.of(2020, 5, 1), LocalDate.of(2021, 7, 1))
    comparatorResult = ainntektPeriodComparator.comparePeriodEntities(ainntektPeriod, newEntities, existingEntities)

    assertTrue(comparatorResult.expiredEntities.size == 5)
    assertTrue(comparatorResult.equalEntities.size == 14)
    assertTrue(comparatorResult.updatedEntities.isEmpty())
  }

  @Test
  fun `skal sjekke om alle inntektsposter tilknyttet to inntekter er like`() {
    val ainntektPeriodComparator = AinntektPeriodComparator()
    var newEntities = createPeriodEntities(
      Period(LocalDate.of(2021, 8, 1), LocalDate.of(2021, 9, 1)), listOf(
        createAinntektpost()
      )
    )
    var existingEntities = createPeriodEntities(
      Period(LocalDate.of(2021, 8, 1), LocalDate.of(2021, 9, 1)), listOf(
        createAinntektpost()
      )
    )

    assertTrue(ainntektPeriodComparator.isEntitiesEqual(newEntities[0], existingEntities[0]))

    newEntities =
      createPeriodEntities(Period(LocalDate.of(2021, 8, 1), LocalDate.of(2021, 9, 1)), listOf(createAinntektpost(belop = BigDecimal(500))))
    existingEntities = createPeriodEntities(
      Period(LocalDate.of(2021, 8, 1), LocalDate.of(2021, 9, 1)), listOf(
        createAinntektpost(belop = BigDecimal(500.0))
      )
    )

    assertTrue(ainntektPeriodComparator.isEntitiesEqual(newEntities[0], existingEntities[0]))

    newEntities =
      createPeriodEntities(Period(LocalDate.of(2021, 8, 1), LocalDate.of(2021, 9, 1)), listOf(createAinntektpost(belop = BigDecimal(450))))

    assertFalse(ainntektPeriodComparator.isEntitiesEqual(newEntities[0], existingEntities[0]))

    newEntities =
      createPeriodEntities(Period(LocalDate.of(2021, 8, 1), LocalDate.of(2021, 9, 1)), listOf(createAinntektpost(beskrivelse = "Beskrivelse2")))

    assertFalse(ainntektPeriodComparator.isEntitiesEqual(newEntities[0], existingEntities[0]))

    newEntities =
      createPeriodEntities(Period(LocalDate.of(2021, 8, 1), LocalDate.of(2021, 9, 1)), listOf(createAinntektpost(fordelType = "Fordeltype2")))

    assertFalse(ainntektPeriodComparator.isEntitiesEqual(newEntities[0], existingEntities[0]))

    newEntities =
      createPeriodEntities(Period(LocalDate.of(2021, 8, 1), LocalDate.of(2021, 9, 1)), listOf(createAinntektpost(inntektType = "Inntekttype2")))

    assertFalse(ainntektPeriodComparator.isEntitiesEqual(newEntities[0], existingEntities[0]))

    newEntities =
      createPeriodEntities(Period(LocalDate.of(2021, 8, 1), LocalDate.of(2021, 9, 1)), listOf(createAinntektpost(opplysningspliktigId = "Opp2")))

    assertFalse(ainntektPeriodComparator.isEntitiesEqual(newEntities[0], existingEntities[0]))

    newEntities = createPeriodEntities(
      Period(LocalDate.of(2021, 8, 1), LocalDate.of(2021, 9, 1)),
      listOf(createAinntektpost(opptjeningsperiodeFra = LocalDate.of(2021, 8, 2)))
    )

    assertFalse(ainntektPeriodComparator.isEntitiesEqual(newEntities[0], existingEntities[0]))

    newEntities = createPeriodEntities(
      Period(LocalDate.of(2021, 8, 1), LocalDate.of(2021, 9, 1)),
      listOf(createAinntektpost(opptjeningsperiodeTil = LocalDate.of(2021, 9, 2)))
    )

    assertFalse(ainntektPeriodComparator.isEntitiesEqual(newEntities[0], existingEntities[0]))

    newEntities = createPeriodEntities(Period(LocalDate.of(2021, 8, 1), LocalDate.of(2021, 9, 1)), listOf(createAinntektpost(virksomhetId = "Virk2")))

    assertFalse(ainntektPeriodComparator.isEntitiesEqual(newEntities[0], existingEntities[0]))

    newEntities =
      createPeriodEntities(Period(LocalDate.of(2021, 8, 1), LocalDate.of(2021, 9, 1)), listOf(createAinntektpost(utbetalingsperiode = "Utb2")))

    assertFalse(ainntektPeriodComparator.isEntitiesEqual(newEntities[0], existingEntities[0]))

    newEntities =
      createPeriodEntities(Period(LocalDate.of(2021, 8, 1), LocalDate.of(2021, 9, 1)), listOf(createAinntektpost(etterbetalingsperiodeFom = LocalDate.of(2021, 10, 1))))

    assertFalse(ainntektPeriodComparator.isEntitiesEqual(newEntities[0], existingEntities[0]))

    newEntities =
      createPeriodEntities(Period(LocalDate.of(2021, 8, 1), LocalDate.of(2021, 9, 1)), listOf(createAinntektpost(etterbetalingsperiodeTom = LocalDate.of(2021, 10, 1))))

    assertFalse(ainntektPeriodComparator.isEntitiesEqual(newEntities[0], existingEntities[0]))
  }

  @Test
  fun `skal både filtrere bort ainntekter som faller utenfor ny periode og sjekke om nye inntekter er oppdatert eller ikke`() {
    val ainntektPeriodComparator = AinntektPeriodComparator()

    // Ingen inntekter er like og det finnes en eksisterende inntekt som ligger utenfor ny periode
    var newEntities = createPeriodEntities(
      Period(LocalDate.of(2021, 8, 1), LocalDate.of(2021, 11, 1)), listOf(
        createAinntektpost(), createAinntektpost(beskrivelse = "Beskrivelse2", fordelType = "Fordeltype2")
      )
    )
    val existingEntities = createPeriodEntities(
      Period(LocalDate.of(2021, 8, 1), LocalDate.of(2021, 12, 1)), listOf(
        createAinntektpost(), createAinntektpost()
      )
    )
    var ainntektPeriod = Period(LocalDate.of(2021, 8, 1), LocalDate.of(2021, 11, 1))
    var comparatorResult = ainntektPeriodComparator.comparePeriodEntities(ainntektPeriod, newEntities, existingEntities)

    assertTrue(comparatorResult.expiredEntities.size == 4)
    assertTrue(comparatorResult.equalEntities.isEmpty())
    assertTrue(comparatorResult.updatedEntities.size == 3)
  }

  @Test
  fun `skal filtrere bort inntekter som ligger innenfor rett periode men som ikke lenger eksisterer i nye inntekter for perioden`() {
    val ainntektPeriodComparator = AinntektPeriodComparator()

    // Nye inntekter mangler inntekt for September, men den ligger i eksisterende inntekter.
    var newEntities = createPeriodEntities(
      Period(LocalDate.of(2021, 8, 1), LocalDate.of(2021, 9, 1)), listOf(
        createAinntektpost()
      )
    )

    newEntities.addAll(
      createPeriodEntities(
        Period(LocalDate.of(2021, 10, 1), LocalDate.of(2021, 11, 1)), listOf(
          createAinntektpost()
        )
      )
    )
    val existingEntities = createPeriodEntities(
      Period(LocalDate.of(2021, 8, 1), LocalDate.of(2021, 11, 1)), listOf(
        createAinntektpost()
      )
    )
    var ainntektPeriod = Period(LocalDate.of(2021, 8, 1), LocalDate.of(2021, 11, 1))
    val comparatorResult = ainntektPeriodComparator.comparePeriodEntities(ainntektPeriod, newEntities, existingEntities)

    assertTrue(comparatorResult.expiredEntities.size == 1)
    assertTrue(comparatorResult.equalEntities.size == 2)
    assertTrue(comparatorResult.updatedEntities.size == 0)
  }

  private fun createPeriodEntities(
    period: IPeriod,
    inntektsposter: List<AinntektspostBo> = emptyList()
  ): MutableList<PeriodComparable<AinntektBo, AinntektspostBo>> {
    val existingEntities = mutableListOf<PeriodComparable<AinntektBo, AinntektspostBo>>()
    var currentStartDate = period.periodeFra
    while (currentStartDate.isBefore(period.periodeTil)) {
      existingEntities.add(
        PeriodComparable(
          AinntektBo(periodeFra = currentStartDate, periodeTil = currentStartDate.plusMonths(1)),
          inntektsposter
        )
      )
      currentStartDate = currentStartDate.plusMonths(1)
    }

    return existingEntities
  }

  private fun createAinntektpost(
    belop: BigDecimal = BigDecimal(1000), beskrivelse: String = "Beskrivelse1", inntektType: String = "Inntekttype1",
    fordelType: String = "Fordeltype1",
    opplysningspliktigId: String = "Opp1",
    opptjeningsperiodeFra: LocalDate = LocalDate.of(2021, 8, 1),
    opptjeningsperiodeTil: LocalDate = LocalDate.of(2021, 9, 1),
    virksomhetId: String = "Virk1",
    utbetalingsperiode: String = "Utb1",
    etterbetalingsperiodeFom: LocalDate? = null,
    etterbetalingsperiodeTom: LocalDate? = null
  ): AinntektspostBo {
    return AinntektspostBo(
      belop = belop,
      beskrivelse = beskrivelse,
      inntektType = inntektType,
      fordelType = fordelType,
      opplysningspliktigId = opplysningspliktigId,
      opptjeningsperiodeFra = opptjeningsperiodeFra,
      opptjeningsperiodeTil = opptjeningsperiodeTil,
      virksomhetId = virksomhetId,
      utbetalingsperiode = utbetalingsperiode,
      etterbetalingsperiodeFra = etterbetalingsperiodeFom,
      etterbetalingsperiodeTil = etterbetalingsperiodeTom
    )
  }
}