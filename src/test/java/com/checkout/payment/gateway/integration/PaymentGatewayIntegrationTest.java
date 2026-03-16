package com.checkout.payment.gateway.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentGatewayIntegrationTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void paymentShouldBeAuthorizedWhenCardEndsWithOddDigit() throws Exception {

    String request = """
        {
          "card_number": "2222405343248877",
          "expiry_month": 12,
          "expiry_year": 2099,
          "currency": "GBP",
          "amount": 100,
          "cvv": "123"
        }
        """;

    mvc.perform(post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("Authorized"))
        .andExpect(jsonPath("$.cardNumberLastFour").value(8877))
        .andExpect(jsonPath("$.currency").value("GBP"))
        .andExpect(jsonPath("$.amount").value(100));
  }

  @Test
  void paymentShouldBeDeclinedWhenCardEndsWithEvenDigit() throws Exception {

    String request = """
        {
          "card_number": "2222405343248878",
          "expiry_month": 12,
          "expiry_year": 2099,
          "currency": "GBP",
          "amount": 100,
          "cvv": "123"
        }
        """;

    mvc.perform(post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("Declined"));
  }

  @Test
  void paymentShouldBeRejectedWhenBankReturns503() throws Exception {

    String request = """
        {
          "card_number": "2222405343248870",
          "expiry_month": 12,
          "expiry_year": 2099,
          "currency": "GBP",
          "amount": 100,
          "cvv": "123"
        }
        """;

    mvc.perform(post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("Rejected"));
  }

  @Test
  void paymentShouldBeStoredAndRetrievable() throws Exception {

    String request = """
        {
          "card_number": "2222405343248877",
          "expiry_month": 12,
          "expiry_year": 2099,
          "currency": "GBP",
          "amount": 100,
          "cvv": "123"
        }
        """;

    MvcResult result = mvc.perform(post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isOk())
        .andReturn();

    JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
    String id = response.get("id").asText();

    mvc.perform(get("/payment/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id))
        .andExpect(jsonPath("$.status").value("Authorized"))
        .andExpect(jsonPath("$.cardNumberLastFour").value(8877));
  }

  @Test
  void invalidPaymentRequestShouldReturnBadRequest() throws Exception {

    String invalidRequest = """
        {
          "card_number": "123",
          "expiry_month": 99,
          "expiry_year": 2000,
          "currency": "AAA",
          "amount": -10,
          "cvv": "1"
        }
        """;

    mvc.perform(post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidRequest))
        .andExpect(status().isBadRequest());
  }
}