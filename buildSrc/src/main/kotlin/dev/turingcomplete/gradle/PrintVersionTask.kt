package dev.turingcomplete.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class PrintVersionTask : DefaultTask() {
  @get:Input
  abstract val versionText: Property<String>

  @TaskAction
  fun printVersion() {
    logger.quiet(versionText.get())
  }
}
