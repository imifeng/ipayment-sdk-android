# iPayment

[![](https://jitpack.io/v/imifeng/ipayment-sdk-android.svg)](https://jitpack.io/#imifeng/ipayment-sdk-android)


## 使用
```
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
               PaymentResult.PAYMENT_SUCCEED ->{
                  // 支付成功
               }
               PaymentResult.PAYMENT_ERROR ->{
                  // 支付异常, 跳转至订单列表
               }
               PaymentResult.PAYMENT_CANCEL ->{
                  // 取消支付, 跳转至订单列表
               }
               PaymentResult.PAYMENT_FAIL ->{
                  // 支付失败, 跳转至订单列表
               }
               PaymentResult.PAYMENT_UNSUPPORTED_PAY->{
                  // 暂不支持该支付类型
               }
               PaymentResult.PAYMENT_UNSUPPORTED_WX_SDK->{
                  // 请先安装微信或升级至最新版本
               }
            }
        }
    }
)
```
