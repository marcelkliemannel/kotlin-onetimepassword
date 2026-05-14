package dev.turingcomplete.kotlinonetimepassword

import java.util.concurrent.TimeUnit

/**
 * Configuration for [TimeBasedOneTimePasswordGenerator].
 *
 * @property timeStep the size of one TOTP time step, expressed in [timeStepUnit]
 * (for example, `30` with [TimeUnit.SECONDS]).
 * @property timeStepUnit the unit used to interpret [timeStep].
 * @property codeDigits the number of digits in the generated code. See
 * [HmacOneTimePasswordConfig.codeDigits].
 * @property hmacAlgorithm the HMAC algorithm used for code generation. See
 * [HmacOneTimePasswordConfig.hmacAlgorithm].
 * @property allowInsecureConfiguration allows insecure legacy values such as a
 * zero time step or short code length. This should only be used for legacy
 * compatibility or test vectors.
 *
 * @throws IllegalArgumentException if `timeStep` is negative or insecure without
 *                                  explicit opt-in.
 */
open class TimeBasedOneTimePasswordConfig(val timeStep: Long,
                                          val timeStepUnit: TimeUnit,
                                          codeDigits: Int,
                                          hmacAlgorithm: HmacAlgorithm,
                                          allowInsecureConfiguration: Boolean = false):
  HmacOneTimePasswordConfig(codeDigits, hmacAlgorithm, allowInsecureConfiguration) {
  // -- Companion Object -------------------------------------------------------------------------------------------- //
  // -- Properties -------------------------------------------------------------------------------------------------- //
  // -- Initialization ---------------------------------------------------------------------------------------------- //

  /**
   * Creates a secure TOTP configuration.
   *
   * Use the primary constructor with `allowInsecureConfiguration = true` only
   * when reproducing legacy behavior or test vectors that use short codes or a
   * zero time step.
   */
  constructor(timeStep: Long, timeStepUnit: TimeUnit, codeDigits: Int, hmacAlgorithm: HmacAlgorithm):
    this(timeStep, timeStepUnit, codeDigits, hmacAlgorithm, false)

  init {
    require(timeStep >= 0) { "Time step must have a positive value." }
    if (!allowInsecureConfiguration) {
      require(timeStep > 0) { "Time step must be greater than zero. Set allowInsecureConfiguration=true to use a static counter." }
    }
    require(TimeUnit.MILLISECONDS.convert(timeStep, timeStepUnit) > 0 || timeStep == 0L) {
      "Time step must be at least one millisecond."
    }
  }

  // -- Exposed Methods --------------------------------------------------------------------------------------------- //
  // -- Private Methods --------------------------------------------------------------------------------------------- //
  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}
