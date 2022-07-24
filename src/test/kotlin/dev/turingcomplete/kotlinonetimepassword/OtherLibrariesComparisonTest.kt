package dev.turingcomplete.kotlinonetimepassword

import com.bastiaanjansen.otp.HMACAlgorithm
import com.bastiaanjansen.otp.TOTP
import com.j256.twofactorauth.TimeBasedOneTimePasswordUtil
import org.apache.commons.codec.binary.Base32
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.crypto.spec.SecretKeySpec

class OtherLibrariesComparisonTest {
  // -- Companion Object -------------------------------------------------------------------------------------------- //

  companion object {
    private val SECRET_PLAIN = "#ug0sEABk,}@anh&<ozWM6,#Nq/<NC3s"
    private val SECRET_BASE_32 = Base32().encodeToString(SECRET_PLAIN.toByteArray()).toString()
  }

  // -- Properties -------------------------------------------------------------------------------------------------- //
  // -- Initialization ---------------------------------------------------------------------------------------------- //
  // -- Exposed Methods --------------------------------------------------------------------------------------------- //

  @ParameterizedTest
  @DisplayName("com.eatthepath:java-otp (https://github.com/jchambers/java-otp)")
  @CsvSource(value = ["15, 6", "15, 8", "30, 6", "30, 8", "45, 6", "45, 8"])
  fun testComparetoEtthapath(timeStepSeconds: Long, digits: Int) {
    val currentTime = System.currentTimeMillis()
    val expectedCode = createExpectedCode(timeStepSeconds, digits, currentTime)

    // Library only supports 6-8 digits
    val eatthepath = com.eatthepath.otp.TimeBasedOneTimePasswordGenerator(Duration.ofSeconds(timeStepSeconds), digits, com.eatthepath.otp.TimeBasedOneTimePasswordGenerator.TOTP_ALGORITHM_HMAC_SHA1)
    val eatthepathCode = eatthepath.generateOneTimePasswordString(SecretKeySpec(SECRET_PLAIN.toByteArray(), "RAW"), Instant.ofEpochMilli(currentTime))
    Assertions.assertEquals(expectedCode, eatthepathCode)
  }

  @ParameterizedTest
  @DisplayName("com.github.bastiaanjansen:otp-java (https://github.com/BastiaanJansen/otp-java)")
  @CsvSource(value = ["15, 6", "15, 8", "30, 6", "30, 8", "45, 6", "45, 8"])
  fun testCompareToBastiaanJansen(timeStepSeconds: Long, digits: Int) {
    val currentTime = System.currentTimeMillis()
    val expectedCode = createExpectedCode(timeStepSeconds, digits, currentTime)

    // Library only supports 6-8 digits
    val bastiaanjansen = TOTP.Builder(SECRET_BASE_32.toByteArray()).withAlgorithm(HMACAlgorithm.SHA1).withPasswordLength(digits).withPeriod(Duration.ofSeconds(timeStepSeconds)).build()
    val bastiaanjansenCode = bastiaanjansen.at(Instant.ofEpochMilli(currentTime))
    Assertions.assertEquals(expectedCode, bastiaanjansenCode)
  }

  @ParameterizedTest
  @DisplayName("com.j256.two-factor-auth:two-factor-auth (https://github.com/j256/two-factor-auth)")
  @CsvSource(value = ["15, 6", "15, 8", "30, 6", "30, 8", "45, 6", "45, 8"])
  fun testCompareToJ256(timeStepSeconds: Long, digits: Int) {
    val currentTime = System.currentTimeMillis()
    val expectedCode = createExpectedCode(timeStepSeconds, digits, currentTime)

    val j256Code = TimeBasedOneTimePasswordUtil.generateNumberString(SECRET_BASE_32, System.currentTimeMillis(), timeStepSeconds.toInt(), digits)
    Assertions.assertEquals(expectedCode, j256Code)
  }

  // -- Private Methods --------------------------------------------------------------------------------------------- //

  private fun createExpectedCode(timeStepSeconds: Long, digits: Int, currentTime: Long): String {
    val config = TimeBasedOneTimePasswordConfig(timeStep = timeStepSeconds, timeStepUnit = TimeUnit.SECONDS, codeDigits = digits, hmacAlgorithm = HmacAlgorithm.SHA1)
    val generator = TimeBasedOneTimePasswordGenerator(SECRET_PLAIN.toByteArray(), config)
    return generator.generate(currentTime)
  }

  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}