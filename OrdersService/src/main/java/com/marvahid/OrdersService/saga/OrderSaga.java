package com.marvahid.OrdersService.saga;

import com.marvahid.OrdersService.command.commands.ApproveOrderCommand;
import com.marvahid.OrdersService.command.commands.RejectOrderCommand;
import com.marvahid.OrdersService.core.events.OrderApprovedEvent;
import com.marvahid.OrdersService.core.events.OrderCreatedEvent;
import com.marvahid.OrdersService.core.events.OrderRejectedEvent;
import com.marvahid.OrdersService.core.model.OrderSummary;
import com.marvahid.OrdersService.query.FindOrderQuery;
import com.marvahid.coremodule.commands.CancelProductReservationCommand;
import com.marvahid.coremodule.commands.ProcessPaymentCommand;
import com.marvahid.coremodule.commands.ReserveProductCommand;
import com.marvahid.coremodule.events.PaymentProcessedEvent;
import com.marvahid.coremodule.events.ProductReservationCanceledEvent;
import com.marvahid.coremodule.events.ProductReservedEvent;
import com.marvahid.coremodule.model.User;
import com.marvahid.coremodule.query.FetchUserPaymentDetailsQuery;
import org.apache.commons.lang.StringUtils;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.annotation.DeadlineHandler;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.axonframework.spring.stereotype.Saga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Saga
public class OrderSaga {

    public static final String PAYMENT_PROCESSING_DEADLINE = "payment-processing-deadline";
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderSaga.class);

    @Autowired
    private transient CommandGateway commandGateway;
    @Autowired
    private transient QueryGateway queryGateway;
    @Autowired
    private transient DeadlineManager deadlineManager;

    @Autowired
    private transient QueryUpdateEmitter queryUpdateEmitter;

    private String scheduleId;

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
            public void onResult(@Nonnull CommandMessage<? extends ReserveProductCommand> commandMessage,
                                 @Nonnull CommandResultMessage<?> commandResultMessage) {
                if (commandResultMessage.isExceptional()) {
                    //start compensating transaction
                    RejectOrderCommand rejectOrderCommand = new RejectOrderCommand(event.getOrderId(),
                            commandResultMessage.exceptionResult().getMessage());
                    commandGateway.send(rejectOrderCommand);
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
            userPaymentDetail = queryGateway.query(fetchUserPaymentDetailsQuery,
                    ResponseTypes.instanceOf(User.class)).join();
        } catch (Exception e) {
            //start compensating transaction
            cancelProductReservation(productReservedEvent, e.getLocalizedMessage());
            return;
        }
        if (userPaymentDetail == null)
            cancelProductReservation(productReservedEvent, "user payment detail not found");

        scheduleId = deadlineManager.schedule(Duration.of(120, ChronoUnit.MINUTES),
                PAYMENT_PROCESSING_DEADLINE, productReservedEvent);

        ProcessPaymentCommand processPaymentCommand = ProcessPaymentCommand.builder()
                .orderId(productReservedEvent.getOrderId())
                .paymentId(UUID.randomUUID().toString())
                .paymentDetails(userPaymentDetail.paymentDetails())
                .build();
        try {
            commandGateway.sendAndWait(processPaymentCommand);
        } catch (Exception e) {
            //start compensating transaction
            cancelProductReservation(productReservedEvent, e.getLocalizedMessage());

        }
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(PaymentProcessedEvent paymentProcessedEvent) {

        cancelProductDeadline();

        ApproveOrderCommand approveOrderCommand = new ApproveOrderCommand(paymentProcessedEvent.orderId());
        commandGateway.send(approveOrderCommand);
    }

    private void cancelProductDeadline() {
        if (!StringUtils.isEmpty(scheduleId)) {
            deadlineManager.cancelAll(PAYMENT_PROCESSING_DEADLINE);
            scheduleId = null;
        }
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderApprovedEvent orderApprovedEvent) {
        //SagaLifecycle.end();
        queryUpdateEmitter.emit(FindOrderQuery.class, query -> true,
                new OrderSummary(orderApprovedEvent.getOrderId(), orderApprovedEvent.getOrderStatus()));
    }

    private void cancelProductReservation(ProductReservedEvent productReservedEvent, String reason) {

        cancelProductDeadline();

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
        RejectOrderCommand rejectOrderCommand = new RejectOrderCommand(productReservationCanceledEvent.getOrderId(),
                productReservationCanceledEvent.getReason());
        commandGateway.send(rejectOrderCommand);
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handler(OrderRejectedEvent orderRejectedEvent) {
        LOGGER.info("Order with id " + orderRejectedEvent.getOrderId() + "is rejected");
        queryUpdateEmitter.emit(FindOrderQuery.class, query -> true,
                new OrderSummary(orderRejectedEvent.getOrderId(), orderRejectedEvent.getOrderStatus()));
    }

    @DeadlineHandler(deadlineName = PAYMENT_PROCESSING_DEADLINE)
    public void handleProductDeadline(ProductReservedEvent productReservedEvent) {
        LOGGER.info("Payment processing deadline toke place. sending a compensating command to " +
                "cancel the product reservation");
        cancelProductReservation(productReservedEvent, "Payment timeout");
    }
}
