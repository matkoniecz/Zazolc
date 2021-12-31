package de.westnordost.streetcomplete.user

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osmnotes.NotesModule
import de.westnordost.streetcomplete.data.UnsyncedChangesCountSource
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.user.*
import de.westnordost.streetcomplete.data.user.achievements.AchievementsSource
import de.westnordost.streetcomplete.data.user.statistics.StatisticsSource
import de.westnordost.streetcomplete.databinding.FragmentProfileBinding
import kotlinx.coroutines.*
import java.io.File
import java.util.Locale
import javax.inject.Inject

import android.util.Log
import android.view.Gravity.CENTER
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.android.flexbox.FlexboxLayout
import de.westnordost.streetcomplete.ktx.*


/** Shows the user profile: username, avatar, star count and a hint regarding unpublished changes */
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    @Inject internal lateinit var userDataSource: UserDataSource
    @Inject internal lateinit var userLoginStatusController: UserLoginStatusController
    @Inject internal lateinit var userUpdater: UserUpdater
    @Inject internal lateinit var statisticsSource: StatisticsSource
    @Inject internal lateinit var achievementsSource: AchievementsSource
    @Inject internal lateinit var unsyncedChangesCountSource: UnsyncedChangesCountSource

    private lateinit var anonAvatar: Bitmap

    private val binding by viewBinding(FragmentProfileBinding::bind)

    private val unsyncedChangesCountListener = object : UnsyncedChangesCountSource.Listener {
        override fun onIncreased() { viewLifecycleScope.launch { updateUnpublishedQuestsText() } }
        override fun onDecreased() { viewLifecycleScope.launch { updateUnpublishedQuestsText() } }
    }
    private val questStatisticsDaoListener = object : StatisticsSource.Listener {
        override fun onAddedOne(questType: QuestType<*>) {
            viewLifecycleScope.launch { updateSolvedQuestsText() }
        }
        override fun onSubtractedOne(questType: QuestType<*>) {
            viewLifecycleScope.launch { updateSolvedQuestsText() }
        }
        override fun onUpdatedAll() {
            viewLifecycleScope.launch { updateStatisticsTexts() }
        }
        override fun onCleared() {
            viewLifecycleScope.launch { updateStatisticsTexts() }
        }
        override fun onUpdatedDaysActive() {
            viewLifecycleScope.launch { updateDaysActiveText() }
        }
    }
    private val userListener = object : UserDataSource.Listener {
        override fun onUpdated() { viewLifecycleScope.launch { updateUserName() } }
    }
    private val userAvatarListener = object : UserUpdater.Listener {
        override fun onUserAvatarUpdated() { viewLifecycleScope.launch { updateAvatar() } }
    }

    init {
        Injector.applicationComponent.inject(this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        anonAvatar = context.getDrawable(R.drawable.ic_osm_anon_avatar)!!.createBitmap()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.logoutButton.setOnClickListener {
            userLoginStatusController.logOut()
        }
        binding.profileButton.setOnClickListener {
            openUrl("https://www.openstreetmap.org/user/" + userDataSource.userName)
        }
    }

    override fun onStart() {
        super.onStart()

        viewLifecycleScope.launch {
            userDataSource.addListener(userListener)
            userUpdater.addUserAvatarListener(userAvatarListener)
            statisticsSource.addListener(questStatisticsDaoListener)
            unsyncedChangesCountSource.addListener(unsyncedChangesCountListener)

            updateUserName()
            updateAvatar()
            updateSolvedQuestsText()
            updateUnpublishedQuestsText()
            updateDaysActiveText()
            updateGlobalRankText()
            updateLocalRankText()
            updateAchievementLevelsText()
            addAnExtraBubble()
        }
    }

    override fun onStop() {
        super.onStop()
        unsyncedChangesCountSource.removeListener(unsyncedChangesCountListener)
        statisticsSource.removeListener(questStatisticsDaoListener)
        userDataSource.removeListener(userListener)
        userUpdater.removeUserAvatarListener(userAvatarListener)
    }

    private fun updateUserName() {
        binding.userNameTextView.text = userDataSource.userName
    }

    private fun updateAvatar() {
        val cacheDir = NotesModule.getAvatarsCacheDirectory(requireContext())
        val avatarFile = File(cacheDir.toString() + File.separator + userDataSource.userId)
        val avatar = if (avatarFile.exists()) BitmapFactory.decodeFile(avatarFile.path) else anonAvatar
        binding.userAvatarImageView.setImageBitmap(avatar)
    }

    private suspend fun updateStatisticsTexts() {
        updateSolvedQuestsText()
        updateDaysActiveText()
        updateGlobalRankText()
        updateLocalRankText()
    }

    private suspend fun updateSolvedQuestsText() {
        binding.solvedQuestsText.text = withContext(Dispatchers.IO) { statisticsSource.getSolvedCount().toString() }
    }

    private suspend fun updateUnpublishedQuestsText() {
        val unsyncedChanges = unsyncedChangesCountSource.getCount()
        binding.unpublishedQuestsText.text = getString(R.string.unsynced_quests_description, unsyncedChanges)
        binding.unpublishedQuestsText.isGone = unsyncedChanges <= 0
    }

    private fun updateDaysActiveText() {
        val daysActive = statisticsSource.daysActive
        binding.daysActiveContainer.isGone = daysActive <= 0
        binding.daysActiveText.text = daysActive.toString()
    }

    private fun updateGlobalRankText() {
        val rank = statisticsSource.rank
        binding.globalRankContainer.isGone = rank <= 0 || statisticsSource.getSolvedCount() <= 100
        binding.globalRankText.text = "#$rank"
    }

    private suspend fun updateLocalRankText() {
        val statistics = withContext(Dispatchers.IO) {
            statisticsSource.getCountryStatisticsOfCountryWithBiggestSolvedCount()
        }
        if (statistics == null) binding.localRankContainer.isGone = true
        else {
            val shouldShow = statistics.rank != null && statistics.rank > 0 && statistics.solvedCount > 50
            val countryLocale = Locale("", statistics.countryCode)
            binding.localRankContainer.isGone = !shouldShow
            binding.localRankText.text = "#${statistics.rank}"

            //binding.localRankText.setBackgroundColor(resources.getColor(R.color.accent_variant))

            binding.localRankText.setBackgroundColor(ContextCompat.getColor(context!!, R.color.accent_variant))

            binding.localRankText.setBackgroundResource(R.drawable.ic_symbolic_bubble_inner)

            //val bgShape = binding.localRankText.background as GradientDrawable
            //bgShape.setColor(Color.rgb(255, 0, 0))

            binding.localRankLabel.text = getString(R.string.user_profile_local_rank, countryLocale.displayCountry)
        }
    }

    private suspend fun updateAchievementLevelsText() {
        val levels = withContext(Dispatchers.IO) { achievementsSource.getAchievements().sumOf { it.second } }
        binding.achievementLevelsContainer.isGone = levels <= 0
        binding.achievementLevelsText.text = "$levels"
    }

    private suspend fun addAnExtraBubble() {
        // buuble is added to android:id="@+id/badgesContainer"
        // in fragment_profile.xml
        val statistics = withContext(Dispatchers.IO) {
            Log.wtf("HEREHERE", statisticsSource.getCountryStatistics().toString())
            statisticsSource.getCountryStatisticsOfCountryWithBiggestSolvedCount()
        }
        Log.wtf("HEREHERE2", statisticsSource.getCountryStatistics().toString())

        // RelativeLayout, equivalent of say @+id/localRankContainer
        val pixelsForDp84 = 84 * context!!.resources.displayMetrics.density.toInt()
        val bubbleContainerLayoutParams = RelativeLayout.LayoutParams(pixelsForDp84, WRAP_CONTENT)
        val bubbleContainer = RelativeLayout(context)
        bubbleContainer.layoutParams = bubbleContainerLayoutParams

        // equivalent of @+id/localRankBubble
        val bubbleLayoutParams = RelativeLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        val bubble = RelativeLayout(context)
        bubble.layoutParams = bubbleLayoutParams

        // equivalent of ImageView @+id/frameView inside localRankBubble
        val pixelsForDp64 = 64 * context!!.resources.displayMetrics.density.toInt()
        val bubbleImageLayoutParams = RelativeLayout.LayoutParams(pixelsForDp64, pixelsForDp64)
        bubbleImageLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
        bubbleImageLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        val bubbleImage = ImageView(context)
        bubbleImage.setImageDrawable(resources.getBitmapDrawable(R.drawable.ic_symbolic_leaves_export))
        bubbleImage.layoutParams = bubbleImageLayoutParams

        // equivalent of @+id/localRankText inside localRankBubble
        val textInsideBubbleLayoutParams = RelativeLayout.LayoutParams(pixelsForDp64, pixelsForDp64)
        val textViewInsideBubble = TextView(context)
        textViewInsideBubble.gravity = CENTER
        textViewInsideBubble.text = "145"
        textViewInsideBubble.setTextAppearance(context, R.style.TextAppearance_AppCompat_Title) // deprecated but needed for SK 21, 22
        textViewInsideBubble.layoutParams = textInsideBubbleLayoutParams

        // equivalent of @+id/localRankLabel inside @+id/localRankContainer (bubbleContainer
        val textBelowBubbleLayoutParams = RelativeLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        textBelowBubbleLayoutParams.addRule(RelativeLayout.BELOW, bubble.id);
        val textViewBelowBubble = TextView(context)
        textViewBelowBubble.text = "below"
        textViewBelowBubble.layoutParams = textBelowBubbleLayoutParams

        bubble.addView(bubbleImage)
        bubble.addView(textViewInsideBubble)
        bubbleContainer.addView(bubble)
        bubbleContainer.addView(textViewBelowBubble)

        val badgesContainer : FlexboxLayout = view!!.findViewById(R.id.badgesContainer)
        badgesContainer.addView(bubbleContainer)
    }

    private fun openUrl(url: String): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        return tryStartActivity(intent)
    }

}
