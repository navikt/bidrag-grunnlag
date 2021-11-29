# Bidrag Grunnlag

![](https://github.com/navikt/bidrag-grunnlag/workflows/continuous%20integration/badge.svg)
![](https://github.com/navikt/bidrag-grunnlag/workflows/release%20bidrag-grunnlag/badge.svg)

Tjeneste for innhenting av grunnlag i bidragssaker. Tjenesten er sentrert rundt begrepet `grunnlagspakke`, som fungerer som en beholder for alle grunnlag tilknyttet en bestemt bidragssak. Konsumenter av tjenesten kan opprette grunnlagspakker og bestemme hvilke grunnlag og perioder som skal hentes for de ulike partene. Tjenesten vil hente alle ønskede grunnlag og knytte de opp mot opprettet grunnlagspakke. Grunnlagspakken kan deretter hentes ut med alle tilhørende grunnlag. Frem til det er fattet et vedtak i en bidragssak tilknyttet en grunnlagspakke, kan alle grunnlagene oppdateres og/eller endres.

Støtter foreløpig følgende grunnlag:
* Inntekt
* Skattegrunnlag
* Utvidet barnetrygd og småbarnstillegg
* ... resterende grunnlag legges til fortløpende

Miljøer:
* GCP-DEB-FEATURE ([https://bidrag-grunnlag-feature.dev.intern.nav.no/bidrag-grunnlag/](https://bidrag-grunnlag-feature.dev.intern.nav.no/bidrag-grunnlag/))
* GCP-DEV ([https://bidrag-grunnlag.dev.intern.nav.no/bidrag-grunnlag/](https://bidrag-grunnlag.dev.intern.nav.no/bidrag-grunnlag/))
* GCP-PROD ([https://bidrag-grunnlag.intern.nav.no/bidrag-grunnlag/](https://bidrag-grunnlag.intern.nav.no/bidrag-grunnlag/))

## Kjøre applikasjon lokalt
En fullstendig fungerende applikasjon kan for øyeblikket ikke kjøres opp lokalt på egen maskin da vi ikke har mulighet til å kommunisere med eksterne tjenester. Applikasjonen kan allikevel kjøres opp for å teste endepunkter fra Swagger ([http://localhost:8080/bidrag-grunnlag](http://localhost:8080/bidrag-grunnlag)) og annen logikk i applikasjonen som er uavhengig av kontakt med eksterne tjenester. Operasjoner som går rett mot databasen, som opprettelse og henting av grunnlagspakker, vil også fungere ved hjelp av in-memory databasen H2.

For å starte applikasjonen kjører man `main`-metoden i fila `BidragGrunnlagLocal.kt` med profilen `local`.

Også når man kjører applikasjonen lokalt vil man trenge et gyldig JWT-token for å kunne kalle på endepunktene. For å utstede et slikt token kan man benytte det åpne endepunktet `GET /local/cookie/` med `issuerId=aad` og `audience=aud-localhost`. Her benyttes en "fake" token-issuer som er satt med wiremock ved hjelp av annotasjonen: `@EnableMockOAuth2Server` fra NAV-biblioteket `token-support`.

Kan vurdere å sette opp wiremocks for de eksterne tjenestene for å kunne kjøre opp en mer fullstedig applikasjon i fremtiden.
