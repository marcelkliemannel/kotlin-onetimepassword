package dev.turingcomplete.kotlinonetimepassword

/**
 * HMAC algorithms supported by HOTP and TOTP generation.
 *
 * @property macAlgorithmName the JCA standard algorithm name passed to
 * [javax.crypto.Mac.getInstance].
 * @property hashBytes the number of bytes produced by the algorithm's HMAC
 * output.
 *
 * @see <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#Mac">Java Cryptography Architecture Standard Algorithm Names</a>
 */
enum class HmacAlgorithm(val macAlgorithmName: String, val hashBytes: Int) {
  // -- Values ------------------------------------------------------------------------------------------------------ //

  /**
   * HMAC-SHA-1 with a 20-byte output.
   */
  SHA1("HmacSHA1", 20),

  /**
   * HMAC-SHA-256 with a 32-byte output.
   */
  SHA256("HmacSHA256", 32),

  /**
   * HMAC-SHA-512 with a 64-byte output.
   */
  SHA512("HmacSHA512", 64)

  // -- Companion Object -------------------------------------------------------------------------------------------- //
  // -- Properties -------------------------------------------------------------------------------------------------- //
  // -- Initialization ---------------------------------------------------------------------------------------------- //
  // -- Exposed Methods --------------------------------------------------------------------------------------------- //
  // -- Private Methods --------------------------------------------------------------------------------------------- //
  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}
