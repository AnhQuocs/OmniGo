package com.trung.fooddeliveryservice.exception;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
class PaymentStatusErrorTest {
    @RestController static class Endpoint {
        @GetMapping("/test/{code}") public void error(@PathVariable int code) {
            throw new ResponseStatusException(HttpStatus.valueOf(code), "payment request rejected");
        }
    }
    @Test void mvcPreservesConflictAndTerminalFailureInsteadOf500() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new Endpoint()).setControllerAdvice(new GlobalExceptionHandler()).build();
        for (int code : new int[]{409,422,503}) mvc.perform(get("/test/"+code))
            .andExpect(status().is(code)).andExpect(jsonPath("$.message").value("payment request rejected"));
    }
}
