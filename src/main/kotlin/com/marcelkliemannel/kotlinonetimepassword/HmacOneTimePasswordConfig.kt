package com.marcelkliemannel.kotlinonetimepassword

/**
 * @property codeDigits The RFC 4226 requires a code digits value between 6 and 8, to assure a good security trade-off.
 * However, this library does not set any requirement for this property. But notice that through the design of the
 * algorithm the maximum code value is 2_147_483_647. Which means that a larger code digits value than 10 just
 * adds more trailing zeros to the code (and in case of 10 digits the first number is 0, 1 or 2).
 *
 * @property hmacAlgorithm The "keyed-hash message authentication code" algorithm to use to generate the hash,
 * from which the code is extracted (see [HmacAlgorithm] for available algorithms).
 */
open class HmacOneTimePasswordConfig(var codeDigits: Int, var hmacAlgorithm: HmacAlgorithm)