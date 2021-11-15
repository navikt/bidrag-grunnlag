/*
package no.nav.bidrag.grunnlag

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JacksonConfig {

  @Bean
  fun objectMapper(): ObjectMapper {
    val objectMapper = ObjectMapper()
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    return ObjectMapper().findAndRegisterModules()
  }
}*/
