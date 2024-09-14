package com.ach.ramppaysdk

expect class  Platform(){

     fun createWebViewWrapper(context: Any?,config: RampPaySdkConfig): WebViewWrapper
}