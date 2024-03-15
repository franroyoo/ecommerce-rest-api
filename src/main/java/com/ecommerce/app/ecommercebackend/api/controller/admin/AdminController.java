package com.ecommerce.app.ecommercebackend.api.controller.admin;

import com.ecommerce.app.ecommercebackend.model.LocalUser;
import com.ecommerce.app.ecommercebackend.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public String greetAdmin(@AuthenticationPrincipal LocalUser user){
        return "Welcome to the admin control panel, " + user.getFirstName() + " " + user.getLastName() + ".";
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocalUser> promoteUserToPremium(@PathVariable Long id){
        LocalUser user = adminService.promoteUserToPremium(id);
        return ResponseEntity.ok(user);
    }
}
