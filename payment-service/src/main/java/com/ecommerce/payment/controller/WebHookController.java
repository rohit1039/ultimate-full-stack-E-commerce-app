package com.ecommerce.payment.controller;

import static com.ecommerce.payment.constants.Constant.ORDER_SERVICE_SERVER_URL;
import static com.ecommerce.payment.constants.Constant.PRODUCT_SERVICE_PAYMENT_FAILURE_URL;
import static com.ecommerce.payment.constants.Constant.PRODUCT_SERVICE_PAYMENT_SUCCESS_URL;

import com.ecommerce.payment.dao.PaymentRepository;
import com.ecommerce.payment.model.Payment;
import com.ecommerce.payment.payload.request.OrderItemRequest;
import com.ecommerce.payment.payload.request.PaymentMethod;
import com.ecommerce.payment.payload.request.PaymentStatus;
import com.ecommerce.payment.payload.request.PaymentStatusUpdate;
import com.ecommerce.payment.payload.response.OrderResponse;
import com.ecommerce.payment.service.PaymentService;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class WebHookController {

  @Autowired private PaymentService paymentService;

  @Value("${razorpay.api.key}")
  private String apiKey;

  @Value("${razorpay.api.secret}")
  private String apiSecret;

  @Autowired private PaymentRepository paymentRepository;

  @Autowired private RestTemplate restTemplate;

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

        PaymentStatusUpdate updateRequest =
            new PaymentStatusUpdate(
                dbPayment.getOrderId(),
                dbPayment.getPaymentStatus().name(),
                dbPayment.getPaymentMethod().name());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PaymentStatusUpdate> requestEntity = new HttpEntity<>(updateRequest, headers);

        ResponseEntity<OrderResponse> response =
            restTemplate.postForEntity(
                ORDER_SERVICE_SERVER_URL, requestEntity, OrderResponse.class);

        List<OrderItemRequest> orderItemRequest =
            Objects.requireNonNull(response.getBody()).getOrderItems();

        HttpEntity<List<OrderItemRequest>> entity = new HttpEntity<>(orderItemRequest, headers);

        String paymentStatus = paymentLink.getString("status");

        ResponseEntity<String> paid =
            paymentStatus.equals("paid")
                ? restTemplate.postForEntity(
                    PRODUCT_SERVICE_PAYMENT_SUCCESS_URL, entity, String.class)
                : restTemplate.postForEntity(
                    PRODUCT_SERVICE_PAYMENT_FAILURE_URL, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful() && paid.getStatusCode().is2xxSuccessful()) {
          return ResponseEntity.ok("Webhook processed and order status updated.");
        } else {
          return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
              .body("Webhook processed but failed to update order.");
        }
      }
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error occurred: " + e.getMessage());
    }
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error handling webhook");
  }
}
