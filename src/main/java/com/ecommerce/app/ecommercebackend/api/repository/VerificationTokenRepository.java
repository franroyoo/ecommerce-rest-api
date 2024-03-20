package com.ecommerce.app.ecommercebackend.api.repository;

import com.ecommerce.app.ecommercebackend.model.LocalUser;
import com.ecommerce.app.ecommercebackend.model.VerificationToken;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends ListCrudRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);

    void deleteByLocalUser(LocalUser localUser);


}
