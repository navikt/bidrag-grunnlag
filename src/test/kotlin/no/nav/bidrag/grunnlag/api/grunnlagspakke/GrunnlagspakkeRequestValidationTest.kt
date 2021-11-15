package no.nav.bidrag.grunnlag.api.grunnlagspakke

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate
import javax.validation.ConstraintViolation
import javax.validation.Validation
import javax.validation.Validator


class GrunnlagspakkeRequestValidationTest {

  private val validator: Validator  = Validation.buildDefaultValidatorFactory().validator

  @Test
  fun `skal validere OpprettGrunnlagspakkeRequest`() {
    var opprettGrunnlagspakkeRequest = OpprettGrunnlagspakkeRequest("Saksbehandler1")
    var violations: Set<ConstraintViolation<OpprettGrunnlagspakkeRequest>> = validator.validate(opprettGrunnlagspakkeRequest)

    assertEquals(0, violations.size)

    opprettGrunnlagspakkeRequest = OpprettGrunnlagspakkeRequest("")
    violations = validator.validate(opprettGrunnlagspakkeRequest)

    assertEquals(1, violations.size)

    opprettGrunnlagspakkeRequest = OpprettGrunnlagspakkeRequest("  ")
    violations = validator.validate(opprettGrunnlagspakkeRequest)

    assertEquals(1, violations.size)
  }

  @Test
  fun `skal validere OppdaterGrunnlagspakkeRequest`() {
    var oppdaterGrunnlagspakkeRequest = OppdaterGrunnlagspakkeRequest(0, "", LocalDate.of(2022, 1, 21), emptyList())
    var violations: Set<ConstraintViolation<OppdaterGrunnlagspakkeRequest>> = validator.validate(oppdaterGrunnlagspakkeRequest)

    // Les inn fil med request-data (json)

    // Les inn fil med request-data (json)
//    try {
//      val json = Files.readString(Paths.get(filnavn))
//    } catch (e: Exception) {
//      Assertions.fail<Any>("Klarte ikke å lese fil: $filnavn")
//    }

    assertEquals(2, violations.size)

    val fileContent = GrunnlagspakkeRequestValidationTest::class.java.getResource("/requests/oppdaterGrunnlagspakke1.json").readText()
    println(fileContent)

    oppdaterGrunnlagspakkeRequest = ObjectMapper().registerKotlinModule().readValue(fileContent, OppdaterGrunnlagspakkeRequest::class.java)

    println(oppdaterGrunnlagspakkeRequest)



//    val testObject = JSONObject()
//    testObject.put("grunnlagspakkeId", 0)
//    testObject.put("formaal", "FORSKUDD")
//    testObject.put("gyldigTil", "21-01-2022")
//    testObject.put("grunnlagstypeRequestListe", listOf(GrunnlagstypeRequest()))
//
//    convertJsonFormat(testObject)
//
//
//    val oppdaterGrunnlagspakkeRequest = ObjectMapper().readValue(testObject.toString(), OppdaterGrunnlagspakkeRequest::class.java)
//
//    val violations = validator.validate(testObject.toString() as OppdaterGrunnlagspakkeRequest)
//
//    println(ObjectMapper().writeValueAsString(violations))


//    oppdaterGrunnlagspakkeRequest = OppdaterGrunnlagspakkeRequest(0, "FORSKUDD", "21-01-2022", listOf(GrunnlagstypeRequest()))
//    violations = validator.validate(oppdaterGrunnlagspakkeRequest)
//
//    assertEquals(1, violations.size)
//
//    oppdaterGrunnlagspakkeRequest = OppdaterGrunnlagspakkeRequest(0, "FORSKUDD", "2022-01-21", listOf(GrunnlagstypeRequest()))
//    violations = validator.validate(oppdaterGrunnlagspakkeRequest)
//
//    assertEquals(0, violations.size)

  }
}