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

/**
 * PaymentController handles API endpoints related to the payment processes. It provides
 * functionality to initiate a payment checkout and retrieve the status of a payment.
 */
@RestController
@RequestMapping("/payments")
public class PaymentController {

  @Autowired private PaymentService paymentService;

  @Value("${razorpay.api.key}")
  private String apiKey;

  @Value("${razorpay.api.secret}")
  private String apiSecret;

  @Autowired private PaymentRepository paymentRepository;

  /**
   * Processes the checkout of products for a given order ID by initiating a payment process. This
   * method uses the payment details provided and communicates with the payment service to create a
   * payment transaction.
   *
   * @param orderId the unique identifier of the order for which the checkout is being performed
   * @param fullName the full name of the customer provided in the request header
   * @param username the username of the customer provided in the request header
   * @param contactNumber the contact number of the customer provided in the request header
   * @param paymentRequest the payment details including amount and other necessary information
   * @return a ResponseEntity containing the payment response with details such as payment status
   *     and payment link information
   * @throws RazorpayException if there is an issue processing the payment with Razorpay
   */
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

  /**
   * Retrieves the payment status for a given order ID.
   *
   * @param orderId the unique identifier of the order whose payment status is to be fetched
   * @return a ResponseEntity containing the payment status as a string
   */
  @GetMapping("/status/{orderId}")
  public ResponseEntity<String> getPaymentStatus(@PathVariable String orderId) {

    String paymentStatus = this.paymentService.getPaymentStatus(orderId);

    return ResponseEntity.status(HttpStatus.OK).body(paymentStatus);
  }
}
