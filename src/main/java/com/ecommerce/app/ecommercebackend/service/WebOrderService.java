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
import com.ecommerce.app.ecommercebackend.validation.FailureType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
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

        Address address = addressRepository.findByAddressLine1AndLocalUser_Id(orderBody.getAddressLine1(),user.getId()).orElseThrow(() -> new ApiResponseFailureException(FailureType.ADDRESS_NOT_FOUND,
                "The address could not be found in your account. Please add it or try a new one"));

        Map<Long, Integer> productsHashMap = getProductBodyMapWithoutDuplicates(orderBody);

        List<Product> productsFound = productRepository.findByIdInOrderById(productsHashMap.keySet());

        verifyProductsExistenceAndStockAvailabilityOrElseThrow(productsHashMap, productsFound);

       productsHashMap.forEach((productId, quantity) -> {
            WebOrderQuantities webOrderQuantities = WebOrderQuantities.builder()
                    .quantity(quantity)
                    .product(productsFound.stream().filter(product -> product.getId() == productId).findFirst().get())
                    .webOrder(order)
                    .build();

            orderQuantities.add(webOrderQuantities);
       });

        order.setLocalUser(user);
        order.setQuantities(orderQuantities);
        order.setAddress(address);

        webOrderRepository.save(order);

        InvoiceJsonBody invoiceJsonBody = InvoiceJsonBody.builder()
                .to(user.getFirstName() + " " + user.getLastName())
                .notes("For any questions regarding your order, please contact me at " + developerEmail)
                .items(convertToItemBodyList(orderQuantities))
                .number(order.getId())
                .shipTo(address.getAddressLine1())
                .build();

        return invoiceService.generateInvoice(invoiceJsonBody);
    }

    private void verifyProductsExistenceAndStockAvailabilityOrElseThrow(Map<Long, Integer> productsHashMap, List<Product> productsFound) {

        List<Long> productBodyIds = new ArrayList<>(productsHashMap.keySet().stream().toList());

        List<Long> productsFoundIds = new ArrayList<>(productsFound.stream().map(Product::getId).toList());;

        Collections.sort(productBodyIds);
        Collections.sort(productsFoundIds);

        // check if all products exist

        if (!productBodyIds.equals(productsFoundIds)){

            List<Long> missingProducts = productBodyIds.stream().filter(productId -> !productsFoundIds.contains(productId)).toList();

            throw new ApiResponseFailureException(FailureType.PRODUCT_NOT_FOUND, "The following product ids were not found: " + missingProducts);
        }

        // check if all products have stock available

        List<Inventory> inventoryList = inventoryRepository.findByProductIdInOrderByProductId(productsFoundIds);

        Collections.sort(productsFoundIds);

        List<String> productsWithNoStock = new ArrayList<>();

        for (Inventory inventory : inventoryList){
            if (inventory.getQuantity() < productsHashMap.get(inventory.getProduct().getId())){
                productsWithNoStock.add(inventory.getProduct().getId().toString());
            }else{
                inventory.setQuantity(inventory.getQuantity() - productsHashMap.get(inventory.getProduct().getId()));
            }
        }

        if (!productsWithNoStock.isEmpty()){
            throw new ApiResponseFailureException(FailureType.OUT_OF_STOCK, "The following products do not have enough stock: " + productsWithNoStock);
        }

        inventoryRepository.saveAll(inventoryList);

    }

    private Map<Long,Integer> getProductBodyMapWithoutDuplicates(OrderBody orderBody){

        Map<Long, Integer> productsHashMap = new HashMap<>();

        for (ProductBody productDTO : orderBody.getProducts()){
            if (productsHashMap.containsKey(productDTO.getProductId())){
                productsHashMap.put(productDTO.getProductId(), productsHashMap.get(productDTO.getProductId()) + productDTO.getQuantity());
            }else{
                productsHashMap.put(productDTO.getProductId(), productDTO.getQuantity());
            }
        }

        return productsHashMap;
    }

    private List<ItemBody> convertToItemBodyList(List<WebOrderQuantities> orderQuantities){
        return orderQuantities.stream()
                .map(wq -> ItemBody.builder()
                        .name(wq.getProduct().getName())
                        .quantity(wq.getQuantity())
                        .unitCost(wq.getProduct().getPrice())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteOrder(Long id){
        webOrderRepository.findById(id).orElseThrow(() -> new ApiResponseFailureException(FailureType.ORDER_NOT_FOUND,
                "Your order id does not exist. Make sure to delete the correct one"));
        webOrderRepository.deleteById(id);
    }
}
