package com.ifeng.ipayment

import androidx.fragment.app.FragmentManager
import com.ifeng.ipayment.data.IPaymentDto
import com.ifeng.ipayment.data.IPaymentResult

object IPaymentManager {

    interface Callback {
        fun onResult(result: IPaymentResult)
    }

    fun showIPaymentLoadingDialog(
        fragmentManager: FragmentManager,
        paymentDto: IPaymentDto,
        onIPaymentCallback: Callback? = null
    ) {
        IPaymentLoadingDialog.newInstance(
            payType = paymentDto.payType,
            payModel = paymentDto.payModel,
            payBody = paymentDto.payBody,
            description = paymentDto.payDescription,
        ).apply {
            onIPaymentResultCallback = object : IPaymentLoadingDialog.Callback{
                override fun onResult(result: IPaymentResult) {
                    onIPaymentCallback?.onResult(result)
                }
            }
        }.show(fragmentManager, "IPaymentLoadingDialog")
    }

}