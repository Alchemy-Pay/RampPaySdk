package com.ach.ramppaysdk

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.Foundation.setValue
import platform.UIKit.*
import platform.WebKit.*
import platform.CoreGraphics.*
import platform.SafariServices.SFSafariViewController


actual class WebViewWrapper(config: RampPaySdkConfig) {
    private val url: String = WidgetUrl.generateWidgetUrl(config)

    internal actual fun initWebView(client: PostMessageClient) {
    }

    actual fun show(mode: RampPayRenderingOption) {
        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController

        when (mode) {
            RampPayRenderingOption.WebViewOverlay -> {
                val webViewController = WebViewController(url)
                rootViewController?.presentViewController(
                    webViewController, animated = true, completion = null
                )

            }

            RampPayRenderingOption.InAppBrowser -> {
                val nsUrl = NSURL(string = url)
                val safariViewController = SFSafariViewController(nsUrl)
                rootViewController?.presentViewController(
                    safariViewController,
                    animated = true,
                    completion = null
                )

            }
        }

    }
}

private class WebViewController(private var url: String) : UIViewController(
    nibName = null,
    bundle = null
), WKNavigationDelegateProtocol,
    WKUIDelegateProtocol {


    private lateinit var webView: WKWebView

    @OptIn(ExperimentalForeignApi::class)
    override fun viewDidLoad() {
        super.viewDidLoad()

        // 初始化 WKWebView 并配置跨域设置
        val webViewConfiguration = WKWebViewConfiguration().apply {
            setValue(true, forKey = "allowsInlineMediaPlayback")
            setValue(true, forKey = "allowsAirPlayForMediaPlayback")
            setValue(true, forKey = "allowsPictureInPictureMediaPlayback")
            setValue(true, forKey = "mediaTypesRequiringUserActionForPlayback")
        }


        webView = WKWebView(
            frame = CGRectZero.readValue(),
            configuration = webViewConfiguration
        )
        webView.navigationDelegate = this
        webView.UIDelegate = this
        webView.allowsBackForwardNavigationGestures = true
        view.addSubview(webView)
        loadUrl(url)

    }


    override fun webView(
        webView: WKWebView,
        createWebViewWithConfiguration: WKWebViewConfiguration,
        forNavigationAction: WKNavigationAction,
        windowFeatures: WKWindowFeatures
    ): WKWebView? {
        if (forNavigationAction.targetFrame == null) {
            // 处理打开新窗口的请求
            webView.loadRequest(forNavigationAction.request)
        }
        return null
    }

    override fun viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        adjustWebViewFrame()
    }


    @OptIn(ExperimentalForeignApi::class)
    private fun adjustWebViewFrame() {
        view.safeAreaInsets.useContents {
            val topInset = this.top
            val bottomInset = this.bottom
            UIApplication.sharedApplication.keyWindow?.bounds?.useContents {
                val width = this.size.width
                val height = this.size.height
                // 设置 WKWebView 的 frame
                webView.setFrame(
                    CGRectMake(
                        x = 0.0,
                        y = topInset,
                        width = width,
                        height = height - bottomInset - topInset
                    )
                )
            }
        }
    }


    private fun loadUrl(url: String) {
        val nsUrl = NSURL(string = url)
        val request = NSURLRequest(nsUrl)
        webView.loadRequest(request)
    }
}

