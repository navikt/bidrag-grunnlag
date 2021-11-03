package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.grunnlag.api.grunnlagspakke.HentGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OppdaterGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OppdaterGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OpprettGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OpprettGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.BidragGcpProxyConsumer
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.HentInntektRequest
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.skatt.HentSkattegrunnlagRequest
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.skatt.Skattegrunnlag
import no.nav.bidrag.grunnlag.consumer.familiebasak.FamilieBaSakConsumer
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.FamilieBaSakRequest
import no.nav.bidrag.grunnlag.dto.GrunnlagspakkeDto
import no.nav.bidrag.grunnlag.dto.InntektAinntektDto
import no.nav.bidrag.grunnlag.dto.InntektspostAinntektDto
import no.nav.bidrag.grunnlag.dto.SkattegrunnlagDto
import no.nav.bidrag.grunnlag.dto.SkattegrunnlagspostDto
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
  private val familieBaSakConsumer: FamilieBaSakConsumer,
  private val bidragGcpProxyConsumer: BidragGcpProxyConsumer
) {

  companion object {

    @JvmStatic
    private val LOGGER = LoggerFactory.getLogger(GrunnlagspakkeService::class.java)

    const val BIDRAG_FILTER = "BidragA-Inntekt"
    const val FORSKUDD_FILTER = "BidragsforskuddA-Inntekt"
    const val BIDRAG_FORMAAL = "Bidrag"
    const val FORSKUDD_FORMAAL = "Bidragsforskudd"
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
      // Innhenter alle grunnlag som er aktuelle for forskudd

      // Henter a-inntekt
      val antallFunnetAinntekt = oppdaterInntektAinntekt(
        oppdaterGrunnlagspakkeRequest.grunnlagspakkeId,
        personId,
        oppdaterGrunnlagspakkeRequest.periodeFom,
        oppdaterGrunnlagspakkeRequest.periodeTom,
        oppdaterGrunnlagspakkeRequest.behandlingType
      )
      status += "Antall elementer funnet: $antallFunnetAinntekt."

      // Henter utvidet barnetrygd og småbarnstillegg
      val antallFunnetUbst = oppdaterUtvidetBarnetrygdOgSmaabarnstillegg(
        oppdaterGrunnlagspakkeRequest.grunnlagspakkeId,
        personId,
        oppdaterGrunnlagspakkeRequest.periodeFom
      )
      status = "Antall elementer funnet utvidet barnetrygd og småbarnstillegg: $antallFunnetUbst"

      // Henter inntekter fra Skatt
      val antallGrunnlag = oppdaterSkattegrunnlag(oppdaterGrunnlagspakkeRequest.grunnlagspakkeId, personId, oppdaterGrunnlagspakkeRequest.periodeTom);
      status += " Antall skattegrunnlag funnet: ${antallGrunnlag}";

    }

    return OppdaterGrunnlagspakkeResponse(status)
  }

  private fun oppdaterInntektAinntekt(grunnlagspakkeId: Int, personId: String, maanedFom: String, maanedTom: String, behandlingType: String): Int {

    var antallPerioderFunnet: Int = 0

    val hentInntektRequest = HentInntektRequest(
      ident = personId,
      maanedFom = maanedFom,
      maanedTom = maanedTom,
      ainntektsfilter = if (behandlingType == BehandlingType.FORSKUDD.toString()) FORSKUDD_FILTER else BIDRAG_FILTER,
      formaal = if (behandlingType == BehandlingType.FORSKUDD.toString()) FORSKUDD_FORMAAL else BIDRAG_FORMAAL
    )
    LOGGER.info(
      "Kaller bidrag-gcp-proxy (Inntektskomponenten) med ident = ********${
        hentInntektRequest.ident.substring(
          IntRange(8, 10)
        )
      }, " +
          "maanedFom = ${hentInntektRequest.maanedFom}, maanedTom = ${hentInntektRequest.maanedTom}, " +
          "ainntektsfilter = ${hentInntektRequest.ainntektsfilter}, formaal = ${hentInntektRequest.formaal}"
    )

    val hentInntektResponse = bidragGcpProxyConsumer.hentInntekt(hentInntektRequest)

    LOGGER.info("bidrag-gcp-proxy (Inntektskomponenten) ga følgende respons: $hentInntektResponse")

    if (hentInntektResponse.arbeidsInntektMaaned.isNullOrEmpty()) {
      return 0
    }

    hentInntektResponse.arbeidsInntektMaaned.forEach() { inntektPeriode ->
      antallPerioderFunnet++
      val opprettetInntektAinntekt = persistenceService.opprettInntektAinntekt(
        InntektAinntektDto(
        grunnlagspakkeId = grunnlagspakkeId,
        personId = personId,
        periodeFra = LocalDate.parse(inntektPeriode.aarMaaned + "-01"),
        periodeTil = LocalDate.parse(inntektPeriode.aarMaaned + "-01").plusMonths(1)
      ))
      if (!inntektPeriode.arbeidsInntektInformasjon.inntektListe.isNullOrEmpty())
        inntektPeriode.arbeidsInntektInformasjon.inntektListe!!.forEach(){ inntektspost ->
        persistenceService.opprettInntektspostAinntekt(
          InntektspostAinntektDto(
            inntektId = opprettetInntektAinntekt.inntektId,
            utbetalingsperiode = inntektspost.utbetaltIMaaned,
            opptjeningsperiodeFra = LocalDate.parse(inntektspost.opptjeningsperiodeFom + "-01"),
            opptjeningsperiodeTil = LocalDate.parse(inntektspost.opptjeningsperiodeTom + "-01").plusMonths(1),
            opplysningspliktigId = inntektspost.opplysningspliktig?.identifikator,
            type = inntektspost.inntektType,
            fordelType = inntektspost.fordel,
            beskrivelse = inntektspost.beskrivelse,
            belop = inntektspost.beloep.toBigDecimal()
          )
        )

      }



    }
    return antallPerioderFunnet
  }

  fun oppdaterUtvidetBarnetrygdOgSmaabarnstillegg(grunnlagspakkeId: Int, personId: String, periodeFom: String): Int {

    var antallFunnet: Int = 0
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
        antallFunnet++
        persistenceService.opprettUtvidetBarnetrygdOgSmaabarnstillegg(
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
    return antallFunnet
  }

  fun oppdaterSkattegrunnlag(grunnlagspakkeId: Int, personId: String, periodeTom: String): Int {
    val inntektAar = LocalDate.parse(periodeTom + "-01").year.toString();
    val skattegrunnlagRequest = HentSkattegrunnlagRequest(inntektAar, "SummertSkattegrunnlagBidrag", personId);


    LOGGER.info(
        "Kaller bidrag-gcp-proxy (Sigrun) med ident = ********${
          skattegrunnlagRequest.personId.substring(
              IntRange(8, 10)
          )
        }, " +
            "inntektsAar = ${skattegrunnlagRequest.inntektsAar} inntektsFilter = ${skattegrunnlagRequest.inntektsFilter}"
    )
    val skattegrunnlagResponse = bidragGcpProxyConsumer.hentSkattegrunnlag(skattegrunnlagRequest);

    val skattegrunnlagsPoster = mutableListOf<Skattegrunnlag>()
    skattegrunnlagsPoster.addAll(skattegrunnlagResponse.grunnlag!!.toMutableList())
    skattegrunnlagsPoster.addAll(skattegrunnlagResponse.svalbardGrunnlag!!.toMutableList())
    if (skattegrunnlagsPoster.size > 0) {
      val opprettetSkattegrunnlag = persistenceService.opprettSkattegrunnlag(SkattegrunnlagDto(
          grunnlagspakkeId = grunnlagspakkeId,
          personId = personId,
          periodeFra = LocalDate.parse("$inntektAar-01-01"),
          periodeTil = LocalDate.parse("$inntektAar-12-31"),
      ))
      skattegrunnlagsPoster.forEach { skattegrunnlagsPost ->
        persistenceService.opprettSkattegrunnlagspost(SkattegrunnlagspostDto(
            skattegrunnlagId = opprettetSkattegrunnlag.skattegrunnlagId,
            type = skattegrunnlagsPost.tekniskNavn,
            belop = BigDecimal(skattegrunnlagsPost.beloep),
        ))
      }
    }

    LOGGER.info("bidrag-gcp-proxy (Sigrun) ga følgende respons: $skattegrunnlagResponse")


    return skattegrunnlagsPoster.size;
  }

  fun hentGrunnlagspakke(grunnlagspakkeId: Int): HentGrunnlagspakkeResponse {
    return persistenceService.hentGrunnlagspakke(grunnlagspakkeId)
  }
}

enum class BehandlingType {
  FORSKUDD,
  BIDRAG,
  SAERTILSKUDD
}
