package com.ach.ramppaysdk

class RampPaySdk {

    private lateinit var webViewWrapper: WebViewWrapper

    constructor(config: RampPaySdkConfig) {
        webViewWrapper = Platform().createWebViewWrapper(null, config)
    }

    constructor(context: Any, config: RampPaySdkConfig) {
        webViewWrapper = Platform().createWebViewWrapper(context, config)
    }


    fun show(mode: RampPayRenderingOption) {
        webViewWrapper.show(mode)
    }

}