package com.ecommerce.app.ecommercebackend.service;

import com.ecommerce.app.ecommercebackend.api.dto.LoginBody;
import com.ecommerce.app.ecommercebackend.api.dto.RegistrationBody;
import com.ecommerce.app.ecommercebackend.api.repository.LocalUserRepository;
import com.ecommerce.app.ecommercebackend.api.repository.RoleRepository;
import com.ecommerce.app.ecommercebackend.api.repository.VerificationTokenRepository;
import com.ecommerce.app.ecommercebackend.exception.EmailFailureException;
import com.ecommerce.app.ecommercebackend.exception.UserAlreadyExistsException;
import com.ecommerce.app.ecommercebackend.exception.UserNotVerifiedException;
import com.ecommerce.app.ecommercebackend.model.LocalUser;
import com.ecommerce.app.ecommercebackend.model.Role;
import com.ecommerce.app.ecommercebackend.model.VerificationToken;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;

@Service
public class UserService {

    private final LocalUserRepository localUserRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EncryptionService encryptionService;
    private final JWTService jwtService;
    private final EmailService emailService;

    private final RoleRepository roleRepository;
    @Autowired
    public UserService(LocalUserRepository localUserRepository, EncryptionService encryptionService, JWTService jwtService, VerificationTokenRepository verificationTokenRepository
            , EmailService emailService, RoleRepository roleRepository) {
        this.localUserRepository = localUserRepository;
        this.encryptionService = encryptionService;
        this.jwtService = jwtService;
        this.verificationTokenRepository = verificationTokenRepository;
        this.emailService = emailService;
        this.roleRepository = roleRepository;
    }

    public LocalUser registerUser(RegistrationBody registrationBody) throws UserAlreadyExistsException, EmailFailureException {


        if (localUserRepository.findByUsernameIgnoreCase(registrationBody.getUsername()).isPresent() ||
                localUserRepository.findByEmailIgnoreCase(registrationBody.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException();
        }


        LocalUser user = new LocalUser();

        user.setUsername(registrationBody.getUsername());
        user.setEmail(registrationBody.getEmail());
        user.setFirstName(registrationBody.getFirstName());
        user.setLastName(registrationBody.getLastName());

        Role roleUser = roleRepository.findByName("ROLE_USER").get();

        user.setRoles(Collections.singletonList(roleUser));
        user.setPassword(encryptionService.encryptPassword(registrationBody.getPassword()));


        LocalUser savedUser = localUserRepository.save(user);

        VerificationToken verificationToken = createVerificationToken(savedUser);

        emailService.sendVerificationEmail(verificationToken);

        verificationTokenRepository.save(verificationToken);

        return savedUser;
    }


    private VerificationToken createVerificationToken(LocalUser user){

        VerificationToken verificationToken = new VerificationToken();

        verificationToken.setToken(jwtService.generateVerificationJWT(user));
        verificationToken.setLocalUser(user);
        verificationToken.setCreatedTimestamp(new Timestamp(System.currentTimeMillis()));

        if (user.getVerificationTokens() == null) {
            user.setVerificationTokens(new ArrayList<>());
        }

        user.getVerificationTokens().add(verificationToken);

        return verificationToken;
    }

    public String loginUser(LoginBody loginBody) throws UserNotVerifiedException, EmailFailureException {

        Optional<LocalUser> opUser = localUserRepository.findByUsernameIgnoreCase(loginBody.getUsername());

        if(opUser.isPresent()){

            LocalUser user = opUser.get();
            if(encryptionService.verifyPassword(loginBody.getPassword(),user.getPassword())){

                if (user.isEmailVerified()){

                    return jwtService.generateJWT(user);
                }else{

                    List<VerificationToken> verificationTokenList = user.getVerificationTokens();

                    boolean resend = verificationTokenList.isEmpty() ||
                            verificationTokenList.get(0).getCreatedTimestamp().before(new Timestamp(System.currentTimeMillis() - (60 * 60 * 1000)));

                    if (resend){
                        VerificationToken verificationToken = createVerificationToken(user);

                        verificationTokenRepository.save(verificationToken);

                        emailService.sendVerificationEmail(verificationToken);
                    }

                    throw new UserNotVerifiedException(resend);
                }
            }
        }
        return null;
    }

    @Transactional
    public boolean verifyUser(String token){

        Optional<VerificationToken> opToken = verificationTokenRepository.findByToken(token);

        if (opToken.isPresent()){

            VerificationToken verificationToken = opToken.get();
            LocalUser user = verificationToken.getLocalUser();

            if (!user.isEmailVerified()){
                user.setEmailVerified(true);
                localUserRepository.save(user);
                verificationTokenRepository.deleteByLocalUser(user);
                return true;
            }

        }
        return false;
    }

}
