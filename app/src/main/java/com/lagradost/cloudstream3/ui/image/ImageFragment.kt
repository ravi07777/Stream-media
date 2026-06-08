package com.lagradost.cloudstream3.ui.image

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.lagradost.cloudstream3.CommonActivity.showToast
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.databinding.FragmentImageViewerBinding
import com.lagradost.cloudstream3.ui.BaseFragment
import com.lagradost.cloudstream3.utils.Coroutines.ioSafe
import com.lagradost.cloudstream3.utils.ImageLink
import com.lagradost.cloudstream3.utils.downloader.ImageDownloadHelper

class ImageFragment : BaseFragment<FragmentImageViewerBinding>(
    BaseFragment.BindingCreator.Inflate(FragmentImageViewerBinding::inflate)
) {
    override fun fixLayout(view: View) {}

    override fun onBindingCreated(binding: FragmentImageViewerBinding) {
        val args = arguments
        val imageUrls = args?.getStringArrayList("imageUrls") ?: return
        val source = args?.getString("source", "") ?: ""
        val names = args?.getStringArrayList("imageNames") ?: arrayListOf()

        val imageLinks = imageUrls.mapIndexed { index, url ->
            ImageLink(
                source = source,
                name = names.getOrElse(index) { "Image ${index + 1}" },
                url = url,
            )
        }

        binding.viewPager.adapter = ImagePagerAdapter(this, imageLinks)
        binding.viewPager.offscreenPageLimit = 1

        binding.pageIndicator.text = "1 / ${imageLinks.size}"
        binding.viewPager.registerOnPageChangeCallback(object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.pageIndicator.text = "${position + 1} / ${imageLinks.size}"
            }
        })

        binding.shareButton.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            if (currentItem < imageLinks.size) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, imageLinks[currentItem].url)
                }
                startActivity(Intent.createChooser(intent, getString(R.string.share_image)))
            }
        }

        binding.copyButton.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            if (currentItem < imageLinks.size) {
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Image URL", imageLinks[currentItem].url))
                showToast(R.string.url_copied, Toast.LENGTH_SHORT)
            }
        }

        binding.downloadButton.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            if (currentItem < imageLinks.size) {
                ioSafe {
                    ImageDownloadHelper.downloadImage(requireContext(), imageLinks[currentItem])
                }
            }
        }

        binding.closeButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        if (imageLinks.size <= 1) {
            binding.pageIndicator.isVisible = false
        }
    }
}