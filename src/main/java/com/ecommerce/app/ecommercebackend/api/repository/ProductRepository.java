package com.ecommerce.app.ecommercebackend.api.repository;

import com.ecommerce.app.ecommercebackend.model.LocalUser;
import com.ecommerce.app.ecommercebackend.model.Product;
import com.ecommerce.app.ecommercebackend.model.WebOrder;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ProductRepository extends ListCrudRepository<Product, Long> {
    Optional<Product> findById(Long id);

    List<Product> findByIdInOrderById(Set<Long> ids);
}
