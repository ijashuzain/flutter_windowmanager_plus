## [1.1.1]

### Fixed
- Removed the unused Kotlin Gradle Plugin from the Java-only Android module,
  eliminating the Flutter 3.47+ Built-in Kotlin migration warning and supporting
  AGP 9 consumers ([#9]).

[#9]: https://github.com/ijashuzain/flutter_windowmanager_plus/issues/9

## [1.1.0]

### Added
- `FlutterWindowManagerPlus.setSecure(bool)` as a single-call way to toggle `FLAG_SECURE`.
- Unit tests covering the platform channel calls for every public method.

### Fixed
- Window flags are now re-applied when Android recreates the Activity (rotation,
  multi-window resize, "don't keep activities", locale change). Previously the new
  Activity got a fresh `Window` without the flags, so `FLAG_SECURE` was silently
  dropped and the app became screenshot-able again ([#6]).
- Flag validation compared `flags & flag` against `1` instead of `0`, so every flag
  except bit 0 skipped validation entirely and invalid flags were passed straight
  through to `Window#addFlags`.
- Deprecated-but-functional flags (`FLAG_SHOW_WHEN_LOCKED`, `FLAG_TURN_SCREEN_ON`,
  `FLAG_DISMISS_KEYGUARD`, `FLAG_BLUR_BEHIND`, `FLAG_DITHER`,
  `FLAG_TOUCHABLE_WHEN_WAKING`) are no longer rejected on modern API levels.
- The native `setSecure` handler was unreachable: it required a `flags` argument
  that the call never carries, so it always failed with `INVALID_ARGUMENT`.
- `addFlags`/`clearFlags` no longer throw a type error when the platform returns
  `null`; they report `false` instead.

### Changed
- Platform detection uses `defaultTargetPlatform` instead of `Platform.isAndroid`,
  so consumers can override the platform in their own unit tests and still assert
  that the plugin's methods are called ([#5]).
- Published `compileSdk 36` (with AGP 8.13.0 / Kotlin 2.1.20), resolving the
  `androidx.window:window:1.2.0 requires compileSdk 34+` build failure. This landed
  on `main` in 1.0.1 but was never released to pub.dev ([#8]).

[#5]: https://github.com/ijashuzain/flutter_windowmanager_plus/issues/5
[#6]: https://github.com/ijashuzain/flutter_windowmanager_plus/issues/6
[#8]: https://github.com/ijashuzain/flutter_windowmanager_plus/issues/8

## [1.0.1]

### Changed
- Specified the `namespace` in the module's `build.gradle` for AGP 8 compatibility.
- Guarded the platform channel calls behind an Android platform check.

## [1.0.0]

### Added
- Initial release of `flutter_windowmanager_plus`.
- Forked from the original `flutter_windowmanager` package.
- Implemented basic functionality for adding and clearing window flags.
- Support for all Android window flags from WindowManager.LayoutParams.
- Added documentation and example usage in README.md.

### Changed
- Updated package to support newer version of android and flutter.
- Modernized plugin structure to use the latest Flutter plugin APIs.

### Fixed
- Resolved issues with deprecated method usage from the original package.
- Improved error handling and API level checking for flag compatibility.
