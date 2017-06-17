package de.westnordost.streetcomplete.quests.validator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import de.westnordost.osmapi.map.data.OsmElement;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.tql.BooleanExpression;
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment;

/** Abstract base class for dialogs in which the user answers a yes/no quest */
public class multidesignatedFootwayToPathForm extends YesNoQuestAnswerFragment
{
	public static final String ANSWER = "answer";
	public static final String FOOT_VALUE = "FOOT_VALUE";


	protected void onClickYesNo(boolean answer)
	{
		OsmElement element = getOsmElement();
		String foot = element != null && element.getTags() != null ? element.getTags().get("foot") : null;

		Bundle bundle = new Bundle();
		bundle.putBoolean(ANSWER, answer);
		bundle.putString(FOOT_VALUE, foot);
		applyImmediateAnswer(bundle);
	}
}
