# Third-Party Notices

This file lists notices for third-party data and resources included with WordBopper.

## English Word List

Word list copyright 2000-2026 by Kevin Atkinson.

Permission to use, copy, modify, distribute, and sell any part of the English Speller Database (ESDB, previously known as SCOWLv2), or word lists created from it, is hereby granted without fee, provided that the above copyright notice appears in all copies and that both the above copyright notice and this notice appear in supporting documentation. Kevin Atkinson makes no representations about the suitability of this database for any purpose. It is provided "as is" without express or implied warranty.

ESDB is derived from many sources, most of which are in the Public Domain. Data from the Corpus of Contemporary American English (COCA) was also used.

More information about COCA is available at https://www.english-corpora.org/coca/.

The primary source of words for ESDB comes from 12dicts and ENABLE2K. Both are in the Public Domain, but Alan Beale deserves special credit as the author of 12dicts and a major contributor to ENABLE2K.

## KeySoft SDK

Monarch support uses HumanWare KeySoft SDK APIs. The SDK itself is not included in this repository; builds resolve it from the configured Maven repository when valid local credentials are supplied.

## Android and Jetpack Libraries

WordBopper depends on AndroidX and Jetpack Compose libraries through Gradle. See `app/build.gradle.kts` and `gradle/libs.versions.toml` for the dependency list used by the build.
