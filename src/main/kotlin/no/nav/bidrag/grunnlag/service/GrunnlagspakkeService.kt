package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.grunnlag.api.grunnlagspakke.GrunnlagRequest
import no.nav.bidrag.grunnlag.api.grunnlagspakke.HentGrunnlagResponse
import no.nav.bidrag.grunnlag.api.grunnlagspakke.HentKomplettGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OppdaterGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OppdaterGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OpprettGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.api.grunnlagspakke.OpprettGrunnlagspakkeResponse
import no.nav.bidrag.grunnlag.comparator.PeriodComparable
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.BidragGcpProxyConsumer
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.ainntekt.ArbeidsInntektInformasjonIntern
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.ainntekt.ArbeidsInntektMaanedIntern
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.ainntekt.HentInntektListeResponseIntern
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.ainntekt.HentInntektRequest
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.ainntekt.InntektIntern
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.ainntekt.OpplysningspliktigIntern
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.ainntekt.TilleggsinformasjonDetaljerIntern
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.ainntekt.TilleggsinformasjonIntern
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.ainntekt.VirksomhetIntern
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.barnetillegg.HentBarnetilleggPensjonRequest
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.skatt.HentSkattegrunnlagRequest
import no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api.skatt.Skattegrunnlag
import no.nav.bidrag.grunnlag.consumer.familiebasak.FamilieBaSakConsumer
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.FamilieBaSakRequest
import no.nav.bidrag.grunnlag.dto.AinntektDto
import no.nav.bidrag.grunnlag.dto.AinntektspostDto
import no.nav.bidrag.grunnlag.dto.BarnetilleggDto
import no.nav.bidrag.grunnlag.dto.GrunnlagspakkeDto
import no.nav.bidrag.grunnlag.dto.SkattegrunnlagDto
import no.nav.bidrag.grunnlag.dto.SkattegrunnlagspostDto
import no.nav.bidrag.grunnlag.dto.UtvidetBarnetrygdOgSmaabarnstilleggDto
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.tjenester.aordningen.inntektsinformasjon.response.HentInntektListeResponse
import no.nav.tjenester.aordningen.inntektsinformasjon.tilleggsinformasjondetaljer.Etterbetalingsperiode
import no.nav.tjenester.aordningen.inntektsinformasjon.tilleggsinformasjondetaljer.TilleggsinformasjonDetaljerType
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime


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
  var timestampOppdatering = LocalDateTime.now()

  fun opprettGrunnlagspakke(opprettGrunnlagspakkeRequest: OpprettGrunnlagspakkeRequest): OpprettGrunnlagspakkeResponse {
    val grunnlagspakkeDto = GrunnlagspakkeDto(
      opprettetAv = opprettGrunnlagspakkeRequest.opprettetAv,
      formaal = opprettGrunnlagspakkeRequest.formaal.name
    )
    val opprettetGrunnlagspakke = persistenceService.opprettNyGrunnlagspakke(grunnlagspakkeDto)
    return OpprettGrunnlagspakkeResponse(opprettetGrunnlagspakke.grunnlagspakkeId)

  }

  fun oppdaterGrunnlagspakke(
    grunnlagspakkeId: Int,
    oppdaterGrunnlagspakkeRequest: OppdaterGrunnlagspakkeRequest
  ): OppdaterGrunnlagspakkeResponse {

    val hentGrunnlagResponseListe = mutableListOf<HentGrunnlagResponse>()

    // Validerer at grunnlagspakke eksisterer
    persistenceService.validerGrunnlagspakke(grunnlagspakkeId)

    val ainntektRequestListe = mutableListOf<PersonIdOgPeriodeRequest>()
    val skattegrunnlagRequestListe = mutableListOf<PersonIdOgPeriodeRequest>()
    val ubstRequestListe = mutableListOf<PersonIdOgPeriodeRequest>()
    val barnetilleggRequestListe = mutableListOf<PersonIdOgPeriodeRequest>()

    oppdaterGrunnlagspakkeRequest.grunnlagRequestListe.forEach { grunnlagRequest ->
      when (grunnlagRequest.grunnlagType) {

        // Bygger opp liste over A-inntekter
        GrunnlagType.AINNTEKT ->
          ainntektRequestListe.add(nyPersonIdOgPeriode(grunnlagRequest))

        // Bygger opp liste over skattegrunnlag
        GrunnlagType.SKATTEGRUNNLAG ->
          skattegrunnlagRequestListe.add(nyPersonIdOgPeriode(grunnlagRequest))

        // Bygger opp liste over utvidet barnetrygd og småbarnstillegg
        GrunnlagType.UTVIDETBARNETRYGDOGSMAABARNSTILLEGG ->
          ubstRequestListe.add(nyPersonIdOgPeriode(grunnlagRequest))

        // Bygger opp liste over barnetillegg
        GrunnlagType.BARNETILLEGG ->
          barnetilleggRequestListe.add(nyPersonIdOgPeriode(grunnlagRequest))
      }
    }

    // Oppdaterer grunnlag for A-inntekt
    hentGrunnlagResponseListe.addAll(
      oppdaterAinntekt(
        grunnlagspakkeId,
        ainntektRequestListe
      )
    )

    // Oppdaterer grunnlag for skattegrunnlag
    hentGrunnlagResponseListe.addAll(
      oppdaterSkattegrunnlag(
        grunnlagspakkeId,
        skattegrunnlagRequestListe
      )
    )

    // Oppdaterer grunnlag for utvidet barnetrygd og småbarnstillegg
    hentGrunnlagResponseListe.addAll(
      oppdaterUtvidetBarnetrygdOgSmaabarnstillegg(
        grunnlagspakkeId,
        ubstRequestListe
      )
    )

    // Oppdaterer grunnlag for barnetillegg
    hentGrunnlagResponseListe.addAll(
      oppdaterBarnetillegg(
        grunnlagspakkeId,
        barnetilleggRequestListe
      )
    )

    return OppdaterGrunnlagspakkeResponse(grunnlagspakkeId, hentGrunnlagResponseListe)
  }

  private fun nyPersonIdOgPeriode(grunnlagRequest: GrunnlagRequest) =
    PersonIdOgPeriodeRequest(
      personId = grunnlagRequest.personId,
      periodeFra = grunnlagRequest.periodeFra,
      periodeTil = grunnlagRequest.periodeTil,
      innsynHistoriskeInntekterDato = grunnlagRequest.innsynHistoriskeInntekterDato
    )

  private fun oppdaterAinntekt(
    grunnlagspakkeId: Int,
    personIdOgPeriodeListe: List<PersonIdOgPeriodeRequest>
  ): List<HentGrunnlagResponse> {

    val hentGrunnlagResponseListe = mutableListOf<HentGrunnlagResponse>()
    val formaal = persistenceService.hentFormaalGrunnlagspakke(grunnlagspakkeId)

    personIdOgPeriodeListe.forEach { personIdOgPeriode ->

      oppdaterGrunnlagspakkeResponseListe.add(OppdaterGrunnlagspakkeResponse())

      val hentHistoriskeInntekter = personIdOgPeriode.innsynHistoriskeInntekterDato != null

      val hentAinntektRequest = HentInntektRequest(
        ident = personIdOgPeriode.personId,
        innsynHistoriskeInntekterDato = personIdOgPeriode.innsynHistoriskeInntekterDato,
        maanedFom = personIdOgPeriode.periodeFra.toString().substring(0, 7),
        maanedTom = personIdOgPeriode.periodeTil.minusMonths(1).toString().substring(0, 7),
        ainntektsfilter = finnFilter(formaal),
        formaal = finnFormaal(formaal)
      )
      LOGGER.info(
        "Kaller bidrag-gcp-proxy (Inntektskomponenten) med ident = ********${
          hentAinntektRequest.ident.substring(
            IntRange(8, 10)
          )
        }, " +
            "innsynHistoriskeInntekterDato = ${hentAinntektRequest.innsynHistoriskeInntekterDato}, " +
            "maanedFom = ${hentAinntektRequest.maanedFom}, maanedTom = ${hentAinntektRequest.maanedTom}, " +
            "ainntektsfilter = ${hentAinntektRequest.ainntektsfilter}, formaal = ${hentAinntektRequest.formaal}"
      )


      when (val restResponseInntekt = bidragGcpProxyConsumer.hentAinntekt(hentAinntektRequest)) {
        is RestResponse.Success -> {
          val hentInntektListeResponse = mapResponsTilInternStruktur(restResponseInntekt.body)
          LOGGER.info("bidrag-gcp-proxy (Inntektskomponenten) ga følgende respons: $hentInntektListeResponse")

          var antallPerioderFunnet = 0
          val nyeAinntekter = mutableListOf<PeriodComparable<AinntektDto, AinntektspostDto>>()

          if (hentInntektListeResponse.arbeidsInntektMaanedIntern.isNullOrEmpty()) {
            hentGrunnlagResponseListe.add(
              HentGrunnlagResponse(
                GrunnlagType.AINNTEKT,
                personIdOgPeriode.personId,
                GrunnlagsRequestStatus.HENTET,
                "Ingen inntekter funnet"
              )
            )
          } else {
            hentInntektListeResponse.arbeidsInntektMaanedIntern.forEach { inntektPeriode ->
              antallPerioderFunnet++
              timestampOppdatering = LocalDateTime.now()
              val inntekt = AinntektDto(
                grunnlagspakkeId = grunnlagspakkeId,
                personId = personIdOgPeriode.personId,
                periodeFra = LocalDate.parse(inntektPeriode.aarMaaned + "-01"),
                periodeTil = LocalDate.parse(inntektPeriode.aarMaaned + "-01").plusMonths(1),
                brukFra = if (hentHistoriskeInntekter) personIdOgPeriode.innsynHistoriskeInntekterDato!!.atStartOfDay() else timestampOppdatering,
                hentetTidspunkt = timestampOppdatering
              )

              val inntektsposter = mutableListOf<AinntektspostDto>()
              inntektPeriode.arbeidsInntektInformasjonIntern.inntektIntern?.forEach { inntektspost ->
                inntektsposter.add(
                  AinntektspostDto(
                    utbetalingsperiode = inntektspost.utbetaltIMaaned,
                    opptjeningsperiodeFra =
                    if (inntektspost.opptjeningsperiodeFom != null) inntektspost.opptjeningsperiodeFom else null,
                    opptjeningsperiodeTil =
                    if (inntektspost.opptjeningsperiodeTom != null) inntektspost.opptjeningsperiodeTom
                      .plusMonths(1) else null,
                    opplysningspliktigId = inntektspost.opplysningspliktig?.identifikator,
                    virksomhetId = inntektspost.virksomhet?.identifikator,
                    inntektType = inntektspost.inntektType,
                    fordelType = inntektspost.fordel,
                    beskrivelse = inntektspost.beskrivelse,
                    belop = inntektspost.beloep,
                    etterbetalingsperiodeFra = inntektspost.tilleggsinformasjon?.tilleggsinformasjonDetaljer?.etterbetalingsperiodeFom,
                    etterbetalingsperiodeTil = inntektspost.tilleggsinformasjon?.tilleggsinformasjonDetaljer?.etterbetalingsperiodeTom

                  )
                )
              }
              nyeAinntekter.add(PeriodComparable(inntekt, inntektsposter))
            }
            persistenceService.oppdaterAinntektForGrunnlagspakke(
              grunnlagspakkeId,
              nyeAinntekter,
              personIdOgPeriode.periodeFra,
              personIdOgPeriode.periodeTil,
              personIdOgPeriode.personId
            )
            hentGrunnlagResponseListe.add(
              HentGrunnlagResponse(
                GrunnlagType.AINNTEKT,
                personIdOgPeriode.personId,
                GrunnlagsRequestStatus.HENTET,
                "Antall inntekter funnet (periode ${personIdOgPeriode.periodeFra} - ${personIdOgPeriode.periodeTil}): $antallPerioderFunnet",
              )
            )
          }
        }
        is RestResponse.Failure -> {
          hentGrunnlagResponseListe.add(
            HentGrunnlagResponse(
              GrunnlagType.AINNTEKT,
              personIdOgPeriode.personId,
              if (restResponseInntekt.statusCode == HttpStatus.NOT_FOUND) GrunnlagsRequestStatus.IKKE_FUNNET else GrunnlagsRequestStatus.FEILET,
              "Feil ved henting av inntekt for perioden: ${personIdOgPeriode.periodeFra} - ${personIdOgPeriode.periodeTil}."
            )
          )
        }
      }
    }
    return hentGrunnlagResponseListe
  }


  fun oppdaterSkattegrunnlag(
    grunnlagspakkeId: Int,
    personIdOgPeriodeListe: List<PersonIdOgPeriodeRequest>
  ): List<HentGrunnlagResponse> {

    val hentGrunnlagResponseListe = mutableListOf<HentGrunnlagResponse>()

    personIdOgPeriodeListe.forEach { personIdOgPeriode ->

      var inntektAar = personIdOgPeriode.periodeFra.year
      val sluttAar = personIdOgPeriode.periodeTil.year

      val periodeFra = LocalDate.of(inntektAar, 1, 1)
      val periodeTil = LocalDate.of(sluttAar, 1, 1)

      val nyeSkattegrunnlag =
        mutableListOf<PeriodComparable<SkattegrunnlagDto, SkattegrunnlagspostDto>>()

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
              timestampOppdatering = LocalDateTime.now()
              val skattegrunnlag = SkattegrunnlagDto(
                grunnlagspakkeId = grunnlagspakkeId,
                personId = personIdOgPeriode.personId,
                periodeFra = LocalDate.parse("$inntektAar-01-01"),
                periodeTil = LocalDate.parse("$inntektAar-01-01").plusYears(1),
                brukFra = timestampOppdatering,
                hentetTidspunkt = timestampOppdatering
              )
              val skattegrunnlagsposter = mutableListOf<SkattegrunnlagspostDto>()
              skattegrunnlagsPosterOrdinaer.forEach { skattegrunnlagsPost ->
                antallSkattegrunnlagsposter++
                skattegrunnlagsposter.add(
                  SkattegrunnlagspostDto(
                    skattegrunnlagId = skattegrunnlag.skattegrunnlagId,
                    skattegrunnlagType = SkattegrunnlagType.ORDINAER.toString(),
                    inntektType = skattegrunnlagsPost.tekniskNavn,
                    belop = BigDecimal(skattegrunnlagsPost.beloep),
                  )
                )
              }
              skattegrunnlagsPosterSvalbard.forEach { skattegrunnlagsPost ->
                antallSkattegrunnlagsposter++
                skattegrunnlagsposter.add(
                  SkattegrunnlagspostDto(
                    skattegrunnlagId = skattegrunnlag.skattegrunnlagId,
                    skattegrunnlagType = SkattegrunnlagType.SVALBARD.toString(),
                    inntektType = skattegrunnlagsPost.tekniskNavn,
                    belop = BigDecimal(skattegrunnlagsPost.beloep),
                  )
                )
              }
              nyeSkattegrunnlag.add(PeriodComparable(skattegrunnlag, skattegrunnlagsposter))
            }
            persistenceService.oppdaterSkattegrunnlagForGrunnlagspakke(
              grunnlagspakkeId,
              nyeSkattegrunnlag,
              periodeFra,
              periodeTil,
              personIdOgPeriode.personId
            )
            hentGrunnlagResponseListe.add(
              HentGrunnlagResponse(
                GrunnlagType.SKATTEGRUNNLAG,
                personIdOgPeriode.personId,
                GrunnlagsRequestStatus.HENTET,
                "Antall skattegrunnlagsposter funnet for innteksåret ${inntektAar}: $antallSkattegrunnlagsposter"
              )
            )
          }
          is RestResponse.Failure -> hentGrunnlagResponseListe.add(
            HentGrunnlagResponse(
              GrunnlagType.SKATTEGRUNNLAG,
              personIdOgPeriode.personId,
              if (restResponseSkattegrunnlag.statusCode == HttpStatus.NOT_FOUND) GrunnlagsRequestStatus.IKKE_FUNNET else GrunnlagsRequestStatus.FEILET,
              "Feil ved henting av skattegrunnlag for inntektsåret ${inntektAar}."
            )
          )
        }
        inntektAar++
      }
    }
    return hentGrunnlagResponseListe
  }


  fun oppdaterUtvidetBarnetrygdOgSmaabarnstillegg(
    grunnlagspakkeId: Int, personIdOgPeriodeListe: List<PersonIdOgPeriodeRequest>
  ): List<HentGrunnlagResponse> {

    val hentGrunnlagResponseListe = mutableListOf<HentGrunnlagResponse>()

    personIdOgPeriodeListe.forEach { personIdOgPeriode ->

      var antallPerioderFunnet = 0

      val familieBaSakRequest = FamilieBaSakRequest(
        personIdent = personIdOgPeriode.personId,
        fraDato = personIdOgPeriode.periodeFra
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
            familieBaSakResponse.perioder.forEach { ubst ->
              if (LocalDate.parse(ubst.fomMåned.toString() + "-01").isBefore(personIdOgPeriode.periodeTil)) {
                timestampOppdatering = LocalDateTime.now()
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
                    brukFra = timestampOppdatering,
                    belop = BigDecimal.valueOf(ubst.beløp),
                    manueltBeregnet = ubst.manueltBeregnet,
                    deltBosted = ubst.deltBosted,
                    hentetTidspunkt = timestampOppdatering
                  )
                )
              }
            }
          hentGrunnlagResponseListe.add(
            HentGrunnlagResponse(
              GrunnlagType.UTVIDETBARNETRYGDOGSMAABARNSTILLEGG,
              personIdOgPeriode.personId,
              GrunnlagsRequestStatus.HENTET,
              "Antall perioder funnet: $antallPerioderFunnet"
            )
          )
        }
        is RestResponse.Failure -> hentGrunnlagResponseListe.add(
          HentGrunnlagResponse(
            GrunnlagType.UTVIDETBARNETRYGDOGSMAABARNSTILLEGG,
            personIdOgPeriode.personId,
            if (restResponseFamilieBaSak.statusCode == HttpStatus.NOT_FOUND) GrunnlagsRequestStatus.IKKE_FUNNET else GrunnlagsRequestStatus.FEILET,
            "Feil ved henting av familie-ba-sak for perioden: ${personIdOgPeriode.periodeFra} - ${personIdOgPeriode.periodeTil}."
          )
        )
      }
    }
    return hentGrunnlagResponseListe
  }


  fun oppdaterBarnetillegg(
    grunnlagspakkeId: Int, personIdOgPeriodeListe: List<PersonIdOgPeriodeRequest>
  ): List<HentGrunnlagResponse> {

    val hentGrunnlagResponseListe = mutableListOf<HentGrunnlagResponse>()

    personIdOgPeriodeListe.forEach { personIdOgPeriode ->

      var antallPerioderFunnet = 0

      val hentBarnetilleggPensjonRequest = HentBarnetilleggPensjonRequest(
        mottaker = personIdOgPeriode.personId,
        fom = personIdOgPeriode.periodeFra,
        tom = personIdOgPeriode.periodeTil.minusDays(1)
      )

      LOGGER.info(
        "Kaller barnetillegg pensjon med personIdent ********${
          hentBarnetilleggPensjonRequest.mottaker.substring(
            IntRange(8, 10)
          )
        } " +
            ", fraDato " + "${hentBarnetilleggPensjonRequest.fom}" +
            ", og tilDato " + "${hentBarnetilleggPensjonRequest.tom}"
      )

      when (val restResponseBarnetilleggPensjon =
        bidragGcpProxyConsumer.hentBarnetilleggPensjon(hentBarnetilleggPensjonRequest)) {
        is RestResponse.Success -> {
          val barnetilleggPensjonResponse = restResponseBarnetilleggPensjon.body
          LOGGER.info("Barnetillegg pensjon ga følgende respons: $barnetilleggPensjonResponse")

          if ((barnetilleggPensjonResponse.barnetilleggPensjonListe != null) && (barnetilleggPensjonResponse.barnetilleggPensjonListe.isNotEmpty()))
            barnetilleggPensjonResponse.barnetilleggPensjonListe.forEach { bt ->
              if (bt.fom.isBefore(personIdOgPeriode.periodeTil)) {
                antallPerioderFunnet++
                timestampOppdatering = LocalDateTime.now()
                persistenceService.opprettBarnetillegg(
                  BarnetilleggDto(
                    grunnlagspakkeId = grunnlagspakkeId,
                    partPersonId = personIdOgPeriode.personId,
                    barnetilleggType = BarnetilleggType.PENSJON.toString(),
                    periodeFra = bt.fom,
                    // justerer frem tildato med én måned for å ha lik logikk som resten av appen. Tildato skal angis som til, men ikke inkludert, måned.
                    periodeTil = if (bt.tom != null) bt.tom.plusMonths(1).withDayOfMonth(1) else null,
                    brukFra = timestampOppdatering,
                    barnPersonId = bt.barn,
                    belopBrutto = bt.beloep,
                    barnType = if (bt.erFellesbarn) BarnType.FELLES.toString() else BarnType.SÆRKULL.toString(),
                    hentetTidspunkt = timestampOppdatering
                  )
                )
              }
            }
          hentGrunnlagResponseListe.add(
            HentGrunnlagResponse(
              GrunnlagType.BARNETILLEGG,
              personIdOgPeriode.personId,
              GrunnlagsRequestStatus.HENTET,
              "Antall perioder funnet: $antallPerioderFunnet"
            )
          )
        }
        is RestResponse.Failure -> hentGrunnlagResponseListe.add(
          HentGrunnlagResponse(
            GrunnlagType.BARNETILLEGG,
            personIdOgPeriode.personId,
            if (restResponseBarnetilleggPensjon.statusCode == HttpStatus.NOT_FOUND) GrunnlagsRequestStatus.IKKE_FUNNET else GrunnlagsRequestStatus.FEILET,
            "Feil ved henting av barnetillegg pensjon for perioden: ${personIdOgPeriode.periodeFra} - ${personIdOgPeriode.periodeTil}."
          )
        )
      }
    }
    return hentGrunnlagResponseListe
  }

  fun hentKomplettGrunnlagspakke(grunnlagspakkeId: Int): HentKomplettGrunnlagspakkeResponse {
    // Validerer at grunnlagspakke eksisterer
    persistenceService.validerGrunnlagspakke(grunnlagspakkeId)
    return persistenceService.hentKomplettGrunnlagspakke(grunnlagspakkeId)
  }

  fun lukkGrunnlagspakke(grunnlagspakkeId: Int): Int {
    // Validerer at grunnlagspakke eksisterer
    persistenceService.validerGrunnlagspakke(grunnlagspakkeId)
    return persistenceService.lukkGrunnlagspakke(grunnlagspakkeId)
  }

  fun finnFilter(formaal: String): String {
    return if (formaal == Formaal.FORSKUDD.toString()) FORSKUDD_FILTER else BIDRAG_FILTER
  }

  fun finnFormaal(formaal: String): String {
    return if (formaal == Formaal.FORSKUDD.toString()) FORSKUDD_FORMAAL else BIDRAG_FORMAAL
  }

  fun mapResponsTilInternStruktur(eksternRespons: HentInntektListeResponse): HentInntektListeResponseIntern {

    val arbeidsInntektMaanedListe = mutableListOf<ArbeidsInntektMaanedIntern>()

    eksternRespons.arbeidsInntektMaaned?.forEach() { arbeidsInntektMaaned ->
      val inntektInternListe = mutableListOf<InntektIntern>()
      arbeidsInntektMaaned.arbeidsInntektInformasjon?.inntektListe?.forEach() { inntekt ->
        val inntektIntern = InntektIntern(
          inntektType = inntekt.inntektType.toString(),
          beloep = inntekt.beloep,
          fordel = inntekt.fordel,
          inntektsperiodetype = inntekt.inntektsperiodetype,
          opptjeningsperiodeFom = inntekt.opptjeningsperiodeFom,
          opptjeningsperiodeTom = inntekt.opptjeningsperiodeTom,
          utbetaltIMaaned = inntekt.utbetaltIMaaned?.toString(),
          opplysningspliktig = OpplysningspliktigIntern(
            inntekt.opplysningspliktig.identifikator,
            inntekt.opplysningspliktig.aktoerType.toString()
          ),
          virksomhet = VirksomhetIntern(
            inntekt.virksomhet.identifikator,
            inntekt.virksomhet.aktoerType.toString()
          ),
          tilleggsinformasjon = if (inntekt?.tilleggsinformasjon?.tilleggsinformasjonDetaljer?.detaljerType == TilleggsinformasjonDetaljerType.ETTERBETALINGSPERIODE)
            TilleggsinformasjonIntern(
              inntekt.tilleggsinformasjon.kategori,
              TilleggsinformasjonDetaljerIntern(
                (inntekt.tilleggsinformasjon?.tilleggsinformasjonDetaljer as Etterbetalingsperiode).etterbetalingsperiodeFom,
                (inntekt.tilleggsinformasjon?.tilleggsinformasjonDetaljer as Etterbetalingsperiode).etterbetalingsperiodeTom.plusDays(
                  1
                ),
              )
            ) else null,
          beskrivelse = inntekt.beskrivelse
        )
        inntektInternListe.add(inntektIntern)
      }
      arbeidsInntektMaanedListe.add(
        ArbeidsInntektMaanedIntern(
          arbeidsInntektMaaned.aarMaaned.toString(),
          ArbeidsInntektInformasjonIntern(inntektInternListe)
        )
      )
    }
    return HentInntektListeResponseIntern(arbeidsInntektMaanedListe)
  }
}


enum class Formaal {
  FORSKUDD,
  BIDRAG,
  SAERTILSKUDD
}

enum class GrunnlagType {
  AINNTEKT,
  SKATTEGRUNNLAG,
  UTVIDETBARNETRYGDOGSMAABARNSTILLEGG,
  BARNETILLEGG
}

enum class SkattegrunnlagType {
  ORDINAER,
  SVALBARD
}

enum class BarnetilleggType {
  PENSJON
}

enum class BarnType {
  FELLES,
  SÆRKULL
}

enum class GrunnlagsRequestStatus {
  HENTET,
  IKKE_FUNNET,
  FEILET,
}

data class PersonIdOgPeriodeRequest(
  val personId: String,
  val periodeFra: LocalDate,
  val periodeTil: LocalDate,
  val innsynHistoriskeInntekterDato: LocalDate?
)
