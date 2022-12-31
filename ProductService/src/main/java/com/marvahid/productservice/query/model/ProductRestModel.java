package com.marvahid.productservice.query.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRestModel {
    private String productId;
    private Integer quantity;
    private String title;
    private BigDecimal price;
}
