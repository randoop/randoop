import java.io.ByteArrayOutputStream
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations

/**
 * Custom class that sets git configuration variables in the repository that contains a given
 * directory. It does nothing if the directory is not in a git repository, and it only warns if a
 * `git` command fails.
 */
abstract class GitConfigTask @Inject constructor(private val execOperations: ExecOperations) :
  DefaultTask() {

  /** A map from each git configuration variable to its value. */
  @get:Input abstract val settings: MapProperty<String, String>

  /** A directory in the git repository to configure. */
  @get:Internal abstract val directory: DirectoryProperty

  @TaskAction
  fun doTaskAction() {
    val dir = directory.get().asFile
    if (!dir.resolve(".git").exists()) {
      return
    }
    for ((name, value) in settings.get()) {
      try {
        // Setting a variable writes the configuration file, which every worktree of the
        // repository shares, so do so only if the variable's value differs.
        if (git(dir, "config", "--get", name) != value && git(dir, "config", name, value) == null) {
          logger.warn("Could not set git configuration variable $name")
        }
      } catch (e: Exception) {
        logger.warn("Could not set git configuration variable $name: $e")
      }
    }
  }

  /**
   * Runs a git command.
   *
   * @param dir the working directory
   * @param args the arguments to git
   * @return the command's standard output without a trailing newline, or null if it fails
   */
  private fun git(dir: java.io.File, vararg args: String): String? {
    val output = ByteArrayOutputStream()
    val result =
      execOperations.exec {
        workingDir = dir
        executable = "git"
        args(*args)
        standardOutput = output
        isIgnoreExitValue = true
      }
    return if (result.exitValue == 0) output.toString().trimEnd('\n') else null
  }
}
