package dev.turingcomplete.kotlinonetimepassword

import java.security.SecureRandom

/**
 * Generator to create a secure random secret via [SecureRandom].
 */
class RandomSecretGenerator {
  private val secureRandom = SecureRandom()

  /**
   * Generates a secure random secret with the same length as a hash
   * of the given HMAC algorithm (defined in [HmacAlgorithm.hashBytes]).
   *
   * @param hmacAlgorithm the HMAC algorithm from that the number of bytes is
   *                      taken.
   */
  fun createRandomSecret(hmacAlgorithm: HmacAlgorithm): ByteArray {
    return createRandomSecret(hmacAlgorithm.hashBytes)
  }

  /**
   * Generates a secure random secret with variable length.
   *
   * @param secretBytes the length (as number of bytes) of the generated secret.
   */
  fun createRandomSecret(secretBytes: Int): ByteArray {
    val randomSecret = ByteArray(secretBytes)
    secureRandom.nextBytes(randomSecret)

    return randomSecret
  }
}