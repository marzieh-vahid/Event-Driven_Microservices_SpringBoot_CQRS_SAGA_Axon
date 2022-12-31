package com.marvahid.productservice.core.data.lookup;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "productLookup")
public class ProductLookupEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 7476101124471387035L;

    @Id
    @Column(unique = true)
    private String productId;

    @Column(unique = true)
    private String title;

}
