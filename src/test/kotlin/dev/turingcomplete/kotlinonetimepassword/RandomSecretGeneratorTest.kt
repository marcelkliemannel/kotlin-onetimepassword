package dev.turingcomplete.kotlinonetimepassword

import dev.turingcomplete.kotlinonetimepassword.HmacAlgorithm
import dev.turingcomplete.kotlinonetimepassword.RandomSecretGenerator
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class RandomSecretGeneratorTest {
  // -- Companion Object -------------------------------------------------------------------------------------------- //
  // -- Properties -------------------------------------------------------------------------------------------------- //
  // -- Initialization ---------------------------------------------------------------------------------------------- //
  // -- Exposed Methods --------------------------------------------------------------------------------------------- //

  @Test
  @DisplayName("Same secret length as the HMAC algorithm hash")
  fun testExpectedHmacAlgorithmHashLength() {
    HmacAlgorithm.values().forEach {
      val randomSecret = RandomSecretGenerator().createRandomSecret(it)
      Assertions.assertEquals(it.hashBytes, randomSecret.size)
    }
  }

  // -- Private Methods --------------------------------------------------------------------------------------------- //
  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}