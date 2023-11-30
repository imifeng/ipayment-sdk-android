package com.ifeng.payment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ifeng.ipayment.IPaymentLoadingDialog
import com.ifeng.ipayment.data.PaymentResult
import com.ifeng.ipayment.data.PaymentType

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TEST CODE
        IPaymentLoadingDialog.IPaymentLoadingDialogBuilder(this)
            .setPayType(PaymentType.PAY_TYPE_ALI)
            .setPayBody("test")
            .setOnPaymentResultCallback { result ->
                when (result) {
                    PaymentResult.PAYMENT_SUCCEED ->{

                    }
                    PaymentResult.PAYMENT_ERROR ->{

                    }
                    PaymentResult.PAYMENT_CANCEL ->{

                    }
                    PaymentResult.PAYMENT_FAIL ->{

                    }
                    PaymentResult.PAYMENT_SUPPORTED_SDK ->{

                    }
                }
            }
    }
}