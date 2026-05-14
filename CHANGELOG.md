# Changelog

All notable changes to this project are documented in this file.

## [Unreleased]

### Added

- Added secure default validation for HOTP and TOTP configuration values.
- Added `allowInsecureConfiguration` opt-in for legacy or test-vector configurations with short code lengths or zero-length TOTP time steps.
- Added `GoogleAuthenticator.createSecureRandomSecretAsByteArray()` for RFC 4226-sized 160-bit secrets.
- Added validation for OTP Auth URI digits, periods, counters, and Base32 secrets.
- Added tests for insecure configuration rejection, invalid OTP Auth URI inputs, and empty random secret lengths.

### Changed

- Updated the build to Gradle 9 and introduced Gradle version catalog configuration.
- Copied provided secret byte arrays before use so later caller mutations do not affect generators.
- Switched HOTP, TOTP, and Google Authenticator code validation to constant-time byte comparisons.
- Replaced floating-point HOTP modulo calculation with integer arithmetic.
- Updated TOTP counter calculation to use integer floor division.
- Clarified documentation around secure code digit ranges, zero time steps, and Google Authenticator-compatible secret lengths.
- Expanded and tightened API documentation across generators, configuration, URI builder, algorithms, and secret generation.

### Fixed

- Rejected empty secrets for generator and URI-builder inputs.
- Rejected empty random secret generation requests.
- Rejected TOTP time steps that convert to less than one millisecond.

## [2.4.1] - 2024-04-15

### Changed

- Improved README wording and minor documentation text.
- Updated copyright year.

### Fixed

- Removed a rogue slash from generated OTP Auth URI output.
- Removed redundant string templating.
- Fixed minor typos.

## [2.4.0] - 2022-08-23

### Added

- Added an OTP Auth URI builder.
- Added a comparison test against popular GitHub TOTP libraries.

### Changed

- Updated project dependencies.

## [2.3.0] - 2022-07-24

### Changed

- Removed default values from convenience methods.
- Clarified that the TOTP generator expects the plain secret bytes and does not operate on Base32-encoded secrets directly.

## [2.2.0] - 2022-01-01

### Changed

- Deprecated string-based Google Authenticator secrets in favor of `ByteArray` secrets.
- Updated Gradle to 7.3.3.
- Updated `commons-codec` to 1.15 and JUnit to 5.8.2.
- Improved documentation and README version references.
- Cleaned up test resources and minor code style.

### Fixed

- Fixed Google Authenticator random secret string generation.
- Used `padStart` to pad generated code strings.
- Fixed minor typos.

## [2.1.0] - 2021-06-03

### Added

- Added a Google Authenticator example application.
- Added TOTP time-slot start calculation support.
- Added tests for TOTP time-slot calculation.

### Changed

- Migrated CI from Travis CI to GitHub Actions.
- Updated Gradle to 7.0.2.
- Updated Dokka for Gradle 7.0.2 compatibility.
- Changed timestamp handling to consistently use Unix timestamps.
- Refactored counter calculation into its own function.
- Improved documentation, README content, and license text.

### Fixed

- Fixed GitHub Actions workflow naming and branch trigger configuration.

## [2.0.1] - 2020-07-03

### Fixed

- Fixed missing floor calculation when deriving the TOTP counter value.

## [2.0.0] - 2019-09-18

### Changed

- Renamed the root package.
- Migrated the build to Gradle Kotlin DSL.
- Updated Kotlin and Gradle versions.
- Adjusted publishing for Nexus and Maven Central.
- Improved README and project documentation.
- Updated license text.
- Improved Travis CI configuration.

### Fixed

- Fixed JUnit 5 unit test execution.
- Fixed examples.

## [1.0.0] - 2018-01-28

### Added

- Initial public release.
- Added first HOTP/TOTP implementation.
- Added project README, manifest, license, and Travis CI configuration.

[Unreleased]: https://github.com/marcelkliemannel/kotlin-onetimepassword/compare/v2.4.1...HEAD
[2.4.1]: https://github.com/marcelkliemannel/kotlin-onetimepassword/compare/v2.4.0...v2.4.1
[2.4.0]: https://github.com/marcelkliemannel/kotlin-onetimepassword/compare/v2.3.0...v2.4.0
[2.3.0]: https://github.com/marcelkliemannel/kotlin-onetimepassword/compare/v2.2.0...v2.3.0
[2.2.0]: https://github.com/marcelkliemannel/kotlin-onetimepassword/compare/v2.1.0...v2.2.0
[2.1.0]: https://github.com/marcelkliemannel/kotlin-onetimepassword/compare/v2.0.1...v2.1.0
[2.0.1]: https://github.com/marcelkliemannel/kotlin-onetimepassword/compare/v2.0.0...v2.0.1
[2.0.0]: https://github.com/marcelkliemannel/kotlin-onetimepassword/compare/v1.0.0...v2.0.0
[1.0.0]: https://github.com/marcelkliemannel/kotlin-onetimepassword/releases/tag/v1.0.0
