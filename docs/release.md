# Release Process

This project publishes the root library artifact to Maven Central through
Sonatype's OSSRH Staging API compatibility service. Releases are published from
a local machine with the Go Task runner.

## Branch and Pull Request Rule

All changes to `master` must go through a GitHub pull request. Do not push a
release commit directly to `master`.

The release commit should contain at least the `CHANGELOG.md` release update.
After that pull request is merged, publish from the reviewed `master` commit.

## Version Bump

The project version is not set directly in `gradle.properties`. It is derived
from `CHANGELOG.md` by the Spotless Changelog plugin:

```kotlin
version = spotlessChangelog.versionNext
```

The release tag prefix is configured as `v` through `changelog.tagPrefix` in
`gradle.properties`, so release tags must use the format `v<version>`, for
example `v<version>`.

To prepare a release pull request:

1. Move the entries under `## [Unreleased]` in `CHANGELOG.md` into a new
   released section with the target version and release date, for example
   `## [<version>] - YYYY-MM-DD`.
2. Add a new empty `## [Unreleased]` section above the released section.
3. Update the comparison links at the bottom of `CHANGELOG.md`:
   - `[Unreleased]` should compare the new release tag with `HEAD`.
   - The new version should compare the previous release tag with the new
     release tag.
4. Update the README dependency examples and Maven coordinates:

```shell
task release:update-readme VERSION=<version>
```

5. Verify the computed Gradle version:

```shell
./gradlew -q printVersion
```

The printed version must match the version you intend to release.

## Local Gradle Settings

Publishing requires local Gradle properties that must not be committed. Put
them in `~/.gradle/gradle.properties` or pass them with `-P` on the command
line.

Required for publishing to Sonatype:

```properties
sonatypeUsername=<central-portal-or-ossrh-user-token-name>
sonatypePassword=<central-portal-or-ossrh-user-token-password>
```

Required for in-memory signing:

```properties
signingInMemoryKey=<ascii-armored-private-key>
signingInMemoryKeyPassword=<gpg-key-password>
```

The build still supports Gradle's file-based signing properties, but releases
should prefer in-memory signing so they do not depend on a local
`~/.gnupg/secring.gpg` file.

## Local Release Tasks

Before opening or merging the release pull request, run:

```shell
task release:check VERSION=<version>
```

This verifies that the requested version matches Gradle, checks that
`README.md` contains matching dependency coordinates, checks that `CHANGELOG.md`
contains a matching release section, and runs `./gradlew releaseCheck`.
`releaseCheck` runs the test checks and assembles the artifacts required for a
release, including the main jar, sources jar, tests jar, and Dokka Javadoc jar.

After the release pull request has been merged into `master`, publish from the
local machine:

```shell
task release:publish VERSION=<version>
```

This runs the release checks again, publishes with signing enabled, and then
uploads the OSSRH compatibility staging repository to Sonatype Central Portal:

```shell
./gradlew publishMavenJavaPublicationToSonatypeRepository -Psigning.required=true
./gradlew uploadSonatypeDeployment
```

The second step is required because Gradle's built-in `maven-publish` plugin is
a Maven-repository uploader: it sends the individual POM, JAR, source, Javadoc,
metadata, and signature files with separate `PUT` requests. Sonatype's OSSRH
compatibility service accepts those files, but it cannot reliably infer when the
Gradle upload is complete and ready to become a Central Portal deployment. The
`uploadSonatypeDeployment` task calls Sonatype's manual upload endpoint for the
project namespace and tells the compatibility service to transfer the staged
repository into the Portal.

By default, `uploadSonatypeDeployment` creates a user-managed Portal deployment.
After it succeeds, validate and publish the deployment in Sonatype Central. To
ask Sonatype to publish automatically after validation, run the upload task with
`-PsonatypePublishingType=automatic`.

Then tag the reviewed `master` commit:

```shell
task release:tag VERSION=<version>
```

To run the local publish and tag steps together:

```shell
task release VERSION=<version>
```

Verify the artifact appears on Maven Central before announcing the release.

## Generate and Publish a New Signing Key

Maven Central requires signed artifacts. Generate a GPG key pair, keep the
private key for Gradle signing, and publish only the public key so Sonatype can
validate uploaded `.asc` signatures.

Generate a new key pair:

```shell
gpg --full-generate-key
```

Use a signing-capable RSA key and choose the project publishing identity. Set a
strong passphrase; this is the value for `signingInMemoryKeyPassword`.

List the secret keys and copy the key ID or fingerprint:

```shell
gpg --list-secret-keys --keyid-format LONG
```

Export the private key for Gradle in-memory signing:

```shell
gpg --armor --export-secret-keys <key-id>
```

The exported key contains multiple lines. Convert it to a single
`gradle.properties` value with escaped line breaks:

```shell
gpg --armor --export-secret-keys <key-id> | awk '{printf "%s\\n", $0}'
```

Put the full output into `~/.gradle/gradle.properties` as
`signingInMemoryKey`. The property must be one physical line:

```properties
signingInMemoryKey=-----BEGIN PGP PRIVATE KEY BLOCK-----\n...\n-----END PGP PRIVATE KEY BLOCK-----\n
signingInMemoryKeyPassword=<gpg-key-password>
```

Do not commit the private key.

Publish the public key to a keyserver supported by Sonatype:

```shell
gpg --keyserver keyserver.ubuntu.com --send-keys <key-id>
```

Verify that the public key can be fetched:

```shell
gpg --keyserver keyserver.ubuntu.com --recv-keys <key-id>
```

The Sonatype Central namespace page is only for namespace and publishing access
management. The PGP private key is never uploaded there; Sonatype validates
artifact signatures by resolving the matching public key from the keyserver.
