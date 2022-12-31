package com.marvahid.productservice.command.controller;

import com.marvahid.productservice.core.model.CreateProductRestModel;
import com.marvahid.productservice.command.CreateProductCommand;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/products")
public class ProductCommandController {

    private final CommandGateway commandGateway;

    @PostMapping
    public String createProduct(@Valid @RequestBody CreateProductRestModel createProductRestModel) {
        CreateProductCommand createProductCommand = CreateProductCommand.builder()
                .productId(UUID.randomUUID().toString()).
                price(createProductRestModel.getPrice()).
                title(createProductRestModel.getTitle()).
                quantity(createProductRestModel.getQuantity()).build();
        String returnValue;
        returnValue = commandGateway.sendAndWait(createProductCommand);
        return returnValue;
    }


}
