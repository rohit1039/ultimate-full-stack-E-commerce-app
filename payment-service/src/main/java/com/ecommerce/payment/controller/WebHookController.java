package com.ecommerce.payment.controller;

import com.ecommerce.payment.dao.PaymentRepository;
import com.ecommerce.payment.model.Payment;
import com.ecommerce.payment.payload.request.PaymentMethod;
import com.ecommerce.payment.payload.request.PaymentStatus;
import com.ecommerce.payment.service.PaymentService;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import java.util.Optional;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebHookController {

  @Autowired private PaymentService paymentService;

  @Value("${razorpay.api.key}")
  private String apiKey;

  @Value("${razorpay.api.secret}")
  private String apiSecret;

  @Autowired private PaymentRepository paymentRepository;

  @PostMapping("/webhook/razorpay")
  public ResponseEntity<String> handleWebhook(
      @RequestBody String payload, @RequestHeader("X-Razorpay-Signature") String signature) {
    try {
      // Verify signature
      String webhookSecret = apiSecret;
      RazorpayClient razorpay = new RazorpayClient(apiKey, apiSecret);
      boolean isValid = Utils.verifyWebhookSignature(payload, signature, webhookSecret);

      if (!isValid) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
      }

      JSONObject eventJson = new JSONObject(payload);
      String eventType = eventJson.getString("event");

      if ("payment_link.paid".equals(eventType)) {
        JSONObject paymentLink =
            eventJson
                .getJSONObject("payload")
                .getJSONObject("payment_link")
                .getJSONObject("entity");
        String paymentLinkId = paymentLink.getString("id");

        // Get payment ID from 'payment' object, not from paymentLink
        JSONObject payment =
            eventJson.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
        String paymentId = payment.getString("id");

        // Fetch payment details
        com.razorpay.Payment razorpayPayment = razorpay.payments.fetch(paymentId);
        String method = razorpayPayment.get("method");

        // Update DB
        Optional<Payment> opt = paymentRepository.findById(paymentLinkId);
        Payment dbPayment = new Payment();
        if (opt.isPresent()) {
          dbPayment = opt.get();
          dbPayment.setPaymentMethod(PaymentMethod.valueOf(method.toUpperCase()));
          dbPayment.setPaymentStatus(PaymentStatus.SUCCESS);
        } else {
          dbPayment.setPaymentMethod(PaymentMethod.valueOf(method.toUpperCase()));
          dbPayment.setPaymentStatus(PaymentStatus.FAILED);
        }
        paymentRepository.save(dbPayment);
      }
      return ResponseEntity.ok("Webhook handled");
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error handling webhook");
    }
  }
}
