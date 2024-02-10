package com.ecommerce.app.ecommercebackend.service;

import com.ecommerce.app.ecommercebackend.api.repository.WebOrderRepository;
import com.ecommerce.app.ecommercebackend.model.LocalUser;
import com.ecommerce.app.ecommercebackend.model.WebOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WebOrderService {
    private WebOrderRepository webOrderRepository;

    @Autowired
    public WebOrderService(WebOrderRepository webOrderRepository) {
        this.webOrderRepository = webOrderRepository;
    }

    public List<WebOrder> getOrderList(LocalUser user){
        return webOrderRepository.findByLocalUser(user);
    }

}
