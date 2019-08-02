package dev.turingcomplete.kotlinonetimepassword

import java.security.SecureRandom

class RandomSecretGenerator {
  private val secureRandom = SecureRandom()

  /**
   * RFC 4226 recommends using a secret of the same size as the hash produced by the used HMAC algorithm.
   */
  fun createRandomSecret(hmacAlgorithm: HmacAlgorithm): ByteArray {
    return createRandomSecret(hmacAlgorithm.hashBytes)
  }

  fun createRandomSecret(secretBytes: Int): ByteArray {
    val randomSecret = ByteArray(secretBytes)
    secureRandom.nextBytes(randomSecret)

    return randomSecret
  }
}