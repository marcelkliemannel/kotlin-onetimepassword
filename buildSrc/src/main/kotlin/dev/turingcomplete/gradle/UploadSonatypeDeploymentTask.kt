package dev.turingcomplete.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.net.HttpURLConnection
import java.net.URI
import java.util.Base64

abstract class UploadSonatypeDeploymentTask : DefaultTask() {
  @get:Input
  abstract val namespace: Property<String>

  @get:Internal
  abstract val username: Property<String>

  @get:Internal
  abstract val password: Property<String>

  @get:Input
  abstract val publishingType: Property<String>

  @TaskAction
  fun upload() {
    val resolvedUsername = username.get()
    val resolvedPassword = password.get()

    if (resolvedUsername.isBlank() || resolvedPassword.isBlank()) {
      throw GradleException(
        "Uploading the Sonatype deployment requires non-blank 'sonatypeUsername' and 'sonatypePassword' " +
          "Gradle properties. Use a Central Portal user token name and password in ~/.gradle/gradle.properties " +
          "or pass them with -P."
      )
    }

    val token = Base64.getEncoder()
      .encodeToString("$resolvedUsername:$resolvedPassword".toByteArray(Charsets.UTF_8))
    val endpoint = URI(
      "https://ossrh-staging-api.central.sonatype.com/manual/upload/defaultRepository/${namespace.get()}" +
        "?publishing_type=${publishingType.get()}"
    ).toURL()
    val connection = endpoint.openConnection() as HttpURLConnection

    connection.requestMethod = "POST"
    connection.setRequestProperty("Authorization", "Bearer $token")
    connection.connectTimeout = 30_000
    connection.readTimeout = 120_000

    val responseCode = connection.responseCode
    val responseBody = if (responseCode in 200..299) {
      connection.inputStream.bufferedReader().use { it.readText() }
    }
    else {
      connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
    }

    if (responseCode !in 200..299) {
      throw GradleException(
        "Sonatype deployment upload failed with HTTP $responseCode." +
          responseBody.takeIf { it.isNotBlank() }?.let { " Response: $it" }.orEmpty()
      )
    }

    logger.lifecycle("Sonatype deployment upload requested for namespace '${namespace.get()}'.")
    if (responseBody.isNotBlank()) {
      logger.info(responseBody)
    }
  }
}
