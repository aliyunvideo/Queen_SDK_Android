/*
 *  Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 *  Use of this source code is governed by a MIT license that can be found in the LICENSE file
 */

package com.netease.yunxin.nertc.demo.basic

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.RelativeLayout
import android.widget.TextView

class CommonBrowseActivity : BaseActivity() {
    private var title: String? = null
    private var url: String? = null
    private var webView: WebView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common_browse)
        title = intent.getStringExtra(PARAM_KEY_TITLE)
        url = intent.getStringExtra(PARAM_KEY_URL)
        initViews()
        paddingStatusBarHeight(R.id.rl_root)
    }

    private fun initViews() {
        val close = findViewById<View>(R.id.iv_close)
        close.setOnClickListener { finish() }
        val tvTitle = findViewById<TextView>(R.id.tv_title)
        tvTitle.text = title
        webView = initWebView()
        val webViewGroup = findViewById<ViewGroup>(R.id.rl_root)
        val layoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        layoutParams.addRule(RelativeLayout.BELOW, R.id.title_divide)
        webView!!.layoutParams = layoutParams
        webViewGroup.addView(webView)
        webView!!.loadUrl(url!!)
    }

    private fun initWebView(): WebView {
        val webView = WebView(applicationContext)
        webView.setOnLongClickListener { true }
        val client: WebViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                val uri = Uri.parse(url)
                val scheme = uri.scheme
                var result =
                    TextUtils.isEmpty(scheme) || scheme != SCHEME_HTTP && scheme != SCHEME_HTTPS
                if (result) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = uri
                    intent.addCategory(Intent.CATEGORY_DEFAULT)
                    if (intent.resolveActivity(packageManager) != null) {
                        startActivity(intent)
                    } else {
                        result = false
                    }
                }
                return result
            }
        }
        webView.webViewClient = client
        webView.webChromeClient = WebChromeClient()
        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.setSupportZoom(true)
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
        webView.settings.domStorageEnabled = true
        webView.settings.blockNetworkImage = false
        webView.settings.javaScriptEnabled = true
        return webView
    }

    override fun onBackPressed() {
        if (webView!!.canGoBack()) {
            webView!!.goBack()
            return
        }
        super.onBackPressed()
    }

    override fun ignoredLoginEvent(): Boolean {
        return true
    }

    override fun provideStatusBarConfig(): StatusBarConfig? {
        return StatusBarConfig.Builder()
            .statusBarDarkFont(true)
            .statusBarColor(R.color.color_ffffff)
            .build()
    }

    companion object {
        private const val PARAM_KEY_TITLE = "param_key_title"
        private const val PARAM_KEY_URL = "param_key_url"
        private const val SCHEME_HTTP = "http"
        private const val SCHEME_HTTPS = "https"
        fun launch(context: Context, title: String?, url: String?) {
            val intent = Intent(context, CommonBrowseActivity::class.java)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            intent.putExtra(PARAM_KEY_TITLE, title)
            intent.putExtra(PARAM_KEY_URL, url)
            context.startActivity(intent)
        }
    }
}