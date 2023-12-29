package de.bsi.ai4coding.product.model;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Data
@Entity
public class Product {
    @Id
    @GeneratedValue
    private UUID id;

    private String name;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Embedded
    private Price price;

    @ElementCollection
    private Map<String, String> attributes;

    public enum Category {
        TOY, TOOL
    }
}
