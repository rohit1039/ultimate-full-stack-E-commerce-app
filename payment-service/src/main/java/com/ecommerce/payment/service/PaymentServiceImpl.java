package com.ecommerce.payment.service;

import com.ecommerce.payment.dao.PaymentRepository;
import com.ecommerce.payment.model.Payment;
import com.ecommerce.payment.payload.request.PaymentRequest;
import com.ecommerce.payment.payload.request.PaymentStatus;
import com.ecommerce.payment.payload.response.PaymentResponse;
import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import java.util.Optional;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service implementation for managing payment operations, including creating payment links and
 * retrieving payment statuses.
 */
@Service
public class PaymentServiceImpl implements PaymentService {

  @Value("${razorpay.api.key}")
  private String apiKey;

  @Value("${razorpay.api.secret}")
  private String apiSecret;

  @Autowired private PaymentRepository paymentRepository;

  /**
   * Creates a payment link using Razorpay and initializes payment details for the specified order.
   *
   * @param orderId the unique identifier for the order
   * @param fullName the full name of the customer
   * @param contactNumber the contact number of the customer
   * @param username the email/username of the customer
   * @param paymentRequest the details of the payment, including the total amount to be paid
   * @return a {@link PaymentResponse} containing the status of the payment, the payment link ID,
   *     and the payment link URL
   * @throws RazorpayException if an error occurs when interacting with Razorpay
   */
  public PaymentResponse checkoutProducts(
      String orderId,
      String fullName,
      String contactNumber,
      String username,
      PaymentRequest paymentRequest)
      throws RazorpayException {

    RazorpayClient razorpayClient = new RazorpayClient(apiKey, apiSecret);

    JSONObject orderRequest = new JSONObject();
    orderRequest.put("amount", paymentRequest.getTotalAmount() * 100);
    orderRequest.put("currency", "INR");

    JSONObject customer = new JSONObject();
    customer.put("name", fullName);
    customer.put("email", username);
    customer.put("contact", contactNumber);
    orderRequest.put("customer", customer);

    JSONObject notify = new JSONObject();
    notify.put("sms", true);
    notify.put("email", true);
    orderRequest.put("notify", notify);

    orderRequest.put(
        "callback_url",
        "https://e424-2401-4900-7012-ffbf-adb8-8a9b-9f96-7c9f.ngrok-free.app/payments/status"); // fallback (optional)

    PaymentLink paymentLink = razorpayClient.paymentLink.create(orderRequest);

    Payment payment = new Payment();
    payment.setOrderId(orderId);
    payment.setPaymentId(paymentLink.get("id"));
    payment.setTotalAmount(paymentRequest.getTotalAmount());
    payment.setPaymentStatus(PaymentStatus.PENDING); // status will be updated later via webhook

    paymentRepository.save(payment);

    return new PaymentResponse(
        PaymentStatus.PENDING.name(), paymentLink.get("id"), paymentLink.get("short_url"));
  }

  /**
   * Retrieves the payment status for a given order ID.
   *
   * @param orderId the unique identifier of the order for which the payment status is to be
   *     retrieved
   * @return a string representing the payment status of the specified order, such as "PENDING",
   *     "SUCCESS", or "FAILED"
   * @throws RuntimeException if no payment record is found corresponding to the given order ID
   */
  @Override
  public String getPaymentStatus(String orderId) {

    Payment payment = this.paymentRepository.findByOrderId(orderId);
    payment =
        Optional.ofNullable(payment)
            .orElseThrow(() -> new RuntimeException("Payment doesn't exist with orderId: " + orderId));

    return payment.getPaymentStatus().name();
  }
}
