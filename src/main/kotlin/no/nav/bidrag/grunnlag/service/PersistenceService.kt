package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.grunnlag.dto.GrunnlagspakkeDto
import no.nav.bidrag.grunnlag.dto.InntektDto
import no.nav.bidrag.grunnlag.dto.toGrunnlagspakkeEntity
import no.nav.bidrag.grunnlag.dto.toInntektEntity
import no.nav.bidrag.grunnlag.persistence.entity.toGrunnlagspakkeDto
import no.nav.bidrag.grunnlag.persistence.entity.toInntektDto
import no.nav.bidrag.grunnlag.persistence.repository.GrunnlagspakkeRepository
import no.nav.bidrag.grunnlag.persistence.repository.InntektRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PersistenceService(
  val grunnlagspakkeRepository: GrunnlagspakkeRepository,
  val inntektRepository: InntektRepository
) {

  private val LOGGER = LoggerFactory.getLogger(PersistenceService::class.java)

  fun opprettNyGrunnlagspakke (grunnlagspakkeDto: GrunnlagspakkeDto): GrunnlagspakkeDto {
    val nyGrunnlagspakke = grunnlagspakkeDto.toGrunnlagspakkeEntity()
    val grunnlagspakke = grunnlagspakkeRepository.save(nyGrunnlagspakke)
    return grunnlagspakke.toGrunnlagspakkeDto()
  }

  fun opprettNyInntekt (inntektDto: InntektDto): InntektDto {
    val nyInntekt = inntektDto.toInntektEntity()
    val inntekt = inntektRepository.save(nyInntekt)
    return inntekt.toInntektDto()
  }

  fun hentGrunnlagspakke(grunnlagspakkeId: Int): GrunnlagspakkeDto {
    val grunnlagspakke = grunnlagspakkeRepository.findById(grunnlagspakkeId)
      .orElseThrow {
        IllegalArgumentException(
          String.format(
            "Fant ikke grunnlagspakke med id %d i databasen",
            grunnlagspakkeId
          )
        )
      }
    return grunnlagspakke.toGrunnlagspakkeDto()
  }

}