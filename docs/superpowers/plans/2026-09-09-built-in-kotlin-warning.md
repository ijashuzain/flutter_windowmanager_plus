# Built-in Kotlin Warning Fix Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Stop Flutter 3.47+ from reporting `flutter_windowmanager_plus` as a plugin that applies KGP while preserving its Java implementation and current package compatibility.

**Architecture:** The Android plugin remains a Java-only `com.android.library` module. Remove every Kotlin-specific build input because no Kotlin source exists, then validate the plugin through a generated Flutter 3.47.2 consumer using AGP 9.1.0.

**Tech Stack:** Flutter 3.47.2 and 3.41.1, Dart, Android Gradle Plugin 9.1.0 consumer integration, Android Gradle Plugin 8.13.0 plugin configuration, Gradle, Java 17, GitHub CLI.

## Global Constraints

- Keep `FlutterWindowManagerPlugin.java`; do not rewrite native code.
- Keep current AGP version, SDK levels, Java 17 compile options, Dart SDK constraint, and Flutter SDK constraint.
- Do not add Kotlin compiler configuration or dependencies.
- Do not change public Dart or platform-channel behavior.
- Limit product changes to `android/build.gradle` and `CHANGELOG.md`.
- Validate warning removal using FVM Flutter 3.47.2 and AGP 9.1.0.

---

### Task 1: Remove Redundant Kotlin Tooling

**Files:**
- Modify: `android/build.gradle:4-50`
- Modify: `CHANGELOG.md:1`
- Test through: `/var/folders/n4/h6n7qt9x4hl3d8f7rtw9zxy80000gn/T/opencode/flutter_windowmanager_plus_issue_9`

**Interfaces:**
- Consumes: Flutter plugin metadata from `pubspec.yaml` and Java source under `android/src/main/java`.
- Produces: Java-only Android library configuration with no applied Kotlin Gradle Plugin.

- [ ] **Step 1: Confirm failing integration baseline**

Run:

```bash
DEVELOPER_DIR=/Library/Developer/CommandLineTools /Users/ijashuzain/fvm/versions/3.47.2/bin/flutter build apk --debug
```

Run from `/var/folders/n4/h6n7qt9x4hl3d8f7rtw9zxy80000gn/T/opencode/flutter_windowmanager_plus_issue_9`.

Expected: APK build succeeds and output contains:

```text
WARNING: Your app uses the following plugins that apply Kotlin Gradle Plugin (KGP): flutter_windowmanager_plus
```

- [ ] **Step 2: Remove Kotlin build configuration**

Apply this focused diff to `android/build.gradle`:

```diff
 buildscript {
-    ext.kotlin_version = '2.1.20'
     repositories {
         google()
         mavenCentral()
     }
 
     dependencies {
         classpath 'com.android.tools.build:gradle:8.13.0'
-        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
     }
 }
@@
 apply plugin: 'com.android.library'
-apply plugin: 'kotlin-android'
 
 android {
@@
-    kotlinOptions {
-        jvmTarget = '17'
-    }
-
-    sourceSets {
-        main.java.srcDirs += 'src/main/kotlin'
-    }
-
     defaultConfig {
         minSdkVersion 16
     }
 }
-
-dependencies {
-    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version"
-}
```

- [ ] **Step 3: Document warning fix**

Prepend this section to `CHANGELOG.md`:

```markdown
## [Unreleased]

### Fixed
- Removed the unused Kotlin Gradle Plugin from the Java-only Android module, eliminating the Flutter 3.47+ Built-in Kotlin migration warning and supporting AGP 9 consumers ([#9]).

[#9]: https://github.com/ijashuzain/flutter_windowmanager_plus/issues/9

```

- [ ] **Step 4: Verify no Kotlin build inputs remain**

Search `android/build.gradle` for `kotlin`, `Kotlin`, and `org.jetbrains.kotlin`.

Expected: no matches.

- [ ] **Step 5: Verify Flutter 3.47.2 consumer build**

Run:

```bash
DEVELOPER_DIR=/Library/Developer/CommandLineTools /Users/ijashuzain/fvm/versions/3.47.2/bin/flutter clean
DEVELOPER_DIR=/Library/Developer/CommandLineTools /Users/ijashuzain/fvm/versions/3.47.2/bin/flutter pub get
DEVELOPER_DIR=/Library/Developer/CommandLineTools /Users/ijashuzain/fvm/versions/3.47.2/bin/flutter build apk --debug
```

Run from `/var/folders/n4/h6n7qt9x4hl3d8f7rtw9zxy80000gn/T/opencode/flutter_windowmanager_plus_issue_9`.

Expected: `build/app/outputs/flutter-apk/app-debug.apk` is produced and output contains no KGP plugin warning.

- [ ] **Step 6: Run package checks on Flutter 3.47.2**

Run from repository root:

```bash
DEVELOPER_DIR=/Library/Developer/CommandLineTools /Users/ijashuzain/fvm/versions/3.47.2/bin/flutter analyze
DEVELOPER_DIR=/Library/Developer/CommandLineTools /Users/ijashuzain/fvm/versions/3.47.2/bin/flutter test
```

Expected: analysis reports no issues and all tests pass.

- [ ] **Step 7: Run compatibility checks on Flutter 3.41.1**

Run from repository root:

```bash
DEVELOPER_DIR=/Library/Developer/CommandLineTools /Users/ijashuzain/fvm/versions/3.41.1/bin/flutter analyze
DEVELOPER_DIR=/Library/Developer/CommandLineTools /Users/ijashuzain/fvm/versions/3.41.1/bin/flutter test
```

Expected: analysis reports no issues and all tests pass.

- [ ] **Step 8: Commit implementation**

```bash
git add android/build.gradle CHANGELOG.md
git commit -m "fix(android): remove unused Kotlin plugin"
```

### Task 2: Publish Pull Request

**Files:**
- Review: all changes from `main...fix/issue-9-built-in-kotlin`

**Interfaces:**
- Consumes: verified branch commits and GitHub issue #9.
- Produces: pull request against `ijashuzain/flutter_windowmanager_plus:main` that automatically closes issue #9 when merged.

- [ ] **Step 1: Inspect branch before publishing**

Run:

```bash
git status --short --branch
git diff --check
git log --oneline -10
git diff main...HEAD
```

Expected: clean branch, no whitespace errors, two documentation commits plus one implementation commit, and only approved files changed.

- [ ] **Step 2: Select requested GitHub account and push**

Run:

```bash
gh auth switch --hostname github.com --user ijashuzain
git push -u origin fix/issue-9-built-in-kotlin
```

Expected: branch is available on `ijashuzain/flutter_windowmanager_plus`.

- [ ] **Step 3: Open pull request**

Run:

```bash
gh pr create --repo ijashuzain/flutter_windowmanager_plus --base main --head fix/issue-9-built-in-kotlin --title "fix(android): remove unused Kotlin Gradle plugin" --body-file /var/folders/n4/h6n7qt9x4hl3d8f7rtw9zxy80000gn/T/opencode/flutter-windowmanager-plus-pr.md
```

PR body must summarize Java-only Kotlin tooling removal, list Flutter 3.47.2 consumer build plus Flutter 3.47.2 and 3.41.1 checks, and include `Closes #9`.

Expected: GitHub returns the new pull request URL.
