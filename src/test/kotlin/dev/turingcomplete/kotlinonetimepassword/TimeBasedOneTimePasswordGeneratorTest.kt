package dev.turingcomplete.kotlinonetimepassword

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvFileSource
import org.junit.jupiter.params.provider.CsvSource
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class TimeBasedOneTimePasswordGeneratorTest {
  @Test
  @DisplayName("Edge case: 0 code digits")
  fun testZeroCodeDigitsTest() {
    val hmacAlgorithm = HmacAlgorithm.SHA512
    val config = TimeBasedOneTimePasswordConfig(42, TimeUnit.HOURS, 0, hmacAlgorithm)
    val secret = "Leia".toByteArray()
    val timeBasedOneTimePasswordGenerator = TimeBasedOneTimePasswordGenerator(secret, config)

    Assertions.assertEquals(0, timeBasedOneTimePasswordGenerator.generate(Date(12345)).length)
  }

  @Test
  fun testCodeValidation() {
    val hmacAlgorithm = HmacAlgorithm.SHA512
    val config = TimeBasedOneTimePasswordConfig(30, TimeUnit.SECONDS, 9, hmacAlgorithm)
    val secret = "Leia".toByteArray()
    val timeBasedOneTimePasswordGenerator = TimeBasedOneTimePasswordGenerator(secret, config)

    val firstTimestamp = 1593727260000 // 2020/07/03 00:01:00
    val code = timeBasedOneTimePasswordGenerator.generate(Date(firstTimestamp))
    val secondTimestamp = 1593727289000 // 2020/07/03 00:01:29
    Assertions.assertTrue(timeBasedOneTimePasswordGenerator.isValid(code, Date(secondTimestamp)))
    val thirdTimestamp = 1593727290000 // 2020/07/03 00:01:30
    Assertions.assertFalse(timeBasedOneTimePasswordGenerator.isValid(code, Date(thirdTimestamp)))
  }

  @Test
  @DisplayName("Zero time step values")
  fun testZeroTimeStep() {
    val hmacAlgorithm = HmacAlgorithm.SHA1
    val secret = "Leia".toByteArray()

    val zeroConfig = TimeBasedOneTimePasswordConfig(0, TimeUnit.MINUTES, 6, hmacAlgorithm)
    val zeroTimeBasedOneTimePasswordGenerator = TimeBasedOneTimePasswordGenerator(secret, zeroConfig)
    Assertions.assertEquals("527464", zeroTimeBasedOneTimePasswordGenerator.generate(Date(12334532445)))
  }

  @Test
  @DisplayName("Negative and zero timestamp values")
  fun testZeroAndNegativeTimestamp() {
    val hmacAlgorithm = HmacAlgorithm.SHA1
    val secret = "Leia".toByteArray()
    val zeroConfig = TimeBasedOneTimePasswordConfig(30, TimeUnit.MINUTES, 6, hmacAlgorithm)
    val zeroTimeBasedOneTimePasswordGenerator = TimeBasedOneTimePasswordGenerator(secret, zeroConfig)

    Assertions.assertEquals("527464", zeroTimeBasedOneTimePasswordGenerator.generate(Date(0)))
    Assertions.assertEquals("630888", zeroTimeBasedOneTimePasswordGenerator.generate(Date(-22334579403)))
  }

  @ParameterizedTest(name = "Timestamp: {0}, expected code: {1}")
  @DisplayName("Test timestamps that are larger than 32-bit")
  @CsvSource(value = ["11111111111, 4948126", "123456789012, 0568513", "6592756306835, 7911959", "${Long.MAX_VALUE}, 3749332"])
  fun testTimestampsThatAreLargerThen32Bit(timestamp: Long, expectedCode: String) {
    val hmacAlgorithm = HmacAlgorithm.SHA256
    validateWithExpectedCode(hmacAlgorithm, 7, Date(timestamp), 1, TimeUnit.DAYS, expectedCode, "Leia")
  }

  @ParameterizedTest
  @DisplayName("Multiple algorithms, code digits, timestamps, sects, time steps and time step units")
  @CsvFileSource(resources = ["/dev/turingcomplete/multipleTimeBasedOneTimePasswordTestVectors.csv"])
  fun testGeneratedCodes(hmacAlgorithmName: String, codeDigits: Int, timestamp: Long, expectedCode: String,
                         secret: String, timeStep: Long, timeStepUnit: String) {

    validateWithExpectedCode(HmacAlgorithm.valueOf(hmacAlgorithmName), codeDigits, Date(timestamp),
                             timeStep, TimeUnit.valueOf(timeStepUnit), expectedCode, secret)
  }

  @ParameterizedTest(name = "{0}, timestamp: {1}, expected code: {2}")
  @DisplayName("RFC 6238 Appendix B Test Vectors")
  @CsvFileSource(resources = ["/dev/turingcomplete/rfc6238AppendixBTimeBasedOneTimePasswordTestVectors.csv"])
  fun testRfc6238AppendixDTestCases(hmacAlgorithmName: String, time: String, expectedCode: String, secret: String) {
    val timestamp = SimpleDateFormat("yyy-MM-dd HH:mm:ss").apply {
      timeZone = TimeZone.getTimeZone("UTC")
    }.parse(time)
    validateWithExpectedCode(HmacAlgorithm.valueOf(hmacAlgorithmName), 8, timestamp,
                             30, TimeUnit.SECONDS, expectedCode, secret)
  }

  private fun validateWithExpectedCode(hmacAlgorithm:
                                       HmacAlgorithm,
                                       codeDigits: Int,
                                       timestamp: Date,
                                       timeStep: Long,
                                       timeStepUnit: TimeUnit,
                                       expectedCode: String,
                                       secret: String) {

    val config = TimeBasedOneTimePasswordConfig(timeStep, timeStepUnit, codeDigits, hmacAlgorithm)
    val timeBasedOneTimePasswordGenerator = TimeBasedOneTimePasswordGenerator(secret.toByteArray(), config)

    Assertions.assertEquals(expectedCode, timeBasedOneTimePasswordGenerator.generate(timestamp))
    Assertions.assertTrue(timeBasedOneTimePasswordGenerator.isValid(expectedCode, timestamp))
  }
}