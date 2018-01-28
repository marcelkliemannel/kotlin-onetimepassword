package com.marcelkliemannel.kotlinonetimepassword

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class RandomSecretGeneratorTest {
  @Test
  @DisplayName("Same secret length as the HMAC algorithm hash")
  fun expectedHmacAlgorithmHashLength() {
    HmacAlgorithm.values().forEach {
      val randomSecret = RandomSecretGenerator().createRandomSecret(it)
      Assertions.assertEquals(it.hashBytes, randomSecret.size)
    }
  }
}