package com.ecommerce.app.ecommercebackend.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ecommerce.app.ecommercebackend.model.LocalUser;
import com.ecommerce.app.ecommercebackend.model.Role;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.stream.Collectors;

@Service
public class JWTService {

    @Value("${jwt.algorithm.key}")
    private String algorithmKey;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.expiry}")
    private int expiry;
    private static final String USERNAME_KEY = "USERNAME";
    private static final String ROLES_KEY = "ROLES";
    private static final String EMAIL_KEY = "EMAIL";

    private Algorithm algorithm;

    @PostConstruct
    public void postConstruct(){
        algorithm = Algorithm.HMAC256(algorithmKey);
    }

    public String generateJWT(LocalUser user){
        return JWT.create()
                .withClaim(USERNAME_KEY, user.getUsername())
                .withClaim(ROLES_KEY, user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                .withExpiresAt(new Date(System.currentTimeMillis() + (1000L * expiry)))
                .withIssuer(issuer)
                .sign(algorithm);

    }

    public String generateVerificationJWT(LocalUser user){
        return JWT.create()
                .withClaim(EMAIL_KEY, user.getEmail())
                .withExpiresAt(new Date(System.currentTimeMillis() + (1000L * expiry)))
                .withIssuer(issuer)
                .sign(algorithm);

    }

    public String getUsername(String token){
        DecodedJWT jwt = JWT.require(algorithm).build().verify(token);
        return jwt.getClaim(USERNAME_KEY).asString();
    }




}
