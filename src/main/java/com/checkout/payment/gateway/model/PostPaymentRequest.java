package com.checkout.payment.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.io.Serializable;

public class PostPaymentRequest implements Serializable {

  //@JsonProperty("card_number_last_four")
  //private int cardNumberLastFour;

  @NotBlank(message = "Card number is required")
  @JsonProperty("card_number")
  @Pattern(regexp = "^\\d{14,19}$", message = "Card number must be between 14-19 numeric characters")
  private String cardNumber;

  @NotNull(message = "Expiry month is required")
  @JsonProperty("expiry_month")
  @Min(value = 1, message = "Expiry month must be between 1 and 12 (January to December)")
  @Max(value = 12, message = "Expiry month must be between 1 and 12 (January to December)")
  private Integer expiryMonth;

  @NotNull(message = "Expiry year is required")
  @JsonProperty("expiry_year")
  private Integer expiryYear;

  @NotBlank(message = "Currency is required")
  @Pattern(regexp = "^(USD|EUR|GBP)$", message = "Currency must be one of USD, EUR, GBP")
  private String currency;

  @NotNull(message = "Amount is required")
  @Positive(message = "Amount must be a positive integer")
  private Integer amount;

  @NotBlank(message = "CVV is required")
  @Pattern(regexp = "^\\d{3,4}$", message = "CVV must be 3 or 4 numeric characters")
  private String cvv;

  public String getCardNumber() {
    return cardNumber;
  }

  /*public void setCardNumberLastFour(int cardNumberLastFour) {
    this.cardNumberLastFour = cardNumberLastFour;
  }*/

  public void setCardNumber(String cardNumber) {
    this.cardNumber = cardNumber;
  }

  public Integer getExpiryMonth() {
    return expiryMonth;
  }

  public void setExpiryMonth(Integer expiryMonth) {
    this.expiryMonth = expiryMonth;
  }

  public Integer getExpiryYear() {
    return expiryYear;
  }

  public void setExpiryYear(Integer expiryYear) {
    this.expiryYear = expiryYear;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public Integer getAmount() {
    return amount;
  }

  public void setAmount(Integer amount) {
    this.amount = amount;
  }

  public String getCvv() {
    return cvv;
  }

  public void setCvv(String cvv) {
    this.cvv = cvv;
  }

  @JsonProperty("expiry_date")
  public String getExpiryDate() {
    return String.format("%d/%d", expiryMonth, expiryYear);
  }

  /*@Override
  public String toString() {
    return "PostPaymentRequest{" +
        "cardNumberLastFour=" + cardNumberLastFour +
        ", expiryMonth=" + expiryMonth +
        ", expiryYear=" + expiryYear +
        ", currency='" + currency + '\'' +
        ", amount=" + amount +
        ", cvv=" + cvv +
        '}';
  }*/
}
