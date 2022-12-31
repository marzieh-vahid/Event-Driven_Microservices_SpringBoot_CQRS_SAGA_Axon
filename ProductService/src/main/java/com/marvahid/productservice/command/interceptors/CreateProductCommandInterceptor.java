package com.marvahid.productservice.command.interceptors;

import com.marvahid.productservice.command.CreateProductCommand;
import com.marvahid.productservice.core.data.lookup.ProductLookupEntity;
import com.marvahid.productservice.core.data.lookup.ProductLookupRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.BiFunction;

@Component
@RequiredArgsConstructor
public class CreateProductCommandInterceptor implements MessageDispatchInterceptor<CommandMessage<?>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateProductCommandInterceptor.class);
    private final ProductLookupRepository productLookupRepository;

    @Nonnull
    @Override
    public BiFunction<Integer, CommandMessage<?>, CommandMessage<?>> handle(@Nonnull List<? extends CommandMessage<?>> list) {
        return (index, command) -> {

            LOGGER.info("Intercepted command: " + command.getPayloadType());

            if (CreateProductCommand.class.equals(command.getPayloadType())) {
                CreateProductCommand createProductCommand = (CreateProductCommand) command.getPayload();
                ProductLookupEntity productLookupEntity = productLookupRepository.
                        findProductLookupEntityByProductIdOrTitle(createProductCommand.getProductId(),
                                createProductCommand.getTitle());
                if (productLookupEntity != null)
                    throw new IllegalStateException(String.format("product with productId %s or title %s is already exist ",
                            createProductCommand.getProductId(), createProductCommand.getTitle()));
            }
            return command;
        };
    }
}
