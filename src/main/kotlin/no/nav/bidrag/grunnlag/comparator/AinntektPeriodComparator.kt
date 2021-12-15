package no.nav.bidrag.grunnlag.comparator

import no.nav.bidrag.grunnlag.dto.AinntektDto
import no.nav.bidrag.grunnlag.dto.AinntektspostDto
import no.nav.bidrag.grunnlag.util.toJsonString

class AinntektPeriodComparator : AbstractPeriodComparator<PeriodComparable<AinntektDto, AinntektspostDto>>() {
  override fun isEntitiesEqual(
    newEntity: PeriodComparable<AinntektDto, AinntektspostDto>,
    existingEntity: PeriodComparable<AinntektDto, AinntektspostDto>
  ): Boolean {
    val newAinntektsposter = sortAinntektsposter(newEntity.children!!)
    val existingAinntektsposter = sortAinntektsposter(existingEntity.children!!)
    if (newAinntektsposter.size != existingAinntektsposter.size) {
      return false
    }
    val differences = mutableMapOf<String, String>()
    for (i in newAinntektsposter.indices) {
      differences.putAll(compareFields(newAinntektsposter[i].inntektType, existingAinntektsposter[i].inntektType,"inntektType"))
      differences.putAll(compareFields(newAinntektsposter[i].beskrivelse, existingAinntektsposter[i].beskrivelse,"beskrivelse"))
      differences.putAll(compareFields(newAinntektsposter[i].belop, existingAinntektsposter[i].belop,"belop"))
      differences.putAll(compareFields(newAinntektsposter[i].fordelType, existingAinntektsposter[i].fordelType,"fordelType"))
      differences.putAll(compareFields(newAinntektsposter[i].opplysningspliktigId, existingAinntektsposter[i].opplysningspliktigId,"opplysningspliktigId"))
      differences.putAll(compareFields(newAinntektsposter[i].opptjeningsperiodeFra, existingAinntektsposter[i].opptjeningsperiodeFra,"opptjeningsperiodeFra"))
      differences.putAll(compareFields(newAinntektsposter[i].opptjeningsperiodeTil, existingAinntektsposter[i].opptjeningsperiodeTil,"opptjeningsperiodeTil"))
      differences.putAll(compareFields(newAinntektsposter[i].utbetalingsperiode, existingAinntektsposter[i].utbetalingsperiode,"utbetalingsperiode"))
      differences.putAll(compareFields(newAinntektsposter[i].virksomhetId, existingAinntektsposter[i].virksomhetId,"virksomhetId"))
      differences.putAll(compareFields(newAinntektsposter[i].etterbetalingsperiodeFom, existingAinntektsposter[i].etterbetalingsperiodeFom, "etterbetalingsperiodeFom"))
      differences.putAll(compareFields(newAinntektsposter[i].etterbetalingsperiodeTom, existingAinntektsposter[i].etterbetalingsperiodeTom, "etterbetalingsperiodeTom"))
    }
    if (differences.isNotEmpty()) {
      LOGGER.debug(toJsonString(differences))
    }
    return differences.isEmpty()
  }

  private fun sortAinntektsposter(ainntektsposter: List<AinntektspostDto>): List<AinntektspostDto> {
    return ainntektsposter.sortedWith(compareBy({it.utbetalingsperiode}, {it.virksomhetId}, {it.opplysningspliktigId}, { it.inntektType }, { it.fordelType }, {it.beskrivelse}))
  }
}