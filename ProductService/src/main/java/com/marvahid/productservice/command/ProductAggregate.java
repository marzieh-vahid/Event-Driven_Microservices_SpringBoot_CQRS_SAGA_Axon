package com.marvahid.productservice.command;

import com.marvahid.coremodule.commands.CancelProductReservationCommand;
import com.marvahid.coremodule.commands.ReserveProductCommand;
import com.marvahid.coremodule.events.ProductReservationCanceledEvent;
import com.marvahid.coremodule.events.ProductReservedEvent;
import com.marvahid.productservice.core.event.ProductCreatedEvent;
import org.apache.commons.lang.StringUtils;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;

@Aggregate
public class ProductAggregate {

    @AggregateIdentifier
    private String productId;
    private Integer quantity;
    private String title;
    private BigDecimal price;

    public ProductAggregate() {
    }

    @CommandHandler
    public ProductAggregate(CreateProductCommand productCommand) {
        //validate Create Product Command
        if (productCommand.getPrice().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("product price cannot be equal or less than zero");
        if (StringUtils.isEmpty(productCommand.getTitle()))
            throw new IllegalArgumentException("product title cannot be empty");
        ProductCreatedEvent productCreatedEvent = new ProductCreatedEvent();
        BeanUtils.copyProperties(productCommand, productCreatedEvent);
        AggregateLifecycle.apply(productCreatedEvent);
    }

    @CommandHandler
    public void handler(ReserveProductCommand reserveProductCommand) {
        ProductReservedEvent productReservedEvent = ProductReservedEvent.builder()
                .productId(reserveProductCommand.getProductId())
                .quantity(reserveProductCommand.getQuantity())
                .orderId(reserveProductCommand.getOrderId())
                .userId(reserveProductCommand.getUserId())
                .build();
        AggregateLifecycle.apply(productReservedEvent);
    }

    @EventSourcingHandler
    public void on(ProductCreatedEvent productCreatedEvent) {
        this.price = productCreatedEvent.getPrice();
        this.productId = productCreatedEvent.getProductId();
        this.quantity = productCreatedEvent.getQuantity();
        this.title = productCreatedEvent.getTitle();
    }

    @EventSourcingHandler
    public void on(ProductReservedEvent productReservedEvent) {
        this.quantity -= productReservedEvent.getQuantity();
    }

    @CommandHandler
    public void handler(CancelProductReservationCommand command) {
        ProductReservationCanceledEvent productReservationCanceledEvent = ProductReservationCanceledEvent.builder()
                .productId(command.getProductId())
                .orderId(command.getOrderId())
                .userId(command.getUserId())
                .quantity(command.getQuantity())
                .reason(command.getReason())
                .build();
        AggregateLifecycle.apply(productReservationCanceledEvent);
    }

    @EventSourcingHandler
    public void on(ProductReservationCanceledEvent productReservationCanceledEvent) {
        this.quantity += productReservationCanceledEvent.getQuantity();
    }
}
