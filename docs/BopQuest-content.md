# Adding BopQuest content

BopQuest content is stored in `app/src/main/assets/BopQuests`. Adding a future quest does not require a Kotlin change.

## Add a quest

1. Copy an existing dated quest folder.
2. Name the folder `YYYY-MM-DD-Name`, using the quest's start date and a short name without spaces.
3. Rename the files inside it to use the stem `YYYY-Name`.
4. Update the title, start date, and end date in the `-info.txt` file.
5. Replace the words in each language file.
6. Add the new folder name to `BopQuests/events.txt`.

For example:

```text
BopQuests/
  events.txt
  2026-10-01-Autumn/
    2026-Autumn-info.txt
    2026-Autumn-en.txt
```

The information file uses this format:

```text
title: Autumn BopQuest
starts: 2026-10-01
ends: 2026-10-31
```

The app currently awards one rank point for each newly found word. Finding the complete list awards a one-time bonus equal to the number of words in that language's list. The existing iOS-compatible `bonus:` metadata may remain in copied files, but it does not control this automatic completion reward.

## Add language lists

Use one word per line. Blank lines and lines beginning with `#` are ignored. Words are lowercased and normalized with the selected language's dictionary rules, duplicate entries are removed in place, and entries shorter than three letters are ignored.

Supported suffixes are:

- `en` for English
- `es` for Spanish
- `fr` for French
- `de` for German
- `nl` for Dutch
- `it` for Italian
- `pt-BR` for Brazilian Portuguese

Only add a language file when the full themed list has been reviewed for that language. When the active quest has no list for the player's selected language, WordBopper explains that the BopQuest is unavailable for that language.

## Edit ranks

Player ranks are stored in `app/src/main/assets/ranks-en.txt`. Each active line contains a numeric threshold, a tab, and the rank title. Keep thresholds unique and in ascending order. Blank lines and lines beginning with `#` are ignored.
