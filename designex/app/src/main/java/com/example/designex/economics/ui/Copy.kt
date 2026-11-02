package com.example.designex.economics.ui

import com.example.designex.economics.data.Lang
import com.example.designex.economics.data.Localized

/** Interface labels in English and Hindi. */
object Copy {
    val tabHome = Localized("Home", "होम")
    val tabLearn = Localized("Learn", "सीखें")
    val tabSongs = Localized("Songs", "गीत")
    val tabQuiz = Localized("Quiz", "क्विज़")
    val tabProfile = Localized("Profile", "प्रोफ़ाइल")
    val welcomeTo = Localized("Welcome to", "स्वागत है")
    val whatToLearn = Localized("What do you want to learn?", "आप क्या सीखना चाहते हैं?")
    val selectRelevant = Localized(
        "Select the subject you are studying",
        "जो विषय आप पढ़ रहे हैं, उसे चुनें",
    )
    val whichLanguage = Localized("Which language suits you?", "कौन-सी भाषा आपको ठीक लगती है?")
    val languageSub = Localized(
        "Lessons, lyrics and songs will use it. You can change it any time.",
        "पाठ, बोल और गीत इसी में होंगे। आप इसे कभी भी बदल सकते हैं।",
    )
    val continueBtn = Localized("Continue", "आगे बढ़ें")
    val goodMorning = Localized("Good morning", "सुप्रभात")
    val goodAfternoon = Localized("Good afternoon", "नमस्कार")
    val goodEvening = Localized("Good evening", "शुभ संध्या")
    val allUnits = Localized("All", "सभी")
    val featured = Localized("Featured", "विशेष")
    val nowSinging = Localized("Now singing", "अभी गाया जा रहा है")
    val volumeLow = Localized("Your phone's media volume is low.", "फ़ोन का मीडिया वॉल्यूम कम है।")
    val turnUp = Localized("Turn up", "बढ़ाएँ")
    val subject = Localized("Subject", "विषय")
    val subjectAndLanguage =Localized("Subject & language", "विषय और भाषा")
    val tagline =Localized("Learn it. Hear it. Remember it.", "सीखो, सुनो, याद रखो।")
    val hello = Localized("Hi, {name}", "नमस्ते, {name}")
    val chooseSubject = Localized("What are we learning today?", "आज हम क्या सीखेंगे?")
    val learned = Localized("Topics learned", "सीखे गए विषय")
    val continueWith = Localized("Continue", "जारी रखें")
    val conceptOfDay = Localized("Concept of the day", "आज की अवधारणा")
    val units = Localized("Units", "इकाइयाँ")
    val unit = Localized("Unit", "इकाई")
    val topics = Localized("topics", "विषय")
    val songLibrary = Localized("Song Library", "गीत संग्रह")
    val songsCount = Localized("90 songs, Hindi & English", "90 गीत, हिंदी और अंग्रेज़ी")
    val quiz = Localized("Quick Quiz", "त्वरित प्रश्नोत्तरी")
    val best = Localized("Best", "सर्वश्रेष्ठ")
    val concept = Localized("Concept", "अवधारणा")
    val keyIdeas = Localized("Key ideas", "मुख्य बिंदु")
    val formula = Localized("Formula", "सूत्र")
    val matchedSong = Localized("Matched song", "मिलान गीत")
    val alsoTry = Localized("Also try", "यह भी सुनें")
    val makeSong = Localized("Turn this topic into a song", "इस विषय को गीत बनाएँ")
    val makeSongSub = Localized(
        "Pick any song you like. The lyrics are fitted to it and sung for you.",
        "अपना पसंदीदा गीत चुनें। बोल उसी के अनुसार बनेंगे और गाए जाएँगे।",
    )
    val markLearned = Localized("Mark as learned", "सीखा हुआ चिह्नित करें")
    val isLearned = Localized("Learned ✓", "सीख लिया ✓")
    val search = Localized("Search songs or artists", "गीत या कलाकार खोजें")
    val all = Localized("All", "सभी")
    val usedIn = Localized("Used for", "इनके लिए")
    val question = Localized("Which topic is this?", "यह कौन-सा विषय है?")
    val next = Localized("Next", "अगला")
    val finish = Localized("See score", "अंक देखें")
    val playAgain = Localized("Play again", "फिर खेलें")
    val score = Localized("Your score", "आपके अंक")
    val back = Localized("Back", "वापस")

    val step = Localized("Step {n} of 3", "चरण {n} / 3")
    val pickSong = Localized("Pick your song", "अपना गीत चुनें")
    val changeSong = Localized("Change song", "गीत बदलें")
    val suggested =Localized("Suggested for this topic", "इस विषय के लिए सुझाव")
    val browseStyles = Localized("Or browse by style", "या शैली से चुनें")
    val traditional = Localized("Traditional tune", "पारंपरिक धुन")
    val yourLyrics = Localized("Your lyrics", "आपके बोल")
    val fittedTo = Localized(
        "Fitted to “{song}”: {bpm} BPM, about {n} syllables a line",
        "“{song}” के अनुसार: {bpm} BPM, हर पंक्ति में लगभग {n} अक्षर",
    )
    val newChorus = Localized("New chorus", "नया मुखड़ा")
    val makeIt = Localized("Make the song", "गीत बनाएँ")
    val enginePhone = Localized("On-phone singer", "फ़ोन गायक")
    val enginePhoneSub = Localized("Free · works offline", "मुफ़्त · ऑफ़लाइन")
    val engineSunoSub = Localized("Real singing · needs internet", "असली गायन · इंटरनेट ज़रूरी")
    val sunoNeedsKey = Localized("Add a key in Settings", "सेटिंग्स में कुंजी जोड़ें")
    val generate = Localized("Generate song", "गीत बनाएँ")
    val stop = Localized("Stop", "रोकें")
    val readAlong = Localized("Read along", "साथ में पढ़ें")
    val tuneHonesty = Localized(
        "You get a new melody in the style, tempo and mood of the song you pick. Famous songs' own melodies can't be copied.",
        "आपको चुने गए गीत की शैली, गति और मूड में नई धुन मिलती है। मशहूर गीतों की असली धुन कॉपी नहीं की जा सकती।",
    )
    val prototypeNote = Localized(
        "Prototype: lyrics come from the syllabus datasets and templates. Songs are sung by Suno AI when a key is added, otherwise by the phone's own voice shaped into notes.",
        "प्रोटोटाइप: बोल पाठ्यक्रम डेटा और टेम्पलेट से बनते हैं। कुंजी जोड़ने पर Suno AI गाता है, वरना फ़ोन की आवाज़ सुरों में ढलकर गाती है।",
    )
}

fun Localized.of(lang: Lang) = this[lang]

fun String.fill(vararg pairs: Pair<String, Any>): String =
    pairs.fold(this) { acc, (k, v) -> acc.replace("{$k}", v.toString()) }
