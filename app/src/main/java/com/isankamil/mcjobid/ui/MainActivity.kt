package com.isankamil.mcjobid.ui

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.isankamil.mcjobid.ui.screen.auth.PinLockScreen
import com.isankamil.mcjobid.util.LocaleHelper
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint
import com.isankamil.mcjobid.data.repository.BookingRepository
import com.isankamil.mcjobid.data.repository.ReminderRepository
import com.isankamil.mcjobid.data.repository.UserProfileRepository
import com.isankamil.mcjobid.ui.navigation.McJobIdNavigation
import com.isankamil.mcjobid.ui.theme.McJobIdTheme
import com.isankamil.mcjobid.util.settingsDataStore
import com.isankamil.mcjobid.util.SettingsKeys

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var bookingRepository: BookingRepository

    @Inject
    lateinit var reminderRepository: ReminderRepository

    @Inject
    lateinit var userProfileRepository: UserProfileRepository

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ensure native Activity window background is white before edge-to-edge layout
        window.setBackgroundDrawableResource(android.R.color.white)
        enableEdgeToEdge()
        
        requestPermissionsIfNeeded()
        val resetCode = parseResetPasswordCode(intent)
        
        setContent {
            val context = LocalContext.current
            val preferencesState by context.settingsDataStore.data.collectAsState(initial = null)

            // Render a clean white surface while DataStore completes initial disk load (~50ms)
            // This eliminates initial PIN state flip (true -> false) and prevents navigation stack crashes.
            if (preferencesState == null) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.ui.graphics.Color.White
                ) {}
                return@setContent
            }

            val currentPrefs = preferencesState ?: return@setContent
            val appTheme = currentPrefs.get(SettingsKeys.APP_THEME) ?: "system"
            val pinEnabled = currentPrefs.get(SettingsKeys.APP_PIN_ENABLED) ?: false
            val pinCode = currentPrefs.get(SettingsKeys.APP_PIN_CODE) ?: ""
            val pinTimeoutMinutes = currentPrefs.get(SettingsKeys.PIN_TIMEOUT_MINUTES) ?: 5
            val backupKey = currentPrefs.get(SettingsKeys.SECURITY_BACKUP_KEY) ?: "MCJOB2026"
            val appLanguage = currentPrefs.get(SettingsKeys.APP_LANGUAGE) ?: "id"

            val systemInDark = isSystemInDarkTheme()
            val isDarkTheme = when (appTheme) {
                "light" -> false
                "dark" -> true
                else -> systemInDark
            }

            var isUnlocked by remember {
                mutableStateOf(!pinEnabled || pinCode.isBlank())
            }

            var lastBackgroundTimestamp by remember { mutableLongStateOf(0L) }

            val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner, pinEnabled, pinCode, pinTimeoutMinutes) {
                val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                    if (event == androidx.lifecycle.Lifecycle.Event.ON_STOP) {
                        lastBackgroundTimestamp = System.currentTimeMillis()
                    } else if (event == androidx.lifecycle.Lifecycle.Event.ON_START) {
                        if (pinEnabled && pinCode.isNotBlank() && lastBackgroundTimestamp > 0L) {
                            val elapsedMs = System.currentTimeMillis() - lastBackgroundTimestamp
                            val timeoutMs = pinTimeoutMinutes * 60 * 1000L
                            if (elapsedMs >= timeoutMs) {
                                isUnlocked = false
                            }
                        }
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            LaunchedEffect(appLanguage) {
                LocaleHelper.applyLocale(context, appLanguage)
            }

            McJobIdTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!isUnlocked && pinEnabled && pinCode.isNotBlank()) {
                        PinLockScreen(
                            targetPin = pinCode,
                            backupKey = backupKey,
                            onPinSuccess = { isUnlocked = true },
                            onResetPin = { isUnlocked = true }
                        )
                    } else {
                        val navController = rememberNavController()
                        
                        McJobIdNavigation(
                            navController = navController,
                            bookingRepository = bookingRepository,
                            reminderRepository = reminderRepository,
                            userProfileRepository = userProfileRepository,
                            initialResetCode = resetCode
                        )
                    }
                }
            }
        }
    }

    private fun parseResetPasswordCode(intent: Intent?): String? {
        val data = intent?.data ?: return null
        val mode = data.getQueryParameter("mode")
        val oobCode = data.getQueryParameter("oobCode")
        return if (mode == "resetPassword" && !oobCode.isNullOrBlank()) oobCode else null
    }

    private fun requestPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(
                    android.Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }
}
