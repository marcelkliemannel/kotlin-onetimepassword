import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import dev.turingcomplete.kotlinonetimepassword.GoogleAuthenticator
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.util.Duration
import org.apache.commons.codec.binary.Base32
import tornadofx.*
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime


class CodeGeneratorView : View() {
  // -- Companion Object -------------------------------------------------------------------------------------------- //

  companion object {
    private const val QR_CODE_SIZE = 180
  }

  // -- Properties -------------------------------------------------------------------------------------------------- //

  override val root = GridPane()

  private var plainTextSecret = "Secret1234".toByteArray(StandardCharsets.UTF_8)

  private val plainTextSecretTextField = TextField(String(plainTextSecret))
  private val base32encodedSecretTextField = TextField().apply { isEditable = false }
  private val base32encodedSecretQrCode = Canvas(QR_CODE_SIZE.toDouble(), QR_CODE_SIZE.toDouble())
  private val codeTextField = TextField().apply { isEditable = false }
  private val codeValidlyIndicator = ProgressBar(0.0)

  // -- Initialization ---------------------------------------------------------------------------------------------- //

  init {

    title = "Google Authenticator Example"

    root.paddingAll = 20
    root.hgap = 4.0
    root.vgap = 4.0

    var row = 0
    root.add(Label("Must have 10 characters to work correctly with most generator apps."), 0, row, 2, 1)

    root.add(Label("Plain text secret:"), 0, ++row, 1, 1)
    root.add(plainTextSecretTextField, 1, row, 1, 1)
    plainTextSecretTextField.textProperty().addListener { _, _, newValue ->
      if (newValue.isBlank()) {
        return@addListener
      }
      generateGoogleAuthenticatorCode()
      refreshQrCode()
    }

    val addSeparator : () -> Separator = {
      val separator = Separator()
      root.add(separator, 0, ++row, 2, 1)
      GridPane.setMargin(separator, Insets(8.0, 0.0, 8.0, 0.0))
      separator
    }
    addSeparator()

    root.add(Label("This is the secret that must be used in the generator apps."), 0, ++row, 2, 1)
    root.add(Label("Base32-encoded secret:"), 0, ++row, 1, 1)
    root.add(base32encodedSecretTextField, 1, row, 1, 1)
    root.add(Button("Generate random").apply {
      setOnAction {
        val createRandomSecret = GoogleAuthenticator.createRandomSecret()
        plainTextSecret = Base32().decode(createRandomSecret.toByteArray(StandardCharsets.UTF_8))
        plainTextSecretTextField.text = String(plainTextSecret)
        generateGoogleAuthenticatorCode()
        refreshQrCode()
      }
    }, 1, ++row, 1, 1)
    root.add(base32encodedSecretQrCode, 1, ++row, 1, 1)

    addSeparator()

    root.add(Label("Code:"), 0, ++row, 1, 1)
    root.add(codeTextField, 1, row, 1, 1)

    root.add(Label("Code validity:"), 0, ++row, 1, 1)
    root.add(codeValidlyIndicator, 1, row, 1, 1)
    val codeValidlyUpdate = Timeline(KeyFrame(Duration.seconds(1.0), EventHandler<ActionEvent?> {
      generateGoogleAuthenticatorCode()
    }))
    codeValidlyUpdate.cycleCount = Timeline.INDEFINITE
    codeValidlyUpdate.play()

    // Initial generation
    generateGoogleAuthenticatorCode()
    refreshQrCode()
  }

  // -- Exposed Methods --------------------------------------------------------------------------------------------- //
  // -- Private Methods --------------------------------------------------------------------------------------------- //

  private fun generateGoogleAuthenticatorCode() {
    val base32secret = Base32().encodeToString(plainTextSecret)
    base32encodedSecretTextField.text = base32secret

    val googleAuthenticator = GoogleAuthenticator(base32secret)
    codeTextField.text = googleAuthenticator.generate()

    val second = (LocalDateTime.now().second) % 30
    codeValidlyIndicator.progress = 1.0 - (second / 30.0)
  }

  private fun refreshQrCode() {
    val base32secret = base32encodedSecretTextField.text
    if (base32secret.isBlank()) {
      return
    }

    // See https://github.com/google/google-authenticator/wiki/Key-Uri-Format
    val qrText = "otpauth://totp/me@company.com:?secret=$base32secret&issuer=GoogleAuthenticatorExample"
    val qurCodeMatrix = QRCodeWriter().encode(qrText, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE,
                                              mapOf(Pair(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L)))

    val graphics: GraphicsContext = base32encodedSecretQrCode.graphicsContext2D

    // Background
    graphics.fill = javafx.scene.paint.Color.WHITE
    graphics.fillRect(0.0, 0.0, QR_CODE_SIZE.toDouble(), QR_CODE_SIZE.toDouble())

    // QR code
    graphics.fill = javafx.scene.paint.Color.BLACK
    for (i in 0 until qurCodeMatrix.width) {
      for (j in 0 until qurCodeMatrix.height) {
        if (qurCodeMatrix[i, j]) {
          graphics.fillRect(i.toDouble(), j.toDouble(), 1.0, 1.0)
        }
      }
    }
  }

  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}