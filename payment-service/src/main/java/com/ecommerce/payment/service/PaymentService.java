package com.ecommerce.payment.service;

import com.ecommerce.payment.payload.request.PaymentRequest;
import com.ecommerce.payment.payload.response.PaymentResponse;
import com.razorpay.RazorpayException;

public interface PaymentService {
  public PaymentResponse checkoutProducts(
      String orderId,
      String fullName,
      String contactNumber,
      String username,
      PaymentRequest paymentRequest)
      throws RazorpayException;
}
