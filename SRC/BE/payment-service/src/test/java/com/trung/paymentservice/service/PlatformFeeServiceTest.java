package com.trung.paymentservice.service;
import com.trung.paymentservice.repository.PlatformFeeConfigRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class PlatformFeeServiceTest {
    private final PlatformFeeConfigRepository repository = mock(PlatformFeeConfigRepository.class);
    private final PlatformFeeService service = new PlatformFeeService(repository);
    @Test void computesConfiguredFeeAndRoundsDong() {
        assertEquals(new BigDecimal("12500"), service.calculate(new BigDecimal("100000"), new BigDecimal("12.5")));
        assertEquals(new BigDecimal("1"), service.calculate(new BigDecimal("5"), new BigDecimal("10")));
        assertEquals(new BigDecimal("0"), service.calculate(new BigDecimal("100000"), BigDecimal.ZERO));
        assertEquals(new BigDecimal("100000"), service.calculate(new BigDecimal("100000"), new BigDecimal("100")));
    }
    @Test void rejectsInvalidRates() {
        for (String value : new String[]{"-1", "100.01", "20.001"})
            assertThrows(ResponseStatusException.class, () -> service.update(new BigDecimal(value)));
        assertThrows(ResponseStatusException.class, () -> service.update(null));
        verifyNoInteractions(repository);
    }
}
