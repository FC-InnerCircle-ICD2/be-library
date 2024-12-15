package com.pgeasy.www;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

class PgPaymentServiceImplTest {
    private PgPaymentServiceImpl pgPaymentService = new PgPaymentServiceImpl();

    Properties properties = new Properties();

    public PgPaymentServiceImplTest() {
        try (FileInputStream fis = new FileInputStream("src/main/resources/application.properties")) {
            properties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void paymentModule_kaKao() {
        String secretKey = properties.getProperty("kakao-secret-key");
        PaymentModule paymentModule =
                PaymentModule.builder()
                             .secretKey(secretKey)
                             .basePaymentModule(KaKaoPaymentModule.builder()
                                                                  .cid("TC0ONETIME")
                                                                  .partner_order_id(1L)
                                                                  .partner_user_id("user1")
                                                                  .item_name("Coffee")
                                                                  .quantity(1)
                                                                  .total_amount(5000)
                                                                  .tax_free_amount(0)
                                                                  .approval_url("http://localhost:8080/kakao/payment/approve/1")
                                                                  .cancel_url("http://localhost:8080/kakao/payment/cancel")
                                                                  .fail_url("http://localhost:8080/kakao/payment/fail")
                                                                  .build())
                             .build();
        CommonResponse<BaseResult> commonResponse = pgPaymentService.paymentModule(paymentModule);
        KaKaoPaymentModule.Result data = (KaKaoPaymentModule.Result) commonResponse.data();
        System.out.println(data);
    }

    @Test
    void createPayment_toss() {
        String secretKey = properties.getProperty("toss-widget-secret-key");
        ApprovePayment approvePayment =
                ApprovePayment.builder()
                              .secretKey(secretKey)
                              .baseApprovePayment(
                                      TossPayApprovePayment.builder()
                                                           .paymentKey("tgen_20241215221803hrQz7")
                                                           .orderId("MC4yOTM4NTkwMDMxMDg3")
                                                           .amount(50000)
                                                           .build())
                              .build();
        CommonResponse<BaseResult> commonResponse = pgPaymentService.approvePayment(approvePayment);
        System.out.println(commonResponse);
    }

    @Test
    void mapToPaymentClass() {
        String jsonString = """
                {
                  "country" : "KR",
                  "metadata" : null,
                  "orderId" : "MC45Njk5NjkwOTY2Mzk3",
                  "cashReceipts" : null,
                  "isPartialCancelable" : true,
                  "lastTransactionKey" : "txrd_a01jf4hhvzrdb0rteae9saepb42",
                  "discount" : null,
                  "taxExemptionAmount" : 0,
                  "suppliedAmount" : 45455,
                  "secret" : "ps_kYG57Eba3GZYYKndlnwL8pWDOxmA",
                  "type" : "NORMAL",
                  "cultureExpense" : false,
                  "taxFreeAmount" : 0,
                  "requestedAt" : "2024-12-15T15:54:02+09:00",
                  "currency" : "KRW",
                  "paymentKey" : "tgen_20241215155402jviQ2",
                  "virtualAccount" : null,
                  "checkout" : {
                    "url" : "https://api.tosspayments.com/v1/payments/tgen_20241215155402jviQ2/checkout"
                  },
                  "orderName" : "토스 티셔츠 외 2건",
                  "method" : "간편결제",
                  "useEscrow" : false,
                  "vat" : 4545,
                  "mId" : "tgen_docs",
                  "approvedAt" : "2024-12-15T15:54:35+09:00",
                  "balanceAmount" : 50000,
                  "version" : "2022-11-16",
                  "easyPay" : {
                    "amount" : 50000,
                    "provider" : "토스페이",
                    "discountAmount" : 0
                  },
                  "totalAmount" : 50000,
                  "cancels" : null,
                  "transfer" : null,
                  "mobilePhone" : null,
                  "failure" : null,
                  "receipt" : {
                    "url" : "https://dashboard.tosspayments.com/receipt/redirection?transactionId=tgen_20241215155402jviQ2&ref=PX"
                  },
                  "giftCertificate" : null,
                  "cashReceipt" : null,
                  "card" : null,
                  "status" : "DONE"
                }
                """;
        BasePayment basePayment = TossPayApprovePayment.builder().build();
        JSONObject jsonObject = pgPaymentService.processStringToJson(jsonString);
        pgPaymentService.processMapToPaymentClass(jsonObject, basePayment.getResultClass());
    }
}
