package com.hhst.youtubelite.onboarding

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hhst.youtubelite.R
import com.hhst.youtubelite.account.SignInActivity
import com.hhst.youtubelite.account.presentation.AccountSwitchSheet
import com.hhst.youtubelite.core.AppLocale
import com.hhst.youtubelite.data.media.MediaSession
import com.hhst.youtubelite.downloader.core.DownloadPrefs
import com.hhst.youtubelite.extension.ExtensionManager
import com.hhst.youtubelite.home.HomeActivity
import com.hhst.youtubelite.onboarding.ui.OnboardingScreen
import com.hhst.youtubelite.player.PlaybackDefaults
import com.hhst.youtubelite.ui.NativeActivity
import com.hhst.youtubelite.ui.design.setAppContent
import org.koin.java.KoinJavaComponent.getKoin

class OnboardingActivity : NativeActivity() {

    private lateinit var viewModel: OnboardingViewModel
    private var showSwitchSheet by mutableStateOf(false)

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (::viewModel.isInitialized) {
            if (result.resultCode == RESULT_OK) {
                viewModel.onSignInCompleted()
            } else {
                viewModel.refreshAccountState()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val koin = getKoin()
        val onboardingPrefs = koin.get<OnboardingPreferences>()
        val extensionManager = koin.get<ExtensionManager>()
        val playbackDefaults = koin.get<PlaybackDefaults>()
        val downloadPrefs = koin.get<DownloadPrefs>()
        val pipSupported = packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
        val dynamicSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        val fromSettings = intent.getBooleanExtra(EXTRA_FROM_SETTINGS, false)

        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OnboardingViewModel(
                    onboardingPrefs = onboardingPrefs,
                    extensionManager = extensionManager,
                    playbackDefaults = playbackDefaults,
                    downloadPrefs = downloadPrefs,
                    pipSupported = pipSupported,
                    dynamicColorsSupported = dynamicSupported,
                    applyLanguage = { tag ->
                        val handledBySystem = AppLocale.apply(this@OnboardingActivity, tag)
                        if (!handledBySystem) recreate()
                    },
                ) as T
            }
        })[OnboardingViewModel::class.java]

        setAppContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            BackHandler {
                when {
                    showSwitchSheet -> showSwitchSheet = false
                    viewModel.previousStep() -> Unit
                    fromSettings -> finish()
                    else -> finish()
                }
            }

            Box(Modifier.fillMaxSize()) {
                OnboardingScreen(
                    state = state,
                    onBack = {
                        if (!viewModel.previousStep() && fromSettings) {
                            finish()
                        }
                    },
                    onNext = viewModel::nextStep,
                    onSkipStep = viewModel::skipCurrentStep,
                    onSkipSetup = { finishAndEnterApp(fromSettings = fromSettings, skipped = true) },
                    onEditFromReview = viewModel::editFromReview,
                    onSignInClick = { launchSignIn() },
                    onCreateAccountClick = { launchCreateAccount() },
                    onContinueGuest = { viewModel.continueAsGuest(advance = true) },
                    onOpenAccountSwitch = { showSwitchSheet = true },
                    onSelectQuality = viewModel::setDefaultQuality,
                    onSelectSpeed = viewModel::setDefaultSpeed,
                    onToggleBackgroundPlay = viewModel::setBackgroundPlay,
                    onToggleAutoplay = viewModel::setAutoplay,
                    onTogglePip = viewModel::setPipEnabled,
                    onToggleMiniPlayer = viewModel::setInAppMiniPlayer,
                    onToggleDataSaver = viewModel::setDataSaver,
                    onToggleRememberPosition = viewModel::setRememberLastPosition,
                    onToggleRememberResize = viewModel::setRememberResizeMode,
                    onToggleGestures = viewModel::setGesturesEnabled,
                    onSelectTheme = viewModel::setThemeMode,
                    onSelectAccent = viewModel::setAccentColor,
                    onToggleDynamicColors = viewModel::setDynamicColors,
                    onSelectGlassStyle = viewModel::setGlassStyle,
                    onSelectLanguage = viewModel::setLanguage,
                    onToggleSponsorBlock = viewModel::setSponsorBlockEnabled,
                    onToggleDeArrow = viewModel::setDeArrowEnabled,
                    onToggleHideShorts = viewModel::setHideShorts,
                    onToggleShowDislikes = viewModel::setShowDislikes,
                    onToggleDownloadWifiOnly = viewModel::setDownloadWifiOnly,
                    onSelectDownloadQuality = viewModel::setDownloadQuality,
                    onSelectHapticStrength = viewModel::setHapticStrength,
                    onStartWatching = { finishAndEnterApp(fromSettings = fromSettings, skipped = false) },
                )

                if (showSwitchSheet) {
                    AccountSwitchSheet(
                        accounts = state.accounts,
                        onSelect = { accountId ->
                            viewModel.selectAccount(accountId)
                            showSwitchSheet = false
                        },
                        onAddAccount = {
                            showSwitchSheet = false
                            launchSignIn()
                        },
                        onDismiss = { showSwitchSheet = false },
                        deviceSelected = MediaSession.anonymousMode(),
                        onUseDevice = {
                            viewModel.continueAsGuest(advance = false)
                            showSwitchSheet = false
                        },
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::viewModel.isInitialized) {
            viewModel.refreshAccountState()
        }
    }

    private fun launchSignIn() {
        signInLauncher.launch(Intent(this, SignInActivity::class.java))
    }

    private fun launchCreateAccount() {
        val uri = Uri.parse("https://accounts.google.com/signup?service=youtube")
        val launched = runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }.isSuccess
        if (!launched) {
            Toast.makeText(
                this,
                getString(R.string.onboarding_account_create_browser_error),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    private fun finishAndEnterApp(fromSettings: Boolean, skipped: Boolean) {
        if (skipped) viewModel.skipSetup() else viewModel.completeOnboarding()
        if (!fromSettings) {
            startActivity(
                Intent(this, HomeActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    putExtra(HomeActivity.EXTRA_SKIP_ONBOARDING, true)
                },
            )
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.navigation_tab_enter, R.anim.navigation_tab_exit)
        }
        finish()
    }

    companion object {
        const val EXTRA_FROM_SETTINGS = "onboarding_from_settings"

        fun intent(context: Context, fromSettings: Boolean = false): Intent =
            Intent(context, OnboardingActivity::class.java)
                .putExtra(EXTRA_FROM_SETTINGS, fromSettings)
    }
}
