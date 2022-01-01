package dev.turingcomplete.kotlinonetimepassword

import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and
import kotlin.math.pow

/**
 * Generator for the RFC 4226 "HOTP: An HMAC-Based One-Time Password Algorithm"
 * (https://tools.ietf.org/html/rfc4226)
 *
 * @property secret the shared secret as a byte array.
 * @property config the configuration for this generator.
 */
open class HmacOneTimePasswordGenerator(private val secret: ByteArray,
                                        private val config: HmacOneTimePasswordConfig) {
  /**
   * Generates a code representing a HMAC-based one-time password.
   *
   * @return The generated code for the provided counter value. Note, that the
   *         code must be represented as a string because it can have trailing
   *         zeros to meet the code digits requirement from the configuration.
   */
  fun generate(counter: Long): String {
    if (config.codeDigits <= 0) {
      return ""
    }

    // The counter value is the input parameter 'message' to the HMAC algorithm.
    // It must be  represented by a byte array with the length of a long (8 byte).
    //
    // Ongoing example:
    // counter = 1234
    // message = [0, 0, 0, 0, 0, 0, 4, -46]
    val message: ByteBuffer = ByteBuffer.allocate(8).putLong(0, counter)

    // Compute the HMAC hash with the algorithm, 'secret' and 'message' as input parameter.
    //
    // Ongoing example:
    // secret = "Leia"
    // algorithm = "HmacSHA1"
    // hash = [-1, 12, -126, -80, -86, 107, 104, -30, -14, 83, 77, -97, -42, -5, 121, -101, 82, -104, 65, -59]
    val hash = Mac.getInstance(config.hmacAlgorithm.macAlgorithmName).run {
      init(SecretKeySpec(secret, "RAW")) // The hard-coded value 'RAW' is specified in the RFC
      doFinal(message.array())
    }

    // The value of the offset is the lower 4 bits of the last byte of the hash
    // (0x0F = 0000 1111).
    //
    // Ongoing example:
    // The last byte of the hash  is at index 19 and has the value -59. The value
    // of the lower 4 bits of -59 is 5.
    val offset = hash.last().and(0x0F).toInt()

    // The first step for extracting the binary value is to collect the next four
    // bytes from the hash, starting at the index of the offset.
    //
    // Ongoing example:
    // Starting at offset 5, the binary value is [107, 104, -30, -14].
    val binary = ByteBuffer.allocate(4).apply {
      for (i in 0..3) {
        put(i, hash[i + offset])
      }
    }

    // The second step is to drop the most significant bit (MSB) from the first
    // step binary value (0x7F = 0111 1111).
    //
    // Ongoing example:
    // The value at index 0 is 107, which has a MSB of 0. So nothing must be done
    // and the binary value remains the same.
    binary.put(0, binary.get(0).and(0x7F))

    // The resulting integer value of the code must have at most the required code
    // digits. Therefore the binary value is reduced by calculating the modulo
    // 10 ^ codeDigits.
    //
    // On going example:
    // binary = [107, 104, -30, -14] = 137359152
    // codeDigits = 6
    // codeInt = 137359152 % 10^6 = 35954
    val codeInt = binary.int.rem(10.0.pow(config.codeDigits).toInt())

    // The integer code variable may contain a value with fewer digits than the
    // required code digits. Therefore the final code value is filled with zeros
    // on the left, till the code digits requirement is fulfilled.
    //
    // Ongoing example:
    // The current value of the 'oneTimePassword' variable has 5 digits. Therefore
    // the resulting code is filled with one 0 at the beginning, to meet the 6
    // digits requirement.
    return codeInt.toString().padStart(config.codeDigits, '0')
  }

  /**
   * Validates the given code.
   *
   * @param code the code calculated from the challenge to validate.
   * @param counter the used challenge for the code.
   */
  fun isValid(code: String, counter: Long): Boolean {
    return code == generate(counter)
  }
}