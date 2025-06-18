package com.ecommerce.payment.controller;

import com.ecommerce.payment.dao.PaymentRepository;
import com.ecommerce.payment.payload.request.PaymentRequest;
import com.ecommerce.payment.payload.response.PaymentResponse;
import com.ecommerce.payment.service.PaymentService;
import com.razorpay.RazorpayException;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
public class PaymentController {

  @Autowired private PaymentService paymentService;

  @Value("${razorpay.api.key}")
  private String apiKey;

  @Value("${razorpay.api.secret}")
  private String apiSecret;

  @Autowired private PaymentRepository paymentRepository;

  @PostMapping("/checkout/{orderId}")
  public ResponseEntity<PaymentResponse> checkoutProducts(
      @PathVariable String orderId,
      @Schema(hidden = true) @RequestHeader(name = "fullname") String fullName,
      @Schema(hidden = true) @RequestHeader(name = "username") String username,
      @Schema(hidden = true) @RequestHeader(name = "contact") String contactNumber,
      @RequestBody PaymentRequest paymentRequest)
      throws RazorpayException {

    PaymentResponse paymentResponse =
        this.paymentService.checkoutProducts(
            orderId, fullName, contactNumber, username, paymentRequest);

    return ResponseEntity.status(HttpStatus.OK).body(paymentResponse);
  }

  @GetMapping("/status/{orderId}")
  public ResponseEntity<String> getPaymentStatus(@PathVariable String orderId) {

    String paymentStatus = this.paymentService.getPaymentStatus(orderId);

    return ResponseEntity.status(HttpStatus.OK).body(paymentStatus);
  }
}
