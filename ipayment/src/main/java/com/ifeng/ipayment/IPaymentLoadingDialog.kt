package com.ifeng.ipayment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.alipay.sdk.app.PayTask
import com.ifeng.ipayment.data.IPaymentResult
import com.ifeng.ipayment.data.IPaymentType
import com.ifeng.ipayment.data.IWechat
import com.ifeng.ipayment.databinding.DialogIpaymentLoadingBinding
import com.tencent.mm.opensdk.constants.Build
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.WXAPIFactory


class IPaymentLoadingDialog : DialogFragment() {

    companion object {
        const val IPAYMENT_ALI_PAY_FLAG = 1

        private const val IPAYMENT_PAY_DESCRIPTION = "payDescription"
        private const val IPAYMENT_PAY_TYPE = "payType"
        private const val IPAYMENT_ALI_PAY_BODY = "payBody"
        private const val IPAYMENT_WX_PAY_MODEL = "payModel"

        fun newInstance(
            description: String?, payType: IPaymentType?, payModel: IWechat?, payBody: String?
        ): IPaymentLoadingDialog {
            val args = Bundle()
            args.putString(IPAYMENT_PAY_DESCRIPTION, description)
            args.putSerializable(IPAYMENT_PAY_TYPE, payType)
            args.putParcelable(IPAYMENT_WX_PAY_MODEL, payModel)
            args.putString(IPAYMENT_ALI_PAY_BODY, payBody)
            val fragment = IPaymentLoadingDialog().apply {
                arguments = args
                isCancelable = false
            }
            return fragment
        }
    }

    private val iDescription: String? by lazy {
        arguments?.getString(IPAYMENT_PAY_DESCRIPTION)
    }

    private val iPayType: IPaymentType? by lazy {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable(IPAYMENT_PAY_TYPE, IPaymentType::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getSerializable(IPAYMENT_PAY_TYPE) as? IPaymentType
        }
    }

    private val iPayBody: String? by lazy {
        arguments?.getString(IPAYMENT_ALI_PAY_BODY)
    }

    private val iPayModel: IWechat? by lazy {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(IPAYMENT_WX_PAY_MODEL, IWechat::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(IPAYMENT_WX_PAY_MODEL)
        }
    }

    interface Callback {
        fun onResult(result: IPaymentResult)
    }

    var onIPaymentResultCallback: Callback? = null


    private var receiver: WxPayBroadcastReceiver? = null

    private var binding: DialogIpaymentLoadingBinding? = null

    override fun getTheme(): Int {
        return R.style.IPaymentDialogStyle
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogIpaymentLoadingBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        iDescription?.let {
            binding?.paymentDescription?.text = it
        }

        when (iPayType) {
            IPaymentType.PAY_TYPE_ALI -> doAliPay(iPayBody)
            IPaymentType.PAY_TYPE_WX -> doWxPay(iPayModel)
            IPaymentType.PAY_TYPE_OTHER -> setResultForPaymentCallback(IPaymentResult.PAYMENT_UNSUPPORTED_PAY)
            else -> {
                setResultForPaymentCallback(IPaymentResult.PAYMENT_ERROR)
            }
        }
    }

    private fun doWxPay(payModel: IWechat?) {
        payModel?.let { model ->
            try {
                receiver = WxPayBroadcastReceiver()
                val filter = IntentFilter("we.chat.pay")
                ContextCompat.registerReceiver(
                    requireContext(),
                    receiver,
                    filter,
                    null,
                    null,
                    ContextCompat.RECEIVER_NOT_EXPORTED
                )

                val msgApi = WXAPIFactory.createWXAPI(activity, model.appid, true)
                if (msgApi.isWXAppInstalled && msgApi.wxAppSupportAPI >= Build.SUBSCRIBE_MESSAGE_SUPPORTED_SDK_INT) {
                    msgApi.registerApp(model.appid)
                    val request = PayReq()
                    request.appId = model.appid
                    request.partnerId = model.partnerid
                    request.prepayId = model.prepayid
                    request.packageValue = model.packages
                    request.nonceStr = model.noncestr
                    request.timeStamp = model.timestamp
                    request.sign = model.sign
                    msgApi.sendReq(request)
                } else {
                    setResultForPaymentCallback(IPaymentResult.PAYMENT_UNSUPPORTED_WX_SDK)
                }
            } catch (e: Exception) {
                setResultForPaymentCallback(IPaymentResult.PAYMENT_FAIL)
            }
        } ?: run {
            setResultForPaymentCallback(IPaymentResult.PAYMENT_FAIL)
        }
    }

    private fun doAliPay(payBody: String?) {
        payBody?.let { body ->
            try {
                val payRunnable = Runnable {
                    val alipay = PayTask(activity)
                    val result: Map<String, String> = alipay.payV2(body, true)
                    val msg = Message()
                    msg.what = IPAYMENT_ALI_PAY_FLAG
                    msg.obj = result
                    mHandler.sendMessage(msg)
                }
                // 必须异步调用
                val payThread = Thread(payRunnable)
                payThread.start()
            } catch (e: Exception) {
                setResultForPaymentCallback(IPaymentResult.PAYMENT_FAIL)
            }
        } ?: run {
            setResultForPaymentCallback(IPaymentResult.PAYMENT_FAIL)
        }
    }

    /**
     * 支付宝支付验证:
     * 返回码 含义
     * 9000	订单支付成功
     * 8000	正在处理中，支付结果未知（有可能已经支付成功），请查询商户订单列表中订单的支付状态
     * 4000	订单支付失败
     * 5000	重复请求
     * 6001	用户中途取消
     * 6002	网络连接出错
     * 6004	支付结果未知（有可能已经支付成功），请查询商户订单列表中订单的支付状态
     * 其它	其它支付错误
     */
    private val mHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val resultMap = msg.obj as HashMap<*, *>
            val status = resultMap["resultStatus"]
            if (status != null && status is String) {
                when (status) {
                    "9000" -> { // 订单支付成功
                        setResultForPaymentCallback(IPaymentResult.PAYMENT_SUCCEED)
                    }

                    "4000" -> { // 订单支付失败
                        setResultForPaymentCallback(IPaymentResult.PAYMENT_FAIL)
                    }

                    "6001" -> { // 用户中途取消支付操作
                        setResultForPaymentCallback(IPaymentResult.PAYMENT_CANCEL)
                    }

                    "6002" -> { // 网络连接出错
                        setResultForPaymentCallback(IPaymentResult.PAYMENT_FAIL)
                    }

                    "8000" -> { // 正在处理中，支付结果未知（有可能已经支付成功），请查询商户订单列表中订单的支付状态（小概率状态）
                        setResultForPaymentCallback(IPaymentResult.PAYMENT_ERROR)
                    }

                    else -> {
                        setResultForPaymentCallback(IPaymentResult.PAYMENT_ERROR)
                    }
                }
            } else {
                setResultForPaymentCallback(IPaymentResult.PAYMENT_ERROR)
            }
        }
    }

    /**
     * 微信支付验证:
     * 返回码 含义
     *  0 成功
     * -1 一般错误 可能的原因：签名错误、未注册APPID、项目设置APPID不正确、注册的APPID与设置的不匹配、其他异常等。
     * -2 用户取消
     */
    private inner class WxPayBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.getIntExtra("errCode", 0)) {
                0 -> setResultForPaymentCallback(IPaymentResult.PAYMENT_SUCCEED)
                -1 -> setResultForPaymentCallback(IPaymentResult.PAYMENT_ERROR)
                -2 -> setResultForPaymentCallback(IPaymentResult.PAYMENT_CANCEL)
                else -> setResultForPaymentCallback(IPaymentResult.PAYMENT_ERROR)
            }
        }
    }

    private fun setResultForPaymentCallback(paymentResult: IPaymentResult) {
        onIPaymentResultCallback?.onResult(paymentResult)
        dismiss()
    }

    override fun onStop() {
        super.onStop()
        receiver?.let {
            context?.unregisterReceiver(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // required on fragment
        binding = null
        mHandler.removeCallbacksAndMessages(null)
    }
}