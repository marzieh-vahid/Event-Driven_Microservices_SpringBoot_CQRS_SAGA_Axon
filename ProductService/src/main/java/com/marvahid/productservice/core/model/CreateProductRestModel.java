package com.marvahid.productservice.core.model;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
public class CreateProductRestModel {

    @NotBlank(message = "title cannot be empty")
    private String title;

    @Min(value = 1, message = "price cannot be lower than 1")
    private BigDecimal price;

    @Min(value = 1, message = "quantity cannot be lower than 1")
    private Integer quantity;

}
