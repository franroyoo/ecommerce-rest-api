package com.ecommerce.app.ecommercebackend.service;

import com.ecommerce.app.ecommercebackend.api.dto.OrderBody;
import com.ecommerce.app.ecommercebackend.api.dto.ProductBody;
import com.ecommerce.app.ecommercebackend.api.dto.invoice.InvoiceJsonBody;
import com.ecommerce.app.ecommercebackend.api.dto.invoice.ItemBody;
import com.ecommerce.app.ecommercebackend.api.repository.AddressRepository;
import com.ecommerce.app.ecommercebackend.api.repository.InventoryRepository;
import com.ecommerce.app.ecommercebackend.api.repository.ProductRepository;
import com.ecommerce.app.ecommercebackend.api.repository.WebOrderRepository;
import com.ecommerce.app.ecommercebackend.exception.*;
import com.ecommerce.app.ecommercebackend.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WebOrderService {
    private WebOrderRepository webOrderRepository;
    private ProductRepository productRepository;
    private InventoryRepository inventoryRepository;
    private AddressRepository addressRepository;
    private InvoiceService invoiceService;

    @Value("${email.developer}")
    private String developerEmail;

    @Autowired
    public WebOrderService(WebOrderRepository webOrderRepository, ProductRepository productRepository, InventoryRepository inventoryRepository, AddressRepository addressRepository, InvoiceService invoiceService) {
        this.webOrderRepository = webOrderRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.addressRepository = addressRepository;
        this.invoiceService = invoiceService;
    }

    public List<WebOrder> getOrderList(LocalUser user){
        return webOrderRepository.findByLocalUser(user);
    }

    @Transactional(readOnly = false)
    public byte[] createOrder(OrderBody orderBody, LocalUser user){

        List<WebOrderQuantities> orderQuantities = new ArrayList<>();

        WebOrder order = new WebOrder();

        Optional<Address> opAddress = addressRepository.findByAddressLine1(orderBody.getAddress_line_1());
        Address address = opAddress.orElseThrow(() -> new ApiResponseFailureException(HttpStatus.BAD_REQUEST, "Address not found"));

        for (ProductBody productDTO : orderBody.getProducts()){

            Optional<Product> optionalProduct = productRepository.findById(productDTO.getProductId());

            if (optionalProduct.isPresent()){

                Product product = optionalProduct.get();

                Long inventoryQuantityForProduct = inventoryRepository.findQuantityByProductId(product.getId());

                if (productDTO.getQuantity() > inventoryQuantityForProduct){
                    log.warn("Out of stock for product {}", product.getId());
                    throw new ApiResponseFailureException(HttpStatus.CONFLICT, "Out of Stock for product " + product.getId());
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
                throw new ApiResponseFailureException(HttpStatus.BAD_REQUEST, "Product not found");
            }

        }

        order.setLocalUser(user);
        order.setQuantities(orderQuantities);
        order.setAddress(address);

        webOrderRepository.save(order);

        InvoiceJsonBody invoiceJsonBody = InvoiceJsonBody.builder()
                .to(user.getFirstName() + " " + user.getLastName())
                .notes("For any questions regarding your order, please contact me at " + developerEmail)
                .items(convertToItemBodyList(orderQuantities))
                .number(order.getId())
                .ship_to(address.getAddressLine1())
                .build();

        return invoiceService.generateInvoice(invoiceJsonBody);
    }

    private List<ItemBody> convertToItemBodyList(List<WebOrderQuantities> orderQuantities){
        return orderQuantities.stream()
                .map(wq -> ItemBody.builder()
                        .name(wq.getProduct().getName())
                        .quantity(wq.getQuantity())
                        .unit_cost(wq.getProduct().getPrice())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteOrder(Long id){
        webOrderRepository.findById(id).orElseThrow(() -> new ApiResponseFailureException(HttpStatus.BAD_REQUEST, "Order not found"));
        webOrderRepository.deleteById(id);
    }
}
