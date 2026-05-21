# Word Bopper — Play Console Store Listing

## App Name
Word Bopper

---

## Short Description
*(80-character max)*

Bop letters, build words, and chain combos in this fast and fun word game!

---

## Full Description
*(4,000-character max — copy/paste directly into Play Console)*

Bop to the Top! Word Bopper is an energetic and accessible word game where you tap colorful letter bubbles on a 5×5 grid to spell words, rack up points, and chase high scores.

**Three ways to play:**

**Timed** — You have 2 minutes. Tap letters, make words, and watch the grid refresh with new letters after every word. Chain together connected letters for bonus points, and rack up three chains in a row to unleash a 3× score multiplier. Run low on time? Clear your tray for a 15-second bonus!

**Bopple** — A strategic twist. Once a letter is bopped, it stays — the grid never refreshes. Words must be built from letters that are adjacent to each other on the grid, and you can't use the same word twice. Every letter counts, so choose wisely and plan your paths across the board.

**Non-Stop** — No clock, no pressure. Bop as many letters and make as many words as you want. Perfect for a relaxed session or for chasing your personal best without the ticking timer.

**Track your personal bests.** Word Bopper saves your records across all three modes — highest score, most words, longest word, and largest letter chain. Beat your best game by game.

**Customizable and accessible.** Word Bopper includes options to speak letter positions and phonetics aloud for accessibility. You can also adjust gameplay announcement verbosity, choose dark or light text on bubbles, and enable BopAway mode — where each letter you tap instantly moves into the Word Tray and is replaced with a fresh one so the grid stays lively.

Whether you have 2 minutes or 2 hours, Word Bopper has a mode for you. Bop those letters!

---

## Store Listing Fields

| Field | Value |
|---|---|
| Package name | com.marconius.WordBopper |
| Default language | English (United States) |
| App type | Game |
| Category | Word |
| Email (support) | marco.salsiccia@gmail.com |
| Contains ads | No |
| In-app purchases | No |

---

## Content Rating

**Everyone** — no violence, no user-generated content, no in-app purchases, no ads.

Questionnaire answers:
- Violence: None
- Sexual content: None
- Profanity: None
- Controlled substances: None
- User interaction / social features: None
- Location sharing: No
- In-app purchases: No

---

## Tags / Keywords

word game, word puzzle, letter game, spelling game, word builder, bubble letters, word scramble, vocabulary game, word search, brain game

---

## What's New — v1.0.0
*(500-character max)*

Word Bopper is here! Tap colorful letter bubbles to spell words, chain combos for bonus points, and trigger the 3× power-up. Choose from Timed, Bopple, or Non-Stop mode and bop your way to the top!

---

## First Release Checklist

### Play Console Setup
- [ ] Create a new app in Play Console (Apps → Create app)
- [ ] Set app name to "Word Bopper", type Game, free, not child-directed
- [ ] Enter short description, full description, and email from this doc
- [ ] Upload feature graphic (1024×500 px) — required for the listing banner
- [ ] Upload at least 2 phone screenshots
- [ ] Set category to Games → Word
- [ ] Enter privacy policy URL: https://marconius.com/wbPrivacy/
- [ ] Complete the content rating questionnaire using the answers above
- [ ] Confirm "Contains ads: No" and "In-app purchases: No" in App content

### Signing and Building
- [ ] Add a `keystore.properties` file in the project root with your keystore path, alias, and passwords (keep this out of git)
- [ ] Wire the keystore.properties values into the release signing config in app/build.gradle.kts
- [ ] Run `./gradlew bundleRelease` from the project root to produce the signed AAB
- [ ] The output AAB will be at app/build/outputs/bundle/release/app-release.aab

### Upload
- [ ] In Play Console: go to Testing → Internal testing (easiest first upload) or Production
- [ ] Create a new release and upload the AAB from the path above
- [ ] Paste the "What's New" text above
- [ ] Review and roll out the release
