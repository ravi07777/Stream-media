package com.lagradost.cloudstream3.ui.archive

import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.utils.ArchiveLink

object ArchiveLinkDialog {
    private const val TAG = "ArchiveLinkDialog"

    fun show(context: Context, link: ArchiveLink) {
        val items = arrayOf(
            context.getString(R.string.copy_url),
            context.getString(R.string.share),
            context.getString(R.string.open_externally),
            context.getString(R.string.download),
        )
        AlertDialog.Builder(context)
            .setTitle(link.name)
            .setItems(items) { _, which ->
                when (which) {
                    0 -> copyUrl(context, link.url)
                    1 -> shareUrl(context, link.url, link.name)
                    2 -> openExternally(context, link.url)
                    3 -> downloadExternally(context, link)
                }
            }
            .show()
    }

    private fun copyUrl(context: Context, url: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("url", url))
        Toast.makeText(context, R.string.url_copied, Toast.LENGTH_SHORT).show()
    }

    private fun shareUrl(context: Context, url: String, name: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "$name\n$url")
        }
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)))
    }

    private fun openExternally(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, R.string.error_invalid_data, Toast.LENGTH_SHORT).show()
        }
    }

    private fun downloadExternally(context: Context, link: ArchiveLink) {
        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
                ?: run {
                    Log.w(TAG, "DownloadManager not available")
                    return
                }
            val fileName = link.name.take(100).replace(Regex("[/\\?%*:|\"<>]"), "_")
            val request = DownloadManager.Request(Uri.parse(link.url))
                .setTitle(fileName)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Cloudstream/$fileName")
            if (link.headers.isNotEmpty()) {
                link.headers.forEach { (key, value) ->
                    request.addRequestHeader(key, value)
                }
            }
            downloadManager.enqueue(request)
        } catch (e: SecurityException) {
            Log.e(TAG, "Download permission denied: ${e.message}")
            Toast.makeText(context, R.string.download_failed, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Download failed: ${e.message}")
            Toast.makeText(context, R.string.download_failed, Toast.LENGTH_SHORT).show()
        }
    }
}