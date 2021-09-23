package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.grunnlag.dto.GrunnlagspakkeDto
import no.nav.bidrag.grunnlag.dto.toGrunnlagspakkeEntity
import no.nav.bidrag.grunnlag.persistence.entity.toGrunnlagspakkeDto
import no.nav.bidrag.grunnlag.persistence.repository.GrunnlagspakkeRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PersistenceService(
  val grunnlagspakkeRepository: GrunnlagspakkeRepository
) {

  private val LOGGER = LoggerFactory.getLogger(PersistenceService::class.java)

  fun opprettNyGrunnlagspakke (grunnlagspakkeDto: GrunnlagspakkeDto): GrunnlagspakkeDto {
    val nyGrunnlagspakke = grunnlagspakkeDto.toGrunnlagspakkeEntity()
    val grunnlagspakke = grunnlagspakkeRepository.save(nyGrunnlagspakke)
    return grunnlagspakke.toGrunnlagspakkeDto()

  }

  fun finnGrunnlagspakke(grunnlagspakkeId: Int): GrunnlagspakkeDto {
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