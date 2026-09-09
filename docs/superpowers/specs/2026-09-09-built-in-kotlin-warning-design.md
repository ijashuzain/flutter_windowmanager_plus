# Built-in Kotlin Warning Fix Design

## Context

A generated Flutter 3.47.2 consumer app using AGP 9.1.0 reproduces the warning from issue #9 when it builds this plugin. Flutter detects the plugin's explicit `kotlin-android` application and reports that future Flutter versions will reject it.

The Android implementation contains only Java source. Its Gradle file nevertheless applies KGP 2.1.20, configures a nonexistent Kotlin source directory, and adds the Kotlin standard library.

## Decision

Keep the native implementation in Java and remove all unused Kotlin build configuration. Built-in Kotlin concerns projects that compile Kotlin; Java remains supported directly by the Android Gradle Plugin. This resolves the warning without a source rewrite or a needless minimum Flutter version increase.

## Changes

- Remove the Kotlin version, KGP classpath, and `kotlin-android` plugin from `android/build.gradle`.
- Remove `kotlinOptions`, the Kotlin source-set entry, and the Kotlin standard-library dependency.
- Keep the existing Android library plugin, AGP version, SDK levels, and Java 17 compile options.
- Add an unreleased changelog entry documenting Flutter 3.47 and AGP 9 compatibility.
- Do not change public Dart or platform-channel behavior.
- Do not change package SDK constraints because the plugin will not use the built-in Kotlin compiler DSL.

## Compatibility

AGP continues compiling `FlutterWindowManagerPlugin.java`. Flutter 3.47 and AGP 9 consumers no longer find an explicitly applied KGP in the plugin. Older supported consumers also avoid loading an unnecessary Kotlin compiler and runtime.

## Validation

- Build the same generated Flutter 3.47.2 and AGP 9.1.0 consumer app and confirm the KGP warning is absent.
- Run `flutter analyze` and `flutter test` with Flutter 3.47.2.
- Run package validation with the existing Flutter 3.41.1 toolchain to catch unintended compatibility regressions.
- Review the final diff for changes outside the Android build configuration, changelog, and approved documentation.

## Delivery

Open a pull request from `fix/issue-9-built-in-kotlin` against `main` with `Closes #9` in the description.
