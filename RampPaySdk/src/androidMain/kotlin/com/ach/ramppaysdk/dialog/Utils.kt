package com.ach.ramp_web_sdk.dialog

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.util.DisplayMetrics
import android.view.Display
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import java.lang.reflect.InvocationTargetException
import kotlin.math.min


fun getWindowWidth(context: Context): Int {
    return (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.width
}

fun getWindowHeight(context: Context?): Int {
//    return (context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.height
    val displayMetrics = DisplayMetrics()
    val windowManager = context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics.heightPixels
}

fun dp2px(context: Context, dipValue: Float): Int {
    val scale = context.resources.displayMetrics.density
    return (dipValue * scale + 0.5f).toInt()
}

fun getStatusBarHeight(): Int {
    val resources = Resources.getSystem()
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    return resources.getDimensionPixelSize(resourceId)
}

/**
 * Return the navigation bar's height.
 *
 * @return the navigation bar's height
 */
fun getNavBarHeight(): Int {
    val res = Resources.getSystem()
    val resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android")
    return if (resourceId != 0) {
        res.getDimensionPixelSize(resourceId)
    } else {
        0
    }
}

/**
 * 全网唯一能兼容所有手机（包括全面屏）判断是否有导航栏的方法
 * @param context
 * @return
 */
fun hasNavigationBar(context: Context): Boolean {
    val appUsableSize = getAppUsableScreenSize(context)
    val realScreenSize = getRealScreenSize(context)
    return appUsableSize.y + getStatusBarHeight() < realScreenSize.y
}

fun getAppUsableScreenSize(context: Context): Point {
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display = windowManager.defaultDisplay
    val size = Point()
    display.getSize(size)
    return size
}

fun getRealScreenSize(context: Context): Point {
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display = windowManager.defaultDisplay
    val size = Point()

    if (Build.VERSION.SDK_INT >= 17) {
        display.getRealSize(size)
    } else if (Build.VERSION.SDK_INT >= 14) {
        try {
            size.x =
                (Display::class.java.getMethod("getRawWidth").invoke(display) as Int)!!
            size.y =
                (Display::class.java.getMethod("getRawHeight").invoke(display) as Int)!!
        } catch (e: IllegalAccessException) {
        } catch (e: InvocationTargetException) {
        } catch (e: NoSuchMethodException) {
        }
    }

    return size
}


fun limitWidthAndHeight(target: View, maxWidth: Int, maxHeight: Int) {
    val params = target.layoutParams
    params.width = min(target.measuredWidth.toDouble(), maxWidth.toDouble()).toInt()
    params.height = min(target.measuredHeight.toDouble(), maxHeight.toDouble()).toInt()
    target.layoutParams = params
}

fun setCursorDrawableColor(editText: EditText, color: Int) {
    try {
        val fCursorDrawableRes =
            TextView::class.java.getDeclaredField("mCursorDrawableRes")
        fCursorDrawableRes.isAccessible = true
        val mCursorDrawableRes = fCursorDrawableRes.getInt(editText)
        val fEditor = TextView::class.java.getDeclaredField("mEditor")
        fEditor.isAccessible = true
        val editor = fEditor[editText]
        val clazz: Class<*> = editor.javaClass
        val fCursorDrawable = clazz.getDeclaredField("mCursorDrawable")
        fCursorDrawable.isAccessible = true

        val drawables = arrayOfNulls<Drawable>(2)
        val res = editText.context.resources
        drawables[0] = res.getDrawable(mCursorDrawableRes)
        drawables[1] = res.getDrawable(mCursorDrawableRes)
        drawables[0]?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        drawables[1]?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        fCursorDrawable[editor] = drawables
    } catch (ignored: Throwable) {
    }
}

fun createBitmapDrawable(resources: Resources?, width: Int, color: Int): BitmapDrawable {
    val bitmap = Bitmap.createBitmap(width, 20, Bitmap.Config.ARGB_4444)
    val canvas = Canvas(bitmap)
    val paint = Paint()
    paint.color = color
    canvas.drawRect(0f, 0f, bitmap.width.toFloat(), 4f, paint)
    paint.color = Color.TRANSPARENT
    canvas.drawRect(0f, 4f, bitmap.width.toFloat(), 20f, paint)
    val bitmapDrawable = BitmapDrawable(resources, bitmap)
    bitmapDrawable.gravity = Gravity.BOTTOM
    return bitmapDrawable
}

//fun createSelector(defaultDrawable: Drawable?, focusDrawable: Drawable?): StateListDrawable {
//    val stateListDrawable = StateListDrawable()
//    stateListDrawable.addState(intArrayOf(R.attr.state_focused), focusDrawable)
//    stateListDrawable.addState(intArrayOf(), defaultDrawable)
//    return stateListDrawable
//}
