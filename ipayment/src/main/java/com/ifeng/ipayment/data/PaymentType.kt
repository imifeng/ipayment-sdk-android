package com.ifeng.ipayment.data

import com.google.gson.annotations.SerializedName

enum class PaymentType {
    @SerializedName("PAY_TYPE_ALI")
    PAY_TYPE_ALI,

    @SerializedName("PAY_TYPE_WX")
    PAY_TYPE_WX
}