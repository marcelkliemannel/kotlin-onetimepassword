package dev.turingcomplete.kotlinonetimepassword

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvFileSource
import org.junit.jupiter.params.provider.CsvSource
import java.util.*
import java.util.concurrent.TimeUnit

class TimeBasedOneTimePasswordGeneratorTest {
  @Test
  @DisplayName("Edge case: 0 code digits")
  fun zeroCodeDigitsTest() {
    val hmacAlgorithm = HmacAlgorithm.SHA512
    val config = TimeBasedOneTimePasswordConfig(42, TimeUnit.HOURS, 0, hmacAlgorithm)
    val secret = "Leia".toByteArray()
    val timeBasedOneTimePasswordGenerator = TimeBasedOneTimePasswordGenerator(secret, config)

    Assertions.assertEquals(0, timeBasedOneTimePasswordGenerator.generate(Date(12345)).length)
  }

  @Test
  @DisplayName("Negative and zero time step values")
  fun zeroAndNegativeTimeStep() {
    val hmacAlgorithm = HmacAlgorithm.SHA1
    val secret = "Leia".toByteArray()

    val zeroConfig = TimeBasedOneTimePasswordConfig(0, TimeUnit.MINUTES, 6, hmacAlgorithm)
    val zeroTimeBasedOneTimePasswordGenerator = TimeBasedOneTimePasswordGenerator(secret, zeroConfig)
    Assertions.assertEquals("527464", zeroTimeBasedOneTimePasswordGenerator.generate(Date(12334532445)))

    val negativeConfig = TimeBasedOneTimePasswordConfig(-12334532445, TimeUnit.MINUTES, 6, hmacAlgorithm)
    val negativeTimeBasedOneTimePasswordGenerator = TimeBasedOneTimePasswordGenerator(secret, negativeConfig)
    Assertions.assertEquals("527464", negativeTimeBasedOneTimePasswordGenerator.generate(Date(14345)))
  }

  @Test
  @DisplayName("Negative and zero timestamp values")
  fun zeroAndNegativeTimestamp() {
    val hmacAlgorithm = HmacAlgorithm.SHA1
    val secret = "Leia".toByteArray()
    val zeroConfig = TimeBasedOneTimePasswordConfig(30, TimeUnit.MINUTES, 6, hmacAlgorithm)
    val zeroTimeBasedOneTimePasswordGenerator = TimeBasedOneTimePasswordGenerator(secret, zeroConfig)

    Assertions.assertEquals("527464", zeroTimeBasedOneTimePasswordGenerator.generate(Date(0)))
    Assertions.assertEquals("194044", zeroTimeBasedOneTimePasswordGenerator.generate(Date(-22334579403)))
  }

  @ParameterizedTest(name = "Timestamp: {0}, expected code: {1}")
  @DisplayName("Ensure dates larger then 31-bit")
  @CsvSource(value = ["11111111111, 4948126", "123456789012, 0568513", "6592756306835, 7911959", "${Long.MAX_VALUE}, 3749332"])
  fun ensureDatesLargerThen32Bit(timestamp: Long, expectedCode: String) {
    val hmacAlgorithm = HmacAlgorithm.SHA256
    validateWithExpectedCode(hmacAlgorithm, 7, Date(timestamp), 1, TimeUnit.DAYS, expectedCode, "Leia")
  }

  @ParameterizedTest
  @DisplayName("Multiple algorithms, code digits, timestamps, sects, time steps and time step units")
  @CsvFileSource(resources = ["/dev/turingcomplete/multipleTimeBasedOneTimePasswordTestVectors.csv"])
  fun multipleTestVectors(hmacAlgorithmName: String, codeDigits: Int, timestamp: Long, expectedCode: String,
                          secret: String, timeStep: Long, timeStepUnit: String) {

    validateWithExpectedCode(HmacAlgorithm.valueOf(hmacAlgorithmName), codeDigits, Date(timestamp),
                             timeStep, TimeUnit.valueOf(timeStepUnit), expectedCode, secret)
  }

  @ParameterizedTest(name = "{0}, timestamp: {1}, expected code: {2}")
  @DisplayName("RFC 6238 Appendix B Test Vectors")
  @CsvFileSource(resources = ["/dev/turingcomplete/rfc6238AppendixBTimeBasedOneTimePasswordTestVectors.csv"])
  fun rfc4226AppendixDTestCases(hmacAlgorithmName: String, timestamp: Long, expectedCode: String, secret: String) {
    val timestampDate = Date(timestamp.times(1000))  // The times in original test vectors are in seconds.
    validateWithExpectedCode(HmacAlgorithm.valueOf(hmacAlgorithmName), 8, timestampDate, 30, TimeUnit.SECONDS, expectedCode, secret)
  }

  private fun validateWithExpectedCode(hmacAlgorithm: HmacAlgorithm, codeDigits: Int, timestamp: Date, timeStep: Long,
                                       timeStepUnit: TimeUnit, expectedCode: String, secret: String) {

    val config = TimeBasedOneTimePasswordConfig(timeStep, timeStepUnit, codeDigits, hmacAlgorithm)
    val timeBasedOneTimePasswordGenerator = TimeBasedOneTimePasswordGenerator(secret.toByteArray(), config)

    Assertions.assertEquals(expectedCode, timeBasedOneTimePasswordGenerator.generate(timestamp))
    Assertions.assertTrue(timeBasedOneTimePasswordGenerator.isValid(expectedCode, timestamp))
  }
}