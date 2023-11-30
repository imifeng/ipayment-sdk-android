package com.ifeng.ipayment.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

//            "appid": "wxe84fac964ea9c74c",
//            "partnerid": "1613546520",
//            "prepayid": "wx01100602242515c127540e67c141360000",
//            "noncestr": "61cfb70a49a23",
//            "timestamp": 1641002762,
//            "package": "Sign=WXPay",
//            "sign": "D9C4E576FB64A2B148F861C9A0634B49"
@Parcelize
data class Wechat(
    val appid: String,
    val partnerid: String,
    val prepayid: String,
    @field:SerializedName("package") val packages: String,
    val noncestr: String,
    val timestamp: String,
    val sign: String,
) : Parcelable