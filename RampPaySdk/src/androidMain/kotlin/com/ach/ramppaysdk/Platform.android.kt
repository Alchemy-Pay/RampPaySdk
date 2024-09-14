package com.ach.ramppaysdk

import androidx.activity.ComponentActivity

actual class Platform {
    actual fun createWebViewWrapper(
        context: Any?,
        config: RampPaySdkConfig
    ): WebViewWrapper {
       val webViewWrapper =  WebViewWrapper(context as ComponentActivity,config)
        webViewWrapper.initWebView(PostMessageClient(webViewWrapper.getWebView(),config))
        return webViewWrapper
    }

}