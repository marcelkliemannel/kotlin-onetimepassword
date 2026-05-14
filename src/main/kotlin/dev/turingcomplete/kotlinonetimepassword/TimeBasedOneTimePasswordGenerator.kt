package dev.turingcomplete.kotlinonetimepassword

import org.apache.commons.codec.binary.Base32
import java.security.MessageDigest
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Generator for RFC 6238 TOTP: Time-Based One-Time Password Algorithm.
 *
 * TOTP uses HOTP with a counter derived from the timestamp and configured time
 * step. The generator copies [secret] during construction. Later changes to the
 * provided array do not affect generated codes.
 *
 * @property secret the shared secret.
 * @property config the [TimeBasedOneTimePasswordConfig] for this generator.
 *
 * @throws IllegalArgumentException if [secret] is empty.
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc6238">RFC 6238</a>
 */
open class TimeBasedOneTimePasswordGenerator(private val secret: ByteArray, private val config: TimeBasedOneTimePasswordConfig) {
  // -- Companion Object -------------------------------------------------------------------------------------------- //
  // -- Properties -------------------------------------------------------------------------------------------------- //

  private val secretCopy = secret.copyOf()
  private val hmacOneTimePasswordGenerator: HmacOneTimePasswordGenerator = HmacOneTimePasswordGenerator(secretCopy, config)

  // -- Initialization ---------------------------------------------------------------------------------------------- //
  // -- Exposed Methods --------------------------------------------------------------------------------------------- //

  /**
   * Calculates the TOTP counter for [timestamp].
   *
   * The counter is the number of configured time steps that fit into the Unix
   * timestamp in milliseconds. For example, with a 30-second time step,
   * timestamps from `0` through `29999` map to counter `0`.
   *
   * @param timestamp the Unix timestamp in milliseconds. The default value is
   * the current system time from [System.currentTimeMillis].
   *
   * @return the TOTP counter for [timestamp].
   */
  fun counter(timestamp: Long = System.currentTimeMillis()): Long {
    if (config.timeStep == 0L) {
      // To avoid a divide by zero
      return 0
    }

    return Math.floorDiv(timestamp, TimeUnit.MILLISECONDS.convert(config.timeStep, config.timeStepUnit))
  }

  /**
   * Calculates the TOTP counter for [date].
   */
  fun counter(date: Date): Long = counter(date.time)

  /**
   * Calculates the TOTP counter for [instant].
   */
  fun counter(instant: Instant): Long = counter(instant.toEpochMilli())

  /**
   * Calculates the start timestamp of [counter]'s time slot.
   *
   * This is the inverse operation for [counter] when the timestamp falls exactly
   * on a time-step boundary.
   *
   * @param counter the counter representing the time slot.
   *
   * @return the Unix timestamp in milliseconds where the time slot starts.
   */
  fun timeslotStart(counter: Long): Long {
    val timeStepMillis = TimeUnit.MILLISECONDS.convert(config.timeStep, config.timeStepUnit).toDouble()
    return (counter * timeStepMillis).toLong()
  }

  /**
   * Generates a TOTP code for [timestamp].
   *
   * The timestamp is converted to a TOTP counter and then passed to
   * [HmacOneTimePasswordGenerator.generate].
   *
   * @param timestamp the Unix timestamp in milliseconds. The default value is
   * the current system time from [System.currentTimeMillis].
   *
   * @return a zero-padded numeric code with the configured digit count.
   */
  fun generate(timestamp: Long = System.currentTimeMillis()): String =
    hmacOneTimePasswordGenerator.generate(counter(timestamp))

  /**
   * Generates a TOTP code for [date].
   */
  fun generate(date: Date): String = generate(date.time)

  /**
   * Generates a TOTP code for [instant].
   */
  fun generate(instant: Instant): String = generate(instant.toEpochMilli())

  /**
   * Validates [code] against the TOTP code generated for [timestamp].
   *
   * The comparison is performed using [MessageDigest.isEqual] to avoid leaking
   * information through simple early-exit string comparison.
   *
   * @param code the code to validate.
   * @param timestamp the Unix timestamp in milliseconds. The default value is
   * the current system time from [System.currentTimeMillis].
   *
   * @return `true` if [code] matches the generated code for [timestamp].
   */
  fun isValid(code: String, timestamp: Long = System.currentTimeMillis()): Boolean {
    val expectedCode = generate(timestamp)
    return MessageDigest.isEqual(code.toByteArray(Charsets.UTF_8), expectedCode.toByteArray(Charsets.UTF_8))
  }

  /**
   * Validates [code] against the TOTP code generated for [date].
   */
  fun isValid(code: String, date: Date) = isValid(code, date.time)

  /**
   * Validates [code] against the TOTP code generated for [instant].
   */
  fun isValid(code: String, instant: Instant) = isValid(code, instant.toEpochMilli())

  /**
   * Creates an [OtpAuthUriBuilder] for a TOTP URI.
   *
   * The returned builder is preconfigured with the Base32-encoded secret and the
   * algorithm, digit count, and time step from [config].
   */
  fun otpAuthUriBuilder(): OtpAuthUriBuilder.Totp {
    return OtpAuthUriBuilder.forTotp(Base32().encode(secretCopy))
      .algorithm(config.hmacAlgorithm)
      .digits(config.codeDigits)
      .period(config.timeStep, config.timeStepUnit)
  }

  // -- Private Methods --------------------------------------------------------------------------------------------- //
  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}
