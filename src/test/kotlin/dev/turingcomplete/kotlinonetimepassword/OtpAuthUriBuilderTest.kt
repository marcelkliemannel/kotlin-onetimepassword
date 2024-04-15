package dev.turingcomplete.kotlinonetimepassword

import org.apache.commons.codec.binary.Base32
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.util.concurrent.TimeUnit

/**
 * Tests the [OtpAuthUriBuilder].
 */
class OtpAuthUriBuilderTest {
  // -- Companion Object -------------------------------------------------------------------------------------------- //

  companion object {
    private val BASE32_SECRET = Base32().encode("secret$9".toByteArray())
    private const val BASE32_SECRET_REMOVED_PADDING = "ONSWG4TFOQSDS"
    
    init {
      assertTrue(String(BASE32_SECRET).startsWith(BASE32_SECRET_REMOVED_PADDING))
    }
  }

  // -- Properties -------------------------------------------------------------------------------------------------- //
  // -- Initialization ---------------------------------------------------------------------------------------------- //
  // -- Exposed Methods --------------------------------------------------------------------------------------------- //

  @Test
  fun testTotpPeriodParameter() {
    assertEquals("otpauth://totp/?period=10&secret=$BASE32_SECRET_REMOVED_PADDING",
                 OtpAuthUriBuilder.forTotp(BASE32_SECRET)
                   .period(10, TimeUnit.SECONDS)
                   .buildToString())

    assertEquals("otpauth://totp/?period=600&secret=$BASE32_SECRET_REMOVED_PADDING",
                 OtpAuthUriBuilder.forTotp(BASE32_SECRET)
                   .period(10, TimeUnit.MINUTES)
                   .buildToString())
  }

  @Test
  fun testHotpCounterByinitialCounterParameter() {
    assertEquals("otpauth://hotp/?counter=99999&secret=$BASE32_SECRET_REMOVED_PADDING",
                 OtpAuthUriBuilder.forHotp(99999, BASE32_SECRET)
                   .buildToString())
  }

  @Test
  fun testHotpCounterParameter() {
    assertEquals("otpauth://hotp/?counter=888888&secret=$BASE32_SECRET_REMOVED_PADDING",
                 OtpAuthUriBuilder.forHotp(99999, BASE32_SECRET)
                   .counter(888888)
                   .buildToString())
  }

  @Test
  fun testDigitsParameter() {
    assertEquals("otpauth://totp/?digits=333&secret=$BASE32_SECRET_REMOVED_PADDING",
                 OtpAuthUriBuilder.forTotp(BASE32_SECRET)
                   .digits(333)
                   .buildToString())
  }

  @Test
  fun testAlgorithmParameter() {
    assertEquals("otpauth://totp/?algorithm=SHA512&secret=$BASE32_SECRET_REMOVED_PADDING",
                 OtpAuthUriBuilder.forTotp(BASE32_SECRET)
                   .algorithm(HmacAlgorithm.SHA512)
                   .buildToString())
  }

  @Test
  fun testIssuerParameter() {
    assertEquals("otpauth://totp/?issuer=foo&secret=$BASE32_SECRET_REMOVED_PADDING",
                 OtpAuthUriBuilder.forTotp(BASE32_SECRET)
                   .issuer("foo")
                   .buildToString())
  }

  @Test
  fun testIssuerParameterUrlEncoding() {
    assertEquals("otpauth://totp/?issuer=f%21%21oo&secret=$BASE32_SECRET_REMOVED_PADDING",
                 OtpAuthUriBuilder.forTotp(BASE32_SECRET)
                   .issuer("f!!oo")
                   .buildToString())
  }

  @Test
  fun testLabel() {
    assertEquals("otpauth://totp/iss:acc?secret=$BASE32_SECRET_REMOVED_PADDING",
                 OtpAuthUriBuilder.forTotp(BASE32_SECRET)
                   .label("acc", "iss", false)
                   .buildToString())

    assertEquals("otpauth://totp/acc?secret=$BASE32_SECRET_REMOVED_PADDING",
                 OtpAuthUriBuilder.forTotp(BASE32_SECRET)
                   .label("acc", null, false)
                   .buildToString())

    assertEquals("otpauth://totp/iss%3Aacc?secret=$BASE32_SECRET_REMOVED_PADDING",
                 OtpAuthUriBuilder.forTotp(BASE32_SECRET)
                   .label("acc", "iss", true)
                   .buildToString())
  }

  @Test
  fun testLabelUrlEncoding() {
    assertEquals("otpauth://totp/i%2F%2Fss%3Aa%2F%2Fcc?secret=$BASE32_SECRET_REMOVED_PADDING",
                 OtpAuthUriBuilder.forTotp(BASE32_SECRET)
                   .label("a//cc", "i//ss", true)
                   .buildToString())
  }

  @Test
  fun testBuildTo() {
    val builder = OtpAuthUriBuilder.forTotp(BASE32_SECRET)
      .label("acc", "i/ss", false)
      .issuer("i/ss")
      .digits(8)

    val stringRepresentation = builder.buildToString()
    assertEquals("otpauth://totp/i%2Fss:acc?issuer=i%2Fss&digits=8&secret=$BASE32_SECRET_REMOVED_PADDING", stringRepresentation)

    val byteArrayRepresentation = builder.buildToByteArray()
    assertEquals(stringRepresentation, String(byteArrayRepresentation))

    val urlRepresentation = builder.buildToUri()
    assertEquals(stringRepresentation, urlRepresentation.toString())
  }

  @ParameterizedTest
  @CsvSource(value = [
    ", ", // null will be transformed to empty string
    "a, ME", // ME======
    "aa, MFQQ", // MFQQ====
    "aaa, MFQWC", // MFQWC===
    "aaaa, MFQWCYI", // MFQWCYI=
    "aaaaa, MFQWCYLB", // MFQWCYLB
    "aaaaaa, MFQWCYLBME", // MFQWCYLBME======
    "aaaaaaa, MFQWCYLBMFQQ", // MFQWCYLBMFQQ====
    "aaaaaaaa, MFQWCYLBMFQWC", // MFQWCYLBMFQWC===
  ])
  fun testRemoveBase32SecretPadding(secret: String?, expectedSecretParameterValue: String?) {
    val secretParameterRegex = Regex("^.*[&|?]secret=(.*)$")
    val otpAuthUri = OtpAuthUriBuilder.forTotp(Base32().encode((secret ?: "").toByteArray())).buildToString()
    val actualSecretParameterValue = secretParameterRegex.matchEntire(otpAuthUri)!!.groups[1]!!.value
    assertEquals(expectedSecretParameterValue ?: "", actualSecretParameterValue)
  }

  // -- Private Methods --------------------------------------------------------------------------------------------- //
  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}
