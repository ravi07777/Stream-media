package com.lagradost.cloudstream3.utils.downloader

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.lagradost.cloudstream3.utils.ImageLink

object ImageDownloadHelper {
    private const val TAG = "ImageDownloadHelper"

    suspend fun downloadImage(context: Context, link: ImageLink) {
        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
                ?: run {
                    Log.w(TAG, "DownloadManager not available")
                    return
                }
            val fileName = link.name.take(100).replace(Regex("[/\\?%*:|\"<>]"), "_")
            val request = DownloadManager.Request(Uri.parse(link.url))
                .setTitle(fileName)
                .setDescription(link.source)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "Cloudstream/$fileName")
            if (link.headers.isNotEmpty()) {
                link.headers.forEach { (key, value) ->
                    request.addRequestHeader(key, value)
                }
            }
            downloadManager.enqueue(request)
        } catch (e: SecurityException) {
            Log.e(TAG, "Download permission denied: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Download failed: ${e.message}")
        }
    }
}