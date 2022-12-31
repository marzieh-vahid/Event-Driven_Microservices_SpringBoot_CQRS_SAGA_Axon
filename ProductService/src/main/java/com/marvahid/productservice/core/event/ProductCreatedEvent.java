package com.marvahid.productservice.core.event;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductCreatedEvent {

    private String productId;
    private Integer quantity;
    private String title;
    private BigDecimal price;
}
