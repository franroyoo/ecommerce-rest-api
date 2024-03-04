package com.ecommerce.app.ecommercebackend.api.repository;

import com.ecommerce.app.ecommercebackend.model.LocalUser;
import com.ecommerce.app.ecommercebackend.model.WebOrder;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WebOrderRepository extends ListCrudRepository<WebOrder, Long> {
    List<WebOrder> findByLocalUser(LocalUser user);

}
