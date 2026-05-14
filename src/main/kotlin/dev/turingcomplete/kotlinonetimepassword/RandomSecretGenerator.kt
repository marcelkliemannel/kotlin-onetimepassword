package dev.turingcomplete.kotlinonetimepassword

import java.security.SecureRandom

/**
 * Generates shared secrets using [SecureRandom].
 *
 * A generated secret is raw random bytes. Encode it with Base32 before placing
 * it in an OTP Auth URI or passing it to authenticator apps that expect textual
 * secrets.
 */
class RandomSecretGenerator {
  // -- Companion Object -------------------------------------------------------------------------------------------- //
  // -- Properties -------------------------------------------------------------------------------------------------- //

  private val secureRandom = SecureRandom()

  // -- Initialization ---------------------------------------------------------------------------------------------- //
  // -- Exposed Methods --------------------------------------------------------------------------------------------- //

  /**
   * Generates a random secret with the same length as [hmacAlgorithm]'s HMAC
   * output.
   *
   * @param hmacAlgorithm the HMAC algorithm whose [HmacAlgorithm.hashBytes]
   * value is used as the secret length.
   *
   * @return a new byte array containing random secret bytes.
   */
  fun createRandomSecret(hmacAlgorithm: HmacAlgorithm): ByteArray {
    return createRandomSecret(hmacAlgorithm.hashBytes)
  }

  /**
   * Generates a random secret with [secretBytes] bytes.
   *
   * @param secretBytes the number of random bytes to generate.
   *
   * @return a new byte array containing random secret bytes.
   *
   * @throws IllegalArgumentException if [secretBytes] is not positive.
   */
  fun createRandomSecret(secretBytes: Int): ByteArray {
    require(secretBytes > 0) { "Secret length must be greater than zero." }

    val randomSecret = ByteArray(secretBytes)
    secureRandom.nextBytes(randomSecret)

    return randomSecret
  }

  // -- Private Methods --------------------------------------------------------------------------------------------- //
  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}
