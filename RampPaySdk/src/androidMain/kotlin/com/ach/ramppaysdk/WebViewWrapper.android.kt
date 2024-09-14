package com.ach.ramppaysdk

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Message
import android.util.TypedValue
import android.view.ViewGroup
import android.webkit.JsResult
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import com.ach.ramp_web_sdk.dialog.BottomSheetFragment
import com.google.android.gms.wallet.PaymentData


actual open class WebViewWrapper(
    private val activity: ComponentActivity,
    private val config: RampPaySdkConfig
) {

    private var bottomSheet: BottomSheetFragment? = null
    private var url = WidgetUrl.generateWidgetUrl(config)
    private val webView = WebView(activity)


    private val googlePayResultLauncher =
        activity.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val paymentData = data?.let { PaymentData.getFromIntent(it) }
                val token = paymentData?.paymentMethodToken?.token
                // 处理 Google Pay 回传的 token
                webView.loadUrl("javascript:googlePay_webview_receive_token($token)")
            } else {
                // 处理支付失败或取消的情况
            }
        }


    private var mFilePathCallback: ValueCallback<Array<Uri>>? = null
    private val fileChooserLauncher: ActivityResultLauncher<Intent> =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val dataIntent = result.data
            if (dataIntent != null && dataIntent.data != null && result.resultCode == Activity.RESULT_OK) {
                val uri = dataIntent.data as Uri;
                val uriArray = arrayOf(uri)
                mFilePathCallback?.onReceiveValue(uriArray)
            } else {
                mFilePathCallback?.onReceiveValue(null)
            }
            mFilePathCallback = null
        }

    private var pendingPermissionRequest: PermissionRequest? = null
    private val requestPermissionLauncher =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions[Manifest.permission.CAMERA] == true &&
                permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
            ) {
                // 权限已授予
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    pendingPermissionRequest?.grant(pendingPermissionRequest?.resources)
                }
            } else {
                // 权限被拒绝
            }
            pendingPermissionRequest = null // 清除引用
        }


    internal actual fun initWebView(client: PostMessageClient) {

        client.onIncomingData = {
            googlePayResultLauncher.launch(it as IntentSenderRequest)
        }


        val sizeInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 60f, activity.resources.displayMetrics
        ).toInt()

        createWebView(client)

//        setupBackKeyListener()
    }

    actual fun show(mode: RampPayRenderingOption) {
        when (mode) {

            RampPayRenderingOption.WebViewOverlay -> {
                (webView.parent as? ViewGroup)?.removeView(webView)
                bottomSheet = BottomSheetFragment(activity, webView)
                bottomSheet?.show()


            }

            RampPayRenderingOption.InAppBrowser -> {
                val customTabsIntent = CustomTabsIntent.Builder().build()
                val uri = Uri.parse(url)
                if (customTabsIntent.intent.resolveActivity(activity.packageManager) != null) {
                    customTabsIntent.launchUrl(activity, uri)
                }
            }
        }
    }


    fun getWebView(): WebView = webView


    private fun createWebView(postMessageClient: PostMessageClient) {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            allowFileAccess = true
            allowContentAccess = true
            javaScriptCanOpenWindowsAutomatically = true
            setSupportMultipleWindows(false)
        }


        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                onOpenCameraPermission(request)
            }

            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                mFilePathCallback = filePathCallback
                val intent = fileChooserParams.createIntent()
                try {
                    fileChooserLauncher.launch(intent)
                } catch (e: ActivityNotFoundException) {
                    mFilePathCallback = null
                    return false
                }
                return true
            }

            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?
            ): Boolean {
                resultMsg?.run {
                    val webViewTransport = obj as? WebView.WebViewTransport
                    webViewTransport?.let { transport ->
                        val webActivity = WebView(activity)
                        transport.webView = webActivity
                    }
                    sendToTarget()
                }
                return true
            }

            override fun onJsAlert(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?
            ): Boolean {
                result?.confirm()
                return true
            }
        }

        webView.addJavascriptInterface(postMessageClient, "ach_ramp")

        webView.loadUrl(url)

        WebView.setWebContentsDebuggingEnabled(config.debug)

    }


    private fun setupBackKeyListener() {
        activity.onBackPressedDispatcher.addCallback(
            activity,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (webView.canGoBack()) {
                        webView.goBack()
                    } else {
                        bottomSheet?.dismiss() ?: kotlin.run {
                            activity.finish()
                        }

                    }
                }
            })
    }


    private fun onOpenCameraPermission(request: PermissionRequest) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            // 权限已授予
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                request.grant(request.resources)
            }
        } else {
            // 保存 request 对象的引用
            pendingPermissionRequest = request
            // 使用 ActivityResultLauncher 请求权限
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            )
        }
    }


}
