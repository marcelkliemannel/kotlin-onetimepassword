package dev.turingcomplete.kotlinonetimepassword

import java.util.concurrent.TimeUnit

/**
 * @param codeDigits See parameter documentation in [HmacOneTimePasswordConfig].
 * @param hmacAlgorithm See parameter documentation in [HmacOneTimePasswordConfig].
 */
open class TimeBasedOneTimePasswordConfig(val timeStep: Long,
                                          val timeStepUnit: TimeUnit,
                                          codeDigits: Int,
                                          hmacAlgorithm: HmacAlgorithm): HmacOneTimePasswordConfig(codeDigits, hmacAlgorithm)