import tornadofx.*

class GoogleAuthenticatorApp : App(CodeGeneratorView::class)

fun main(args: Array<String>) {
  System.setProperty("prism.lcdtext", "false") // Fix fonts on mac
  launch<GoogleAuthenticatorApp>(args)
}