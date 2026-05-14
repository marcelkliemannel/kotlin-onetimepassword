import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  application
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.javafx)
}

dependencies {
  implementation(project(":"))

  implementation(libs.commons.codec)
  implementation(libs.tornadofx)
  implementation(libs.zxing.core)
}

application {
  mainClass.set("GoogleAuthenticatorApp")
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<JavaCompile>().configureEach {
  options.release.set(11)
}

val javaFxVersion = libs.versions.javafx.sdk.get()
javafx {
  version = javaFxVersion
  modules = listOf("javafx.controls", "javafx.graphics")
}

kotlin {
  compilerOptions {
    freeCompilerArgs.add("-Xjsr305=strict")
    jvmTarget.set(JvmTarget.JVM_11)
  }
}
