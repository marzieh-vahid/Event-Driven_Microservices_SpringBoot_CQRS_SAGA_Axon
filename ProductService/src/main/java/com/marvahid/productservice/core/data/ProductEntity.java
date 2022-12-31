package com.marvahid.productservice.core.data;


import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Entity(name = "products")
public class ProductEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -6662858440090872770L;

    @Id
    @Column(unique = true)
    private String productId;
    private Integer quantity;
    private String title;
    private BigDecimal price;
}
