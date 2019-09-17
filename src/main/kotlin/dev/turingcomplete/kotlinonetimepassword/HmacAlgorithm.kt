package dev.turingcomplete.kotlinonetimepassword

/**
 * Available "keyed-hash message authentication code" algorithms.
 *
 * @property hashBytes the length of the returned hash produced by the algorithm.
 */
enum class HmacAlgorithm(val macAlgorithmName: String, val hashBytes: Int) {
  SHA1("HmacSHA1", 20),
  SHA256("HmacSHA256", 32),
  SHA512("HmacSHA512", 64);
}