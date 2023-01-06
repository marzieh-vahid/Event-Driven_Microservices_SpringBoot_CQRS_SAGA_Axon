package com.marvahid.OrdersService.query;

import com.marvahid.OrdersService.core.data.OrderEntity;
import com.marvahid.OrdersService.core.data.OrdersRepository;
import com.marvahid.OrdersService.core.model.OrderSummary;
import lombok.RequiredArgsConstructor;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class OrderQueriesHandler {

    private final OrdersRepository ordersRepository;

    @QueryHandler
    public OrderSummary findOrder(FindOrderQuery findOrderQuery) {

        OrderEntity orderEntity = ordersRepository.findOrderEntityByOrderId(findOrderQuery.getOrderId());
        return new OrderSummary(orderEntity.orderId, orderEntity.getOrderStatus());
    }
}
