package com.marvahid.productservice.core.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, String> {

    ProductEntity findProductEntityByProductId(String productId);

    ProductEntity findProductEntityByProductIdOrTitle(String productId, String title);
}
