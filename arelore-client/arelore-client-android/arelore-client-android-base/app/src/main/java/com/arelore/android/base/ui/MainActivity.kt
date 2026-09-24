package com.arelore.android.base.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.arelore.android.base.BuildConfig
import com.arelore.android.base.databinding.ActivityMainBinding
import com.arelore.android.base.hotupdate.HotUpdateManager
import kotlinx.coroutines.launch

/**
 * Portrait WebView shell that renders remote H5 content.
 * Content hot-update is achieved by loading a remote URL (and optional remote config).
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var hotUpdateManager: HotUpdateManager

    private var currentUrl: String = BuildConfig.DEFAULT_CONTENT_URL
    private var pageLoadFailed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hotUpdateManager = HotUpdateManager(applicationContext)
        setupWebView()
        setupRetry()
        setupBackNavigation()
        bootstrapContent()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            // Prefer network so server-side H5 updates show without reinstalling the APK.
            cacheMode = WebSettings.LOAD_DEFAULT
            mediaPlaybackRequiresUserGesture = true
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false
            userAgentString = "$userAgentString AreloreAndroid/${BuildConfig.VERSION_NAME}"
        }

        binding.webView.webChromeClient = WebChromeClient()
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                pageLoadFailed = false
                showLoading()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                if (!pageLoadFailed) {
                    showContent()
                }
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                if (request?.isForMainFrame == true) {
                    pageLoadFailed = true
                    showError()
                }
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                // Keep navigation inside the app WebView.
                return false
            }
        }
    }

    private fun setupRetry() {
        binding.retryButton.setOnClickListener {
            bootstrapContent()
        }
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.webView.canGoBack()) {
                        binding.webView.goBack()
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )
    }

    private fun bootstrapContent() {
        showLoading()
        lifecycleScope.launch {
            val config = hotUpdateManager.resolveContentUrl()
            currentUrl = config.contentUrl
            if (config.forceRefresh) {
                binding.webView.clearCache(true)
            }
            binding.webView.loadUrl(currentUrl)
        }
    }

    private fun showLoading() {
        binding.loadingContainer.visibility = View.VISIBLE
        binding.errorContainer.visibility = View.GONE
        binding.webView.visibility = View.GONE
    }

    private fun showContent() {
        binding.loadingContainer.visibility = View.GONE
        binding.errorContainer.visibility = View.GONE
        binding.webView.visibility = View.VISIBLE
    }

    private fun showError() {
        binding.loadingContainer.visibility = View.GONE
        binding.webView.visibility = View.GONE
        binding.errorContainer.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        binding.webView.apply {
            stopLoading()
            destroy()
        }
        super.onDestroy()
    }
}
