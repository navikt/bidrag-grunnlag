package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.grunnlag.api.grunnlagspakke.GrunnlagstypeResponse
import no.nav.bidrag.grunnlag.api.grunnlagspakke.HentGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.api.grunnlagspakke.LukkGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OppdaterGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OppdaterGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OpprettGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OpprettGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.api.grunnlagspakke.PersonIdOgPeriodeRequest
import no.nav.bidrag.grunnlag.api.grunnlagspakke.RestkallResponse
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.BidragGcpProxyConsumer
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.HentInntektRequest
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.skatt.HentInntektSkattRequest
import no.nav.bidrag.grunnlag.consumer.familiebasak.FamilieBaSakConsumer
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.FamilieBaSakRequest
import no.nav.bidrag.grunnlag.dto.GrunnlagspakkeDto
import no.nav.bidrag.grunnlag.dto.InntektAinntektDto
import no.nav.bidrag.grunnlag.dto.InntektspostAinntektDto
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

  val oppdaterGrunnlagspakkeResponseListe = mutableListOf<OppdaterGrunnlagspakkeResponse>()

  fun opprettGrunnlagspakke(opprettGrunnlagspakkeRequest: OpprettGrunnlagspakkeRequest): OpprettGrunnlagspakkeResponse {
    val grunnlagspakkeDto = GrunnlagspakkeDto(
      opprettetAv = opprettGrunnlagspakkeRequest.opprettetAv
    )
    val opprettetGrunnlagspakke = persistenceService.opprettNyGrunnlagspakke(grunnlagspakkeDto)
    return OpprettGrunnlagspakkeResponse(opprettetGrunnlagspakke.grunnlagspakkeId)

  }

  fun oppdaterGrunnlagspakke(oppdaterGrunnlagspakkeRequest: OppdaterGrunnlagspakkeRequest): OppdaterGrunnlagspakkeResponse {

    val grunnlagstypeResponseListe = mutableListOf<GrunnlagstypeResponse>()

    oppdaterGrunnlagspakkeRequest.grunnlagtypeRequestListe.forEach() { grunnlagstypeRequest ->
      when (grunnlagstypeRequest.grunnlagstype) {
        Grunnlagstype.AINNTEKT.toString() ->
          grunnlagstypeResponseListe.add(
            oppdaterInntektAinntekt(
              oppdaterGrunnlagspakkeRequest.grunnlagspakkeId, oppdaterGrunnlagspakkeRequest.formaal,
              grunnlagstypeRequest.personIdOgPeriodeRequestListe
            )
          )


        //Grunnlagstype.SKATTEGRUNNLAG.toString() ->
        //TODO

        Grunnlagstype.UTVIDETBARNETRYGDOGSMAABARNSTILLEGG.toString() ->
          grunnlagstypeResponseListe.add(
            oppdaterUtvidetBarnetrygdOgSmaabarnstillegg(
              oppdaterGrunnlagspakkeRequest.grunnlagspakkeId,
              grunnlagstypeRequest.personIdOgPeriodeRequestListe
            )
          )
      }
    }

    return OppdaterGrunnlagspakkeResponse(
      oppdaterGrunnlagspakkeRequest.grunnlagspakkeId,
      grunnlagstypeResponseListe
    )

  }


  private fun oppdaterInntektAinntekt(
    grunnlagspakkeId: Int, formaal: String,
    personIdOgPeriodeListe: List<PersonIdOgPeriodeRequest>
  ): GrunnlagstypeResponse {

    val restkallResponseListe = mutableListOf<RestkallResponse>()

    personIdOgPeriodeListe.forEach() { personIdOgPeriode ->

      var antallPerioderFunnet = 0

      val hentInntektRequest = HentInntektRequest(
        ident = personIdOgPeriode.personId,
        maanedFom = personIdOgPeriode.periodeFra,
        maanedTom = personIdOgPeriode.periodeFra,
        ainntektsfilter = if (formaal == Formaal.FORSKUDD.toString()) FORSKUDD_FILTER else BIDRAG_FILTER,
        formaal = if (formaal == Formaal.FORSKUDD.toString()) FORSKUDD_FORMAAL else BIDRAG_FORMAAL
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

      oppdaterGrunnlagspakkeResponseListe.add(
        OppdaterGrunnlagspakkeResponse()
      )

      if (hentInntektResponse.arbeidsInntektMaaned.isNullOrEmpty())
        restkallResponseListe.add(
          RestkallResponse(
            personIdOgPeriode.personId,
            "Ingen inntekter funnet"
          )
        )
      else
        hentInntektResponse.arbeidsInntektMaaned.forEach() { inntektPeriode ->
          antallPerioderFunnet++
          val opprettetInntektAinntekt = persistenceService.opprettInntektAinntekt(
            InntektAinntektDto(
              grunnlagspakkeId = grunnlagspakkeId,
              personId = personIdOgPeriode.personId,
              periodeFra = LocalDate.parse(inntektPeriode.aarMaaned + "-01"),
              periodeTil = LocalDate.parse(inntektPeriode.aarMaaned + "-01").plusMonths(1)
            )
          )
          inntektPeriode.arbeidsInntektInformasjon.inntektListe?.forEach() { inntektspost ->
            persistenceService.opprettInntektspostAinntekt(
              InntektspostAinntektDto(
                inntektId = opprettetInntektAinntekt.inntektId,
                utbetalingsperiode = inntektspost.utbetaltIMaaned,
                opptjeningsperiodeFra = LocalDate.parse(inntektspost.opptjeningsperiodeFom + "-01"),
                opptjeningsperiodeTil = LocalDate.parse(inntektspost.opptjeningsperiodeTom + "-01")
                  .plusMonths(1),
                opplysningspliktigId = inntektspost.opplysningspliktig?.identifikator,
                type = inntektspost.inntektType,
                fordelType = inntektspost.fordel,
                beskrivelse = inntektspost.beskrivelse,
                belop = inntektspost.beloep.toBigDecimal()
              )
            )
          }
        }
      restkallResponseListe.add(
        RestkallResponse(
          personIdOgPeriode.personId,
          "Antall inntekter funnet $antallPerioderFunnet"
        )
      )
    }
    return GrunnlagstypeResponse(Grunnlagstype.AINNTEKT.toString(), restkallResponseListe)
  }

  fun oppdaterUtvidetBarnetrygdOgSmaabarnstillegg(
    grunnlagspakkeId: Int, personIdOgPeriodeListe: List<PersonIdOgPeriodeRequest>
  ): GrunnlagstypeResponse {

    val restkallResponseListe = mutableListOf<RestkallResponse>()

    personIdOgPeriodeListe.forEach() { personIdOgPeriode ->

      var antallPerioderFunnet = 0

      val familieBaSakRequest = FamilieBaSakRequest(
        personIdent = personIdOgPeriode.personId,
        fraDato = LocalDate.parse(personIdOgPeriode.periodeFra + "-01")
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

      familieBaSakResponse.perioder.forEach() { ubst ->
        antallPerioderFunnet++
        persistenceService.opprettUtvidetBarnetrygdOgSmaabarnstillegg(
          UtvidetBarnetrygdOgSmaabarnstilleggDto(
            grunnlagspakkeId = grunnlagspakkeId,
            personId = personIdOgPeriode.personId,
            type = ubst.stønadstype.toString(),
            periodeFra = LocalDate.parse(ubst.fomMåned.toString() + "-01"),
            // justerer frem tildato med én måned for å ha lik logikk som resten av appen. Tildato skal angis som til, men ikke inkludert, måned.
            periodeTil = if (ubst.tomMåned != null) LocalDate.parse(ubst.tomMåned.toString() + "-01")
              .plusMonths(1) else null,
            belop = BigDecimal.valueOf(ubst.beløp),
            manueltBeregnet = ubst.manueltBeregnet
          )
        )
      }
      restkallResponseListe.add(
        RestkallResponse(
          personIdOgPeriode.personId,
          "Antall inntekter funnet $antallPerioderFunnet"
        )
      )
    }
    return GrunnlagstypeResponse(
      Grunnlagstype.UTVIDETBARNETRYGDOGSMAABARNSTILLEGG.toString(),
      restkallResponseListe
    )


  }

  fun oppdaterInntektSkatt(grunnlagspakkeId: Int, personId: String, periodeTom: String): Int {
    val inntektAar = LocalDate.parse(periodeTom + "-01").year.toString();
    val inntektSkattRequest =
      HentInntektSkattRequest(inntektAar, "SummertSkattegrunnlagBidrag", personId);


    LOGGER.info(
      "Kaller bidrag-gcp-proxy (Sigrun) med ident = ********${
        inntektSkattRequest.personId.substring(
          IntRange(8, 10)
        )
      }, " +
          "inntektsAar = ${inntektSkattRequest.inntektsAar} inntektsFilter = ${inntektSkattRequest.inntektsFilter}"
    )
    val inntektSkattResponse = bidragGcpProxyConsumer.hentInntektSkatt(inntektSkattRequest);

    LOGGER.info("bidrag-gcp-proxy (Sigrun) ga følgende respons: $inntektSkattResponse")

    return inntektSkattResponse.grunnlag?.size ?: 0;
  }

  fun hentGrunnlagspakke(grunnlagspakkeId: Int): HentGrunnlagspakkeResponse {
    return persistenceService.hentGrunnlagspakke(grunnlagspakkeId)
  }

  fun settGyldigTildatoGrunnlagspakke(lukkGrunnlagspakkeRequest: LukkGrunnlagspakkeRequest): Int {
    return persistenceService.settGyldigTildatoGrunnlagspakke(
      lukkGrunnlagspakkeRequest.grunnlagspakkeId,
      lukkGrunnlagspakkeRequest.gyldigTil
    )
  }

}

enum class Formaal {
  FORSKUDD,
  BIDRAG,
  SAERTILSKUDD
}

enum class Grunnlagstype {
  AINNTEKT,
  SKATTEGRUNNLAG,
  UTVIDETBARNETRYGDOGSMAABARNSTILLEGG
}
