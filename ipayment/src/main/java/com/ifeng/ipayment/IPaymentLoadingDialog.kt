package com.ifeng.ipayment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.*
import com.alipay.sdk.app.PayTask
import com.ifeng.ipayment.data.PaymentResult
import com.ifeng.ipayment.data.PaymentType
import com.ifeng.ipayment.data.Wechat
import com.ifeng.ipayment.databinding.DialogIpaymentLoadingBinding
import com.tencent.mm.opensdk.constants.Build
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.WXAPIFactory


class IPaymentLoadingDialog(context: Context, val builder: IPaymentLoadingDialogBuilder) :
    Dialog(context) {

    companion object {
        const val KEY_STATUS = "resultStatus"
        const val SDK_PAY_FLAG = 1
    }

    private var receiver: WxPayBroadcastReceiver? = null

    private val binding: DialogIpaymentLoadingBinding by lazy {
        val inflater: LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        DialogIpaymentLoadingBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        this.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        this.window?.setGravity(Gravity.CENTER)
        this.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)


        with(builder) {
            when (payType) {
                PaymentType.PAY_TYPE_ALI -> doAliPay(payBody)
                PaymentType.PAY_TYPE_WX -> doWxPay(payModel)
                PaymentType.PAY_TYPE_OTHER -> setResultForPaymentCallback(PaymentResult.PAYMENT_UNSUPPORTED_PAY)
                else -> {
                    setResultForPaymentCallback(PaymentResult.PAYMENT_ERROR)
                }
            }
        }

    }

    private fun doWxPay(payModel: Wechat?) {
        payModel?.let { payModel ->
            try {
                receiver = WxPayBroadcastReceiver()
                val filter = IntentFilter("we.chat.pay")
                ownerActivity?.registerReceiver(receiver, filter)

                val msgApi = WXAPIFactory.createWXAPI(ownerActivity, payModel.appid, true)
                if (msgApi.isWXAppInstalled && msgApi.wxAppSupportAPI >= Build.SUBSCRIBE_MESSAGE_SUPPORTED_SDK_INT) {
                    msgApi.registerApp(payModel.appid)
                    val request = PayReq()
                    request.appId = payModel.appid
                    request.partnerId = payModel.partnerid
                    request.prepayId = payModel.prepayid
                    request.packageValue = payModel.packages
                    request.nonceStr = payModel.noncestr
                    request.timeStamp = payModel.timestamp
                    request.sign = payModel.sign
                    msgApi.sendReq(request)
                } else {
                    setResultForPaymentCallback(PaymentResult.PAYMENT_UNSUPPORTED_WX_SDK)
                }
            } catch (e: Exception) {
                setResultForPaymentCallback(PaymentResult.PAYMENT_FAIL)
            }
        } ?: run {
            setResultForPaymentCallback(PaymentResult.PAYMENT_FAIL)
        }
    }


    private fun doAliPay(payBody: String?) {
        payBody?.let { payBody ->
            try {
                val payRunnable = Runnable {
                    val alipay = PayTask(ownerActivity)
                    val result: Map<String, String> = alipay.payV2(payBody, true)
                    val msg = Message()
                    msg.what = SDK_PAY_FLAG
                    msg.obj = result
                    mHandler.sendMessage(msg)
                }
                // 必须异步调用
                val payThread = Thread(payRunnable)
                payThread.start()
            } catch (e: Exception) {
                setResultForPaymentCallback(PaymentResult.PAYMENT_FAIL)
            }
        } ?: run {
            setResultForPaymentCallback(PaymentResult.PAYMENT_FAIL)
        }

    }

    //支付宝 返回码	含义
    //9000	订单支付成功
    //8000	正在处理中，支付结果未知（有可能已经支付成功），请查询商户订单列表中订单的支付状态
    //4000	订单支付失败
    //5000	重复请求
    //6001	用户中途取消
    //6002	网络连接出错
    //6004	支付结果未知（有可能已经支付成功），请查询商户订单列表中订单的支付状态
    //其它	其它支付错误
    private val mHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            val resultMap = msg.obj as HashMap<*, *>
            val status = resultMap[KEY_STATUS]
            if (status != null && status is String) {
                when (status) {
                    "9000" -> { // 订单支付成功
                        setResultForPaymentCallback(PaymentResult.PAYMENT_SUCCEED)
                    }
                    "4000" -> { // 订单支付失败
                        setResultForPaymentCallback(PaymentResult.PAYMENT_FAIL)
                    }
                    "6001" -> { // 用户中途取消支付操作
                        setResultForPaymentCallback(PaymentResult.PAYMENT_CANCEL)
                    }
                    "6002" -> { // 网络连接出错
                        setResultForPaymentCallback(PaymentResult.PAYMENT_FAIL)
                    }
                    "8000" -> { // 正在处理中，支付结果未知（有可能已经支付成功），请查询商户订单列表中订单的支付状态（小概率状态）
                        setResultForPaymentCallback(PaymentResult.PAYMENT_ERROR)
                    }
                    else -> {
                        setResultForPaymentCallback(PaymentResult.PAYMENT_ERROR)
                    }
                }
            } else {
                setResultForPaymentCallback(PaymentResult.PAYMENT_ERROR)
            }
        }
    }

    //微信 返回码	含义
    //  0 //成功
    // -1 //一般错误 可能的原因：签名错误、未注册APPID、项目设置APPID不正确、注册的APPID与设置的不匹配、其他异常等。
    // -2 //用户取消
    private inner class WxPayBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.getIntExtra("errCode", 0)) {
                0 -> setResultForPaymentCallback(PaymentResult.PAYMENT_SUCCEED)
                -1 -> setResultForPaymentCallback(PaymentResult.PAYMENT_ERROR)
                -2 -> setResultForPaymentCallback(PaymentResult.PAYMENT_CANCEL)
                else -> setResultForPaymentCallback(PaymentResult.PAYMENT_ERROR)
            }
        }
    }

    private fun setResultForPaymentCallback(paymentResult: PaymentResult) {
        builder.onPaymentResultCallback?.invoke(paymentResult)
        dismiss()
    }

    override fun onStop() {
        super.onStop()
        receiver?.let {
            context.unregisterReceiver(it)
        }
    }

    class IPaymentLoadingDialogBuilder(private val activity: Activity) {
        var description: String? = null
        var payType: PaymentType? = null

        // 微信支付
        var payModel: Wechat? = null

        // 支付宝支付
        var payBody: String? = null

        var onPaymentResultCallback: ((result: PaymentResult) -> Unit)? = null

        fun setDescription(value: String): IPaymentLoadingDialogBuilder =
            apply { this.description = value }

        fun setPayType(value: PaymentType): IPaymentLoadingDialogBuilder =
            apply { this.payType = value }

        fun setPayModel(value: Wechat?): IPaymentLoadingDialogBuilder =
            apply { this.payModel = value }

        fun setPayBody(value: String?): IPaymentLoadingDialogBuilder =
            apply { this.payBody = value }


        fun setOnPaymentResultCallback(value: ((result: PaymentResult) -> Unit)): IPaymentLoadingDialogBuilder =
            apply { this.onPaymentResultCallback = value }


        fun build(): IPaymentLoadingDialog = IPaymentLoadingDialog(
            context = activity,
            builder = this
        ).apply {
            setOwnerActivity(activity)
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
        }
    }
}