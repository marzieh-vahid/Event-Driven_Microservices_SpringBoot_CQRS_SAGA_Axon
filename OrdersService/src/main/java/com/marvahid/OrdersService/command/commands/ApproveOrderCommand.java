package com.marvahid.OrdersService.command.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@AllArgsConstructor
@Data
public class ApproveOrderCommand {
    @TargetAggregateIdentifier
    private final String orderId;
}
