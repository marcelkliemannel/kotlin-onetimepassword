package dev.turingcomplete.kotlinonetimepassword

import org.apache.commons.codec.binary.Base32
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * This class is a decorator of the [TimeBasedOneTimePasswordGenerator] that
 * provides the default values used by the Google Authenticator: HMAC algorithm:
 * SHA1; time step: 30 seconds and code digits: 6.
 *
 * @param base32secret the shared secret <b>that must already be Base32-encoded</b>
 *                     (use [org.apache.commons.codec.binary.BaseNCodec.encode(byte[])]).
 */
class GoogleAuthenticator(base32secret: String) {
  private val timeBasedOneTimePasswordGenerator: TimeBasedOneTimePasswordGenerator

  init {
    val hmacAlgorithm = HmacAlgorithm.SHA1
    val config = TimeBasedOneTimePasswordConfig(30, TimeUnit.SECONDS, 6, hmacAlgorithm)

    timeBasedOneTimePasswordGenerator = TimeBasedOneTimePasswordGenerator(Base32().decode(base32secret), config)
  }

  /**
   * Generates a code as a TOTP one-time password.
   *
   * @param timestamp the challenge for the code. The default value is the
   *                  current system time from [System.currentTimeMillis].
   */
  fun generate(timestamp: Date = Date(System.currentTimeMillis())): String {
    return timeBasedOneTimePasswordGenerator.generate(timestamp)
  }

  /**
   * Validates the given code.
   *
   * @param code the code calculated from the challenge to validate.
   * @param timestamp the used challenge for the code. The default value is the
   *                  current system time from [System.currentTimeMillis].
   */
  fun isValid(code: String, timestamp: Date = Date(System.currentTimeMillis())): Boolean {
    return code == generate(timestamp)
  }

  companion object {
    /**
     * Generates a 16-byte secret as a Base32-encoded string.
     *
     * Due to the overhead of 160% of the Base32 encoding, only 10 bytes are
     * needed for the random secret to generate a 16-byte array.
     */
    fun createRandomSecret(): String {
      val randomSecret = RandomSecretGenerator().createRandomSecret(10)
      return Base32().encodeToString(randomSecret)
    }
  }
}