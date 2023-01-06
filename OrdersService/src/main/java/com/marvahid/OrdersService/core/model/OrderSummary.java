package com.marvahid.OrdersService.core.model;

import lombok.Data;

@Data
public class OrderSummary {
    private final String OrderId;
    private final OrderStatus orderStatus;
}
