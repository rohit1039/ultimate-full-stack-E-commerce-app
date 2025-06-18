package com.ecommerce.payment.payload.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusUpdate {
  private String orderId;
  private String paymentStatus;
  private String paymentMethod;
}
