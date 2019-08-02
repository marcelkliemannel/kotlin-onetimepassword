plugins {
  `java-library`
  kotlin("jvm") version "1.3.41"

  signing
  `maven-publish`
  id("de.marcphilipp.nexus-publish") version "0.2.0"
}

group = "dev.turingcomplete"
version = "1.1.0"

tasks.withType<Wrapper> {
  gradleVersion = "5.5.1"
}

repositories {
  mavenCentral()
}

configure<JavaPluginExtension> {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
  implementation(kotlin("stdlib"))
  implementation("commons-codec:commons-codec:1.12")

  val jUnitVersion = "5.5.1"
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
    }
  }
}

/*
  signing.keyId=
  signing.password=
  signing.secretKeyRingFile=
 */
signing {
  sign(publishing.publications[project.name])
}

/*
  nexusUsername=
  nexusPassword=
 */

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
              name.set("MIT License")
              url.set("https://opensource.org/licenses/MIT")
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
}