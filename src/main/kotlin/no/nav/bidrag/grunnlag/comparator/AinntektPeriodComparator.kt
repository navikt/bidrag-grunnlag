package no.nav.bidrag.grunnlag.comparator

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.grunnlag.dto.AinntektDto
import no.nav.bidrag.grunnlag.dto.AinntektspostDto

class AinntektPeriodComparator : AbstractPeriodComparator<AinntektDto, PeriodComparableWithChildren<AinntektDto, AinntektspostDto>>() {
  override fun isEntitiesEqual(
    newEntity: PeriodComparableWithChildren<AinntektDto, AinntektspostDto>,
    existingEntity: PeriodComparableWithChildren<AinntektDto, AinntektspostDto>
  ): Boolean {
    val newAinntektsposter = sortAinntektsposter(newEntity.children)
    val existingAinntektsposter = sortAinntektsposter(existingEntity.children)
    if (newAinntektsposter.size != existingAinntektsposter.size) {
      return false
    }
    val differentFields = mutableMapOf<String, String>()
    for (i in newAinntektsposter.indices) {
      if (newAinntektsposter[i].inntektType != existingAinntektsposter[i].inntektType) {
        differentFields["inntektType"] = "${newAinntektsposter[i].inntektType} != ${existingAinntektsposter[i].inntektType}"
      }
      if (newAinntektsposter[i].beskrivelse != existingAinntektsposter[i].beskrivelse) {
        differentFields["beskrivelse"] = "${newAinntektsposter[i].beskrivelse} != ${existingAinntektsposter[i].beskrivelse}"
      }
      if (newAinntektsposter[i].belop.compareTo(existingAinntektsposter[i].belop) != 0) {
        differentFields["belop"] = "${newAinntektsposter[i].belop} != ${existingAinntektsposter[i].belop}"
      }
      if (newAinntektsposter[i].fordelType != existingAinntektsposter[i].fordelType) {
        differentFields["fordelType"] = "${newAinntektsposter[i].fordelType} != ${existingAinntektsposter[i].fordelType}"
      }
      if (newAinntektsposter[i].opplysningspliktigId != existingAinntektsposter[i].opplysningspliktigId) {
        differentFields["opplysningspliktigId"] = "${newAinntektsposter[i].opplysningspliktigId} != ${existingAinntektsposter[i].opplysningspliktigId}"
      }
      if (newAinntektsposter[i].opptjeningsperiodeFra != existingAinntektsposter[i].opptjeningsperiodeFra) {
        differentFields["opptjeningsperiodeFra"] = "${newAinntektsposter[i].opptjeningsperiodeFra} != ${existingAinntektsposter[i].opptjeningsperiodeFra}"
      }
      if (newAinntektsposter[i].opptjeningsperiodeTil != existingAinntektsposter[i].opptjeningsperiodeTil) {
        differentFields["opptjeningsperiodeTil"] = "${newAinntektsposter[i].opptjeningsperiodeTil} != ${existingAinntektsposter[i].opptjeningsperiodeTil}"
      }
      if (newAinntektsposter[i].utbetalingsperiode != existingAinntektsposter[i].utbetalingsperiode) {
        differentFields["utbetalingsperiode"] = "${newAinntektsposter[i].utbetalingsperiode} != ${existingAinntektsposter[i].utbetalingsperiode}"
      }
      if (newAinntektsposter[i].virksomhetId != existingAinntektsposter[i].virksomhetId) {
        differentFields["virksomhetId"] = "${newAinntektsposter[i].virksomhetId} != ${existingAinntektsposter[i].virksomhetId}"
      }
    }
    if (differentFields.isNotEmpty()) {
      LOGGER.info(ObjectMapper().findAndRegisterModules().writeValueAsString(differentFields))
    }
    return differentFields.isEmpty()
  }

  private fun sortAinntektsposter(ainntektsposter: List<AinntektspostDto>): List<AinntektspostDto> {
    return ainntektsposter.sortedWith(compareBy({ it.beskrivelse }, { it.inntektType }, { it.fordelType }))
  }
}