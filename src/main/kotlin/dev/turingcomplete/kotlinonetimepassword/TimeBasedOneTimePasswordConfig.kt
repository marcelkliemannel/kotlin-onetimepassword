package dev.turingcomplete.kotlinonetimepassword

import java.lang.IllegalArgumentException
import java.util.concurrent.TimeUnit

/**
 * The configuration for the [TimeBasedOneTimePasswordGenerator].
 *
 * @property timeStep represents together with the [timeStepUnit] parameter the
 *                    time range in which the challenge is valid (e.g. 30 seconds).
 * @property timeStepUnit see [timeStep]
 * @property codeDigits see documentation in [HmacOneTimePasswordConfig].
 * @property hmacAlgorithm see documentation in [HmacOneTimePasswordConfig].
 *
 * @throws IllegalArgumentException if `timeStep` is negative.
 */
open class TimeBasedOneTimePasswordConfig(val timeStep: Long,
                                          val timeStepUnit: TimeUnit,
                                          codeDigits: Int,
                                          hmacAlgorithm: HmacAlgorithm): HmacOneTimePasswordConfig(codeDigits, hmacAlgorithm) {
  // -- Companion Object -------------------------------------------------------------------------------------------- //
  // -- Properties -------------------------------------------------------------------------------------------------- //
  // -- Initialization ---------------------------------------------------------------------------------------------- //

  init {
    require(timeStep >= 0) { "Time step must have a positive value." }
  }

  // -- Exposed Methods --------------------------------------------------------------------------------------------- //
  // -- Private Methods --------------------------------------------------------------------------------------------- //
  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}