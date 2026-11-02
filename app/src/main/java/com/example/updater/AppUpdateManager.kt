package com.example.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.example.BuildConfig
import com.example.data.remote.GitHubReleaseApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

sealed interface UpdateUiState {
    object Idle : UpdateUiState
    object Checking : UpdateUiState
    data class UpToDate(val currentVersion: String) : UpdateUiState
    data class UpdateAvailable(
        val currentVersion: String,
        val latestVersion: String,
        val releaseNotes: String,
        val apkUrl: String,
        val apkSize: Long
    ) : UpdateUiState
    data class Downloading(val progressPercent: Int) : UpdateUiState
    data class DownloadCompleted(val apkFile: File) : UpdateUiState
    data class Error(val message: String) : UpdateUiState
}

class AppUpdateManager(
    private val context: Context,
    private val apiService: GitHubReleaseApiService = GitHubReleaseApiService.create()
) {
    private val _uiState = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    private val httpClient = OkHttpClient.Builder().build()

    suspend fun checkForUpdates(
        owner: String = "truedano",
        repo: String = "truedanoIPOAndroid",
        isManual: Boolean = false
    ) = withContext(Dispatchers.IO) {
        _uiState.value = UpdateUiState.Checking
        try {
            val release = apiService.getLatestRelease(owner, repo)
            val currentVersion = BuildConfig.VERSION_NAME
            val remoteTag = release.tagName

            if (isNewerVersion(current = currentVersion, remote = remoteTag)) {
                val apkAsset = release.assets.find { it.name.endsWith(".apk", ignoreCase = true) }
                    ?: release.assets.firstOrNull()

                if (apkAsset != null) {
                    _uiState.value = UpdateUiState.UpdateAvailable(
                        currentVersion = currentVersion,
                        latestVersion = remoteTag.removePrefix("v").removePrefix("V"),
                        releaseNotes = release.body ?: "新版本已發布，建議立即更新以獲取最新功能與穩定性修正。",
                        apkUrl = apkAsset.downloadUrl,
                        apkSize = apkAsset.size
                    )
                } else {
                    _uiState.value = if (isManual) UpdateUiState.UpToDate(currentVersion) else UpdateUiState.Idle
                }
            } else {
                _uiState.value = if (isManual) UpdateUiState.UpToDate(currentVersion) else UpdateUiState.Idle
            }
        } catch (e: Exception) {
            if (isManual) {
                _uiState.value = UpdateUiState.Error("檢查更新失敗: ${e.localizedMessage ?: "網路異常"}")
            } else {
                _uiState.value = UpdateUiState.Idle
            }
        }
    }

    suspend fun downloadApk(apkUrl: String) = withContext(Dispatchers.IO) {
        try {
            _uiState.value = UpdateUiState.Downloading(0)
            val request = Request.Builder().url(apkUrl).build()
            val response = httpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                _uiState.value = UpdateUiState.Error("下載失敗 (HTTP ${response.code})")
                return@withContext
            }

            val body = response.body ?: run {
                _uiState.value = UpdateUiState.Error("回應內容為空")
                return@withContext
            }

            val totalBytes = body.contentLength()
            val updateDir = File(context.cacheDir, "updates").apply { mkdirs() }
            val apkFile = File(updateDir, "update.apk")
            if (apkFile.exists()) {
                apkFile.delete()
            }

            body.byteStream().use { inputStream ->
                FileOutputStream(apkFile).use { outputStream ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    var totalRead = 0L

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        if (totalBytes > 0) {
                            val percent = ((totalRead * 100) / totalBytes).toInt()
                            _uiState.value = UpdateUiState.Downloading(percent)
                        }
                    }
                    outputStream.flush()
                }
            }

            _uiState.value = UpdateUiState.DownloadCompleted(apkFile)
        } catch (e: Exception) {
            _uiState.value = UpdateUiState.Error("下載過程發生錯誤: ${e.localizedMessage}")
        }
    }

    fun installApk(apkFile: File) {
        // Android 8.0+ 檢查未知來源安裝權限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(settingsIntent)
                return
            }
        }

        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(installIntent)
    }

    fun dismissUpdate() {
        _uiState.value = UpdateUiState.Idle
    }

    companion object {
        fun isNewerVersion(current: String, remote: String): Boolean {
            val cleanCurrent = current.removePrefix("v").removePrefix("V").trim()
            val cleanRemote = remote.removePrefix("v").removePrefix("V").trim()

            val currentParts = cleanCurrent.split(".").map { it.toIntOrNull() ?: 0 }
            val remoteParts = cleanRemote.split(".").map { it.toIntOrNull() ?: 0 }

            val maxLength = maxOf(currentParts.size, remoteParts.size)
            for (i in 0 until maxLength) {
                val currentPart = currentParts.getOrElse(i) { 0 }
                val remotePart = remoteParts.getOrElse(i) { 0 }
                if (remotePart > currentPart) return true
                if (remotePart < currentPart) return false
            }
            return false
        }
    }
}
