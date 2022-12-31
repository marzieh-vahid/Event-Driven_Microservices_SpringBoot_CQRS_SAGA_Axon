package com.marvahid.coremodule.events;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductReservedEvent {
    private String productId;
    private int quantity;
    private String orderId;
    private String userId;
}
