package dev.turingcomplete.kotlinonetimepassword

import dev.turingcomplete.kotlinonetimepassword.OtpAuthUriBuilder.Companion.forHotp
import dev.turingcomplete.kotlinonetimepassword.OtpAuthUriBuilder.Companion.forTotp
import java.io.ByteArrayOutputStream
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

/**
 * Builder for OTP Auth URIs as defined by the
 * [Key Uri Format](https://github.com/google/google-authenticator/wiki/Key-Uri-Format).
 *
 * OTP Auth URIs contain the metadata needed by authenticator apps to configure
 * TOTP or HOTP code generation. They are commonly encoded as QR codes.
 *
 * Example:
 * ```text
 * otpauth://totp/Company:John@company.com?secret=SGWY3DPESRKFPHH&issuer=Company&digits=8&algorithm=SHA1
 * ```
 *
 * Use the factory methods [forTotp]/[TimeBasedOneTimePasswordGenerator.otpAuthUriBuilder]
 * or [forHotp]/[HmacOneTimePasswordGenerator.otpAuthUriBuilder] to create a
 * TOTP- or HOTP-specific builder.
 *
 * @param type the OTP Auth URI type, usually `totp` or `hotp`.
 * @param base32Secret the shared secret as Base32-encoded bytes.
 * @param removePaddingFromBase32Secret whether Base32 padding characters (`=`)
 * should be removed from the `secret` URI parameter. The key URI format expects
 * an unpadded Base32 value.
 * @property charset the [Charset] to be used at various places inside this
 * builder, for example URL encoding and byte-array conversion.
 *
 * @throws IllegalArgumentException if [base32Secret] is not a valid non-empty
 * Base32 value.
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
    private val BASE32_SECRET_REGEX = Regex("^[A-Z2-7]+={0,6}$")

    /**
     * Creates a new builder for a TOTP OTP Auth URI.
     *
     * @param base32Secret the secret as Base32-encoded bytes.
     *
     * @throws IllegalArgumentException if [base32Secret] is not a valid
     * non-empty Base32 value.
     *
     * @see TimeBasedOneTimePasswordGenerator.otpAuthUriBuilder
     */
    fun forTotp(base32Secret: ByteArray): Totp {
      return Totp(base32Secret)
    }

    /**
     * Creates a new builder for an HOTP OTP Auth URI.
     *
     * @param initialCounter the initial HOTP counter value.
     * @param base32Secret the secret as Base32-encoded bytes.
     *
     * @throws IllegalArgumentException if [initialCounter] is negative or
     * [base32Secret] is not a valid non-empty Base32 value.
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
    validateBase32Secret(base32Secret)
    this.base32Secret = if (removePaddingFromBase32Secret) removePaddingFromBase32Secret(base32Secret) else base32Secret.copyOf()
  }

  // -- Exposed Methods --------------------------------------------------------------------------------------------- //

  /**
   * Sets the label path part of the URI.
   *
   * The label consists of an account name and an optional issuer. If an issuer
   * is provided, both values are separated by a colon (`:`). Set
   * [encodeSeparator] to `true` to URL-encode that separator as `%3A`.
   *
   * The issuer is the provider or service the account belongs to. Both issuer
   * and account name are URL-encoded.
   *
   * The OTP Auth URI specification recommends setting both label values and
   * mirroring the issuer with [issuer].
   *
   * @param accountName the account name shown by authenticator apps.
   * @param issuer the optional provider or service name.
   * @param encodeSeparator whether the separator between issuer and account name
   * should be URL-encoded.
   *
   * @return this builder.
   *
   * @throws IllegalArgumentException if [accountName] or [issuer] contains a
   * literal or URL-encoded colon.
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
   * Sets the `issuer` query parameter.
   *
   * The issuer identifies the provider or service the account belongs to. The
   * OTP Auth URI specification recommends setting this parameter and also
   * including the same issuer in [label].
   *
   * The value will be URL encoded.
   *
   * @return this builder.
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
   * @return this builder.
   */
  fun algorithm(algorithm: HmacAlgorithm): S {
    parameters["algorithm"] = algorithm.name

    @Suppress("UNCHECKED_CAST")
    return this as S
  }

  /**
   * Sets the `digits` query parameter.
   *
   * This value is the length of the generated code and is equivalent to
   * [TimeBasedOneTimePasswordConfig.codeDigits] and
   * [HmacOneTimePasswordConfig.codeDigits].
   *
   * The Google Authenticator may ignore this value and always uses `6`.
   *
   * @param digits the number of digits in generated codes.
   *
   * @return this builder.
   *
   * @throws IllegalArgumentException if [digits] is outside the supported URI
   * range.
   */
  fun digits(digits: Int): S {
    require(digits in 1..HmacOneTimePasswordConfig.MAX_CODE_DIGITS) {
      "Number of code digits must be between 1 and ${HmacOneTimePasswordConfig.MAX_CODE_DIGITS}."
    }

    parameters["digits"] = digits.toString()

    @Suppress("UNCHECKED_CAST")
    return this as S
  }

  /**
   * Builds the final OTP Auth URI as a [String].
   *
   * Warning: the returned string contains the shared secret. Prefer
   * [buildToByteArray] when you want to avoid keeping secrets in immutable JVM
   * strings.
   */
  fun buildToString(): String {
    return buildUriWithoutSecret(mapOf(Pair("secret", base32Secret.toString(charset))))
  }

  /**
   * Builds the final OTP Auth URI as a [URI].
   *
   * Warning: creating a [URI] requires a string representation that contains the
   * shared secret. Prefer [buildToByteArray] when you want to avoid keeping
   * secrets in immutable JVM strings.
   */
  fun buildToUri(): URI {
    return URI(buildToString())
  }

  /**
   * Builds the final OTP Auth URI as a [ByteArray].
   *
   * This method appends the Base32 secret directly to the output bytes and
   * avoids creating a complete URI string containing the secret.
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

  private fun validateBase32Secret(base32Secret: ByteArray) {
    val base32SecretString = base32Secret.toString(charset)
    require(BASE32_SECRET_REGEX.matches(base32SecretString)) { "Secret must be a non-empty Base32-encoded value." }
  }

  private fun removePaddingFromBase32Secret(base32Secret: ByteArray): ByteArray {
    return base32Secret.toString(charset).trimEnd('=').toByteArray(charset)
  }

  // -- Inner Type -------------------------------------------------------------------------------------------------- //

  /**
   * A builder for a TOTP OTP Auth URI.
   *
   * An instance should be created via [forTotp]
   * or [TimeBasedOneTimePasswordGenerator.otpAuthUriBuilder].
   *
   * @param base32Secret the secret as Base32-encoded bytes.
   */
  class Totp(base32Secret: ByteArray) : OtpAuthUriBuilder<Totp>("totp", base32Secret) {

    /**
     * Sets the `period` query parameter, which defines the validity of a TOTP
     * code in seconds.
     *
     * This value is equivalent to the [TimeBasedOneTimePasswordConfig.timeStep] and
     * [TimeBasedOneTimePasswordConfig.timeStepUnit] configuration.
     *
     * @param timeStep the size of one TOTP time step.
     * @param timeStepUnit the unit used to convert [timeStep] to seconds.
     *
     * @return this builder.
     *
     * @throws IllegalArgumentException if the converted period is less than one
     * second.
     */
    fun period(timeStep: Long, timeStepUnit: TimeUnit): Totp {
      require(timeStep > 0) { "Time step must be greater than zero." }
      require(timeStepUnit.toSeconds(timeStep) > 0) { "Time step must be at least one second." }

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
   * @param initialCounter the initial HOTP counter value.
   * @param base32Secret the secret as Base32-encoded bytes.
   */
  class Hotp(initialCounter: Long, base32Secret: ByteArray) : OtpAuthUriBuilder<Hotp>("hotp", base32Secret) {

    init {
      counter(initialCounter)
    }

    /**
     * Sets the `counter` parameter, which defines the initial counter value.
     *
     * This is a required HOTP parameter and is initially set by the constructor.
     * Calling this method overwrites that initial value.
     *
     * @param initialCounter the initial HOTP counter value.
     *
     * @return this builder.
     *
     * @throws IllegalArgumentException if [initialCounter] is negative.
     */
    fun counter(initialCounter: Long): Hotp {
      require(initialCounter >= 0) { "Counter must not be negative." }

      parameters["counter"] = initialCounter.toString()
      return this
    }
  }
}
