package com.marcelkliemannel.kotlinonetimepassword

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvFileSource
import java.util.*

class GoogleAuthenticatorTest {
  @ParameterizedTest(name = "Timestamp: {0}, expected code: {1}")
  @DisplayName("Multiple Test Vectors")
  @CsvFileSource(resources = ["/googleAuthenticatorTestVectors.csv"])
  fun zeroCodeDigitsTest(timestamp: Long, expectedCode: String) {
    val googleAuthenticator = GoogleAuthenticator("Leia")
    Assertions.assertEquals(expectedCode, googleAuthenticator.generate(Date(timestamp)))
    Assertions.assertTrue(googleAuthenticator.isValid(expectedCode, Date(timestamp)))
  }

  @Test
  @DisplayName("16 Bytes generated secret")
  fun generatedSecretExact16Bytes() {
    val googleAuthenticatorRandomSecret = GoogleAuthenticator.createRandomSecret()
    Assertions.assertEquals(16, googleAuthenticatorRandomSecret.toByteArray().size)
  }
}