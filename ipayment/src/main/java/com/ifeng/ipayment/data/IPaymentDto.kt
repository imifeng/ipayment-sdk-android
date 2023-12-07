package com.ifeng.ipayment.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class IPaymentDto(
    val payType: IPaymentType,
    val payModel: IWechat? = null,
    val payBody: String? = null,
    val payDescription: String? = null,
) : Parcelable