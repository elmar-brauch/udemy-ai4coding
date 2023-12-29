package de.bsi.ai4coding.product.model;

import java.math.BigDecimal;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Embeddable
@Data
class Price {
    @Enumerated(EnumType.STRING)
    private Currency currency;

    private BigDecimal dutyFreeAmount;
    private BigDecimal taxIncludedAmount;
    private BigDecimal taxOnlyAmount;
    private int taxRate;

    public enum Currency {
        EURO, DOLLAR
    }
}
