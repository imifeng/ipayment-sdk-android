package com.ifeng.payment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.ifeng.ipayment.IPaymentLoadingDialog
import com.ifeng.ipayment.IPaymentManager
import com.ifeng.ipayment.data.IPaymentDto
import com.ifeng.ipayment.data.IPaymentResult
import com.ifeng.ipayment.data.IPaymentType

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        findViewById<TextView>(R.id.text_click).setOnClickListener {
            // TEST CODE
            IPaymentManager.showIPaymentLoadingDialog(
                supportFragmentManager,
                paymentDto = IPaymentDto(
                    payType = IPaymentType.PAY_TYPE_ALI,
                    payBody = "test-ali-pay-body",
                ),
                onIPaymentCallback = object : IPaymentManager.Callback{
                    override fun onResult(result: IPaymentResult) {
                        when (result) {
                            IPaymentResult.PAYMENT_SUCCEED -> {

                            }
                            IPaymentResult.PAYMENT_ERROR -> {

                            }
                            IPaymentResult.PAYMENT_CANCEL -> {

                            }
                            IPaymentResult.PAYMENT_FAIL -> {

                            }
                            IPaymentResult.PAYMENT_UNSUPPORTED_PAY -> {

                            }
                            IPaymentResult.PAYMENT_UNSUPPORTED_WX_SDK -> {

                            }
                        }
                    }
                }
            )
        }
    }

}