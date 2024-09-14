package com.ach.ramppaysdk

internal class WidgetUrl {
    companion object {
         fun generateWidgetUrl(
            config: RampPaySdkConfig,
        ): String {
            val params = config.params
            val sdkEnvironment = config.environment

            val hostname = getHostnameForEnvironment(sdkEnvironment)

            return when (params.isNullOrEmpty()) {
                true -> hostname
                else -> buildString {
                    append(hostname)
                    append("?$params")
                }
            }


        }

        private fun getHostnameForEnvironment(environment: RampPayEnvironment): String {
            return when (environment) {
                RampPayEnvironment.Production -> "https://ramp.alchemypay.org"
                RampPayEnvironment.Sandbox -> "https://ramp-test.alchemytech.cc"
           }
        }

    }
}