package com.marcelkliemannel.kotlinonetimepassword

import org.apache.commons.codec.binary.Base32
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * This class is a decorator of the [TimeBasedOneTimePasswordGenerator], which provides the default values
 * used by the Google Authenticator: HMAC algorithm: SHA1; time step: 30 seconds and code digits: 6.
 *
 * @param secret The secret must be a Base32-encoded string.
 */
class GoogleAuthenticator(secret: String) {
  private val timeBasedOneTimePasswordGenerator: TimeBasedOneTimePasswordGenerator

  init {
    val hmacAlgorithm = HmacAlgorithm.SHA1
    val config = TimeBasedOneTimePasswordConfig(30, TimeUnit.SECONDS, 6, hmacAlgorithm)

    timeBasedOneTimePasswordGenerator = TimeBasedOneTimePasswordGenerator(Base32().decode(secret), config);
  }

  /**
   * @param timestamp The default value is the current system time.
   */
  fun generate(timestamp: Date = Date(System.currentTimeMillis())): String {
    return timeBasedOneTimePasswordGenerator.generate(timestamp)
  }

  /**
   * @param code The received code to validate.
   * @param timestamp The default value is the current system time.
   */
  fun isValid(code: String, timestamp: Date = Date(System.currentTimeMillis())): Boolean {
    return code == generate(timestamp)
  }

  companion object {
    /**
     * Generates a 16-byte secret key, which is a Base32-encoded string.
     *
     * Due to the overhead of 160% of Base32 encoding, only 10 bytes are needed for the random secret
     * to generate a 16-byte array.
     */
    fun createRandomSecret(): String {
      val randomSecret = RandomSecretGenerator().createRandomSecret(10)
      return Base32().encodeToString(randomSecret)
    }
  }
}