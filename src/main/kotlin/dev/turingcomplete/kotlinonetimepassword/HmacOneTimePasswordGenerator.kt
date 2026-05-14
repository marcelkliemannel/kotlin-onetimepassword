package dev.turingcomplete.kotlinonetimepassword

import org.apache.commons.codec.binary.Base32
import java.nio.ByteBuffer
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and

/**
 * Generator for RFC 4226 HOTP: HMAC-Based One-Time Password Algorithm.
 *
 * The generator copies [secret] during construction. Later changes to the
 * provided array do not affect generated codes.
 *
 * @property secret the shared secret.
 * @property config the configuration for this generator.
 *
 * @throws IllegalArgumentException if [secret] is empty.
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc4226">RFC 4226</a>
 */
open class HmacOneTimePasswordGenerator(private val secret: ByteArray,
                                        private val config: HmacOneTimePasswordConfig) {
  // -- Companion Object -------------------------------------------------------------------------------------------- //
  // -- Properties -------------------------------------------------------------------------------------------------- //
  // -- Initialization ---------------------------------------------------------------------------------------------- //

  init {
    require(secret.isNotEmpty()) { "Secret must not be empty." }
  }

  private val secretCopy = secret.copyOf()

  // -- Exposed Methods --------------------------------------------------------------------------------------------- //

  /**
   * Generates an HOTP code for [counter].
   *
   * The counter is encoded as an 8-byte big-endian value as specified by RFC
   * 4226. The returned value is left-padded with zeroes until it reaches
   * [HmacOneTimePasswordConfig.codeDigits].
   *
   * @return the generated code. The code is returned as a string because leading
   * zeroes are significant.
   */
  fun generate(counter: Long): String {
    if (config.codeDigits == 0) {
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
      init(SecretKeySpec(secretCopy, "RAW")) // The hard-coded value 'RAW' is specified in the RFC
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
    // The value at index 0 is 107, which has an MSB of 0. So nothing must be done
    // and the binary value remains the same.
    binary.put(0, binary.get(0).and(0x7F))

    // The resulting integer value of the code must have at most the required code
    // digits. Therefore, the binary value is reduced by calculating the modulo
    // 10 ^ codeDigits.
    //
    // On going example:
    // binary = [107, 104, -30, -14] = 137359152
    // codeDigits = 6
    // codeInt = 137359152 % 10^6 = 35954
    val codeInt = binary.int.rem(codeModulo(config.codeDigits))

    // The integer code variable may contain a value with fewer digits than the
    // required code digits. Therefore, the final code value is filled with zeros
    // on the left, till the code digits requirement is fulfilled.
    //
    // Ongoing example:
    // The current value of the 'oneTimePassword' variable has 5 digits. Therefore
    // the resulting code is filled with one 0 at the beginning, to meet the 6
    // digits requirement.
    return codeInt.toString().padStart(config.codeDigits, '0')
  }

  /**
   * Validates [code] against the HOTP code generated for [counter].
   *
   * The comparison is performed using [MessageDigest.isEqual] to avoid leaking
   * information through simple early-exit string comparison.
   *
   * @param code the code to validate.
   * @param counter the counter value used as the HOTP challenge.
   *
   * @return `true` if [code] matches the generated code for [counter].
   */
  fun isValid(code: String, counter: Long): Boolean {
    val expectedCode = generate(counter)
    return MessageDigest.isEqual(code.toByteArray(Charsets.UTF_8), expectedCode.toByteArray(Charsets.UTF_8))
  }

  /**
   * Creates an [OtpAuthUriBuilder] for an HOTP URI.
   *
   * The returned builder is preconfigured with the Base32-encoded secret,
   * [initialCounter], and the algorithm and digit count from [config].
   *
   * @param initialCounter the initial counter value to place in the URI.
   */
  fun otpAuthUriBuilder(initialCounter: Long): OtpAuthUriBuilder.Hotp {
    return OtpAuthUriBuilder.forHotp(initialCounter, Base32().encode(secretCopy))
      .algorithm(config.hmacAlgorithm)
      .digits(config.codeDigits)
  }

  // -- Private Methods --------------------------------------------------------------------------------------------- //

  private fun codeModulo(codeDigits: Int): Int {
    var modulo = 1
    repeat(codeDigits) {
      modulo *= 10
    }

    return modulo
  }

  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}
