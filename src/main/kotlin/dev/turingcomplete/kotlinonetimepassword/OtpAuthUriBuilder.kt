package dev.turingcomplete.kotlinonetimepassword

import dev.turingcomplete.kotlinonetimepassword.OtpAuthUriBuilder.Companion.forHotp
import dev.turingcomplete.kotlinonetimepassword.OtpAuthUriBuilder.Companion.forTotp
import java.io.ByteArrayOutputStream
import java.net.URI
import java.net.URLEncoder
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * A builder to create an OTP Auth URI as defined in
 * [Key Uri Format](https://github.com/google/google-authenticator/wiki/Key-Uri-Format).
 * This URI contains all necessary information for a TOTP/HOTP client to set up
 * the code generation.
 *
 * This URI can be used, for example, to be encoded into a QR code.
 *
 * An example OTP Auth URI would be:
 * ```text
 * otpauth://totp/Company:John@company.com?secret=SGWY3DPESRKFPHH&issuer=Company&digits=8&algorithm=SHA1
 * ```
 *
 * Use the factory methods [forTotp]/[TimeBasedOneTimePasswordGenerator.otpAuthUriBuilder]
 * or [forHotp]/[HmacOneTimePasswordGenerator.otpAuthUriBuilder] to create a/an
 * TOTP/HOTP specific instance of an [OtpAuthUriBuilder].
 *
 * @param removePaddingFromBase32Secret if set to `true`, the Base32 padding
 * character `=` will be removed from the `secret` URI parameter (e.g.,
 * `MFQWC===` will be transformed to `MFQWC`.), this is required by the
 * specification.
 * @property charset the [Charset] to be used at various places inside this
 * builder, for example, for the URL encoding.
 *
 * @see OtpAuthUriBuilder.Totp
 * @see OtpAuthUriBuilder.Hotp
 */
open class OtpAuthUriBuilder<S : OtpAuthUriBuilder<S>>(private val type: String,
                                                       base32Secret: ByteArray,
                                                       removePaddingFromBase32Secret: Boolean = true,
                                                       private val charset: Charset = StandardCharsets.UTF_8) {
  // -- Companion Object -------------------------------------------------------------------------------------------- //

  companion object {
    /**
     * Creates a new [OtpAuthUriBuilder] for a __TOTP__ OTP Auth URI.
     *
     * @param base32Secret the secret as a Base32 encoded [ByteArray].
     *
     * @see TimeBasedOneTimePasswordGenerator.otpAuthUriBuilder
     */
    fun forTotp(base32Secret: ByteArray): Totp {
      return Totp(base32Secret)
    }

    /**
     * Creates a new [OtpAuthUriBuilder] for a __HOTP__ OTP Auth URI.
     *
     * @param base32Secret the secret as a Base32 encoded [ByteArray].
     *
     * @see HmacOneTimePasswordGenerator.otpAuthUriBuilder
     */
    fun forHotp(initialCounter: Long, base32Secret: ByteArray): Hotp {
      return Hotp(initialCounter, base32Secret)
    }
  }

  // -- Properties -------------------------------------------------------------------------------------------------- //

  private val base32Secret: ByteArray
  private var label: String? = null
  protected var parameters = mutableMapOf<String, String>()

  // -- Initialization ---------------------------------------------------------------------------------------------- //

  init {
    this.base32Secret = if (removePaddingFromBase32Secret) removePaddingFromBase32Secret(base32Secret) else base32Secret
  }

  // -- Exposed Methods --------------------------------------------------------------------------------------------- //

  /**
   * Sets the label path part of the URI, which consist of an account name
   * and an optional issuer. Both values will be separated by a colon (`:`),
   * which can be URL encoded by setting the parameter [encodeSeparator].
   *
   * The issuer is a provider or service to which the account name (for
   * which the OTP code gets used) belongs to.
   *
   * The issuer and account name will be URL encoded.
   *
   * The OTP Auth URI specification recommends to always set this path part
   * with both values. And if it is set, the [issuer] parameter should also be
   * set.
   *
   * This is an _optional_ path part.
   */
  fun label(accountName: String, issuer: String?, encodeSeparator: Boolean = false): S {
    if (accountName.contains(":")
      || accountName.contains("%3A")
      || issuer?.contains(":") == true
      || issuer?.contains("%3A") == true) {
      throw IllegalArgumentException("Neither the account name nor the issuer are allowed to contain a colon.")
    }

    val encodedAccountName = URLEncoder.encode(accountName, charset.name())
    label = if (issuer != null) {
      val colon = if (encodeSeparator) "%3A" else ":"
      URLEncoder.encode(issuer, charset.name()) + colon + encodedAccountName
    }
    else {
      encodedAccountName
    }

    @Suppress("UNCHECKED_CAST")
    return this as S
  }

  /**
   * Sets the `issuer` query parameter, which indicates the provider or service
   * the account (for which the OTP code gets used) belongs to.
   *
   * The OTP Auth URI specification recommends to always set this parameter. And
   * if it is set, the [label] path part should also be set.
   *
   * The value will be URL encoded.
   *
   * This is an _optional_ parameter.
   */
  fun issuer(issuer: String): S {
    parameters["issuer"] = URLEncoder.encode(issuer, StandardCharsets.UTF_8.name())

    @Suppress("UNCHECKED_CAST")
    return this as S
  }

  /**
   * Sets the `algorithm` query parameter, which is the uppercase name of the
   * HMAC algorithms defined in [HmacAlgorithm].
   *
   * This value is equivalent to the [TimeBasedOneTimePasswordConfig.hmacAlgorithm]
   * and [HmacOneTimePasswordConfig.hmacAlgorithm] configuration.
   *
   * The Google Authenticator may ignore this value and always uses `SHA1`.
   *
   * This is an _optional_ parameter.
   */
  fun algorithm(algorithm: HmacAlgorithm): S {
    parameters["algorithm"] = algorithm.name

    @Suppress("UNCHECKED_CAST")
    return this as S
  }

  /**
   * Sets the `digits` query parameter, which is the length of the generated
   * code.
   *
   * This value is equivalent to the [TimeBasedOneTimePasswordConfig.codeDigits] and
   * [HmacOneTimePasswordConfig.codeDigits] configuration.
   *
   * The Google Authenticator may ignore this value and always uses `6`.
   *
   * This is an _optional_ parameter.
   */
  fun digits(digits: Int): S {
    parameters["digits"] = digits.toString()

    @Suppress("UNCHECKED_CAST")
    return this as S
  }

  /**
   * Builds the final OTP Auth URI as a [String].
   *
   * Warning: Handling the URI as a string may leak the secret into the String
   * pool of the JVM. Consider using [buildToByteArray] instead.
   */
  fun buildToString(): String {
    return buildUriWithoutSecret(mapOf(Pair("secret", base32Secret.toString(charset))))
  }

  /**
   * Builds the final OTP Auth URI as a [URI].
   *
   * Warning: Handling the URI as a string may leak the secret into the String
   * pool of the JVM. Consider using [buildToByteArray] instead.
   */
  fun buildToUri(): URI {
    return URI(buildToString())
  }

  /**
   * Builds the final OTP Auth URI as a [ByteArray].
   */
  fun buildToByteArray(): ByteArray {
    return ByteArrayOutputStream().apply {
      write(buildUriWithoutSecret().toByteArray(charset))
      write(if (parameters.isNotEmpty()) '&'.code else '?'.code)
      write("secret=".toByteArray(charset))
      write(base32Secret)
    }.toByteArray()
  }

  // -- Private Methods --------------------------------------------------------------------------------------------- //

  private fun buildUriWithoutSecret(additionalParameters: Map<String, String> = emptyMap()): String {
    val query = parameters.plus(additionalParameters).map { "${it.key}=${it.value}" }.joinToString(separator = "&", prefix = "?")
    return "otpauth://$type/${if (label != null) label else ""}$query"
  }

  private fun removePaddingFromBase32Secret(base32Secret: ByteArray): ByteArray {
    val base32SecretByteBuffer = ByteBuffer.wrap(base32Secret)
    val base32SecretCharBuffer: CharBuffer = charset.decode(base32SecretByteBuffer)

    var cleanedBase32SecretLength = 0
    val cleanedBase32SecretCharBuffer = CharBuffer.allocate(base32SecretCharBuffer.length)
    for(i in base32SecretCharBuffer.indices) {
      if (base32SecretCharBuffer[i] != '=') {
        cleanedBase32SecretLength++
        cleanedBase32SecretCharBuffer.put(i, base32SecretCharBuffer[i])
      }
    }

    val cleanedBase32SecretByteBuffer = charset.encode(cleanedBase32SecretCharBuffer.subSequence(0, cleanedBase32SecretLength))
    val cleanedBase32Secret = Arrays.copyOfRange(cleanedBase32SecretByteBuffer.array(),
                                                 cleanedBase32SecretByteBuffer.position(),
                                                 cleanedBase32SecretByteBuffer.limit())

    // Clean up
    // `base32SecretByteBuffer` holds a reference to the original array
    Arrays.fill(base32SecretCharBuffer.array(), '-')
    Arrays.fill(cleanedBase32SecretCharBuffer.array(), '-')
    Arrays.fill(cleanedBase32SecretByteBuffer.array(), 0.toByte())

    return cleanedBase32Secret
  }

  // -- Inner Type -------------------------------------------------------------------------------------------------- //

  /**
   * A builder for a TOTP OTP Auth URI.
   *
   * An instance should be created via [forTotp]
   * or [TimeBasedOneTimePasswordGenerator.otpAuthUriBuilder].
   */
  class Totp(base32Secret: ByteArray) : OtpAuthUriBuilder<Totp>("totp", base32Secret) {

    /**
     * Sets the `period` query parameter, which defines the validity of a TOTP
     * code in seconds.
     *
     * This value is equivalent to the [TimeBasedOneTimePasswordConfig.timeStep] and
     * [TimeBasedOneTimePasswordConfig.timeStepUnit] configuration.
     *
     * This is an _optional_ parameter.
     */
    fun period(timeStep: Long, timeStepUnit: TimeUnit): Totp {
      parameters["period"] = timeStepUnit.toSeconds(timeStep).toString()
      return this
    }
  }

  // -- Inner Type -------------------------------------------------------------------------------------------------- //

  /**
   * A builder for an HOTP OTP Auth URI.
   *
   * An instance should be created via [forHotp]
   * or [HmacOneTimePasswordGenerator.otpAuthUriBuilder].
   *
   * @param initialCounter the initial [counter] value.
   */
  class Hotp(initialCounter: Long, base32Secret: ByteArray) : OtpAuthUriBuilder<Hotp>("hotp", base32Secret) {

    init {
      counter(initialCounter)
    }

    /**
     * Sets the `counter` parameter, which defines the initial counter value.
     *
     * This is a _required_ parameter and will be initially set by the
     * constructor parameter `initialCounter`. Calling this method will
     * overwrite the initial value from the constructor.
     */
    fun counter(initialCounter: Long): Hotp {
      parameters["counter"] = initialCounter.toString()
      return this
    }
  }
}
