package com.marvahid.coremodule.model;

public record User(String firstName, String lastName,
                   String userId, PaymentDetails paymentDetails) {
}
