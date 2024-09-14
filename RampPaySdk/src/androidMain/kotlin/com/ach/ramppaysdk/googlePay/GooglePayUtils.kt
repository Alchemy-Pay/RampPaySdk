package com.ach.ramp_web_sdk.googlePay

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.google.android.gms.common.internal.Constants
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.CardRequirements
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentMethodTokenizationParameters
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.TransactionInfo
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import org.json.JSONObject
import java.util.Arrays

class GooglePayUtils(val context: Context,val environment:Int) {

    val LOAD_PAYMENT_DATA_REQUEST_CODE = 991

    fun requestPayment(json: String) {
        AutoResolveHelper.resolveTask(
            createPaymentDataRequest(json),
            context as Activity, LOAD_PAYMENT_DATA_REQUEST_CODE
        )

    }


     fun startGooglePay(json: String): Task<PaymentData>{
        return createPaymentDataRequest(json)
    }


    /**
     * Creates an instance of [PaymentsClient] for use in an [Context] using the
     * environment and theme set in [Constants].
     *
     * @param context from the caller activity.
     */
    private fun createPaymentsClient(): PaymentsClient {
        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(environment)
            .build()

        return Wallet.getPaymentsClient(context, walletOptions)
    }



    private fun createPaymentDataRequest(jsonStr: String): Task<PaymentData> {

        val json = JSONObject(jsonStr)
        val transactionInfo = json.getJSONObject("transactionInfo")
        val merchantInfo = json.getJSONObject("merchantInfo")
        val allowedPaymentMethod = json.getJSONArray("allowedPaymentMethods")[0] as JSONObject
        val tokenizationSpecification =
            allowedPaymentMethod.getJSONObject("tokenizationSpecification")


        val totalPriceStatus = when (transactionInfo.getString("totalPriceStatus")) {
            "FINAL" -> WalletConstants.TOTAL_PRICE_STATUS_FINAL
            "ESTIMATED" -> WalletConstants.TOTAL_PRICE_STATUS_ESTIMATED
            else -> WalletConstants.TOTAL_PRICE_STATUS_NOT_CURRENTLY_KNOWN
        }
        val request = PaymentDataRequest.newBuilder()
            .setTransactionInfo(
                TransactionInfo.newBuilder()
                    .setTotalPriceStatus(totalPriceStatus)
                    .setTotalPrice(transactionInfo.getString("totalPrice"))
                    .setCurrencyCode(transactionInfo.getString("currencyCode"))
                    .build()
            )
            .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
            .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
            .setCardRequirements(
                CardRequirements.newBuilder()
                    .addAllowedCardNetworks(
                        Arrays.asList(
                            WalletConstants.CARD_NETWORK_VISA,
                            WalletConstants.CARD_NETWORK_MASTERCARD
                        )
                    )
                    .build()
            )
        val params = PaymentMethodTokenizationParameters.newBuilder()
            .setPaymentMethodTokenizationType(
                WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_PAYMENT_GATEWAY
            )
            .addParameter(
                "gateway",
                tokenizationSpecification.getJSONObject("parameters").getString("gateway")
            )
            .addParameter(
                "gatewayMerchantId",
                tokenizationSpecification.getJSONObject("parameters").getString("gatewayMerchantId")
            )
            .build()



        request.setPaymentMethodTokenizationParameters(params)
        request.setShippingAddressRequired(false)

        return createPaymentsClient().loadPaymentData(request.build())
    }


}