package de.westnordost.streetcomplete.quests.road_surface;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.westnordost.osmapi.map.data.OsmElement;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class showInvalidSurfaceForm extends AbstractQuestAnswerFragment
{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		OsmElement element = getOsmElement();
		String surface = element != null && element.getTags() != null ? element.getTags().get("surface") : null;
		if (surface != null && !surface.trim().isEmpty())
		{
			setTitle("surface=" + surface);
		} else
		{
			setTitle("something went wrong");
		}
		return view;
	}

	@Override public boolean hasChanges()
	{
		return false;
	}
}

