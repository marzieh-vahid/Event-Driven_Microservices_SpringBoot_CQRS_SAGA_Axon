package com.marvahid.OrdersService.saga;

import com.marvahid.OrdersService.command.commands.ApproveOrderCommand;
import com.marvahid.OrdersService.command.commands.RejectOrderCommand;
import com.marvahid.OrdersService.core.events.OrderApprovedEvent;
import com.marvahid.OrdersService.core.events.OrderCreatedEvent;
import com.marvahid.OrdersService.core.events.OrderRejectedEvent;
import com.marvahid.coremodule.commands.CancelProductReservationCommand;
import com.marvahid.coremodule.commands.ProcessPaymentCommand;
import com.marvahid.coremodule.commands.ReserveProductCommand;
import com.marvahid.coremodule.events.PaymentProcessedEvent;
import com.marvahid.coremodule.events.ProductReservationCanceledEvent;
import com.marvahid.coremodule.events.ProductReservedEvent;
import com.marvahid.coremodule.model.User;
import com.marvahid.coremodule.query.FetchUserPaymentDetailsQuery;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Saga
public class OrderSaga {

    @Autowired
    private transient CommandGateway commandGateway;
    @Autowired
    private transient QueryGateway queryGateway;

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderCreatedEvent event) {
        ReserveProductCommand reserveProductCommand = ReserveProductCommand.builder()
                .orderId(event.getOrderId())
                .productId(event.getProductId())
                .quantity(event.getQuantity())
                .userId(event.getUserId()).build();
        commandGateway.send(reserveProductCommand, new CommandCallback<ReserveProductCommand, Object>() {
            @Override
            public void onResult(@Nonnull CommandMessage<? extends ReserveProductCommand> commandMessage, @Nonnull CommandResultMessage<?> commandResultMessage) {
                if (commandResultMessage.isExceptional()) {
                    //start compensating transaction
                }
            }

        });
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReservedEvent productReservedEvent) {
        //process user payment
        FetchUserPaymentDetailsQuery fetchUserPaymentDetailsQuery = new FetchUserPaymentDetailsQuery(
                productReservedEvent.getUserId());
        User userPaymentDetail = null;
        try {
            userPaymentDetail = queryGateway.query(fetchUserPaymentDetailsQuery, ResponseTypes.instanceOf(User.class)).join();
        } catch (Exception e) {
            //start compensating transaction
            cancelProductReservation(productReservedEvent, e.getLocalizedMessage());
            return;
        }
        if (userPaymentDetail == null)
            cancelProductReservation(productReservedEvent, "user payment detail not found");

        ProcessPaymentCommand processPaymentCommand = ProcessPaymentCommand.builder()
                .orderId(productReservedEvent.getOrderId())
                .paymentId(UUID.randomUUID().toString())
                .paymentDetails(userPaymentDetail.paymentDetails())
                .build();
        try {
            commandGateway.sendAndWait(processPaymentCommand, 10, TimeUnit.SECONDS);
        } catch (Exception e) {
            //start compensating transaction
            cancelProductReservation(productReservedEvent, e.getLocalizedMessage());

        }
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(PaymentProcessedEvent paymentProcessedEvent) {
        ApproveOrderCommand approveOrderCommand = new ApproveOrderCommand(paymentProcessedEvent.orderId());
        commandGateway.send(approveOrderCommand);
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderApprovedEvent orderApprovedEvent) {
        SagaLifecycle.end();
    }

    private void cancelProductReservation(ProductReservedEvent productReservedEvent, String reason) {
        CancelProductReservationCommand command = CancelProductReservationCommand.builder()
                .orderId(productReservedEvent.getOrderId())
                .productId(productReservedEvent.getProductId())
                .quantity(productReservedEvent.getQuantity())
                .userId(productReservedEvent.getUserId())
                .reason(reason)
                .build();
        commandGateway.send(command);
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handler(ProductReservationCanceledEvent productReservationCanceledEvent) {
        RejectOrderCommand rejectOrderCommand = new RejectOrderCommand(productReservationCanceledEvent.getOrderId(), productReservationCanceledEvent.getReason());
        commandGateway.send(rejectOrderCommand);
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handler(OrderRejectedEvent orderRejectedEvent) {
      //log
    }
}
