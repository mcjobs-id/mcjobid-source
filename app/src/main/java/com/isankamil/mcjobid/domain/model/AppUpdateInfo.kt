package com.isankamil.mcjobid.domain.model

data class AppUpdateInfo(
    val latestVersionCode: Int = 0,
    val latestVersionName: String = "",
    val minSupportedVersionCode: Int = 0,
    val apkDownloadUrl: String = "",
    val apkSizeMb: String = "",
    val releaseNotes: String = "",
    val isForceUpdate: Boolean = false,
    val releaseDate: String = "",
    val isUpdateAvailable: Boolean = false,
    val isForced: Boolean = false
)
