package no.nav.bidrag.grunnlag.validation

import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Constraint(validatedBy = [DateValidator::class])
annotation class DateValid (
  val message: String = "Ugyldig datoformat. Dato må være på formatet YYYY-MM-DD.",
  val format: String = "yyyy-MM-dd",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = []
)