package com.marvahid.coremodule.events;

public record PaymentProcessedEvent(String orderId, String paymentId) {
}
