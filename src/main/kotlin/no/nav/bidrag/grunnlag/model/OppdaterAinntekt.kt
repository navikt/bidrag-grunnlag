package no.nav.bidrag.grunnlag.model

import no.nav.bidrag.behandling.felles.dto.grunnlag.OppdaterGrunnlagDto
import no.nav.bidrag.behandling.felles.enums.Formaal
import no.nav.bidrag.behandling.felles.enums.GrunnlagRequestType
import no.nav.bidrag.behandling.felles.enums.GrunnlagsRequestStatus
import no.nav.bidrag.grunnlag.bo.AinntektBo
import no.nav.bidrag.grunnlag.bo.AinntektspostBo
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
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.service.PersistenceService
import no.nav.bidrag.grunnlag.service.PersonIdOgPeriodeRequest
import no.nav.tjenester.aordningen.inntektsinformasjon.response.HentInntektListeResponse
import no.nav.tjenester.aordningen.inntektsinformasjon.tilleggsinformasjondetaljer.Etterbetalingsperiode
import no.nav.tjenester.aordningen.inntektsinformasjon.tilleggsinformasjondetaljer.TilleggsinformasjonDetaljerType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import java.time.LocalDate
import java.time.LocalDateTime

class OppdaterAinntekt(
  private val grunnlagspakkeId: Int,
  private val timestampOppdatering: LocalDateTime,
  private val persistenceService: PersistenceService,
  private val bidragGcpProxyConsumer: BidragGcpProxyConsumer
) : MutableList<OppdaterGrunnlagDto> by mutableListOf() {

  companion object {
    @JvmStatic
    val LOGGER: Logger = LoggerFactory.getLogger(OppdaterAinntekt::class.java)

    const val BIDRAG_FILTER = "BidragA-Inntekt"
    const val FORSKUDD_FILTER = "BidragsforskuddA-Inntekt"
    const val BIDRAG_FORMAAL = "Bidrag"
    const val FORSKUDD_FORMAAL = "Bidragsforskudd"
  }

  fun oppdaterAinntekt(ainntektRequestListe: List<PersonIdOgPeriodeRequest>): OppdaterAinntekt {
    val formaal = persistenceService.hentFormaalGrunnlagspakke(grunnlagspakkeId)

    ainntektRequestListe.forEach { personIdOgPeriode ->

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
            this.add(
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
            this.add(
              OppdaterGrunnlagDto(
                GrunnlagRequestType.AINNTEKT,
                personIdOgPeriode.personId,
                GrunnlagsRequestStatus.HENTET,
                "Antall inntekter funnet (periode ${personIdOgPeriode.periodeFra} - ${personIdOgPeriode.periodeTil}): $antallPerioderFunnet",
              )
            )
          }
        }

        is RestResponse.Failure -> {
          this.add(
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
    return this
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
            inntekt.opplysningspliktig?.identifikator,
            inntekt.opplysningspliktig?.aktoerType.toString()
          ),
          virksomhet = VirksomhetIntern(
            inntekt.virksomhet?.identifikator,
            inntekt.virksomhet?.aktoerType.toString()
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