package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.grunnlag.api.grunnlagspakke.HentGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OppdaterGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OppdaterGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OpprettGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OpprettGrunnlagspakkeResponse
/*import no.nav.bidrag.grunnlag.api.ainntekt.OpprettInntektAinntektRequest
import no.nav.bidrag.grunnlag.api.ainntekt.OpprettInntektspostAinntektRequest
import no.nav.bidrag.grunnlag.api.ainntekt.toInntektAinntektDto
import no.nav.bidrag.grunnlag.api.ainntekt.toInntektspostAinntektDto
import no.nav.bidrag.grunnlag.api.skatt.OpprettInntektSkattRequest
import no.nav.bidrag.grunnlag.api.skatt.OpprettInntektspostSkattRequest
import no.nav.bidrag.grunnlag.api.skatt.toInntektSkattDto
import no.nav.bidrag.grunnlag.api.skatt.toInntektspostSkattDto*/
/*import no.nav.bidrag.grunnlag.api.ubst.OpprettUtvidetBarnetrygdOgSmaabarnstilleggRequest
import no.nav.bidrag.grunnlag.api.ubst.toUtvidetBarnetrygdOgSmaabarnstilleggDto*/
import no.nav.bidrag.grunnlag.consumer.familiebasak.FamilieBaSakConsumer
import no.nav.bidrag.grunnlag.dto.GrunnlagspakkeDto

import no.nav.bidrag.grunnlag.consumer.familiebasak.api.FamilieBaSakRequest
import no.nav.bidrag.grunnlag.dto.UtvidetBarnetrygdOgSmaabarnstilleggDto

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate

@Service
@Transactional
class GrunnlagspakkeService(
  private val persistenceService: PersistenceService,
  private val familieBaSakConsumer: FamilieBaSakConsumer
) {

  companion object {

    @JvmStatic
    private val LOGGER = LoggerFactory.getLogger(GrunnlagspakkeService::class.java)
  }

  fun opprettGrunnlagspakke(opprettGrunnlagspakkeRequest: OpprettGrunnlagspakkeRequest): OpprettGrunnlagspakkeResponse {
    val grunnlagspakkeDto = GrunnlagspakkeDto(
      opprettetAv = opprettGrunnlagspakkeRequest.opprettetAv
    )
    val opprettetGrunnlagspakke = persistenceService.opprettNyGrunnlagspakke(grunnlagspakkeDto)
    return OpprettGrunnlagspakkeResponse(opprettetGrunnlagspakke.grunnlagspakkeId)

  }

  fun oppdaterGrunnlagspakke(oppdaterGrunnlagspakkeRequest: OppdaterGrunnlagspakkeRequest): OppdaterGrunnlagspakkeResponse {

    return when (oppdaterGrunnlagspakkeRequest.behandlingType) {
      BehandlingType.FORSKUDD.toString() -> innhentGrunnlagForskudd(oppdaterGrunnlagspakkeRequest)
//      BehandlingType.BIDRAG.toString() -> innhentGrunnlagBidrag(oppdaterGrunnlagspakkeRequest)
//      BehandlingType.SAERTILSKUDD.toString() -> innhentGrunnlagSaertilskudd(oppdaterGrunnlagspakkeRequest)

      else -> OppdaterGrunnlagspakkeResponse("Ukjent behandlingType angitt")
    }
  }

  fun innhentGrunnlagForskudd(oppdaterGrunnlagspakkeRequest: OppdaterGrunnlagspakkeRequest): OppdaterGrunnlagspakkeResponse {
    var status = ""
    oppdaterGrunnlagspakkeRequest.identListe.forEach() { personId ->
//      hentInntektAinntekt(personId)
//      hentInntektSkatt(personId)
      val utvidetBarnetrygdOgSmaabarnstilleggListe = hentUtvidetBarnetrygdOgSmaabarnstillegg(
        oppdaterGrunnlagspakkeRequest.grunnlagspakkeId,
        personId,
        oppdaterGrunnlagspakkeRequest.periodeFom
      )

      utvidetBarnetrygdOgSmaabarnstilleggListe.forEach() { ubst ->
        persistenceService.opprettUtvidetBarnetrygdOgSmaabarnstillegg(ubst)
      }
      status = "Antall elementer funnet: ${utvidetBarnetrygdOgSmaabarnstilleggListe.size}"
    }

    return OppdaterGrunnlagspakkeResponse(status)

  }


  fun hentUtvidetBarnetrygdOgSmaabarnstillegg(grunnlagspakkeId: Int, personId: String, periodeFom: String)
      : List<UtvidetBarnetrygdOgSmaabarnstilleggDto> {

    val ubstDtoListe = mutableListOf<UtvidetBarnetrygdOgSmaabarnstilleggDto>()
    val familieBaSakRequest = FamilieBaSakRequest(
      personIdent = personId,
      fraDato = LocalDate.parse("$periodeFom-01")
    )

    LOGGER.info(
      "Kaller familie-ba-sak med personIdent ********${
        familieBaSakRequest.personIdent.substring(
          IntRange(8, 10)
        )
      } " +
          "og fraDato " + "${familieBaSakRequest.fraDato}"
    )

    val familieBaSakResponse = familieBaSakConsumer.hentFamilieBaSak(familieBaSakRequest)

    LOGGER.info("familie-ba-sak ga følgende respons: $familieBaSakResponse")

    if (familieBaSakResponse.perioder.isNotEmpty())
      familieBaSakResponse.perioder.forEach() { ubst ->
        ubstDtoListe.add(
          UtvidetBarnetrygdOgSmaabarnstilleggDto(
            grunnlagspakkeId = grunnlagspakkeId,
            personId = personId,
            type = ubst.stønadstype.toString(),
            periodeFra = LocalDate.parse(ubst.fomMåned.toString() + "-01"),
            // justerer frem tildato med én måned for å ha lik logikk som resten av appen. Tildato skal angis som til, men ikke inkludert, måned.
            periodeTil = if (ubst.tomMåned != null) LocalDate.parse(ubst.tomMåned.toString() + "-01").plusMonths(1) else null,
            belop = BigDecimal.valueOf(ubst.beløp),
            manueltBeregnet = ubst.manueltBeregnet
          )
        )
      }
    return ubstDtoListe
  }

/*
private fun opprettInntektAinntekt(
  opprettInntektAinntektRequest: OpprettInntektAinntektRequest,
  grunnlagspakkeId: Int
): InntektAinntektDto {
  return persistenceService.opprettInntektAinntekt(
    opprettInntektAinntektRequest.toInntektAinntektDto(
      grunnlagspakkeId
    )
  )
}

private fun opprettInntektspostAinntekt(
  opprettInntektspostAinntektRequest: OpprettInntektspostAinntektRequest,
  inntektId: Int
): InntektspostAinntektDto {
  return persistenceService.opprettInntektspostAinntekt(
    opprettInntektspostAinntektRequest.toInntektspostAinntektDto(
      inntektId
    )
  )
}


private fun opprettInntektSkatt(
  opprettInntektSkattRequest: OpprettInntektSkattRequest,
  grunnlagspakkeId: Int
): InntektSkattDto {
  return persistenceService.opprettInntektSkatt(
    opprettInntektSkattRequest.toInntektSkattDto(
      grunnlagspakkeId
    )
  )
}

private fun opprettInntektspostSkatt(
  opprettInntektspostSkattRequest: OpprettInntektspostSkattRequest,
  inntektId: Int
): InntektspostSkattDto {
  return persistenceService.opprettInntektspostSkatt(
    opprettInntektspostSkattRequest.toInntektspostSkattDto(
      inntektId
    )
  )
}

private fun opprettUtvidetBarnetrygdOgSmaabarnstillegg(
  opprettUtvidetBarnetrygdOgSmaabarnstilleggRequest:
  OpprettUtvidetBarnetrygdOgSmaabarnstilleggRequest, grunnlagspakkeId: Int
): UtvidetBarnetrygdOgSmaabarnstilleggDto {
  return persistenceService.opprettUtvidetBarnetrygdOgSmaabarnstillegg(
    opprettUtvidetBarnetrygdOgSmaabarnstilleggRequest.toUtvidetBarnetrygdOgSmaabarnstilleggDto(
      grunnlagspakkeId
    )
  )
}*/

  fun hentGrunnlagspakke(grunnlagspakkeId: Int): HentGrunnlagspakkeResponse {
    return persistenceService.hentGrunnlagspakke(grunnlagspakkeId)
  }
}

enum class BehandlingType {
  FORSKUDD,
  BIDRAG,
  SAERTILSKUDD
}
