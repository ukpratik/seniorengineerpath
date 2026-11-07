# F-Droid

`io.github.ukpratik.seniorpath.yml` is the build recipe for F-Droid, in the canonical format (`fdroid rewritemeta`) and clean under `fdroid lint`. To submit, copy it to `metadata/` in a fork of https://gitlab.com/fdroid/fdroiddata and open a merge request.

- The build is Gradle only: the lesson content is committed in `android/app/src/main/assets/content` (CI fails if it is stale), so F-Droid needs no Node.
- Store text and images come from `fastlane/metadata/android/en-US/` at the repo root (the only place F-Droid looks for a non-flavoured app); Play uses the same files.
- `UpdateCheckMode: Tags ^android-v.*$` picks up new releases from `android-vX.Y.Z` tags; versionName and versionCode are read from `android/app/build.gradle.kts`.
- Anti-feature: `NonFreeAssets`, because the lessons are CC BY-NC-SA 4.0. The code is MIT.
- F-Droid signs with its own key, so F-Droid and Play installs can't update each other: users should export progress before switching.
