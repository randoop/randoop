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
 * Environment variables that would make git use a repository other than the one in its working
 * directory, or make `git config` write a file other than that repository's configuration file.
 */
private val repositoryEnvironmentVariables =
  setOf("GIT_DIR", "GIT_COMMON_DIR", "GIT_WORK_TREE", "GIT_CONFIG")

/**
 * Custom class that sets git configuration variables in the git repository that has a working tree
 * whose top-level directory is a given directory. The variables go in the repository's
 * configuration file, which all of its worktrees share. The task sets the variables only if none of
 * them is set, so it never overrides a configuration that a developer chose. If it cannot set every
 * variable, it unsets the ones that it set, so that a later run sets them all. It does nothing if
 * the directory is not the top level of a working tree (so it never modifies an enclosing
 * repository). It warns, but does not fail, if the [precondition] command or a `git` command fails.
 */
abstract class GitConfigTask @Inject constructor(private val execOperations: ExecOperations) :
  DefaultTask() {

  /** A map from each git configuration variable to its value. */
  @get:Input abstract val settings: MapProperty<String, String>

  /** The top-level directory of a working tree of the git repository to configure. */
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
    try {
      // `git config --get` fails if the variable is not set in any configuration file.
      if (settings.get().keys.any { git(dir, "config", "--get", it) != null }) {
        return
      }
      val preconditionHolds =
        precondition.get().isEmpty() ||
          try {
            runCommand(dir, precondition.get(), discardErrors = true) != null
          } catch (e: Exception) {
            false
          }
      if (!preconditionHolds) {
        logger.warn("Not setting git configuration variables, because ${precondition.get()} failed")
        return
      }
      val setNames = mutableListOf<String>()
      for ((name, value) in settings.get()) {
        if (git(dir, "config", name, value) == null) {
          logger.warn("Could not set git configuration variable $name")
          for (setName in setNames) {
            git(dir, "config", "--unset", setName)
          }
          return
        }
        setNames.add(name)
      }
    } catch (e: Exception) {
      logger.warn("Could not set git configuration variables: $e")
    }
  }

  /**
   * Runs a git command.
   *
   * @param dir the working directory
   * @param args the arguments to git
   * @return the command's standard output without a trailing newline, or null if it fails
   */
  private fun git(dir: java.io.File, vararg args: String): String? =
    runCommand(dir, listOf("git") + args)

  /**
   * Runs a command. The command does not inherit the [repositoryEnvironmentVariables], so git
   * operates on the repository in [dir].
   *
   * @param dir the working directory
   * @param command the command and its arguments
   * @param discardErrors if true, discard the command's standard error rather than showing it
   * @return the command's standard output without a trailing newline, or null if it fails
   */
  private fun runCommand(
    dir: java.io.File,
    command: List<String>,
    discardErrors: Boolean = false,
  ): String? {
    val output = ByteArrayOutputStream()
    val result = execOperations.exec {
      workingDir = dir
      environment.keys.removeAll(repositoryEnvironmentVariables)
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
