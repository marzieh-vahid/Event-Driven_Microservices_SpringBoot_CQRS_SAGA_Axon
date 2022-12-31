package com.marvahid.OrdersService.command.commands;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record RejectOrderCommand(@TargetAggregateIdentifier String orderId, String reason) {
}
