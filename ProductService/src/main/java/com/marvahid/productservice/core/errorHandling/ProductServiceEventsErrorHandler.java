package com.marvahid.productservice.core.errorHandling;

import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.EventMessageHandler;
import org.axonframework.eventhandling.ListenerInvocationErrorHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
public class ProductServiceEventsErrorHandler implements ListenerInvocationErrorHandler {
    @Override
    public void onError(@Nonnull Exception e, @Nonnull EventMessage<?> eventMessage,
                        @Nonnull EventMessageHandler eventMessageHandler) throws Exception {
        throw e;//to rollback transaction in a processing group
    }
}
