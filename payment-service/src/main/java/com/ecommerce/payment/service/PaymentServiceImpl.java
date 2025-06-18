package com.ecommerce.payment.service;

import com.ecommerce.payment.dao.PaymentRepository;
import com.ecommerce.payment.model.Payment;
import com.ecommerce.payment.payload.request.PaymentRequest;
import com.ecommerce.payment.payload.request.PaymentStatus;
import com.ecommerce.payment.payload.response.PaymentResponse;
import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements PaymentService {

  @Value("${razorpay.api.key}")
  private String apiKey;

  @Value("${razorpay.api.secret}")
  private String apiSecret;

  @Autowired private PaymentRepository paymentRepository;

  public PaymentResponse checkoutProducts(
      String orderId,
      String fullName,
      String contactNumber,
      String username,
      PaymentRequest paymentRequest)
      throws RazorpayException {

    RazorpayClient razorpayClient = new RazorpayClient(apiKey, apiSecret);

    JSONObject orderRequest = new JSONObject();
    orderRequest.put("amount", paymentRequest.getTotalAmount() * 100); // Razorpay expects paise
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
        "callback_url", "https://yourdomain.com/payment/status"); // fallback (optional)

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
}
