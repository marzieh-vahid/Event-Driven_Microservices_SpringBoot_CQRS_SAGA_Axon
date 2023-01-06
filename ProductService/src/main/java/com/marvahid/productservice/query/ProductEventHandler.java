package com.marvahid.productservice.query;

import com.marvahid.coremodule.events.ProductReservationCanceledEvent;
import com.marvahid.coremodule.events.ProductReservedEvent;
import com.marvahid.productservice.core.data.ProductEntity;
import com.marvahid.productservice.core.data.ProductRepository;
import com.marvahid.productservice.core.event.ProductCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
import org.axonframework.messaging.interceptors.ExceptionHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Slf4j
@ProcessingGroup("product-group")
@RequiredArgsConstructor
@Component
public class ProductEventHandler {

    private final ProductRepository productRepository;

    @EventHandler
    public void on(ProductCreatedEvent productCreatedEvent) {
        ProductEntity productEntity = new ProductEntity();
        BeanUtils.copyProperties(productCreatedEvent, productEntity);
        productRepository.save(productEntity);
    }

    @EventHandler
    public void on(ProductReservedEvent productReservedEvent) {
        ProductEntity productEntity = productRepository.findProductEntityByProductId(productReservedEvent.getProductId());
        productEntity.setQuantity(productEntity.getQuantity() - productReservedEvent.getQuantity());
        productRepository.save(productEntity);
    }

    @ExceptionHandler(resultType = Exception.class)
    public void exceptionHandler(Exception e) {
        log.error(e.getLocalizedMessage());
    }

    @EventHandler
    public void on(ProductReservationCanceledEvent productReservationCanceledEvent) {
        ProductEntity storedProductEntity = productRepository.findProductEntityByProductId(productReservationCanceledEvent.getProductId());
        int newQuantity = storedProductEntity.getQuantity() + productReservationCanceledEvent.getQuantity();
        storedProductEntity.setQuantity(newQuantity);
        productRepository.save(storedProductEntity);
    }

    @ResetHandler
    public void reset() {
        productRepository.deleteAll();
    }
}
