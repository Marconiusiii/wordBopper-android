# WordBopper for Android

WordBopper is an accessible word game for Android. Build words from neighboring letters, submit valid words for points, and use chain bonuses to push your score higher.

The app is designed for everyday Android phones and has an experimental Monarch mode for the APH Monarch tactile display through HumanWare KeySoft self-brailling APIs.

## Gameplay

- Build words by selecting letters in the grid.
- Submit the current word to score points.
- Clear the current letters when you want to start over.
- Chain connected letters for bonus points.
- Use BopAway mode for a faster challenge where tapped letters move into the word tray and are immediately replaced in the grid.

## Accessibility

WordBopper is built accessibility-first:

- TalkBack-friendly controls on Android phones.
- Spoken scoring, invalid-word, duplicate-word, and clear-word announcements.
- Optional letter position and phonetic announcements.
- A Monarch-specific tactile display mode with braille letter cells, word tray output, Dot 8 submit, Dot 7 clear, and BopAway support.

## Monarch Mode

On supported Monarch hardware, the app switches from the standard Compose phone UI to a self-brailling tactile layout.

Current Monarch controls:

- Dot 8: submit the current word.
- Dot 7: clear letters, or clear word when BopAway is on.
- Type `b` on the home screen: toggle BopAway.
- Double-tap a tactile letter cell: add or select that letter.

The Monarch display uses compact tactile status text such as `word 15pts cb`, while spoken announcements keep the full wording, such as `word, 15 points, chain bonus`.

## Dictionary

The game validates words against `app/src/main/res/raw/words.txt`. The current English list is derived from the English Speller Database / SCOWL family of word lists and is loaded by `DictionaryService` at runtime.

See `THIRD_PARTY_NOTICES.md` for word list notices.

## Build Requirements

- Android Studio or the Android command-line tools.
- JDK compatible with the Android Gradle Plugin used by this project.
- Android SDK installed locally.
- Access to the KeySoft SDK Maven repository if building Monarch support.

## Private Local Setup

Do not commit signing keys or private tokens.

Create a local `keystore.properties` file in the project root when you need release signing or KeySoft SDK access:

```properties
storeFile=/absolute/path/to/upload-key.jks
storePassword=your_store_password
keyAlias=your_key_alias
keyPassword=your_key_password
gitlab_maven_repo_deployToken=your_keysoft_sdk_token
```

This file is ignored by Git.

`local.properties` is also ignored and should contain local SDK paths only.

## Build Commands

Build a debug APK:

```bash
./gradlew --no-configuration-cache :app:assembleDebug
```

Install on a connected Android device:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Launch the app:

```bash
adb shell monkey -p com.marconius.WordBopper 1
```

Build a release bundle for Play Console:

```bash
./gradlew --no-configuration-cache :app:bundleRelease
```

Release builds require valid signing values in the ignored `keystore.properties` file.

## Repository Safety

The root `.gitignore` excludes:

- Android and Gradle build output.
- Android Studio local files.
- `local.properties`.
- `keystore.properties`.
- `.jks`, `.keystore`, `.p12`, `.pem`, and other private key files.
- common Play Console service account JSON filenames.

Before publishing, it is still wise to run:

```bash
git status --short --ignored
git ls-files
```

Confirm that private files are ignored and not tracked.

## License

WordBopper app code is released under the MIT License. See `LICENSE`.

Third-party notices for dictionary data and other included resources are listed in `THIRD_PARTY_NOTICES.md`.
