package com.isankamil.mcjobid

import android.app.Application
import com.isankamil.mcjobid.data.repository.SyncManager
import com.isankamil.mcjobid.util.NotificationScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class McJobIdApplication : Application() {
    
    @Inject
    lateinit var syncManager: SyncManager

    override fun onCreate() {
        super.onCreate()
        // Access syncManager to force Hilt to instantiate it immediately.
        // This ensures real-time sync listeners start as soon as auth state is available,
        // rather than waiting until first ViewModel injection.
        @Suppress("UNUSED_EXPRESSION")
        syncManager
        
        // Ensure Notification Channel is created on app startup
        NotificationScheduler(this)
    }
}


