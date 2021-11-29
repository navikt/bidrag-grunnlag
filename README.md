# Bidrag Grunnlag

![](https://github.com/navikt/bidrag-grunnlag/workflows/continuous%20integration/badge.svg)
![](https://github.com/navikt/bidrag-grunnlag/workflows/release%20bidrag-grunnlag/badge.svg)

Tjeneste for innhenting og behandling av grunnlag i bidragssaker.


## Kjøre applikasjon lokalt
En fullstendig fungerende applikasjon kan for øyeblikket ikke kjøres opp lokalt på egen maskin da vi ikke har mulighet til å kommunisere med eksterne tjenester. Applikasjonen kan allikevel kjøres opp for å teste endepunkter fra Swagger ([http://localhost:8080/bidrag-grunnlag](http://localhost:8080/bidrag-grunnlag)) og annen logikk i applikasjonen som er uavhengig av kontakt med eksterne tjenester. Operasjoner som går rett mot databasen, som opprettelse og henting av grunnlagspakker, vil også fungere ved hjelp av in-memory databasen H2.

For å starte applikasjonen kjører man `main`-metoden i fila `BidragGrunnlagLocal.kt` med profilen `local`.

Også når man kjører applikasjonen lokalt vil man trenge et gyldig JWT-token for å kunne kalle på endepunktene. For å utstede et slikt token kan man benytte det åpne endepunktet `GET /local/cookie/` med `issuerId=aad` og `audience=aud-localhost`. Her benyttes en "fake" token-issuer som er satt med wiremock ved hjelp av annotasjonen: `@EnableMockOAuth2Server` fra NAV-biblioteket `token-support`.

Kan vurdere å sette opp wiremocks for de eksterne tjenestene for å kunne kjøre opp en mer fullstedig applikasjon i fremtiden.
