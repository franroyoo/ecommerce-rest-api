package com.ecommerce.app.ecommercebackend.api.repository;

import com.ecommerce.app.ecommercebackend.api.dto.ProductBody;
import com.ecommerce.app.ecommercebackend.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    @Query("SELECT i.quantity FROM Inventory i WHERE i.product.id = :productId")
    Long findQuantityByProductId(@Param("productId") Long productId);

    @Modifying
    @Transactional
    @Query("UPDATE Inventory i SET i.quantity = :quantity WHERE i.product.id = :productId")
    void updateQuantityByProductId(@Param("productId") Long productId, @Param("quantity") Long id);

    List<Inventory> findByProductIdInOrderByProductId(List<Long> actualProductsId);
}
