package dev.turingcomplete.kotlinonetimepassword

import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.floor

/**
 * Generator for the RFC 6238 "TOTP: Time-Based One-Time Password Algorithm"
 * (https://tools.ietf.org/html/rfc6238)
 *
 * @property secret the shared secret as a byte array.
 * @property config the [TimeBasedOneTimePasswordConfig] for this generator.
 */
open class TimeBasedOneTimePasswordGenerator(private val secret: ByteArray, private val config: TimeBasedOneTimePasswordConfig) {

  private val hmacOneTimePasswordGenerator: HmacOneTimePasswordGenerator = HmacOneTimePasswordGenerator(secret, config)

  /**
   * Generates a code representing the time-based one-time password.
   *
   * The TOTP algorithm uses the HTOP algorithm via [HmacOneTimePasswordGenerator.generate],
   * with a counter parameter that represents the number of `timeStep`s from
   * [TimeBasedOneTimePasswordConfig] which fits into the [timestamp].
   *
   * The timestamp can be seen as the challenge to be solved. This should
   * normally be a continuous value over time (e.g. the current time).
   *
   * @param timestamp The Unix timestamp against the counting of the time
   * steps is calculated. The default value is the current system time from
   * [System.currentTimeMillis].
   */
  fun generate(timestamp: Long = System.currentTimeMillis()): String {

    val counter = if (config.timeStep == 0L) {
      0 // To avoid a divide by zero exception
    }
    else {
      floor(timestamp.toDouble()
                    .div(TimeUnit.MILLISECONDS.convert(config.timeStep, config.timeStepUnit).toDouble()))
              .toLong()
    }

    return hmacOneTimePasswordGenerator.generate(counter)
  }

  fun generate(date: Date = Date(System.currentTimeMillis())) = generate(date.time)
  fun generate(instant: Instant = Instant.now()) = generate(instant.toEpochMilli())

  /**
   * Validates the given code.
   *
   * @param code the code calculated from the challenge to validate.
   * @param timestamp the used challenge for the code. The default value is the
   *                  current system time from [System.currentTimeMillis].
   */
  fun isValid(code: String, timestamp: Long = System.currentTimeMillis()): Boolean {
    return code == generate(timestamp)
  }

  fun isValid(code: String, date: Date = Date(System.currentTimeMillis())) = isValid(code, date.time)
  fun isValid(code: String, instant: Instant = Instant.now()) = isValid(code, instant.toEpochMilli())
}