import java.io.ByteArrayOutputStream
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations

/**
 * Custom class that sets git configuration variables in the git working tree whose top-level
 * directory is a given directory. It sets only variables that are not already set, so it never
 * overrides a value that a developer chose. It does nothing if the directory is not the top level
 * of a git working tree (so it never modifies an enclosing repository) or if the [precondition]
 * command fails, and it only warns if a `git` command fails.
 */
abstract class GitConfigTask @Inject constructor(private val execOperations: ExecOperations) :
  DefaultTask() {

  /** A map from each git configuration variable to the value to give it if it is not set. */
  @get:Input abstract val settings: MapProperty<String, String>

  /** The top-level directory of the git working tree to configure. */
  @get:Internal abstract val directory: DirectoryProperty

  /**
   * A command that must succeed, when run in [directory], before the task sets any variable. If it
   * is empty, the task sets variables unconditionally.
   */
  @get:Input abstract val precondition: ListProperty<String>

  @TaskAction
  fun doTaskAction() {
    val dir = directory.get().asFile
    if (!dir.resolve(".git").exists()) {
      return
    }
    // Whether the precondition command succeeds, or null if it has not been run.
    var preconditionHolds: Boolean? = null
    for ((name, value) in settings.get()) {
      try {
        // `git config --get` fails if the variable is not set in any configuration file.
        if (git(dir, "config", "--get", name) != null) {
          continue
        }
        if (preconditionHolds == null) {
          preconditionHolds =
            precondition.get().isEmpty() ||
              run(dir, precondition.get(), discardErrors = true) != null
          if (!preconditionHolds) {
            logger.info("Not setting git configuration variables: ${precondition.get()} failed")
          }
        }
        if (!preconditionHolds) {
          return
        }
        if (git(dir, "config", name, value) == null) {
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
  private fun git(dir: java.io.File, vararg args: String): String? = run(dir, listOf("git") + args)

  /**
   * Runs a command. The command does not inherit `GIT_*` environment variables, such as `GIT_DIR`,
   * which could make git operate on a repository other than the one in [dir].
   *
   * @param dir the working directory
   * @param command the command and its arguments
   * @param discardErrors if true, discard the command's standard error rather than showing it
   * @return the command's standard output without a trailing newline, or null if it fails
   */
  private fun run(
    dir: java.io.File,
    command: List<String>,
    discardErrors: Boolean = false,
  ): String? {
    val output = ByteArrayOutputStream()
    val result = execOperations.exec {
      workingDir = dir
      environment = System.getenv().filterKeys { !it.startsWith("GIT_") }
      commandLine(command)
      standardOutput = output
      if (discardErrors) {
        errorOutput = ByteArrayOutputStream()
      }
      isIgnoreExitValue = true
    }
    return if (result.exitValue == 0) output.toString().trimEnd('\n') else null
  }
}
