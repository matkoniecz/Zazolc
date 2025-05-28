package de.westnordost.streetcomplete.screens.measure

import android.app.ActivityManager
import android.content.Context
import androidx.core.content.getSystemService
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.util.ktx.isPackageInstalled

class ArSupportChecker(private val context: Context) {
    operator fun invoke(): Boolean = hasArMeasureSupport(context)
}

private fun hasArMeasureSupport(context: Context): Boolean = false

