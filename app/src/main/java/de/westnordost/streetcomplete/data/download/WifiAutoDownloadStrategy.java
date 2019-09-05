package de.westnordost.streetcomplete.data.download;


import javax.inject.Inject;

import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao;
import de.westnordost.streetcomplete.data.tiles.DownloadedTilesDao;
import de.westnordost.streetcomplete.data.visiblequests.OrderedVisibleQuestTypesProvider;

public class WifiAutoDownloadStrategy extends AActiveRadiusStrategy
{
	@Inject public WifiAutoDownloadStrategy(
			OsmQuestDao osmQuestDB, DownloadedTilesDao downloadedTilesDao,
			OrderedVisibleQuestTypesProvider questTypes)
	{
		super(osmQuestDB, downloadedTilesDao, questTypes);
	}

	/** Let's assume that if the user is on wifi, he is either at home, at work, in the hotel, at a
	 *  café,... in any case, somewhere that would act as a "base" from which he can go on an
	 *  excursion. Let's make sure he can, even if there is no or bad internet.
     */

	@Override public int getQuestTypeDownloadCount()
	{
		return 1000;
	}

	@Override protected int getMinQuestsInActiveRadiusPerKm2()
	{
		return 500;
	}

	@Override protected int[] getActiveRadii()
	{
		// checks if either in 600 or 300m radius, there are enough quests.
		return new int[]{600, 300};
	}

	@Override protected int getDownloadRadius()
	{
		return 1200;
	}
}
