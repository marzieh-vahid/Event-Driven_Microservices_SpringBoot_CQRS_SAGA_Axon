package com.marvahid.coremodule.model;


public record PaymentDetails(String name, String cardNumber
        , int validUntilMonth, int validUntilYear, String cvv) {
}
