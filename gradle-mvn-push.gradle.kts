import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.PublishToMavenLocal
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.plugins.signing.Sign
import org.gradle.plugins.signing.SigningExtension

val isSnapshot = version.toString().contains("SNAPSHOT")

fun sharedPublicationConfiguration(publication: MavenPublication) {
  publication.pom {
    url = "https://randoop.github.io/randoop/"
    developers {
      // These are the lead developers, not all the contributors.
      developer {
        id = "mernst"
        name = "Michael Ernst"
        email = "mernst@cs.washington.edu"
        url = "https://homes.cs.washington.edu/~mernst/"
        organization = "University of Washington"
        organizationUrl = "https://www.cs.washington.edu/"
      }
    }

    scm {
      url = "https://github.com/randoop/randoop"
      connection = "scm:git:git://github.com/randoop/randoop.git"
      developerConnection = "scm:git:ssh://git@github.com/randoop/randoop.git"
    }

    licenses {
      license {
        name = "The MIT License"
        url = "http://opensource.org/licenses/MIT"
        distribution = "repo"
      }
    }
  }
}

configure<PublishingExtension> {
  publications {
    create<MavenPublication>("remote") {
      sharedPublicationConfiguration(this)
    }

    create<MavenPublication>("local") {
      sharedPublicationConfiguration(this)
    }
  }

  repositories {
    maven {
      url =
        uri(
          if (isSnapshot) {
            providers
              .gradleProperty("SNAPSHOT_REPOSITORY_URL")
              .getOrElse("https://central.sonatype.com/repository/maven-snapshots/")
          } else {
            providers
              .gradleProperty("RELEASE_REPOSITORY_URL")
              .getOrElse("https://ossrh-staging-api.central.sonatype.com/service/local/")
          }
        )
      credentials {
        username = providers.gradleProperty("SONATYPE_NEXUS_USERNAME").orNull
        password = providers.gradleProperty("SONATYPE_NEXUS_PASSWORD").orNull
      }
    }

    maven {
      name = "fakeRemote"
      url = uri("file://${layout.buildDirectory.get()}/maven-fake-remote-repository")
    }
  }
}

val publishingExtension = the<PublishingExtension>()

configure<SigningExtension> {
  // Use external gpg cmd.  This makes it easy to use gpg-agent,
  // to avoid being prompted for a password once per artifact.
  useGpgCmd()

  // If anything about signing is misconfigured, fail loudly rather than quietly continuing with
  // unsigned artifacts.
  isRequired = true

  // Only sign publications sent to remote repositories; local installations are unsigned.
  // The `sign` invocation below causes eager creation of three tasks per subproject:
  // `signRemotePublication` is created immediately and `generateMetadataFileForRemotePublication`
  // and `generatePomFileForRemotePublication` are created during configuration.  Creating these
  // lazily instead will require a fix to <https://github.com/gradle/gradle/issues/8796>.
  sign(publishingExtension.publications["remote"])
}

// Only sign releases; snapshots are unsigned.
tasks.withType<Sign>().configureEach {
  onlyIf {
    !isSnapshot
  }
}

tasks.withType<PublishToMavenRepository>().configureEach {
  onlyIf {
    publication == publishingExtension.publications["remote"]
  }
}

tasks.withType<PublishToMavenLocal>().configureEach {
  onlyIf {
    publication == publishingExtension.publications["local"]
  }
}
