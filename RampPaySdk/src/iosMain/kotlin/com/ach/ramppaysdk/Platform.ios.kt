package com.ach.ramppaysdk

actual class Platform {

    actual fun createWebViewWrapper(
        context: Any?,
        config: RampPaySdkConfig
    ): WebViewWrapper {
        val webViewWrapper = WebViewWrapper(config)
        webViewWrapper.initWebView(PostMessageClient())
        return webViewWrapper
    }

}