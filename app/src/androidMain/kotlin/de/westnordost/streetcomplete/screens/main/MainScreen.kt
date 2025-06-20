package de.westnordost.streetcomplete.screens.main

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.messages.Message
import de.westnordost.streetcomplete.screens.about.AboutActivity
import de.westnordost.streetcomplete.screens.main.controls.AttributionButton
import de.westnordost.streetcomplete.screens.main.controls.AttributionLink
import de.westnordost.streetcomplete.screens.main.controls.CompassButton
import de.westnordost.streetcomplete.screens.main.controls.Crosshair
import de.westnordost.streetcomplete.screens.main.controls.LocationStateButton
import de.westnordost.streetcomplete.screens.main.controls.MainMenuButton
import de.westnordost.streetcomplete.screens.main.controls.MapButton
import de.westnordost.streetcomplete.screens.main.controls.MessagesButton
import de.westnordost.streetcomplete.screens.main.controls.OverlaySelectionButton
import de.westnordost.streetcomplete.screens.main.controls.PointerPinButton
import de.westnordost.streetcomplete.screens.main.controls.ScaleBar
import de.westnordost.streetcomplete.screens.main.controls.StarsCounter
import de.westnordost.streetcomplete.screens.main.controls.ZoomButtons
import de.westnordost.streetcomplete.screens.main.controls.findEllipsisIntersection
import de.westnordost.streetcomplete.screens.main.edithistory.EditHistorySidebar
import de.westnordost.streetcomplete.screens.main.edithistory.EditHistoryViewModel
import de.westnordost.streetcomplete.screens.main.errors.LastCrashEffect
import de.westnordost.streetcomplete.screens.main.errors.LastDownloadErrorEffect
import de.westnordost.streetcomplete.screens.main.errors.LastUploadErrorEffect
import de.westnordost.streetcomplete.screens.main.messages.MessageDialog
import de.westnordost.streetcomplete.screens.main.overlays.OverlaySelectionDropdownMenu
import de.westnordost.streetcomplete.screens.main.teammode.TeamModeWizard
import de.westnordost.streetcomplete.screens.main.urlconfig.ApplyUrlConfigEffect
import de.westnordost.streetcomplete.screens.settings.SettingsActivity
import de.westnordost.streetcomplete.screens.tutorial.IntroTutorialScreen
import de.westnordost.streetcomplete.screens.tutorial.OverlaysTutorialScreen
import de.westnordost.streetcomplete.screens.user.UserActivity
import de.westnordost.streetcomplete.ui.common.AnimatedScreenVisibility
import de.westnordost.streetcomplete.ui.common.LargeCreateIcon
import de.westnordost.streetcomplete.ui.common.StopRecordingIcon
import de.westnordost.streetcomplete.ui.common.UndoIcon
import de.westnordost.streetcomplete.ui.ktx.dir
import de.westnordost.streetcomplete.ui.ktx.pxToDp
import de.westnordost.streetcomplete.util.ktx.sendErrorReportEmail
import de.westnordost.streetcomplete.util.ktx.toast
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs

/** Map controls shown on top of the map. */
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    editHistoryViewModel: EditHistoryViewModel,
    onClickZoomIn: () -> Unit,
    onClickZoomOut: () -> Unit,
    onClickCompass: () -> Unit,
    onClickLocation: () -> Unit,
    onClickLocationPointer: () -> Unit,
    onClickCreate: () -> Unit,
    onClickStopTrackRecording: () -> Unit,
    onClickDownload: () -> Unit,
    onExplainedNeedForLocationPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    val context = LocalContext.current

    val starsCount by viewModel.starsCount.collectAsState()
    val isShowingStarsCurrentWeek by viewModel.isShowingStarsCurrentWeek.collectAsState()

    val overlays by viewModel.overlays.collectAsState()
    val selectedOverlay by viewModel.selectedOverlay.collectAsState()
    val isCreateNodeEnabled by remember { derivedStateOf { selectedOverlay?.isCreateNodeEnabled == true } }

    val isAutoSync by viewModel.isAutoSync.collectAsState()
    val unsyncedEditsCount by viewModel.unsyncedEditsCount.collectAsState()

    val isTeamMode by viewModel.isTeamMode.collectAsState()
    val indexInTeam by viewModel.indexInTeam.collectAsState()

    val messagesCount by viewModel.messagesCount.collectAsState()
    val hasMessages by remember { derivedStateOf { messagesCount > 0 } }

    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val isUploadingOrDownloading by viewModel.isUploadingOrDownloading.collectAsState()

    val urlConfig by viewModel.urlConfig.collectAsState()
    val lastCrashReport by viewModel.lastCrashReport.collectAsState()
    val lastDownloadError by viewModel.lastDownloadError.collectAsState()
    val lastUploadError by viewModel.lastUploadError.collectAsState()

    val locationState by viewModel.locationState.collectAsState()
    val isNavigationMode by viewModel.isNavigationMode.collectAsState()
    val isFollowingPosition by viewModel.isFollowingPosition.collectAsState()
    val isRecordingTracks by viewModel.isRecordingTracks.collectAsState()
    val userHasMovedCamera by viewModel.userHasMovedCamera.collectAsState()

    val mapCamera by viewModel.mapCamera.collectAsState()
    val metersPerDp by viewModel.metersPerDp.collectAsState()
    val displayedPosition by viewModel.displayedPosition.collectAsState()

    val editItems by editHistoryViewModel.editItems.collectAsState()
    val selectedEdit by editHistoryViewModel.selectedEdit.collectAsState()
    val hasEdits by remember { derivedStateOf { editItems.isNotEmpty() } }

    val showZoomButtons by viewModel.showZoomButtons.collectAsState()

    val isRequestingLogin by viewModel.isRequestingLogin.collectAsState()

    var showOverlaysDropdown by remember { mutableStateOf(false) }
    var showOverlaysTutorial by remember { mutableStateOf(false) }
    var showIntroTutorial by remember { mutableStateOf(false) }
    var showTeamModeWizard by remember { mutableStateOf(false) }
    var showMainMenuDialog by remember { mutableStateOf(false) }
    var shownMessage by remember { mutableStateOf<Message?>(null) }
    val showEditHistorySidebar by editHistoryViewModel.isShowingSidebar.collectAsState()

    val mapRotation = mapCamera?.rotation ?: 0.0
    val mapTilt = mapCamera?.tilt ?: 0.0

    val mapAttribution = listOf(
        AttributionLink(stringResource(R.string.map_attribution_osm), "https://osm.org/copyright"),
        AttributionLink("© JawgMaps", "https://jawg.io")
    )

    fun onClickOverlays() {
        showOverlaysDropdown = true
    }

    fun onClickMessages() {
        scope.launch {
            shownMessage = viewModel.popMessage()
        }
    }

    fun onClickUpload() {
        if (viewModel.isConnected) {
            viewModel.upload()
        } else {
            context.toast(R.string.offline)
        }
    }

    fun sendErrorReport(error: Exception) {
        scope.launch {
            val report = viewModel.createErrorReport(error)
            context.sendErrorReportEmail(report)
        }
    }

    LaunchedEffect(viewModel.hasShownTutorial) {
        if (!viewModel.hasShownTutorial && !isLoggedIn) {
            showIntroTutorial = true
        }
    }

    LaunchedEffect(isTeamMode) {
        // always show this toast on start to remind user that it is still on
        if (isTeamMode) {
            context.toast(R.string.team_mode_active)
        }
        // show this only once when turning it off
        else if (viewModel.teamModeChanged) {
            context.toast(R.string.team_mode_deactivated)
            viewModel.teamModeChanged = false
        }
    }

    Box(modifier) {
        if (isCreateNodeEnabled) {
            Crosshair()
        }

        var screen by remember { mutableStateOf<Rect?>(null) }
        val intersection = remember(displayedPosition, screen) {
            findEllipsisIntersection(screen, displayedPosition)
        }

        intersection?.let { (offset, angle) ->
            val rotation = angle * 180 / PI
            PointerPinButton(
                onClick = onClickLocationPointer,
                rotate = rotation.toFloat(),
                modifier = Modifier.absoluteOffset(offset.x.pxToDp(), offset.y.pxToDp()),
            ) { Image(painterResource(R.drawable.location_dot_small), null) }
        }

        Box(Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .onGloballyPositioned { screen = it.boundsInRoot() }
        ) {
            // top-start controls
            Box(Modifier.align(Alignment.TopStart)) {
                // stars counter
                StarsCounter(
                    count = starsCount,
                    modifier = Modifier
                        .defaultMinSize(minWidth = 96.dp)
                        .clickable(null, null) { viewModel.toggleShowingCurrentWeek() },
                    isCurrentWeek = isShowingStarsCurrentWeek,
                    showProgress = isUploadingOrDownloading
                )
            }

            // top-end controls
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedVisibility(hasMessages) {
                    MessagesButton(
                        onClick = ::onClickMessages,
                        messagesCount = messagesCount
                    )
                }
                if (overlays.isNotEmpty()) {
                    Box {
                        OverlaySelectionButton(
                            onClick = ::onClickOverlays,
                            overlay = selectedOverlay
                        )
                        OverlaySelectionDropdownMenu(
                            expanded = showOverlaysDropdown,
                            onDismissRequest = { showOverlaysDropdown = false },
                            overlays = overlays,
                            onSelect = { viewModel.selectOverlay(it) }
                        )
                    }
                }

                MainMenuButton(
                    onClick = { showMainMenuDialog = true },
                    unsyncedEditsCount = if (!isAutoSync) unsyncedEditsCount else 0,
                    indexInTeam = if (isTeamMode) indexInTeam else null
                )
            }

            // bottom controls
            Column(Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
            ) {
                Box(Modifier.fillMaxWidth()) {
                    // bottom-end controls
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.End,
                    ) {
                        val isCompassVisible = abs(mapRotation) >= 1.0 || abs(mapTilt) >= 1.0
                        AnimatedVisibility(
                            visible = isCompassVisible,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            CompassButton(
                                onClick = onClickCompass,
                                modifier = Modifier.graphicsLayer(
                                    rotationZ = -mapRotation.toFloat(),
                                    rotationX = mapTilt.toFloat()
                                )
                            )
                        }
                        if (showZoomButtons) {
                            ZoomButtons(
                                onZoomIn = onClickZoomIn,
                                onZoomOut = onClickZoomOut
                            )
                        }
                        LocationStateButton(
                            onClick = onClickLocation,
                            state = locationState,
                            isNavigationMode = isNavigationMode,
                            isFollowing = isFollowingPosition,
                        )
                    }

                    if (isCreateNodeEnabled) {
                        MapButton(
                            onClick = {
                                if ((mapCamera?.zoom ?: 0.0) >= 17.0) {
                                    onClickCreate()
                                } else {
                                    context.toast(R.string.download_area_too_big, Toast.LENGTH_LONG)
                                }
                            },
                            modifier = Modifier
                                .align(BiasAlignment(0.333f, 1f))
                                .padding(4.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = MaterialTheme.colors.secondaryVariant,
                            ),
                        ) {
                            LargeCreateIcon()
                        }
                    }

                    // bottom-start controls
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isRecordingTracks) {
                            MapButton(
                                onClick = onClickStopTrackRecording,
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = MaterialTheme.colors.secondaryVariant,
                                ),
                            ) {
                                StopRecordingIcon()
                            }
                        }

                        if (hasEdits) {
                            MapButton(
                                onClick = { editHistoryViewModel.showSidebar() },
                                // Don't allow undoing while uploading. Should prevent race conditions.
                                // (Undoing quest while also uploading it at the same time)
                                enabled = !isUploadingOrDownloading,
                            ) {
                                UndoIcon()
                            }
                        }
                    }
                }
                // Alternative to this would be to put the tutorial screens into a separate
                // navigation destination in a TBD MainNavHost after complete migration to Compose
                // (see #6255)
                if (!showIntroTutorial) {
                    Box(Modifier.fillMaxWidth().padding(4.dp)) {
                        AttributionButton(
                            userHasMovedMap = userHasMovedCamera,
                            attributions = mapAttribution,
                            modifier = Modifier.align(Alignment.TopStart),
                            popupElevation = 4.dp,
                        )
                        ScaleBar(
                            metersPerDp = metersPerDp,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(horizontal = 12.dp),
                            alignment = Alignment.End,
                        )
                    }
                }
            }
        }

        val dir = LocalLayoutDirection.current.dir
        AnimatedVisibility(
            visible = showEditHistorySidebar,
            enter = fadeIn() + slideInHorizontally(initialOffsetX = { -it / 2 * dir }),
            exit = fadeOut() + slideOutHorizontally(targetOffsetX = { -it / 2 * dir }),
        ) {
            EditHistorySidebar(
                editItems = editItems,
                selectedEdit = selectedEdit,
                onSelectEdit = { editHistoryViewModel.select(it.key) },
                onUndoEdit = { editHistoryViewModel.undo(it.key) },
                onDismissRequest = { editHistoryViewModel.hideSidebar() },
                featureDictionaryLazy = editHistoryViewModel.featureDictionaryLazy,
                getEditElement = editHistoryViewModel::getEditElement,
            )
        }
    }

    shownMessage?.let { message ->
        val questIcons = remember { viewModel.allQuestTypes.map { it.icon } }
        MessageDialog(
            message = message,
            onDismissRequest = { shownMessage = null },
            allQuestIconIds = questIcons
        )
    }

    if (showMainMenuDialog) {
        MainMenuDialog(
            onDismissRequest = { showMainMenuDialog = false },
            onClickProfile = { context.startActivity(Intent(context, UserActivity::class.java)) },
            onClickSettings = { context.startActivity(Intent(context, SettingsActivity::class.java)) },
            onClickAbout = { context.startActivity(Intent(context, AboutActivity::class.java)) },
            onClickDownload = onClickDownload,
            onClickUpload = ::onClickUpload,
            onClickEnterTeamMode = { showTeamModeWizard = true },
            onClickExitTeamMode = { viewModel.disableTeamMode() },
            isLoggedIn = isLoggedIn,
            indexInTeam = if (isTeamMode) indexInTeam else null,
            unsyncedEditsCount = if (!isAutoSync) unsyncedEditsCount else null,
            isUploadingOrDownloading = isUploadingOrDownloading,
        )
    }

    urlConfig?.let { config ->
        ApplyUrlConfigEffect(
            urlConfig = config.urlConfig,
            presetNameAlreadyExists = config.alreadyExists,
            onApplyUrlConfig = { viewModel.applyUrlConfig(it) }
        )
    }
    lastDownloadError?.let { error ->
        LastDownloadErrorEffect(lastError = error, onReportError = ::sendErrorReport)
    }
    lastUploadError?.let { error ->
        LastUploadErrorEffect(lastError = error, onReportError = ::sendErrorReport)
    }
    lastCrashReport?.let { report ->
        LastCrashEffect(lastReport = report, onReport = { context.sendErrorReportEmail(it) })
    }

    if (isRequestingLogin) {
        RequestLoginDialog(
            onDismissRequest = { viewModel.finishRequestingLogin() },
            onConfirmed = {
                val intent = Intent(context, UserActivity::class.java)
                intent.putExtra(UserActivity.EXTRA_LAUNCH_AUTH, true)
                context.startActivity(intent)
            }
        )
    }
}
