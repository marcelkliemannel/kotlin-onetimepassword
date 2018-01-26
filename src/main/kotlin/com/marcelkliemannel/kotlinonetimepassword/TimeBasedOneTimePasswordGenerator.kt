package com.marcelkliemannel.kotlinonetimepassword

import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Implementation of RFC 6238 "TOTP: Time-Based One-Time Password Algorithm"
 * (https://tools.ietf.org/html/rfc6238)
 *
 * @param secret The shared secret as a byte array.
 */
open class TimeBasedOneTimePasswordGenerator(secret: ByteArray, var config: TimeBasedOneTimePasswordConfig){
  private val hmacOneTimePasswordGenerator: HmacOneTimePasswordGenerator = HmacOneTimePasswordGenerator(secret, config)

  /**
   * @param timestamp The default value is the current system time.
   */
  fun generate(timestamp: Date = Date(System.currentTimeMillis())): String {
    val counter = if (config.timeStep == 0L) {
      0 // Avoiding a dive by zero exception
    }
    else {
      timestamp.time.div(TimeUnit.MILLISECONDS.convert(config.timeStep, config.timeStepUnit))
    }

    return hmacOneTimePasswordGenerator.generate(counter)
  }

  /**
   * @param code The received code to validate.
   * @param timestamp The default value is the current system time.
   */
  fun isValid(code: String, timestamp: Date = Date(System.currentTimeMillis())): Boolean {
    return code == generate(timestamp)
  }
}