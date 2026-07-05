package app.kobuggi.hyuabot.ui.notice

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.net.toUri
import app.kobuggi.hyuabot.databinding.FragmentNoticeWebviewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NoticeWebviewFragment @Inject constructor() : BottomSheetDialogFragment() {
    private val binding by lazy { FragmentNoticeWebviewBinding.inflate(layoutInflater) }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.toolbar.setOnMenuItemClickListener {
            dismiss()
            true
        }
        arguments?.getString("url")?.let { url ->
            binding.noticeWebview.apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                }
                webViewClient = object: WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        val uri = request?.url ?: return false
                        if (uri.scheme == "http" || uri.scheme == "https") {
                            return false
                        }

                        return openExternalUrl(uri.toString(), view)
                    }

                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        binding.progressBar.visibility = View.VISIBLE
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        binding.progressBar.visibility = View.GONE
                    }
                }
                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        binding.progressBar.progress = newProgress
                        binding.progressBar.visibility = if (newProgress >= 100) View.GONE else View.VISIBLE
                    }
                }
                loadUrl(url)
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
            isFitToContents = true
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
            setOnShowListener {
                val bottomSheet = findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                bottomSheet?.let {
                    it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                }
            }
        }
    }

    override fun onDestroyView() {
        binding.noticeWebview.apply {
            stopLoading()
            webChromeClient = null
            destroy()
        }
        super.onDestroyView()
    }

    private fun openExternalUrl(url: String, view: WebView?): Boolean {
        val intent = runCatching {
            if (url.startsWith("intent://")) {
                Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
            } else {
                Intent(Intent.ACTION_VIEW, url.toUri())
            }
        }.getOrNull() ?: return true

        runCatching {
            startActivity(intent)
        }.onFailure { error ->
            if (error is ActivityNotFoundException) {
                intent.getStringExtra("browser_fallback_url")?.let { fallbackUrl ->
                    view?.loadUrl(fallbackUrl)
                }
            }
        }

        return true
    }
}
