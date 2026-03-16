package com.checkout.payment.gateway.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

class CommonExceptionHandlerTest {

  private MockMvc mvc;

  @BeforeEach
  void setUp() {
    mvc = MockMvcBuilders
        .standaloneSetup(new TestController())
        .setControllerAdvice(new CommonExceptionHandler())
        .build();
  }

  @Test
  void shouldReturnBadRequestAndMessage_whenEventProcessingExceptionIsThrown() throws Exception {
    mvc.perform(post("/test/event-processing-error"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Invalid ID"))
        .andExpect(jsonPath("$.errors").doesNotExist());
  }

  @Test
  void shouldReturnBadRequestAndValidationErrors_whenMethodArgumentNotValidExceptionIsThrown()
      throws Exception {

    String requestBody = """
        {
          "name": ""
        }
        """;

    mvc.perform(post("/test/method-argument-validation-error")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.errors").isArray())
        .andExpect(jsonPath("$.errors[0]").value("name: Name is required"));
  }

  @Test
  void shouldReturnBadRequestAndValidationErrors_whenValidationExceptionIsThrown()
      throws Exception {

    mvc.perform(post("/test/custom-validation-error"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.errors").isArray())
        .andExpect(jsonPath("$.errors[0]").value("Card has expired"))
        .andExpect(jsonPath("$.errors[1]").value("Currency must be one of USD, EUR, GBP"));
  }

  @RestController
  static class TestController {

    @PostMapping("/test/event-processing-error")
    public void throwEventProcessingException() {
      throw new EventProcessingException("Invalid ID");
    }

    @PostMapping("/test/method-argument-validation-error")
    public void throwMethodArgumentValidationError(
        @Valid @RequestBody TestValidationRequest request) {
      // validation happens before method body
    }

    @PostMapping("/test/custom-validation-error")
    public void throwValidationException() {
      throw new ValidationException(List.of(
          "Card has expired",
          "Currency must be one of USD, EUR, GBP"
      ));
    }
  }

  static class TestValidationRequest {

    @NotBlank(message = "Name is required")
    private String name;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }
}