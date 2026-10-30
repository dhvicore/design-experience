package com.example.designex.economics.ui

import androidx.compose.ui.graphics.Color
import com.example.designex.economics.data.Localized
import com.example.designex.ui.theme.Lavender
import com.example.designex.ui.theme.Rose
import com.example.designex.ui.theme.SkyBlue

/** Accent colour, badge, scenery and one-line summary for each subject. */
data class SubjectLook(val color: Color, val badge: String, val sceneKey: String, val summary: Localized) {
    companion object {
        fun of(subjectId: String) = when (subjectId) {
            "chemistry" -> SubjectLook(
                SkyBlue, "H₂O", "aurora-chem",
                Localized("Atoms, gases, water and materials", "परमाणु, गैस, जल और सामग्री", "अणू, वायू, पाणी आणि साहित्य"),
            )
            "calculus" -> SubjectLook(
                Rose, "∫dx", "dusk-calc",
                Localized("Derivatives, integrals and vectors", "अवकलज, समाकल और सदिश", "विकलज, संकलन आणि सदिश"),
            )
            else -> SubjectLook(
                Lavender, "₹", "lake-econ",
                Localized("Markets, money and management", "बाज़ार, धन और प्रबंधन", "बाजार, पैसा आणि व्यवस्थापन"),
            )
        }
    }
}
