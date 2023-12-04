package no.nav.bidrag.grunnlag.exception.custom

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.transport.behandling.grunnlag.request.OpprettGrunnlagspakkeRequestDto
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException

private val objectmapper = ObjectMapper().findAndRegisterModules()
fun OpprettGrunnlagspakkeRequestDto.manglerOpprettetAv(): Nothing =
    throw HttpClientErrorException(HttpStatus.BAD_REQUEST, "Forespørsel mangler informasjon om hvem som forsøker å opprette grunnlagspakken", objectmapper.writeValueAsBytes(this.copy(opprettetAv = "Opprettet av kan ikke være tom. Må være null eller satt til en verdi")), null)
