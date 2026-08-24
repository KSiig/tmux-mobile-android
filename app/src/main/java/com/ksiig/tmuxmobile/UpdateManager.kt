package com.ksiig.tmuxmobile

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class UpdateManager(private val activity: Activity) {

    fun checkAndInstall() {
        Toast.makeText(activity, "Checking for updates…", Toast.LENGTH_SHORT).show()

        Thread {
            try {
                val release = fetchLatestRelease()
                val remoteVersion = release.getString("tag_name").removePrefix("v")
                val localVersion = activity.packageManager
                    .getPackageInfo(activity.packageName, 0).versionName ?: "0.0.0"

                if (!isNewer(remoteVersion, localVersion)) {
                    activity.runOnUiThread {
                        Toast.makeText(activity, "Already on latest ($localVersion)", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }

                val assets = release.getJSONArray("assets")
                var downloadUrl: String? = null
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    if (asset.getString("name") == "app-debug.apk") {
                        downloadUrl = asset.getString("browser_download_url")
                        break
                    }
                }

                if (downloadUrl == null) {
                    activity.runOnUiThread {
                        Toast.makeText(activity, "No debug APK in release $remoteVersion", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }

                activity.runOnUiThread {
                    Toast.makeText(activity, "Downloading v$remoteVersion…", Toast.LENGTH_SHORT).show()
                }

                val apkFile = downloadApk(downloadUrl, remoteVersion)
                installApk(apkFile)
            } catch (e: Exception) {
                activity.runOnUiThread {
                    Toast.makeText(activity, "Update failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    private fun fetchLatestRelease(): JSONObject {
        val url = URL("$GITHUB_API/releases/latest")
        val conn = url.openConnection() as HttpURLConnection
        conn.setRequestProperty("Accept", "application/vnd.github.v3+json")
        conn.connectTimeout = 10_000
        conn.readTimeout = 10_000
        try {
            return JSONObject(conn.inputStream.bufferedReader().readText())
        } finally {
            conn.disconnect()
        }
    }

    private fun downloadApk(downloadUrl: String, version: String): File {
        val dir = File(activity.cacheDir, "updates")
        dir.mkdirs()
        val file = File(dir, "tmux-mobile-$version.apk")

        val url = URL(downloadUrl)
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 30_000
        conn.readTimeout = 60_000
        try {
            conn.inputStream.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } finally {
            conn.disconnect()
        }
        return file
    }

    private fun installApk(file: File) {
        val uri = FileProvider.getUriForFile(
            activity,
            "${activity.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        activity.startActivity(intent)
    }

    private fun isNewer(remote: String, local: String): Boolean {
        val r = remote.split(".").map { it.toIntOrNull() ?: 0 }
        val l = local.split(".").map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(r.size, l.size)) {
            val rv = r.getOrElse(i) { 0 }
            val lv = l.getOrElse(i) { 0 }
            if (rv > lv) return true
            if (rv < lv) return false
        }
        return false
    }

    companion object {
        private const val GITHUB_API = "https://api.github.com/repos/KSiig/tmux-mobile-android"
    }
}
