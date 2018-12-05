package de.westnordost.streetcomplete.data.osm;

import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.Map;

/** Some defaults for OsmElementQuestType interface */
public abstract class AOsmElementQuestType implements OsmElementQuestType
{
	@Override public int getDefaultDisabledMessage() { return 0; }
	@NonNull @Override public Countries getEnabledForCountries() { return Countries.ALL; }
	@Override public void cleanMetadata() { }
	@Override public int getTitle() { return getTitle(Collections.emptyMap()); }
	@Override public String getTitleSuffixHack(@NonNull Map<String, String> tags) {
		return "";
	}
	@Override public boolean hasMarkersAtEnds() { return false; }
}
