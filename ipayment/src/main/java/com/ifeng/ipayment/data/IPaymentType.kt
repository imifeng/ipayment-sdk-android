package com.ifeng.ipayment.data

import com.google.gson.annotations.SerializedName

enum class IPaymentType {
    @SerializedName("PAY_TYPE_ALI")
    PAY_TYPE_ALI,

    @SerializedName("PAY_TYPE_WX")
    PAY_TYPE_WX,

    @SerializedName("PAY_TYPE_OTHER")
    PAY_TYPE_OTHER
}