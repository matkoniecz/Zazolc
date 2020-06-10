package de.westnordost.streetcomplete.data.download


import javax.inject.Inject

import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesDao
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource
import de.westnordost.streetcomplete.data.visiblequests.OrderedVisibleQuestTypesProvider

/** Download strategy if user is on wifi */
class WifiAutoDownloadStrategy @Inject constructor(
    visibleQuestsSource: VisibleQuestsSource,
    downloadedTilesDao: DownloadedTilesDao,
    questTypes: OrderedVisibleQuestTypesProvider
) : AActiveRadiusStrategy(visibleQuestsSource, downloadedTilesDao, questTypes) {

    /** Let's assume that if the user is on wifi, he is either at home, at work, in the hotel, at a
     * café,... in any case, somewhere that would act as a "base" from which he can go on an
     * excursion. Let's make sure he can, even if there is no or bad internet.
     *
     * Since download size is almost unlimited, we can be very generous here.
     * However, Overpass is as limited as always, so the number of quest types we download is
     * limited as before  */

    override val questTypeDownloadCount = 1000
    override val minQuestsInActiveRadiusPerKm2 = 1000

    // checks if either in 2600 or 2300m radius, there are enough quests.
    override val activeRadii = intArrayOf(2600, 2300)
    override val downloadRadius = 2500
}
