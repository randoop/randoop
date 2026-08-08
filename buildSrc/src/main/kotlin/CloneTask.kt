import java.io.File
import java.time.Duration
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations

/** Custom class that clones or updates a given Git repository. */
abstract class CloneTask @Inject constructor(private val execOperations: ExecOperations) :
  DefaultTask() {

  @get:Input abstract val url: Property<String>

  @get:OutputDirectory abstract val directory: Property<File>

  init {
    // A `git` invocation that hangs should not hang the build.  The budget covers two clone
    // attempts plus the RETRY_DELAY between them.
    timeout.set(RETRY_DELAY.multipliedBy(3))
  }

  @TaskAction
  fun doTaskAction() {
    cloneAndUpdate(url.get(), directory.get())
  }

  private fun cloneAndUpdate(url: String, directory: File) {
    // Gradle creates the directory if it does not exist, so check to see if the directory has a
    // .git directory.
    if (File(directory, ".git").exists()) {
      execOperations.exec {
        workingDir = directory
        executable = "git"
        args("pull", "-q")
        isIgnoreExitValue = true
      }
    } else {
      try {
        clone(url, directory, true)
      } catch (t: Throwable) {
        println("Exception while cloning $url")
        t.printStackTrace()
      }
      if (!File(directory, ".git").exists()) {
        println(
          "Cloning failed, will try again in ${RETRY_DELAY.toSeconds()} seconds:" +
            " clone($url, $directory, true)"
        )
        Thread.sleep(RETRY_DELAY.toMillis())
        clone(url, directory, false)
      }
    }
  }

  /**
   * Quietly clones the given git repository, [url], to [directory] at a depth of 1.
   *
   * @param url git repository to clone
   * @param directory where to clone
   * @param ignoreError whether to fail the build if the clone command fails
   * @param extraArgs any extra arguments to pass to git
   */
  private fun clone(
    url: String,
    directory: File,
    ignoreError: Boolean,
    extraArgs: List<String> = emptyList(),
  ) {
    execOperations.exec {
      workingDir = directory.parentFile
      executable = "git"
      args("clone", "-q", "--depth=1", url, directory.name)
      args(extraArgs)
      isIgnoreExitValue = ignoreError
    }
  }

  companion object {
    /** How long to wait after a failed clone, before trying again. */
    private val RETRY_DELAY: Duration = Duration.ofMinutes(1)
  }
}
