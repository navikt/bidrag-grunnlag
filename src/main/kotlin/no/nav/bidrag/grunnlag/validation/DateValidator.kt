package no.nav.bidrag.grunnlag.validation

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext


class DateValidator : ConstraintValidator<DateValid, String> {

  private lateinit var dateFormat: String

  override fun initialize(constraintAnnotation: DateValid) {
    super.initialize(constraintAnnotation)
    dateFormat = constraintAnnotation.format
  }

  override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
    val formatter = DateTimeFormatter.ofPattern(this.dateFormat)
    try {
      LocalDate.parse(value, formatter)
    } catch (e: DateTimeParseException) {
      return false
    }
    return true
  }
}
