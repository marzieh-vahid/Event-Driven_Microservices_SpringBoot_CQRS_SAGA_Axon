package com.marvahid.productservice.core.data.lookup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductLookupRepository extends JpaRepository<ProductLookupEntity, String> {
    ProductLookupEntity findProductLookupEntityByProductIdOrTitle(String productId, String title);
}
