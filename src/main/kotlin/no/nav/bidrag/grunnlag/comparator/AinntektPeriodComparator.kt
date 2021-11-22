package no.nav.bidrag.grunnlag.comparator

import no.nav.bidrag.grunnlag.dto.AinntektDto
import no.nav.bidrag.grunnlag.dto.AinntektspostDto

class AinntektPeriodComparator : AbstractPeriodComparator<AinntektDto, AinntektspostDto>() {
  override fun isEntitiesEqual(
    newEntity: PeriodComparable<AinntektDto, AinntektspostDto>,
    existingEntity: PeriodComparable<AinntektDto, AinntektspostDto>
  ): Boolean {
    val newAinntektsposter = sortAinntektsposter(newEntity.children)
    val existingAinntektsposter = sortAinntektsposter(existingEntity.children)
    if (newAinntektsposter.size != existingAinntektsposter.size) {
      return false
    }
    for (i in 0 until newAinntektsposter.size) {
      if (newAinntektsposter[i].inntektType != existingAinntektsposter[i].inntektType) return false
      if (newAinntektsposter[i].beskrivelse != existingAinntektsposter[i].beskrivelse) return false
      if (newAinntektsposter[i].belop != existingAinntektsposter[i].belop) return false
      if (newAinntektsposter[i].fordelType != existingAinntektsposter[i].fordelType) return false
      if (newAinntektsposter[i].opplysningspliktigId != existingAinntektsposter[i].opplysningspliktigId) return false
      if (newAinntektsposter[i].opptjeningsperiodeFra != existingAinntektsposter[i].opptjeningsperiodeFra) return false
      if (newAinntektsposter[i].opptjeningsperiodeTil != existingAinntektsposter[i].opptjeningsperiodeTil) return false
      if (newAinntektsposter[i].utbetalingsperiode != existingAinntektsposter[i].utbetalingsperiode) return false
      if (newAinntektsposter[i].virksomhetId != existingAinntektsposter[i].virksomhetId) return false
    }
    return true
  }

  private fun sortAinntektsposter(ainntektsposter: List<AinntektspostDto>): List<AinntektspostDto> {
    return ainntektsposter.sortedWith(compareBy({it.beskrivelse}, {it.inntektType}, {it.fordelType}))
  }
}