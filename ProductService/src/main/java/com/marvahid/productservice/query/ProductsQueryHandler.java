package com.marvahid.productservice.query;

import com.marvahid.productservice.core.data.ProductEntity;
import com.marvahid.productservice.core.data.ProductRepository;
import com.marvahid.productservice.query.model.ProductRestModel;
import lombok.RequiredArgsConstructor;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class ProductsQueryHandler {
    private final ProductRepository productRepository;

    @QueryHandler
    public List<ProductRestModel> findProducts(FindProductsQuery query) {
        List<ProductRestModel> productRestModels = new ArrayList<>();
        List<ProductEntity> storedProducts = productRepository.findAll();
        for (ProductEntity entity : storedProducts) {
            ProductRestModel model = new ProductRestModel();
            BeanUtils.copyProperties(entity, model);
            productRestModels.add(model);
        }
        return productRestModels;
    }
}
