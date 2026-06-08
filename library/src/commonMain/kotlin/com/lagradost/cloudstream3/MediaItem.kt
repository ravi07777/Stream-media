package com.lagradost.cloudstream3

import com.lagradost.cloudstream3.utils.ArchiveLink
import com.lagradost.cloudstream3.utils.ArchiveType
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ImageLink

/**
 * Unified sealed class for any playable/viewable media.
 * Replaces ad-hoc ExtractorLink-only approach with a generic model.
 * Backward-compatible via adapter functions.
 */
sealed class MediaItem {
    abstract val source: String
    abstract val name: String
    abstract val url: String
    abstract val referer: String
    abstract val headers: Map<String, String>
}

data class VideoItem(
    override val source: String,
    override val name: String,
    override val url: String,
    override val referer: String,
    override val headers: Map<String, String> = mapOf(),
    val quality: Int,
    val type: com.lagradost.cloudstream3.utils.ExtractorLinkType,
    val extractorData: String? = null,
    val audioTracks: List<AudioFile> = emptyList(),
) : MediaItem()

data class ImageItem(
    override val source: String,
    override val name: String,
    override val url: String,
    override val referer: String = "",
    override val headers: Map<String, String> = mapOf(),
    val width: Int? = null,
    val height: Int? = null,
) : MediaItem()

data class ArchiveItem(
    override val source: String,
    override val name: String,
    override val url: String,
    override val referer: String = "",
    override val headers: Map<String, String> = mapOf(),
    val archiveType: ArchiveType = ArchiveType.Unknown,
    val fileSize: Long? = null,
) : MediaItem()

/** Adapter: ExtractorLink -> VideoItem */
fun ExtractorLink.toVideoItem(): VideoItem = VideoItem(
    source = source,
    name = name,
    url = url,
    referer = referer,
    headers = headers,
    quality = quality,
    type = type,
    extractorData = extractorData,
    audioTracks = audioTracks,
)

/** Adapter: ImageLink -> ImageItem */
fun ImageLink.toImageItem(): ImageItem = ImageItem(
    source = source,
    name = name,
    url = url,
    referer = referer,
    headers = headers,
    width = width,
    height = height,
)

/** Adapter: ArchiveLink -> ArchiveItem */
fun ArchiveLink.toArchiveItem(): ArchiveItem = ArchiveItem(
    source = source,
    name = name,
    url = url,
    referer = referer,
    headers = headers,
    archiveType = archiveType,
    fileSize = fileSize,
)

/** Detect archive type from URL extension */
fun detectArchiveType(url: String): ArchiveType {
    return when {
        url.endsWith(".zip", true) -> ArchiveType.Zip
        url.endsWith(".rar", true) -> ArchiveType.Rar
        url.endsWith(".7z", true) -> ArchiveType.SevenZ
        else -> ArchiveType.Unknown
    }
}