package com.ach.ramp_web_sdk.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ach.ramppaysdk.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog


class BottomSheetFragment(context: Context, private val webView: WebView) :
    BottomSheetDialog(context) {

    private var isWebViewAtTop: Boolean = false

    init {
        setContentView(R.layout.fragment_bottom_sheet)
        setupDialog()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupDialog() {
        // 获取 BottomSheet 的根视图
        val bottomSheet = findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.isHideable = true
            behavior.state = BottomSheetBehavior.STATE_EXPANDED

            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        dismiss()
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                }
            })

            it.background = ContextCompat.getDrawable(context, R.drawable.button_while_r8)
            window?.setBackgroundDrawableResource(android.R.color.transparent)

            it.post {
                val bottomInsets = ViewCompat.getRootWindowInsets(it)
                    ?.getInsets(WindowInsetsCompat.Type.systemBars())?.bottom ?: 0
                val layoutParams = it.layoutParams
                layoutParams.height = (context.resources.displayMetrics.heightPixels) - bottomInsets
                it.layoutParams = layoutParams
            }


        }

        // 设置 WebView 和关闭按钮
        findViewById<FrameLayout>(R.id.ll_view)?.addView(webView)
        findViewById<View>(R.id.tv_close)?.setOnClickListener { dismiss() }

        // 监听WebView的滚动
        webView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            isWebViewAtTop = (scrollY == 0)
            val contentHeight = webView.contentHeight * webView.scale
            isWebViewAtTop = scrollY == 0
            val isWebViewAtBottom = (webView.height + scrollY >= contentHeight)

            if (isWebViewAtTop || isWebViewAtBottom) {
                behavior.isDraggable = true
            } else {
                behavior.isDraggable = false
            }
        }

        // 处理WebView的触摸事件
        webView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    if (isWebViewAtTop) {
                        behavior.isDraggable = true
                    } else {
                        behavior.isDraggable = false
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    behavior.isDraggable = true
                }
            }
            false
        }
    }





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setOnKeyListener { dialog, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    dismiss()
                }
                true // 返回 true 表示消费了事件
            } else {
                false // 返回 false 表示未消费，继续传递事件
            }
        }
    }


}
