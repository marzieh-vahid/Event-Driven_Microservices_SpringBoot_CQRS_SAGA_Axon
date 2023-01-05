/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.marvahid.UsersService.query;

import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import com.marvahid.coremodule.model.PaymentDetails;
import com.marvahid.coremodule.model.User;
import com.marvahid.coremodule.query.FetchUserPaymentDetailsQuery;
 

@Component
public class UserEventsHandler {

    @QueryHandler
    public User findUserPaymentDetails(FetchUserPaymentDetailsQuery query) {
        
        PaymentDetails paymentDetails = new PaymentDetails("123Card","123"
        ,12,2030,"Marzieh Vahid");

        return new User("Marzieh","Vahid",query.getUserId(),paymentDetails);
    }
    
    
}
