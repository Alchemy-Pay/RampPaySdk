package com.ach.ramppaysdk


import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.activity.result.IntentSenderRequest
import com.ach.ramp_web_sdk.googlePay.GooglePayUtils
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.wallet.WalletConstants

actual class PostMessageClient(private var webView: WebView, val config: RampPaySdkConfig) {

    var onIncomingData: ((Any) -> Unit)? = null

    @JavascriptInterface
    fun onGooglePay(json: String) {

        val task = GooglePayUtils(
            webView.context, when (config.environment) {
                RampPayEnvironment.Production -> WalletConstants.ENVIRONMENT_PRODUCTION
                else -> WalletConstants.ENVIRONMENT_TEST
            }
        ).startGooglePay(json)

        task.addOnFailureListener { exception ->
            // 处理任务失败
            when (exception) {
                is ResolvableApiException -> {
                    // 处理可解决的 API 异常
                    val intent: IntentSenderRequest =
                        IntentSenderRequest.Builder(exception.resolution).build()
                    onIncomingData?.invoke(intent)
                }
            }
        }
    }


    @JavascriptInterface
    fun refresh(view: View?) {
        webView.reload()
    }


}