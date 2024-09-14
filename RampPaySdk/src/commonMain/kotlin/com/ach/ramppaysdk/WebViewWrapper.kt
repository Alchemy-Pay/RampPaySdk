package com.ach.ramppaysdk



expect class WebViewWrapper {

    internal fun initWebView(client:PostMessageClient)
    fun show(mode:RampPayRenderingOption)
}
