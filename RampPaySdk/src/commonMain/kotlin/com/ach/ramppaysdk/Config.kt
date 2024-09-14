package com.ach.ramppaysdk

enum class RampPayEnvironment {
    Production,
    Sandbox;
}

data class RampPaySdkConfig(
    var debug: Boolean=false,
    var environment: RampPayEnvironment,
    var params: String? = null,
)



enum class RampPayRenderingOption {
    InAppBrowser,
    WebViewOverlay
}
