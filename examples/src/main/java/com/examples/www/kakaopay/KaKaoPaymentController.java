package com.examples.www.kakaopay;

import com.pgeasy.www.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/kakao")
@Controller
public class KaKaoPaymentController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final PgPaymentService pgPaymentService;

    @Value("${kakao-client-id}")
    private String CLIENT_ID;

    @Value("${kakao-secret-key}")
    private String KAKAO_SECRET_KEY;

    private String lastTid;
    private String lastUserId;

    @ResponseBody
    @PostMapping("/payment/ready")
    public ResponseEntity<BaseResult> confirmPayment(@RequestBody KaKaoConfirmPaymentRequest confirmPaymentRequest) {
        PaymentModule paymentModule = createPaymentModule(confirmPaymentRequest);
        CommonResponse<BaseResult> commonResponse = pgPaymentService.paymentModule(paymentModule);
        logger.info("commonResponse: {}", commonResponse);
        updateLastOrder(confirmPaymentRequest, commonResponse);
        return ResponseEntity.status(commonResponse.code()).body(commonResponse.data());
    }

    private PaymentModule createPaymentModule(KaKaoConfirmPaymentRequest confirmPaymentRequest) {
        Long orderId = confirmPaymentRequest.orderId();

        return PaymentModule.builder()
                            .secretKey(KAKAO_SECRET_KEY)
                            .basePaymentModule(KaKaoPaymentModule.builder()
                                                                 .cid(CLIENT_ID)
                                                                 .partner_order_id(orderId)
                                                                 .partner_user_id(confirmPaymentRequest.userId())
                                                                 .item_name(confirmPaymentRequest.itemName())
                                                                 .quantity(confirmPaymentRequest.quantity())
                                                                 .total_amount(confirmPaymentRequest.totalAmount())
                                                                 .tax_free_amount(0)
                                                                 .approval_url("http://localhost:8080/kakao/payment/approve/" +orderId)
                                                                 .cancel_url("http://localhost:8080/kakao/payment/cancel")
                                                                 .fail_url("http://localhost:8080/kakao/payment/fail")
                                                                 .build())
                            .build();
    }

    private void updateLastOrder(KaKaoConfirmPaymentRequest confirmPaymentRequest, CommonResponse<BaseResult> commonResponse) {
        KaKaoPaymentModule.Result data = (KaKaoPaymentModule.Result) commonResponse.data();
        lastTid = data.tid();
        lastUserId = confirmPaymentRequest.userId();
    }

    @ResponseBody
    @GetMapping("/payment/approve/{id}")
    public ResponseEntity<BaseResult> approvePayment(@PathVariable("id") String orderId, @RequestParam("pg_token") String pgToken) {
        ApprovePayment approvePayment = createApprovePayment(orderId, pgToken);
        CommonResponse<BaseResult> commonResponse = pgPaymentService.approvePayment(approvePayment);
        logger.info("commonResponse: {}", commonResponse);
        return ResponseEntity.status(commonResponse.code()).body(commonResponse.data());
    }

    private ApprovePayment createApprovePayment(String orderId, String pgToken) {
        return ApprovePayment.builder()
                             .secretKey(KAKAO_SECRET_KEY)
                             .baseApprovePayment(KaKaoPayApprovePayment.builder()
                                                                       .cid(CLIENT_ID)
                                                                       .tid(lastTid)
                                                                       .partner_order_id(orderId)
                                                                       .partner_user_id(lastUserId)
                                                                       .pg_token(pgToken)
                                                                       .build())
                             .build();
    }
}