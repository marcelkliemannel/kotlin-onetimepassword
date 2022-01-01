import java.net.URI
import kotlin.math.sign

plugins {
  `java-library`
  kotlin("jvm") version "1.3.41"
  id("org.jetbrains.dokka") version "1.4.32"

  signing
  `maven-publish`
}

allprojects {
  group = "dev.turingcomplete"
  version = "2.1.0"

  repositories {
    mavenLocal()
    mavenCentral()
  }
}

tasks {
  val sourcesJar by creating(Jar::class) {
    group = "build"
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
  }

  val testsJar by creating(Jar::class) {
    dependsOn(JavaPlugin.TEST_CLASSES_TASK_NAME)
    group = "build"
    archiveClassifier.set("tests")
    from(sourceSets["test"].output)
  }

  val dokkaJar by creating(Jar::class) {
    dependsOn("dokkaHtml")
    group = "build"
    archiveClassifier.set("javadoc")
    from(getByPath("dokkaHtml").outputs)
  }

  artifacts {
    add("archives", sourcesJar)
    add("archives", testsJar)
    add("archives", dokkaJar)
  }
}

configure<JavaPluginExtension> {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
  implementation(kotlin("stdlib"))
  implementation("commons-codec:commons-codec:1.15")

  val jUnitVersion = "5.8.2"
  testImplementation("org.junit.jupiter:junit-jupiter-params:$jUnitVersion")
  testImplementation("org.junit.jupiter:junit-jupiter-api:$jUnitVersion")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jUnitVersion")
}

tasks.withType<Test> {
  useJUnitPlatform()
}

publishing {
  publications {
    create<MavenPublication>(project.name) {
      from(components["java"])
      setArtifacts(configurations.archives.get().allArtifacts)
    }
  }
}

/**
 * See https://docs.gradle.org/current/userguide/signing_plugin.html#sec:signatory_credentials
 *
 * The following Gradle properties must be set:
 * - signing.keyId (last 8 symbols of the key ID from 'gpg -K')
 * - signing.password
 * - signing.secretKeyRingFile ('gpg --keyring secring.gpg --export-secret-keys > ~/.gnupg/secring.gpg')
 */
signing {
  sign(publishing.publications[project.name])
}

configure<PublishingExtension> {
  publications {
    afterEvaluate {
      named<MavenPublication>(project.name) {
        pom {
          name.set("Kotlin One-Time Password Library")
          description.set("A Kotlin one-time password library to generate \"Google Authenticator\", \"Time-based One-time Password\" (TOTP) and \"HMAC-based One-time Password\" (HOTP) codes based on RFC 4226 and 6238.")
          url.set("https://github.com/marcelkliemannel/kotlin-onetimepassword")
          developers {
            developer {
              name.set("Marcel Kliemannel")
              id.set("marcelkliemannel")
              email.set("dev@marcelkliemannel.com")
            }
          }
          licenses {
            license {
              name.set("The Apache Software License, Version 2.0")
              url.set("http://www.apache.org/licenses/LICENSE-2.0")
            }
          }
          issueManagement {
            system.set("Github")
            url.set("https://github.com/marcelkliemannel/kotlin-onetimepassword/issues")
          }
          scm {
            connection.set("scm:git:git://github.com:marcelkliemannel/kotlin-onetimepassword.git")
            developerConnection.set("scm:git:git://github.com:marcelkliemannel/kotlin-onetimepassword.git")
            url.set("https://github.com/marcelkliemannel/kotlin-onetimepassword")
          }
        }
      }
    }
  }
  repositories {
    maven {
      url = URI("https://oss.sonatype.org/service/local/staging/deploy/maven2")
      credentials {
        username = ""
        password = ""
      }
    }
  }
}