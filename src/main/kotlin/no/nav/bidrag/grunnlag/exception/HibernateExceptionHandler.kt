package no.nav.bidrag.grunnlag.exception

import no.nav.bidrag.commons.ExceptionLogger
import org.hibernate.HibernateException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
@Component
class HibernateExceptionHandler(private val exceptionLogger: ExceptionLogger) {

    @ResponseBody
    @ExceptionHandler(HibernateException::class)
    protected fun handleHibernateException(e: HibernateException): ResponseEntity<*> {
        exceptionLogger.logException(e, "SqlExceptionHandler")
        return ResponseEntity("Feil ved kommunikasjon med databasen. ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
