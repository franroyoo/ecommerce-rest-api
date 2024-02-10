package com.ecommerce.app.ecommercebackend.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class EncryptionService {
    @Value("${encryption.salt.rounds}")
    private int saltRounds; // tecnicamente se inyecta y al instanciar en el constuctor es null, por eso uso @PostConstruct
    private String salt;

    @PostConstruct
    public void postConstruct(){
        salt = BCrypt.gensalt(saltRounds); // se genera el salt
    }

    public String encryptPassword(String password){
        return BCrypt.hashpw(password,salt); // con la password en plain text, genero el hash con el salt
    }

    public boolean verifyPassword(String password, String hash){ // used for login
        return BCrypt.checkpw(password, hash); // chequeo si es true la comparacion entre la pass y el hash
    }
}