package com.ecommerce.payment.payload.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.io.Serializable;
import java.time.LocalDateTime;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentRequest implements Serializable {
  private Long totalAmount;
  @JsonIgnore
  private PaymentStatus paymentStatus;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy hh:mm:ss a")
  @JsonIgnore
  private LocalDateTime paymentDate;

  public PaymentRequest() {}

  public PaymentRequest(
      Long totalAmount,
      PaymentStatus paymentStatus,
      LocalDateTime paymentDate) {

    this.totalAmount = totalAmount;
    this.paymentStatus = paymentStatus;
    this.paymentDate = paymentDate;
  }

  public Long getTotalAmount() {

    return totalAmount;
  }

  public void setTotalAmount(Long totalAmount) {

    this.totalAmount = totalAmount;
  }

  public PaymentStatus getPaymentStatus() {

    return paymentStatus;
  }

  public void setPaymentStatus(PaymentStatus paymentStatus) {

    this.paymentStatus = paymentStatus;
  }

  public LocalDateTime getPaymentDate() {

    return paymentDate;
  }

  public void setPaymentDate(LocalDateTime paymentDate) {

    this.paymentDate = paymentDate;
  }

  @Override
  public String toString() {

    return "PaymentRequest{"
        + "totalAmount="
        + totalAmount
        + ", paymentStatus="
        + paymentStatus
        + ", paymentDate="
        + paymentDate
        + '}';
  }
}
