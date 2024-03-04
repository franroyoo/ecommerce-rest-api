package com.ecommerce.app.ecommercebackend.service;

import com.ecommerce.app.ecommercebackend.api.repository.ProductRepository;
import com.ecommerce.app.ecommercebackend.exception.ProductDoesNotExistException;
import com.ecommerce.app.ecommercebackend.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

import java.util.List;

@Service
public class ProductService {
    private ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getProductList(){
        return productRepository.findAll();
    }

    public Product getProductById(Long id){
        Optional<Product> opProduct = productRepository.findById(id);

        if (opProduct.isPresent()) {
            return opProduct.get();
        } else {
            throw new ProductDoesNotExistException();
        }
    }
}
