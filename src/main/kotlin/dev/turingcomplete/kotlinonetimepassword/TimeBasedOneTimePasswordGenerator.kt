package dev.turingcomplete.kotlinonetimepassword

import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Generator for the RFC 6238 "TOTP: Time-Based One-Time Password Algorithm"
 * (https://tools.ietf.org/html/rfc6238)
 *
 * @property secret the shared secret as a byte array.
 * @property config the configuration for this generator.
 */
open class TimeBasedOneTimePasswordGenerator(private val secret: ByteArray, private val config: TimeBasedOneTimePasswordConfig){
  private val hmacOneTimePasswordGenerator: HmacOneTimePasswordGenerator = HmacOneTimePasswordGenerator(secret, config)

  /**
   * Generated a code as a TOTP one-time password.
   *
   * @param timestamp the challenge for the code. The default value is the
   *                  current system time from [System.currentTimeMillis].
   */
  fun generate(timestamp: Date = Date(System.currentTimeMillis())): String {
    val counter = if (config.timeStep == 0L) {
      0 // To avoide a divide by zero exception
    }
    else {
      timestamp.time.div(TimeUnit.MILLISECONDS.convert(config.timeStep, config.timeStepUnit))
    }

    return hmacOneTimePasswordGenerator.generate(counter)
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
}