package dev.turingcomplete.kotlinonetimepassword

/**
 * Configuration for [HmacOneTimePasswordGenerator].
 *
 * @property codeDigits the number of digits in the generated code. RFC 4226
 * recommends at least 6 digits. This implementation supports values from `0`
 * to [MAX_CODE_DIGITS], but values below [MIN_SECURE_CODE_DIGITS] require
 * [allowInsecureConfiguration]. A value of `0` produces an empty code and is
 * intended only for tests or legacy compatibility.
 *
 * @property hmacAlgorithm the keyed-hash message authentication code algorithm
 * used to generate the HMAC value from which the one-time password is extracted.
 * See [HmacAlgorithm] for the supported algorithms.
 * @property allowInsecureConfiguration allows code digit values below the RFC
 * 4226 security recommendation. This should only be used for legacy
 * compatibility or test vectors.
 *
 * @throws IllegalArgumentException if `codeDigits` is outside the supported range
 *                                  or insecure without explicit opt-in.
 */
open class HmacOneTimePasswordConfig(val codeDigits: Int,
                                     val hmacAlgorithm: HmacAlgorithm,
                                     val allowInsecureConfiguration: Boolean = false) {
  // -- Companion Object -------------------------------------------------------------------------------------------- //

  companion object {
    /**
     * Minimum code length recommended for normal use.
     */
    const val MIN_SECURE_CODE_DIGITS = 6

    /**
     * Maximum code length supported by this implementation.
     *
     * RFC 4226 dynamic truncation produces a 31-bit integer, so values above
     * this limit would mostly add deterministic leading zeroes instead of
     * meaningful entropy.
     */
    const val MAX_CODE_DIGITS = 9
  }

  // -- Properties -------------------------------------------------------------------------------------------------- //
  // -- Initialization ---------------------------------------------------------------------------------------------- //

  /**
   * Creates a secure HOTP configuration.
   *
   * Use the primary constructor with `allowInsecureConfiguration = true` only
   * when reproducing legacy behavior or test vectors that use short codes.
   */
  constructor(codeDigits: Int, hmacAlgorithm: HmacAlgorithm) : this(codeDigits, hmacAlgorithm, false)

  init {
    require(codeDigits in 0..MAX_CODE_DIGITS) { "Number of code digits must be between 0 and $MAX_CODE_DIGITS." }
    if (!allowInsecureConfiguration) {
      require(codeDigits >= MIN_SECURE_CODE_DIGITS) {
        "Number of code digits must be at least $MIN_SECURE_CODE_DIGITS. Set allowInsecureConfiguration=true to use shorter codes."
      }
    }
  }

  // -- Exposed Methods --------------------------------------------------------------------------------------------- //
  // -- Private Methods --------------------------------------------------------------------------------------------- //
  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}
