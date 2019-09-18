package dev.turingcomplete.kotlinonetimepassword

/**
 * Available "keyed-hash message authentication code" (HMAC) algorithms.
 * See: https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#Mac
 *
 * @property macAlgorithmName the name of the algorithm used for
 *                            [javax.crypto.Mac.getInstance(java.lang.String)]
 * @property hashBytes the length of the returned hash produced by the algorithm.
 */
enum class HmacAlgorithm(val macAlgorithmName: String, val hashBytes: Int) {
  /**
   * SHA1 HMAC with a hash of 20-bytes
   */
  SHA1("HmacSHA1", 20),
  /**
   * SHA256 HMAC with a hash of 32-bytes
   */
  SHA256("HmacSHA256", 32),
  /**
   * SHA512 HMAC with a hash of 64-bytes
   */
  SHA512("HmacSHA512", 64);
}