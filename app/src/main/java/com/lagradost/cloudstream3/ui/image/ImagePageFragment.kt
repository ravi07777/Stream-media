package com.lagradost.cloudstream3.ui.image

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.lagradost.cloudstream3.utils.AppUtils.toJson
import com.lagradost.cloudstream3.utils.AppUtils.tryParseJson
import com.lagradost.cloudstream3.utils.ImageLink
import com.lagradost.cloudstream3.utils.ImageLoader.loadImage
import com.lagradost.cloudstream3.utils.UiImage

class ImagePagerAdapter(
    fragment: Fragment,
    private val images: List<ImageLink>,
) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = images.size

    override fun createFragment(position: Int): Fragment {
        return ImagePageFragment.newInstance(images[position].url, images[position].headers)
    }
}

class ImagePageFragment : Fragment() {
    companion object {
        private const val ARG_URL = "url"
        private const val ARG_HEADERS = "headers"

        fun newInstance(url: String, headers: Map<String, String> = emptyMap()): ImagePageFragment {
            return ImagePageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_URL, url)
                    putString(ARG_HEADERS, headers.toJson())
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val url = arguments?.getString(ARG_URL) ?: return View(requireContext())
        val headersJson = arguments?.getString(ARG_HEADERS)
        val headers: Map<String, String>? = tryParseJson(headersJson)

        val imageView = ImageView(requireContext()).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        imageView.loadImage(UiImage(url = url, headers = headers))
        return imageView
    }
}