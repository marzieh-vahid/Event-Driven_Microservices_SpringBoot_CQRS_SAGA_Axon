package com.marvahid.coremodule.commands;

import lombok.Builder;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@Builder
public class ReserveProductCommand {

    @TargetAggregateIdentifier
    private String productId;
    private int quantity;
    private String orderId;
    private String userId;
}
