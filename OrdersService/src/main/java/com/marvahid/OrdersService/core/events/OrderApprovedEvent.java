package com.marvahid.OrdersService.core.events;

import com.marvahid.OrdersService.core.model.OrderStatus;
import lombok.Value;

@Value
public class OrderApprovedEvent {
    private final String orderId;
    private final OrderStatus orderStatus=OrderStatus.APPROVED;
}