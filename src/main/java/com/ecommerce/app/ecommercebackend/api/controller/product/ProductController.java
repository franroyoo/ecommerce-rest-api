package com.ecommerce.app.ecommercebackend.api.controller.product;

import com.ecommerce.app.ecommercebackend.exception.ApiResponseError;
import com.ecommerce.app.ecommercebackend.model.Product;
import com.ecommerce.app.ecommercebackend.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {
    private ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "Get product list")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Get product list successfully",
                            content = @Content(mediaType = "application/json")
                    ),
            }
    )
    @GetMapping
    public List<Product> getProductList(){
        return productService.getProductList();
    }

    @Operation(summary = "Get product by its id")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Product found successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Product does not exist",
                            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseError.class))}
                    ),

                    @ApiResponse(
                            responseCode = "403",
                            description = "User not authenticated",
                            content = @Content
                    )
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id){
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

}
