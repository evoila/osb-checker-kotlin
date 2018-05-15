package de.evoila.osb.checker.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TestConfig {

  @Bean
  fun configuration(): de.evoila.osb.checker.config.Configuration {
    return de.evoila.osb.checker.config.Configuration()
  }
}