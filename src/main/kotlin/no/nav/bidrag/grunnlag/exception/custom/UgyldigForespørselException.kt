package no.nav.bidrag.grunnlag.exception.custom

import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException

fun manglerOpprettetAv(): Nothing =
    throw HttpClientErrorException(HttpStatus.BAD_REQUEST, "Forespørsel mangler informasjon om hvem som forsøker å opprette grunnlagspakken")
