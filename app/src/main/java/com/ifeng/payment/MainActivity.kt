package com.ifeng.payment

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.ifeng.ipayment.IPaymentLoadingDialog
import com.ifeng.ipayment.data.PaymentResult
import com.ifeng.ipayment.data.PaymentType

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        findViewById<TextView>(R.id.text_click).setOnClickListener {
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
                        PaymentResult.PAYMENT_UNSUPPORTED_PAY->{

                        }
                        PaymentResult.PAYMENT_UNSUPPORTED_WX_SDK->{

                        }
                    }
                }.build().show()
        }

    }

}