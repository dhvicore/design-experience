"""Fit lyric phrase boundaries to a supplied melody's syllable budgets.

Counts are estimates, not a phonetic alignment guarantee. Text is preserved except
for section labels; the curated Rolle demo has explicit musical phrase wording.
"""
import re

ROLLE_RAGHUPATI = [
    "A se B tak sada satat rahe yeh apna function",
    "Bheetar avkalan ho har ek bindu par",
    "Dono siron par maan sada samaan ho",
    "Beech kahin C par dhalan shunya",
    "Om vidya prakashaya namah, shubh Rolle niyam gaayein",
    "Ek flat tangent mile, ganit ki jai ho",
]


def syllables(word, language="en"):
    if re.search(r"[\u0900-\u097f]", word):
        # Akshara estimate only; Hindi schwa deletion and melisma still need listening review.
        vowels_or_consonants = len(re.findall(r"[अ-औक-हक़-य़]", word))
        return max(1, vowels_or_consonants - word.count("्"))
    word = re.sub(r"[^a-z]", "", word.lower())
    if not word:
        return 1
    if language == "hi":
        return max(1, len(re.findall(r"[aeiou]+", word)))
    count = len(re.findall(r"[aeiouy]+", word))
    if word.endswith("e") and not word.endswith(("le", "ee", "ye")) and count > 1:
        count -= 1
    return max(1, count)


def repeats_for(lyrics, profile, language="en"):
    budgets = profile.get("phrase_syllables", [])
    if not budgets:
        return 1
    words = re.sub(r"\[[^\]]+\]", " ", lyrics).split()
    total = sum(syllables(word, language) for word in words)
    # Repeat the musical cycle instead of squeezing an entire lesson into one verse.
    max_repeats = max(1, min(3, int(150 / max(1, profile.get("duration_seconds", 50)))))
    return max(1, min(max_repeats, int(total / sum(budgets) + 0.5)))


def fit(lyrics, profile, topic="", category="", language="en"):
    budgets = profile.get("phrase_syllables", [])
    if not budgets:
        return {"lyrics": lyrics, "method": "reference_audio_only", "lines": [],
                "note": "Reference audio is supplied; phrase timing metadata is missing."}
    if topic == "Rolle's Theorem" and profile["id"] == "raghupati-raghav-raja-ram" and category == "Bhajan":
        lines = ROLLE_RAGHUPATI
        method = "curated_demo"
    else:
        words = re.sub(r"\[[^\]]+\]", " ", lyrics).split()
        if len(words) < len(budgets):
            return {"lyrics": lyrics, "method": "needs_review", "lines": [], "note": "Too few words for the melody phrases."}
        budgets = budgets * repeats_for(lyrics, profile, language)
        lengths = [syllables(word, language) for word in words]
        prefix = [0]
        for length in lengths:
            prefix.append(prefix[-1] + length)
        n = len(words)
        previous = {0: (0, [])}
        for line_index, target in enumerate(budgets):
            current = {}
            remaining = len(budgets) - line_index - 1
            for end in range(line_index + 1, n - remaining + 1):
                candidates = []
                for start, (cost, cuts) in previous.items():
                    if start < end:
                        error = prefix[end] - prefix[start] - target
                        punctuation_penalty = 0 if words[end - 1].endswith((",", ".", "!", "?")) else 0.2
                        candidates.append((cost + error * error + punctuation_penalty, cuts + [end]))
                if candidates:
                    current[end] = min(candidates, key=lambda item: item[0])
            previous = current
        _, cuts = previous[n]
        lines, start = [], 0
        for end in cuts:
            lines.append(" ".join(words[start:end]))
            start = end
        method = "meter_partition"
    report = [{"target": target, "estimated": sum(syllables(w, language) for w in line.split())}
              for line, target in zip(lines, budgets)]
    split = max(1, len(lines) // 2)
    text = "[Verse]\n" + "\n".join(lines[:split]) + "\n\n[Chorus]\n" + "\n".join(lines[split:])
    return {"lyrics": text, "method": method, "lines": report,
            "note": "Phrase lengths are estimated; the melody repeats for longer lessons. Listen and review pronunciation."}
