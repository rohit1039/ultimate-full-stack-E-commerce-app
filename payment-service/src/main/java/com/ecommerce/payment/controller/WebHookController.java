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

/**
 * The WebHookController class handles incoming Razorpay webhook events. It validates the event
 * payload and signature, processes the payment information, updates the database, and notifies
 * other services about the payment status.
 *
 * <p>This controller is specifically designed to handle the `payment_link.paid` webhook event from
 * Razorpay and perform the following actions: - Validate the webhook signature for authenticity. -
 * Parse the event payload to extract relevant payment details. - Fetch payment information and
 * update the application's database accordingly. - Notify external services like order service and
 * product service about the payment status to ensure consistency across systems.
 *
 * <p>Dependencies: - PaymentService: Used to manage payment-related business logic. -
 * PaymentRepository: Used to interact with the database storing payment information. -
 * RestTemplate: Used to interact with external services like order and product service.
 *
 * <p>Configuration: - Requires the Razorpay API key and secret to be configured in the environment.
 *
 * <p>Endpoint: - POST /webhook/razorpay: Handles webhook events from Razorpay.
 *
 * <p>Errors: - Returns a 401 Unauthorized response if the webhook signature is invalid. - Returns a
 * 500 Internal Server Error response in case of any processing failure. - Returns a 502 Bad Gateway
 * response if communication with external services fails.
 */
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
