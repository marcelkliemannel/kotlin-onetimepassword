import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  `java-library`
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.dokka)
  alias(libs.plugins.dokka.javadoc)
  alias(libs.plugins.spotless.changelog)
  `maven-publish`
  signing
}

version = spotlessChangelog.versionNext

spotlessChangelog {
  tagPrefix(providers.gradleProperty("changelog.tagPrefix").get())
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8

  withSourcesJar()
}

kotlin {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_1_8)
  }
}

dependencies {
  implementation(libs.commons.codec)

  testImplementation(platform(libs.junit.bom))
  testImplementation(libs.junit.jupiter)
  testRuntimeOnly(libs.junit.platform.launcher)

  testImplementation(libs.otp.java) {
    because("For `OtherLibrariesComparisonTest`")
  }
  testImplementation(libs.java.otp) {
    because("For `OtherLibrariesComparisonTest`")
  }
  testImplementation(libs.two.factor.auth) {
    because("For `OtherLibrariesComparisonTest`")
  }
}

tasks.withType<JavaCompile>().configureEach {
  options.release.set(8)
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
}

val testsJar by tasks.registering(Jar::class) {
  group = LifecycleBasePlugin.BUILD_GROUP
  description = "Assembles a jar archive containing the test classes."
  archiveClassifier.set("tests")
  from(sourceSets.test.map { it.output })
}

val dokkaJavadocJar by tasks.registering(Jar::class) {
  group = LifecycleBasePlugin.BUILD_GROUP
  description = "Assembles a jar archive containing Dokka Javadoc."
  dependsOn(tasks.named("dokkaGeneratePublicationJavadoc"))
  archiveClassifier.set("javadoc")
  from(tasks.named("dokkaGeneratePublicationJavadoc"))
}

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      from(components["java"])
      artifact(testsJar)
      artifact(dokkaJavadocJar)

      pom {
        name.set("Kotlin One-Time Password Library")
        description.set("A Kotlin one-time password library to generate \"Google Authenticator\", \"Time-based One-time Password\" (TOTP) and \"HMAC-based One-time Password\" (HOTP) codes based on RFC 4226 and 6238.")
        url.set("https://github.com/marcelkliemannel/kotlin-onetimepassword")
        developers {
          developer {
            id.set("marcelkliemannel")
            name.set("Marcel Kliemannel")
            email.set("dev@marcelkliemannel.com")
          }
        }
        licenses {
          license {
            name.set("The Apache Software License, Version 2.0")
            url.set("https://www.apache.org/licenses/LICENSE-2.0")
          }
        }
        issueManagement {
          system.set("GitHub")
          url.set("https://github.com/marcelkliemannel/kotlin-onetimepassword/issues")
        }
        scm {
          connection.set("scm:git:git://github.com:marcelkliemannel/kotlin-onetimepassword.git")
          developerConnection.set("scm:git:ssh://github.com/marcelkliemannel/kotlin-onetimepassword.git")
          url.set("https://github.com/marcelkliemannel/kotlin-onetimepassword")
        }
      }
    }
  }

  repositories {
    maven {
      name = "sonatype"
      url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
      credentials {
        username = providers.gradleProperty("sonatypeUsername").orNull
        password = providers.gradleProperty("sonatypePassword").orNull
      }
    }
  }
}

signing {
  isRequired = providers.gradleProperty("signing.required").map(String::toBoolean).getOrElse(false)
  sign(publishing.publications["mavenJava"])
}
