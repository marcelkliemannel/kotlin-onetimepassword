plugins {
  id("application")
  id("org.openjfx.javafxplugin") version "0.0.8"
}

apply(plugin = "java")
apply(plugin = "org.jetbrains.kotlin.jvm")

dependencies {
  implementation(kotlin("stdlib"))

  implementation(project(":"))

  implementation("commons-codec:commons-codec:1.12")
  implementation("no.tornado:tornadofx:1.7.20")
  implementation("com.google.zxing:core:3.4.1")
}

javafx {
  version = "11.0.2"
  modules = listOf("javafx.controls", "javafx.graphics")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs = listOf("-Xjsr305=strict")
    jvmTarget = "11"
  }
}
