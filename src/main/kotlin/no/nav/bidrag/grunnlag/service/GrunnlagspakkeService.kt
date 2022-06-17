package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.behandling.felles.dto.grunnlag.GrunnlagRequestDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.HentGrunnlagspakkeDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.OppdaterGrunnlagDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.OppdaterGrunnlagspakkeDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.OppdaterGrunnlagspakkeRequestDto
import no.nav.bidrag.behandling.felles.dto.grunnlag.OpprettGrunnlagspakkeRequestDto
import no.nav.bidrag.behandling.felles.enums.BarnType
import no.nav.bidrag.behandling.felles.enums.BarnetilleggType
import no.nav.bidrag.behandling.felles.enums.Formaal
import no.nav.bidrag.behandling.felles.enums.GrunnlagRequestType
import no.nav.bidrag.behandling.felles.enums.GrunnlagsRequestStatus
import no.nav.bidrag.behandling.felles.enums.SkattegrunnlagType
import no.nav.bidrag.grunnlag.bo.AinntektBo
import no.nav.bidrag.grunnlag.bo.AinntektspostBo
import no.nav.bidrag.grunnlag.bo.BarnetilleggBo
import no.nav.bidrag.grunnlag.bo.SkattegrunnlagBo
import no.nav.bidrag.grunnlag.bo.SkattegrunnlagspostBo
import no.nav.bidrag.grunnlag.bo.UtvidetBarnetrygdOgSmaabarnstilleggBo
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
import no.nav.bidrag.grunnlag.consumer.infotrygdkontantstottev2.KontantstotteConsumer
import no.nav.bidrag.grunnlag.consumer.infotrygdkontantstottev2.api.Foedselsnummer
import no.nav.bidrag.grunnlag.consumer.infotrygdkontantstottev2.api.InnsynRequest
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
  private val bidragGcpProxyConsumer: BidragGcpProxyConsumer,
  private val kontantstotteConsumer: KontantstotteConsumer
) {

  companion object {

    @JvmStatic
    private val LOGGER = LoggerFactory.getLogger(GrunnlagspakkeService::class.java)

    const val BIDRAG_FILTER = "BidragA-Inntekt"
    const val FORSKUDD_FILTER = "BidragsforskuddA-Inntekt"
    const val BIDRAG_FORMAAL = "Bidrag"
    const val FORSKUDD_FORMAAL = "Bidragsforskudd"
  }

  fun opprettGrunnlagspakke(opprettGrunnlagspakkeRequestDto: OpprettGrunnlagspakkeRequestDto): Int {
    val opprettetGrunnlagspakke = persistenceService.opprettNyGrunnlagspakke(opprettGrunnlagspakkeRequestDto)
    return opprettetGrunnlagspakke.grunnlagspakkeId
  }

  fun oppdaterGrunnlagspakke(
    grunnlagspakkeId: Int,
    oppdaterGrunnlagspakkeRequestDto: OppdaterGrunnlagspakkeRequestDto
  ): OppdaterGrunnlagspakkeDto {

    val oppdaterGrunnlagDtoListe = mutableListOf<OppdaterGrunnlagDto>()
    val timestampOppdatering = LocalDateTime.now()

    // Validerer at grunnlagspakke eksisterer
    persistenceService.validerGrunnlagspakke(grunnlagspakkeId)

    val ainntektRequestListe = mutableListOf<PersonIdOgPeriodeRequest>()
    val skattegrunnlagRequestListe = mutableListOf<PersonIdOgPeriodeRequest>()
    val ubstRequestListe = mutableListOf<PersonIdOgPeriodeRequest>()
    val barnetilleggRequestListe = mutableListOf<PersonIdOgPeriodeRequest>()

    val kontantstotteRequestListe = mutableListOf<PersonIdOgPeriodeRequest>()

    oppdaterGrunnlagspakkeRequestDto.grunnlagRequestDtoListe.forEach { grunnlagRequest ->
      when (grunnlagRequest.type) {

        // Bygger opp liste over A-inntekter
        GrunnlagRequestType.AINNTEKT ->
          ainntektRequestListe.add(nyPersonIdOgPeriode(grunnlagRequest))

        // Bygger opp liste over skattegrunnlag
        GrunnlagRequestType.SKATTEGRUNNLAG ->
          skattegrunnlagRequestListe.add(nyPersonIdOgPeriode(grunnlagRequest))

        // Bygger opp liste over utvidet barnetrygd og småbarnstillegg
        GrunnlagRequestType.UTVIDET_BARNETRYGD_OG_SMAABARNSTILLEGG ->
          ubstRequestListe.add(nyPersonIdOgPeriode(grunnlagRequest))

        // Bygger opp liste over barnetillegg
        GrunnlagRequestType.BARNETILLEGG ->
          barnetilleggRequestListe.add(nyPersonIdOgPeriode(grunnlagRequest))

        // Bygger opp liste over kontantstøtte
        GrunnlagRequestType.KONTANTSTOTTE ->
          kontantstotteRequestListe.add(nyPersonIdOgPeriode(grunnlagRequest))

        else -> {
          //Todo
        }
      }
    }

    val forekomsterFunnet = false

    // Oppdaterer grunnlag for A-inntekt
    oppdaterGrunnlagDtoListe.addAll(
      oppdaterAinntekt(
        grunnlagspakkeId,
        ainntektRequestListe,
        timestampOppdatering,
        forekomsterFunnet
      )
    )

    // Oppdaterer grunnlag for skattegrunnlag
    oppdaterGrunnlagDtoListe.addAll(
      oppdaterSkattegrunnlag(
        grunnlagspakkeId,
        skattegrunnlagRequestListe,
        timestampOppdatering,
        forekomsterFunnet
      )
    )

    // Oppdaterer grunnlag for utvidet barnetrygd og småbarnstillegg
    oppdaterGrunnlagDtoListe.addAll(
      oppdaterUtvidetBarnetrygdOgSmaabarnstillegg(
        grunnlagspakkeId,
        ubstRequestListe,
        timestampOppdatering,
        forekomsterFunnet
      )
    )

    // Oppdaterer grunnlag for barnetillegg
    oppdaterGrunnlagDtoListe.addAll(
      oppdaterBarnetillegg(
        grunnlagspakkeId,
        barnetilleggRequestListe,
        timestampOppdatering,
        forekomsterFunnet
      )
    )

    // Oppdaterer grunnlag for kontantstøtte
    oppdaterGrunnlagDtoListe.addAll(
      oppdaterKontantstotte(
        grunnlagspakkeId,
        kontantstotteRequestListe,
        timestampOppdatering,
        forekomsterFunnet
      )
    )

    // Oppdaterer endret_timestamp på grunnlagspakke
    if (forekomsterFunnet) {
      persistenceService.oppdaterEndretTimestamp(grunnlagspakkeId, timestampOppdatering)
    }

    return OppdaterGrunnlagspakkeDto(grunnlagspakkeId, oppdaterGrunnlagDtoListe)
  }

  private fun nyPersonIdOgPeriode(grunnlagRequestDto: GrunnlagRequestDto) =
    PersonIdOgPeriodeRequest(
      personId = grunnlagRequestDto.personId,
      periodeFra = grunnlagRequestDto.periodeFra,
      periodeTil = grunnlagRequestDto.periodeTil
    )

  private fun oppdaterAinntekt(
    grunnlagspakkeId: Int,
    personIdOgPeriodeListe: List<PersonIdOgPeriodeRequest>,
    timestampOppdatering: LocalDateTime,
    forekomsterFunnet: Boolean
  ): List<OppdaterGrunnlagDto> {

    val oppdaterGrunnlagDtoListe = mutableListOf<OppdaterGrunnlagDto>()
    val formaal = persistenceService.hentFormaalGrunnlagspakke(grunnlagspakkeId)

    personIdOgPeriodeListe.forEach { personIdOgPeriode ->

      val hentAinntektRequest = HentInntektRequest(
        ident = personIdOgPeriode.personId,
        innsynHistoriskeInntekterDato = null,
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
          val nyeAinntekter = mutableListOf<PeriodComparable<AinntektBo, AinntektspostBo>>()

          if (hentInntektListeResponse.arbeidsInntektMaanedIntern.isNullOrEmpty()) {
            oppdaterGrunnlagDtoListe.add(
              OppdaterGrunnlagDto(
                GrunnlagRequestType.AINNTEKT,
                personIdOgPeriode.personId,
                GrunnlagsRequestStatus.HENTET,
                "Ingen inntekter funnet"
              )
            )
          } else {
            hentInntektListeResponse.arbeidsInntektMaanedIntern.forEach { inntektPeriode ->
              antallPerioderFunnet++
              val inntekt = AinntektBo(
                grunnlagspakkeId = grunnlagspakkeId,
                personId = personIdOgPeriode.personId,
                periodeFra = LocalDate.parse(inntektPeriode.aarMaaned + "-01"),
                periodeTil = LocalDate.parse(inntektPeriode.aarMaaned + "-01").plusMonths(1),
                aktiv = true,
                brukFra = timestampOppdatering,
                brukTil = null,
                hentetTidspunkt = timestampOppdatering
              )

              val inntektsposter = mutableListOf<AinntektspostBo>()
              inntektPeriode.arbeidsInntektInformasjonIntern.inntektIntern?.forEach { inntektspost ->
                inntektsposter.add(
                  AinntektspostBo(
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
              personIdOgPeriode.personId,
              timestampOppdatering
            )
            oppdaterGrunnlagDtoListe.add(
              OppdaterGrunnlagDto(
                GrunnlagRequestType.AINNTEKT,
                personIdOgPeriode.personId,
                GrunnlagsRequestStatus.HENTET,
                "Antall inntekter funnet (periode ${personIdOgPeriode.periodeFra} - ${personIdOgPeriode.periodeTil}): $antallPerioderFunnet",
              )
            )
            if (antallPerioderFunnet > 0) {
              forekomsterFunnet
            }
          }
        }
        is RestResponse.Failure -> {
          oppdaterGrunnlagDtoListe.add(
            OppdaterGrunnlagDto(
              GrunnlagRequestType.AINNTEKT,
              personIdOgPeriode.personId,
              if (restResponseInntekt.statusCode == HttpStatus.NOT_FOUND) GrunnlagsRequestStatus.IKKE_FUNNET else GrunnlagsRequestStatus.FEILET,
              "Feil ved henting av inntekt for perioden: ${personIdOgPeriode.periodeFra} - ${personIdOgPeriode.periodeTil}."
            )
          )
        }
      }
    }
    return oppdaterGrunnlagDtoListe
  }


  fun oppdaterSkattegrunnlag(
    grunnlagspakkeId: Int,
    personIdOgPeriodeListe: List<PersonIdOgPeriodeRequest>,
    timestampOppdatering: LocalDateTime,
    forekomsterFunnet: Boolean
  ): List<OppdaterGrunnlagDto> {

    val oppdaterGrunnlagDtoListe = mutableListOf<OppdaterGrunnlagDto>()

    personIdOgPeriodeListe.forEach { personIdOgPeriode ->

      var inntektAar = personIdOgPeriode.periodeFra.year
      val sluttAar = personIdOgPeriode.periodeTil.year

      val periodeFra = LocalDate.of(inntektAar, 1, 1)
      val periodeTil = LocalDate.of(sluttAar, 1, 1)

      val nyeSkattegrunnlag =
        mutableListOf<PeriodComparable<SkattegrunnlagBo, SkattegrunnlagspostBo>>()

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
              val skattegrunnlag = SkattegrunnlagBo(
                grunnlagspakkeId = grunnlagspakkeId,
                personId = personIdOgPeriode.personId,
                periodeFra = LocalDate.parse("$inntektAar-01-01"),
                periodeTil = LocalDate.parse("$inntektAar-01-01").plusYears(1),
                brukFra = timestampOppdatering,
                hentetTidspunkt = timestampOppdatering
              )
              val skattegrunnlagsposter = mutableListOf<SkattegrunnlagspostBo>()
              skattegrunnlagsPosterOrdinaer.forEach { skattegrunnlagsPost ->
                antallSkattegrunnlagsposter++
                skattegrunnlagsposter.add(
                  SkattegrunnlagspostBo(
//                    skattegrunnlagId = skattegrunnlag.skattegrunnlagId,
                    skattegrunnlagType = SkattegrunnlagType.ORDINAER.toString(),
                    inntektType = skattegrunnlagsPost.tekniskNavn,
                    belop = BigDecimal(skattegrunnlagsPost.beloep),
                  )
                )
              }
              skattegrunnlagsPosterSvalbard.forEach { skattegrunnlagsPost ->
                antallSkattegrunnlagsposter++
                skattegrunnlagsposter.add(
                  SkattegrunnlagspostBo(
//                    skattegrunnlagId = skattegrunnlag.skattegrunnlagId,
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
              personIdOgPeriode.personId,
              timestampOppdatering
            )
            oppdaterGrunnlagDtoListe.add(
              OppdaterGrunnlagDto(
                GrunnlagRequestType.SKATTEGRUNNLAG,
                personIdOgPeriode.personId,
                GrunnlagsRequestStatus.HENTET,
                "Antall skattegrunnlagsposter funnet for innteksåret ${inntektAar}: $antallSkattegrunnlagsposter"
              )
            )
            if (antallSkattegrunnlagsposter > 0) {
              forekomsterFunnet
            }
          }
          is RestResponse.Failure -> oppdaterGrunnlagDtoListe.add(
            OppdaterGrunnlagDto(
              GrunnlagRequestType.SKATTEGRUNNLAG,
              personIdOgPeriode.personId,
              if (restResponseSkattegrunnlag.statusCode == HttpStatus.NOT_FOUND) GrunnlagsRequestStatus.IKKE_FUNNET else GrunnlagsRequestStatus.FEILET,
              "Feil ved henting av skattegrunnlag for inntektsåret ${inntektAar}."
            )
          )
        }
        inntektAar++
      }
    }
    return oppdaterGrunnlagDtoListe
  }


  fun oppdaterUtvidetBarnetrygdOgSmaabarnstillegg(
    grunnlagspakkeId: Int,
    personIdOgPeriodeListe: List<PersonIdOgPeriodeRequest>,
    timestampOppdatering: LocalDateTime,
    forekomsterFunnet: Boolean
  ): List<OppdaterGrunnlagDto> {

    val oppdaterGrunnlagDtoListe = mutableListOf<OppdaterGrunnlagDto>()

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

          if (familieBaSakResponse.perioder.isNotEmpty()) {
            persistenceService.oppdaterEksisterendeUtvidetBarnetrygOgSmaabarnstilleggTilInaktiv(
              grunnlagspakkeId,
              personIdOgPeriode.personId,
              timestampOppdatering
            )
            familieBaSakResponse.perioder.forEach { ubst ->
              if (LocalDate.parse(ubst.fomMåned.toString() + "-01").isBefore(personIdOgPeriode.periodeTil)) {
                antallPerioderFunnet++
                persistenceService.opprettUtvidetBarnetrygdOgSmaabarnstillegg(
                  UtvidetBarnetrygdOgSmaabarnstilleggBo(
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
          }
          oppdaterGrunnlagDtoListe.add(
            OppdaterGrunnlagDto(
              GrunnlagRequestType.UTVIDET_BARNETRYGD_OG_SMAABARNSTILLEGG,
              personIdOgPeriode.personId,
              GrunnlagsRequestStatus.HENTET,
              "Antall perioder funnet: $antallPerioderFunnet"
            )
          )
          if (antallPerioderFunnet > 0) {
            forekomsterFunnet
          }
        }
        is RestResponse.Failure -> oppdaterGrunnlagDtoListe.add(
          OppdaterGrunnlagDto(
            GrunnlagRequestType.UTVIDET_BARNETRYGD_OG_SMAABARNSTILLEGG,
            personIdOgPeriode.personId,
            if (restResponseFamilieBaSak.statusCode == HttpStatus.NOT_FOUND) GrunnlagsRequestStatus.IKKE_FUNNET else GrunnlagsRequestStatus.FEILET,
            "Feil ved henting av familie-ba-sak for perioden: ${personIdOgPeriode.periodeFra} - ${personIdOgPeriode.periodeTil}."
          )
        )
      }
    }
    return oppdaterGrunnlagDtoListe
  }


  fun oppdaterBarnetillegg(
    grunnlagspakkeId: Int,
    personIdOgPeriodeListe: List<PersonIdOgPeriodeRequest>,
    timestampOppdatering: LocalDateTime,
    forekomsterFunnet: Boolean
  ): List<OppdaterGrunnlagDto> {

    val oppdaterGrunnlagDtoListe = mutableListOf<OppdaterGrunnlagDto>()

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

          if ((barnetilleggPensjonResponse.barnetilleggPensjonListe != null) && (barnetilleggPensjonResponse.barnetilleggPensjonListe.isNotEmpty())) {
            persistenceService.oppdaterEksisterendeBarnetilleggPensjonTilInaktiv(
              grunnlagspakkeId,
              personIdOgPeriode.personId,
              timestampOppdatering
            )
            barnetilleggPensjonResponse.barnetilleggPensjonListe.forEach { bt ->
              if (bt.fom.isBefore(personIdOgPeriode.periodeTil)) {
                antallPerioderFunnet++
                persistenceService.opprettBarnetillegg(
                  BarnetilleggBo(
                    grunnlagspakkeId = grunnlagspakkeId,
                    partPersonId = personIdOgPeriode.personId,
                    barnPersonId = bt.barn,
                    barnetilleggType = BarnetilleggType.PENSJON.toString(),
                    periodeFra = bt.fom,
                    // justerer frem tildato med én måned for å ha lik logikk som resten av appen. Tildato skal angis som til, men ikke inkludert, måned.
                    periodeTil = if (bt.tom != null) bt.tom.plusMonths(1).withDayOfMonth(1) else null,
                    aktiv = true,
                    brukFra = timestampOppdatering,
                    brukTil = null,
                    belopBrutto = bt.beloep,
                    barnType = if (bt.erFellesbarn) BarnType.FELLES.toString() else BarnType.SÆRKULL.toString(),
                    hentetTidspunkt = timestampOppdatering
                  )
                )
              }
            }
          }
          oppdaterGrunnlagDtoListe.add(
            OppdaterGrunnlagDto(
              GrunnlagRequestType.BARNETILLEGG,
              personIdOgPeriode.personId,
              GrunnlagsRequestStatus.HENTET,
              "Antall perioder funnet: $antallPerioderFunnet"
            )
          )
          if (antallPerioderFunnet > 0) {
            forekomsterFunnet
          }
        }
        is RestResponse.Failure -> oppdaterGrunnlagDtoListe.add(
          OppdaterGrunnlagDto(
            GrunnlagRequestType.BARNETILLEGG,
            personIdOgPeriode.personId,
            if (restResponseBarnetilleggPensjon.statusCode == HttpStatus.NOT_FOUND) GrunnlagsRequestStatus.IKKE_FUNNET else GrunnlagsRequestStatus.FEILET,
            "Feil ved henting av barnetillegg pensjon for perioden: ${personIdOgPeriode.periodeFra} - ${personIdOgPeriode.periodeTil}."
          )
        )
      }
    }
    return oppdaterGrunnlagDtoListe
  }


  fun oppdaterKontantstotte(
    grunnlagspakkeId: Int,
    personIdOgPeriodeListe: List<PersonIdOgPeriodeRequest>,
    timestampOppdatering: LocalDateTime,
    forekomsterFunnet: Boolean
  ): List<OppdaterGrunnlagDto> {

    val oppdaterGrunnlagDtoListe = mutableListOf<OppdaterGrunnlagDto>()

    personIdOgPeriodeListe.forEach { personIdOgPeriode ->

      var antallPerioderFunnet = 0

      // Input til tjeneste er en liste over alle personnr for en person,
      // kall PDL for å hente historikk på fnr?
      val innsynRequestListe = mutableListOf<String>()

      innsynRequestListe.add(
        personIdOgPeriode.personId)

      val kontantstotteRequest = InnsynRequest(
        innsynRequestListe)

      LOGGER.info(
        "Kaller kontantstøtte med personIdent ********${
          kontantstotteRequest.fnr[0].substring(
            IntRange(8, 10)
          )
        } "
      )

      when (val restResponseKontantstotte =
        kontantstotteConsumer.hentKontantstotte(kontantstotteRequest)) {
        is RestResponse.Success -> {
          val kontantstotteResponse = restResponseKontantstotte.body
          LOGGER.info("kontantstotte ga følgende respons: $kontantstotteResponse")

//          if (kontantstotteResponse.data.isNotEmpty()) {
/*            persistenceService.oppdaterEksisterendeKontantstotteTilInaktiv(
              grunnlagspakkeId,
              personIdOgPeriode.personId,
              timestampOppdatering
            )*/
/*            kontantstotteResponse.data.forEach { ks ->
              if (LocalDate.parse(ks.utbetalinger.toString() + "-01").isBefore(personIdOgPeriode.periodeTil)) {
                antallPerioderFunnet++
                persistenceService.opprettUtvidetBarnetrygdOgSmaabarnstillegg(
                  UtvidetBarnetrygdOgSmaabarnstilleggBo(
                    grunnlagspakkeId = grunnlagspakkeId,
                    personId = personIdOgPeriode.personId,
                    type = ks.stønadstype.toString(),
                    periodeFra = LocalDate.parse(ks.fomMåned.toString() + "-01"),
                    // justerer frem tildato med én måned for å ha lik logikk som resten av appen. Tildato skal angis som til, men ikke inkludert, måned.
                    periodeTil = if (ks.tomMåned != null) LocalDate.parse(ks.tomMåned.toString() + "-01")
                      .plusMonths(1) else null,
                    brukFra = timestampOppdatering,
                    belop = BigDecimal.valueOf(ks.beløp),
                    manueltBeregnet = ks.manueltBeregnet,
                    deltBosted = ks.deltBosted,
                    hentetTidspunkt = timestampOppdatering
                  )
                )
              }
            }*/
//          }
          oppdaterGrunnlagDtoListe.add(
            OppdaterGrunnlagDto(
              GrunnlagRequestType.KONTANTSTOTTE,
              personIdOgPeriode.personId,
              GrunnlagsRequestStatus.HENTET,
              "Antall perioder funnet: $antallPerioderFunnet"
            )
          )
          if (antallPerioderFunnet > 0) {
            forekomsterFunnet
          }
        }
        is RestResponse.Failure -> oppdaterGrunnlagDtoListe.add(
          OppdaterGrunnlagDto(
            GrunnlagRequestType.KONTANTSTOTTE,
            personIdOgPeriode.personId,
            if (restResponseKontantstotte.statusCode == HttpStatus.NOT_FOUND) GrunnlagsRequestStatus.IKKE_FUNNET else GrunnlagsRequestStatus.FEILET,
            "Feil ved henting av kontantstøtte for perioden: ${personIdOgPeriode.periodeFra} - ${personIdOgPeriode.periodeTil}."
          )
        )
      }
    }
    return oppdaterGrunnlagDtoListe
  }



  fun hentGrunnlagspakke(grunnlagspakkeId: Int): HentGrunnlagspakkeDto {
    // Validerer at grunnlagspakke eksisterer
    persistenceService.validerGrunnlagspakke(grunnlagspakkeId)
    return persistenceService.hentGrunnlagspakke(grunnlagspakkeId)
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

data class PersonIdOgPeriodeRequest(
  val personId: String,
  val periodeFra: LocalDate,
  val periodeTil: LocalDate
)
