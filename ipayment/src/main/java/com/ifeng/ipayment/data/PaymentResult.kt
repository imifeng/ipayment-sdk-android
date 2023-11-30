package com.ifeng.ipayment.data

import com.google.gson.annotations.SerializedName

enum class PaymentResult {
    @SerializedName("PAYMENT_SUCCEED")
    PAYMENT_SUCCEED,

    @SerializedName("PAYMENT_FAIL")
    PAYMENT_FAIL,

    @SerializedName("PAYMENT_CANCEL")
    PAYMENT_CANCEL,

    @SerializedName("PAYMENT_ERROR")
    PAYMENT_ERROR,

    @SerializedName("PAYMENT_SUPPORTED_SDK")
    PAYMENT_SUPPORTED_SDK
}