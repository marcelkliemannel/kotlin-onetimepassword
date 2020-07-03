package dev.turingcomplete.kotlinonetimepassword

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvFileSource
import org.junit.jupiter.params.provider.CsvSource

class HmacOneTimePasswordGeneratorTest {
  @Test
  @DisplayName("Edge case: 0 code digits")
  fun testZeroCodeDigits() {
    val config = HmacOneTimePasswordConfig(0, HmacAlgorithm.SHA1)
    val hmacOneTimePasswordGenerator = HmacOneTimePasswordGenerator("Leia".toByteArray(), config)

    Assertions.assertEquals(0, hmacOneTimePasswordGenerator.generate(42).length)
  }

  @Test
  @DisplayName("Zero counter value")
  fun testZeroAndCounterValue() {
    val config = HmacOneTimePasswordConfig(8, HmacAlgorithm.SHA1)
    val hmacOneTimePasswordGenerator = HmacOneTimePasswordGenerator("Leia".toByteArray(), config)

    Assertions.assertEquals("67527464", hmacOneTimePasswordGenerator.generate(0))
  }

  @ParameterizedTest(name = "{0}, code digits: {1}, counter: {2}, code: {3}, secret: {4}")
  @DisplayName("Multiple algorithms, code digits, counter and secrets")
  @CsvFileSource(resources = ["/dev/turingcomplete/multipleHmacOneTimePasswordTestVectors.csv"])
  fun testGeneratedCodes(hmacAlgorithm: String, codeDigits: Int, counter: Long, expectedCode: String, secret: String) {
    validateWithExpectedCode(counter, expectedCode, codeDigits, secret, HmacAlgorithm.valueOf(hmacAlgorithm))
  }

  @ParameterizedTest(name = "Counter: {0}, code: {1}")
  @DisplayName("RFC 4226 appendix D test vectors")
  @CsvSource(value = [
    "0, 755224", "1, 287082", "2, 359152", "3, 969429", "4, 338314",
    "5, 254676", "6, 287922", "7, 162583", "8, 399871", "9, 520489"
  ])
  fun testRfc4226AppendixDTestCases(counter: Long, code: String) {
    validateWithExpectedCode(counter, code, 6, "12345678901234567890", HmacAlgorithm.SHA1)
  }

  private fun validateWithExpectedCode(counter: Long, expectedCode: String, codeDigits: Int, secret: String, hmacAlgorithm: HmacAlgorithm) {
    val config = HmacOneTimePasswordConfig(codeDigits, hmacAlgorithm)
    val hmacOneTimePasswordGenerator = HmacOneTimePasswordGenerator(secret.toByteArray(), config)

    Assertions.assertEquals(expectedCode, hmacOneTimePasswordGenerator.generate(counter))
    Assertions.assertTrue(hmacOneTimePasswordGenerator.isValid(expectedCode, counter))
  }
}