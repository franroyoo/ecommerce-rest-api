package com.ecommerce.app.ecommercebackend.service;

import com.ecommerce.app.ecommercebackend.api.dto.OrderBody;
import com.ecommerce.app.ecommercebackend.api.dto.ProductBody;
import com.ecommerce.app.ecommercebackend.api.repository.InventoryRepository;
import com.ecommerce.app.ecommercebackend.api.repository.ProductRepository;
import com.ecommerce.app.ecommercebackend.api.repository.WebOrderRepository;
import com.ecommerce.app.ecommercebackend.exception.OrderDoesNotExistException;
import com.ecommerce.app.ecommercebackend.exception.OutOfStockException;
import com.ecommerce.app.ecommercebackend.exception.ProductDoesNotExistException;
import com.ecommerce.app.ecommercebackend.model.LocalUser;
import com.ecommerce.app.ecommercebackend.model.Product;
import com.ecommerce.app.ecommercebackend.model.WebOrder;
import com.ecommerce.app.ecommercebackend.model.WebOrderQuantities;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class WebOrderService {
    private WebOrderRepository webOrderRepository;
    private ProductRepository productRepository;
    private InventoryRepository inventoryRepository;

    @Autowired
    public WebOrderService(WebOrderRepository webOrderRepository, ProductRepository productRepository, InventoryRepository inventoryRepository) {
        this.webOrderRepository = webOrderRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public List<WebOrder> getOrderList(LocalUser user){
        return webOrderRepository.findByLocalUser(user);
    }

    @Transactional(readOnly = false)
    public WebOrder createOrder(OrderBody orderBody, LocalUser user) throws OutOfStockException, ProductDoesNotExistException {

        // TODO: Refactor logic to add Address through JSON (use AddressService to create endpoint for adding address too)

        List<WebOrderQuantities> orderQuantities = new ArrayList<>();

        WebOrder order = new WebOrder();

        for (ProductBody productDTO : orderBody.getProducts()){

            Optional<Product> optionalProduct = productRepository.findById(productDTO.getProductId());

            if (optionalProduct.isPresent()){

                Product product = optionalProduct.get();

                Long inventoryQuantityForProduct = inventoryRepository.findQuantityByProductId(product.getId());

                if (productDTO.getQuantity() > inventoryQuantityForProduct){
                    log.warn("Out of stock for product {}", product.getId());
                    throw new OutOfStockException();
                }else{
                    WebOrderQuantities webOrderQuantities = WebOrderQuantities.builder()
                            .product(product)
                            .quantity(productDTO.getQuantity())
                            .webOrder(order)
                            .build();

                    orderQuantities.add(webOrderQuantities);
                    inventoryRepository.updateQuantityByProductId(product.getId(), inventoryQuantityForProduct - productDTO.getQuantity());
                }

            }else {
                throw new ProductDoesNotExistException();
            }

        }

        order.setLocalUser(user);
        order.setQuantities(orderQuantities);
        order.setAddress(user.getAddresses().get(0));

        WebOrder savedOrder = webOrderRepository.save(order);

        return savedOrder;
    }

    @Transactional
    public void deleteOrder(Long id){
        webOrderRepository.findById(id).orElseThrow(OrderDoesNotExistException::new);
        webOrderRepository.deleteById(id);
    }
}
