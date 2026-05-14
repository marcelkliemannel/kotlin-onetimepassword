package dev.turingcomplete.kotlinonetimepassword

import org.apache.commons.codec.binary.Base32
import java.security.MessageDigest
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Convenience wrapper around [TimeBasedOneTimePasswordGenerator] using the
 * default parameters expected by Google Authenticator compatible apps:
 *
 * - HMAC algorithm: [HmacAlgorithm.SHA1]
 * - time step: 30 seconds
 * - code digits: 6
 *
 * The constructor expects the shared secret as Base32-encoded bytes. The input
 * is copied before decoding so later changes to the provided array do not affect
 * this instance.
 *
 * @param base32secret the shared Base32-encoded secret.
 *
 * @throws IllegalArgumentException if [base32secret] is empty.
 */
class GoogleAuthenticator(private val base32secret: ByteArray) {
  // -- Companion Object -------------------------------------------------------------------------------------------- //

  companion object {
    private const val GOOGLE_AUTHENTICATOR_COMPATIBLE_SECRET_BYTES = 10

    /**
     * Generates a Google Authenticator compatible 16-character Base32-encoded string.
     *
     * This preserves the historical Google Authenticator convention of an
     * 80-bit secret. For new deployments that do not require this compatibility,
     * prefer [createSecureRandomSecretAsByteArray].
     */
    @Deprecated("Use ByteArray representation",
                replaceWith = ReplaceWith("createRandomSecretAsByteArray()"))
    fun createRandomSecret(): String {
      val randomSecret = RandomSecretGenerator().createRandomSecret(GOOGLE_AUTHENTICATOR_COMPATIBLE_SECRET_BYTES)
      return Base32().encodeAsString(randomSecret)
    }

    /**
     * Generates a Google Authenticator compatible 16-character Base32-encoded [ByteArray].
     *
     * This preserves the historical Google Authenticator convention of an
     * 80-bit secret. For new deployments that do not require this compatibility,
     * prefer [createSecureRandomSecretAsByteArray].
     */
    fun createRandomSecretAsByteArray(): ByteArray {
      val randomSecret = RandomSecretGenerator().createRandomSecret(GOOGLE_AUTHENTICATOR_COMPATIBLE_SECRET_BYTES)
      return Base32().encode(randomSecret)
    }

    /**
     * Generates an RFC 4226 recommended 160-bit secret as a Base32-encoded [ByteArray].
     */
    fun createSecureRandomSecretAsByteArray(): ByteArray {
      val randomSecret = RandomSecretGenerator().createRandomSecret(HmacAlgorithm.SHA1)
      return Base32().encode(randomSecret)
    }

    /**
     * Default TOTP configuration used by this wrapper.
     */
    val CONFIG = TimeBasedOneTimePasswordConfig(30, TimeUnit.SECONDS, 6, HmacAlgorithm.SHA1)
  }

  // -- Properties -------------------------------------------------------------------------------------------------- //

  private val timeBasedOneTimePasswordGenerator: TimeBasedOneTimePasswordGenerator

  // -- Initialization ---------------------------------------------------------------------------------------------- //

  init {
    require(base32secret.isNotEmpty()) { "Secret must not be empty." }
    timeBasedOneTimePasswordGenerator = TimeBasedOneTimePasswordGenerator(Base32().decode(base32secret.copyOf()), CONFIG)
  }

  /**
   * Creates a Google Authenticator wrapper from a Base32-encoded secret string.
   *
   * Prefer [GoogleAuthenticator] with a [ByteArray] to avoid keeping secrets in
   * immutable JVM strings.
   */
  @Deprecated("Use ByteArray representation",
              replaceWith = ReplaceWith("GoogleAuthenticator(ByteArray)"))
  constructor(base32secret: String) : this(base32secret.toByteArray())

  // -- Exposed Methods --------------------------------------------------------------------------------------------- //

  /**
   * Generates the TOTP code for [timestamp].
   *
   * @param timestamp the time used as the TOTP challenge. The default value is the
   *                  current system time from [System.currentTimeMillis].
   *
   * @return a zero-padded numeric code with [CONFIG]'s digit count.
   */
  fun generate(timestamp: Date = Date(System.currentTimeMillis())): String {
    return timeBasedOneTimePasswordGenerator.generate(timestamp)
  }

  /**
   * Validates [code] against the code generated for [timestamp].
   *
   * The comparison is performed using [MessageDigest.isEqual] to avoid leaking
   * information through simple early-exit string comparison.
   *
   * @param code the code to validate.
   * @param timestamp the time used as the TOTP challenge. The default value is the
   *                  current system time from [System.currentTimeMillis].
   *
   * @return `true` if [code] matches the generated code for [timestamp].
   */
  fun isValid(code: String, timestamp: Date = Date(System.currentTimeMillis())): Boolean {
    val expectedCode = generate(timestamp)
    return MessageDigest.isEqual(code.toByteArray(Charsets.UTF_8), expectedCode.toByteArray(Charsets.UTF_8))
  }

  /**
   * Creates an [OtpAuthUriBuilder] preconfigured with the secret and the fixed
   * Google Authenticator compatible configuration for the algorithm,
   * code digits and time step.
   */
  fun otpAuthUriBuilder(): OtpAuthUriBuilder.Totp = timeBasedOneTimePasswordGenerator.otpAuthUriBuilder()

  // -- Private Methods --------------------------------------------------------------------------------------------- //
  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}
