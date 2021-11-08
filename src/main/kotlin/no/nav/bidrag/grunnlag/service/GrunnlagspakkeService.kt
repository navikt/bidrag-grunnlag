package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.grunnlag.api.grunnlagspakke.GrunnlagstypeResponse
import no.nav.bidrag.grunnlag.api.grunnlagspakke.HentKomplettGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.api.grunnlagspakke.SettGyldigTilDatoForGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OppdaterGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OppdaterGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OpprettGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OpprettGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.api.grunnlagspakke.PersonIdOgPeriodeRequest
import no.nav.bidrag.grunnlag.api.grunnlagspakke.HentGrunnlagkallResponse
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.BidragGcpProxyConsumer
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.HentAinntektRequest
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
import no.nav.bidrag.grunnlag.exception.RestResponse
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

        // Henter Ainntekter
        Grunnlagstype.AINNTEKT.toString() ->
          grunnlagstypeResponseListe.add(
            oppdaterInntektAinntekt(
              oppdaterGrunnlagspakkeRequest.grunnlagspakkeId, oppdaterGrunnlagspakkeRequest.formaal,
              grunnlagstypeRequest.personIdOgPeriodeRequestListe
            )
          )

        // Henter skattegrunnlag
        Grunnlagstype.SKATTEGRUNNLAG.toString() ->
          grunnlagstypeResponseListe.add(
            oppdaterSkattegrunnlag(
              oppdaterGrunnlagspakkeRequest.grunnlagspakkeId,
              grunnlagstypeRequest.personIdOgPeriodeRequestListe
            )
          )

        // Henter utvidet barnetrygd og småbarnstillegg
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

    val hentGrunnlagkallResponseListe = mutableListOf<HentGrunnlagkallResponse>()

    personIdOgPeriodeListe.forEach() { personIdOgPeriode ->

      oppdaterGrunnlagspakkeResponseListe.add(
        OppdaterGrunnlagspakkeResponse()
      )

      val hentAinntektRequest = HentAinntektRequest(
        ident = personIdOgPeriode.personId,
        maanedFom = personIdOgPeriode.periodeFra,
        maanedTom = personIdOgPeriode.periodeTil,
        ainntektsfilter = if (formaal == Formaal.FORSKUDD.toString()) FORSKUDD_FILTER else BIDRAG_FILTER,
        formaal = if (formaal == Formaal.FORSKUDD.toString()) FORSKUDD_FORMAAL else BIDRAG_FORMAAL
      )
      LOGGER.info(
        "Kaller bidrag-gcp-proxy (Inntektskomponenten) med ident = ********${
          hentAinntektRequest.ident.substring(
            IntRange(8, 10)
          )
        }, " +
            "maanedFom = ${hentAinntektRequest.maanedFom}, maanedTom = ${hentAinntektRequest.maanedTom}, " +
            "ainntektsfilter = ${hentAinntektRequest.ainntektsfilter}, formaal = ${hentAinntektRequest.formaal}"
      )


      when (val restResponseInntekt = bidragGcpProxyConsumer.hentAinntekt(hentAinntektRequest)) {
        is RestResponse.Success -> {
          val hentInntektListeResponse = restResponseInntekt.body
          LOGGER.info("bidrag-gcp-proxy (Inntektskomponenten) ga følgende respons: $hentInntektListeResponse")

          var antallPerioderFunnet = 0

          if (hentInntektListeResponse.arbeidsInntektMaaned.isNullOrEmpty()) {
            hentGrunnlagkallResponseListe.add(
              HentGrunnlagkallResponse(
                personIdOgPeriode.personId,
                "Ingen inntekter funnet"
              )
            )
          } else {
            val opprettetInntektAinntekt = persistenceService.opprettInntektAinntekt(
              InntektAinntektDto(
                grunnlagspakkeId = grunnlagspakkeId,
                personId = personIdOgPeriode.personId,
                periodeFra = LocalDate.parse(personIdOgPeriode.periodeFra + "-01"),
                periodeTil = LocalDate.parse(personIdOgPeriode.periodeTil + "-01")
              )
            )
            hentInntektListeResponse.arbeidsInntektMaaned.forEach() { inntektPeriode ->
              antallPerioderFunnet++
              inntektPeriode.arbeidsInntektInformasjon.inntektListe?.forEach() { inntektspost ->
                persistenceService.opprettInntektspostAinntekt(
                  InntektspostAinntektDto(
                    inntektId = opprettetInntektAinntekt.inntektId,
                    utbetalingsperiode = inntektspost.utbetaltIMaaned,
                    opptjeningsperiodeFra =
                    if (inntektspost.opptjeningsperiodeFom != null) LocalDate.parse(inntektspost.opptjeningsperiodeFom + "-01") else null,
                    opptjeningsperiodeTil =
                    if (inntektspost.opptjeningsperiodeTom != null) LocalDate.parse(inntektspost.opptjeningsperiodeTom + "-01")
                      .plusMonths(1) else null,
                    opplysningspliktigId = inntektspost.opplysningspliktig?.identifikator,
                    type = inntektspost.inntektType,
                    fordelType = inntektspost.fordel,
                    beskrivelse = inntektspost.beskrivelse,
                    belop = inntektspost.beloep.toBigDecimal()
                  )
                )
              }
            }
            hentGrunnlagkallResponseListe.add(
              HentGrunnlagkallResponse(
                personIdOgPeriode.personId,
                "Antall inntekter funnet $antallPerioderFunnet"
              )
            )
          }
        }
        is RestResponse.Failure -> {
          hentGrunnlagkallResponseListe.add(
            HentGrunnlagkallResponse(
              personIdOgPeriode.personId,
              "Feil ved henting av inntekt for perioden: ${personIdOgPeriode.periodeFra} - ${personIdOgPeriode.periodeTil}. Status: ${restResponseInntekt.statusCode}"
            )
          )
        }
      }
    }
    return GrunnlagstypeResponse(Grunnlagstype.AINNTEKT.toString(), hentGrunnlagkallResponseListe)
  }


  fun oppdaterSkattegrunnlag(
    grunnlagspakkeId: Int,
    personIdOgPeriodeListe: List<PersonIdOgPeriodeRequest>
  ): GrunnlagstypeResponse {

    val hentGrunnlagkallResponseListe = mutableListOf<HentGrunnlagkallResponse>()

    personIdOgPeriodeListe.forEach() { personIdOgPeriode ->

      var inntektAar = LocalDate.parse(personIdOgPeriode.periodeFra + "-01").year
      val sluttAar = LocalDate.parse(personIdOgPeriode.periodeTil + "-01").year

      while (inntektAar < sluttAar) {
        val skattegrunnlagRequest = HentSkattegrunnlagRequest(
          inntektAar.toString(),
          "SummertSkattegrunnlagBidrag",
          personIdOgPeriode.personId
        )
        LOGGER.info(
          "Kaller bidrag-gcp-proxy (Sigrun) med ident = ********${
            skattegrunnlagRequest.personId.substring(
              IntRange(8, 10)
            )
          }, " +
              "inntektsAar = ${skattegrunnlagRequest.inntektsAar} inntektsFilter = ${skattegrunnlagRequest.inntektsFilter}"
        )

        when (val restResponseSkattegrunnlag =
          bidragGcpProxyConsumer.hentSkattegrunnlag(skattegrunnlagRequest)) {
          is RestResponse.Success -> {
            var antallSkattegrunnlagsposter = 0
            val skattegrunnlagResponse = restResponseSkattegrunnlag.body
            LOGGER.info("bidrag-gcp-proxy (Sigrun) ga følgende respons: $skattegrunnlagResponse")

            val skattegrunnlagsPosterOrdinaer = mutableListOf<Skattegrunnlag>()
            val skattegrunnlagsPosterSvalbard = mutableListOf<Skattegrunnlag>()
            skattegrunnlagsPosterOrdinaer.addAll(skattegrunnlagResponse.grunnlag!!.toMutableList())
            skattegrunnlagsPosterSvalbard.addAll(skattegrunnlagResponse.svalbardGrunnlag!!.toMutableList())

            if (skattegrunnlagsPosterOrdinaer.size > 0 || skattegrunnlagsPosterSvalbard.size > 0) {
              val opprettetSkattegrunnlag = persistenceService.opprettSkattegrunnlag(
                SkattegrunnlagDto(
                  grunnlagspakkeId = grunnlagspakkeId,
                  personId = personIdOgPeriode.personId,
                  periodeFra = LocalDate.parse("$inntektAar-01-01"),
                  periodeTil = LocalDate.parse("$inntektAar-01-01").plusYears(1),
                )
              )
              skattegrunnlagsPosterOrdinaer.forEach { skattegrunnlagsPost ->
                antallSkattegrunnlagsposter++
                persistenceService.opprettSkattegrunnlagspost(
                  SkattegrunnlagspostDto(
                    skattegrunnlagId = opprettetSkattegrunnlag.skattegrunnlagId,
                    skattegrunnlagType = SkattegrunnlagType.ORDINAER.toString(),
                    type = skattegrunnlagsPost.tekniskNavn,
                    belop = BigDecimal(skattegrunnlagsPost.beloep),
                  )
                )
              }
              skattegrunnlagsPosterSvalbard.forEach { skattegrunnlagsPost ->
                antallSkattegrunnlagsposter++
                persistenceService.opprettSkattegrunnlagspost(
                  SkattegrunnlagspostDto(
                    skattegrunnlagId = opprettetSkattegrunnlag.skattegrunnlagId,
                    skattegrunnlagType = SkattegrunnlagType.SVALBARD.toString(),
                    type = skattegrunnlagsPost.tekniskNavn,
                    belop = BigDecimal(skattegrunnlagsPost.beloep),
                  )
                )
              }
            }
            hentGrunnlagkallResponseListe.add(
              HentGrunnlagkallResponse(
                personIdOgPeriode.personId,
                "Antall skattegrunnlagsposter funnet for innteksåret ${inntektAar}: $antallSkattegrunnlagsposter"
              )
            )
          }
          is RestResponse.Failure -> hentGrunnlagkallResponseListe.add(
            HentGrunnlagkallResponse(
              personIdOgPeriode.personId,
              "Feil ved henting av skattegrunnlag for inntektsåret ${inntektAar}. Status: ${restResponseSkattegrunnlag.statusCode}"
            )
          )
        }
        inntektAar++
      }
    }
    return GrunnlagstypeResponse(
      Grunnlagstype.SKATTEGRUNNLAG.toString(),
      hentGrunnlagkallResponseListe
    )
  }


  fun oppdaterUtvidetBarnetrygdOgSmaabarnstillegg(
    grunnlagspakkeId: Int, personIdOgPeriodeListe: List<PersonIdOgPeriodeRequest>
  ): GrunnlagstypeResponse {

    val hentGrunnlagkallResponseListe = mutableListOf<HentGrunnlagkallResponse>()

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

      when (val restResponseFamilieBaSak =
        familieBaSakConsumer.hentFamilieBaSak(familieBaSakRequest)) {
        is RestResponse.Success -> {
          val familieBaSakResponse = restResponseFamilieBaSak.body
          LOGGER.info("familie-ba-sak ga følgende respons: $familieBaSakResponse")

          if (familieBaSakResponse.perioder.isNotEmpty())
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
                  manueltBeregnet = ubst.manueltBeregnet,
                  deltBosted = ubst.deltBosted
                )
              )
            }
          hentGrunnlagkallResponseListe.add(
            HentGrunnlagkallResponse(
              personIdOgPeriode.personId,
              "Antall inntekter funnet $antallPerioderFunnet"
            )
          )
        }
        is RestResponse.Failure -> hentGrunnlagkallResponseListe.add(
          HentGrunnlagkallResponse(
            personIdOgPeriode.personId,
            "Feil ved henting av familie-ba-sak for perioden: ${personIdOgPeriode.periodeFra} - ${personIdOgPeriode.periodeTil}. Status: ${restResponseFamilieBaSak.statusCode}"
          )
        )
      }
    }
    return GrunnlagstypeResponse(
      Grunnlagstype.UTVIDETBARNETRYGDOGSMAABARNSTILLEGG.toString(),
      hentGrunnlagkallResponseListe
    )
  }


  fun hentKomplettGrunnlagspakke(grunnlagspakkeId: Int): HentKomplettGrunnlagspakkeResponse {
    return persistenceService.hentKomplettGrunnlagspakke(grunnlagspakkeId)
  }

  fun settGyldigTildatoGrunnlagspakke(settGyldigTilDatoForGrunnlagspakkeRequest: SettGyldigTilDatoForGrunnlagspakkeRequest): Int {
    return persistenceService.settGyldigTildatoGrunnlagspakke(
      settGyldigTilDatoForGrunnlagspakkeRequest.grunnlagspakkeId,
      LocalDate.parse(settGyldigTilDatoForGrunnlagspakkeRequest.gyldigTil)
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

enum class SkattegrunnlagType {
  ORDINAER,
  SVALBARD
}