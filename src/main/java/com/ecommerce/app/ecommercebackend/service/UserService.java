package com.ecommerce.app.ecommercebackend.service;

import com.ecommerce.app.ecommercebackend.api.dto.LoginBody;
import com.ecommerce.app.ecommercebackend.api.dto.RegistrationBody;
import com.ecommerce.app.ecommercebackend.api.repository.LocalUserRepository;
import com.ecommerce.app.ecommercebackend.api.repository.VerificationTokenRepository;
import com.ecommerce.app.ecommercebackend.exception.EmailFailureException;
import com.ecommerce.app.ecommercebackend.exception.UserAlreadyExistsException;
import com.ecommerce.app.ecommercebackend.exception.UserNotVerifiedException;
import com.ecommerce.app.ecommercebackend.model.LocalUser;
import com.ecommerce.app.ecommercebackend.model.VerificationToken;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
public class UserService { // se comunica con el repositorio y lo vamos a usar en el controlador

    private LocalUserRepository localUserRepository; // inyeccion de dependencia del repositorio
    private VerificationTokenRepository verificationTokenRepository;
    private EncryptionService encryptionService;
    private JWTService jwtService;
    private EmailService emailService;

    @Autowired
    public UserService(LocalUserRepository localUserRepository, EncryptionService encryptionService, JWTService jwtService, VerificationTokenRepository verificationTokenRepository
            , EmailService emailService) {
        this.localUserRepository = localUserRepository;
        this.encryptionService = encryptionService;
        this.jwtService = jwtService;
        this.verificationTokenRepository = verificationTokenRepository;
        this.emailService = emailService;
    }

    // esto sería como el metodo save() de la app con registro que hice antes
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

        // Encrypt the password
        user.setPassword(encryptionService.encryptPassword(registrationBody.getPassword()));


        LocalUser savedUser = localUserRepository.save(user);

        // Create a verification token for the registered user
        VerificationToken verificationToken = createVerificationToken(savedUser);

        // Send the verification email
        emailService.sendVerificationEmail(verificationToken);

        // Save the verification token to the database
        verificationTokenRepository.save(verificationToken);

        return savedUser;
    }


    // Crear token de verificacion
    private VerificationToken createVerificationToken(LocalUser user){

        // Creo una instancia de verificationToken
        VerificationToken verificationToken = new VerificationToken();

        // Setteo los datos del verificationToken (token, usuario, timestamp)
        verificationToken.setToken(jwtService.generateVerificationJWT(user)); // token
        verificationToken.setLocalUser(user); // user
        verificationToken.setCreatedTimestamp(new Timestamp(System.currentTimeMillis())); // timestamp

        // agrego token de verificacion al usuario
        user.getVerificationTokens().add(verificationToken); // sets bidirectional linking

        return verificationToken;
    }

    public String loginUser(LoginBody loginBody) throws UserNotVerifiedException, EmailFailureException {

        Optional<LocalUser> opUser = localUserRepository.findByUsernameIgnoreCase(loginBody.getUsername()); // busco el user

        if(opUser.isPresent()){

            LocalUser user = opUser.get(); // si está presente, entonces uso get() para obtener el usuario
            if(encryptionService.verifyPassword(loginBody.getPassword(),user.getPassword())){ // si la contraseña q me pasan se verifica con el hash en la base de datos

                // Si el mail esta verificado return JWT
                if (user.isEmailVerified()){
                    return jwtService.generateJWT(user);
                }else{

                    // Obtengo la lista de verificationToken
                    List<VerificationToken> verificationTokenList = user.getVerificationTokens();

                    // Valor de resend depende si la lista esta vacia o si el ultimo verificationToken tiene mas de una hora
                    boolean resend = verificationTokenList.isEmpty() ||
                            verificationTokenList.get(0).getCreatedTimestamp().before(new Timestamp(System.currentTimeMillis() - (60 * 60 * 1000)));

                    // Si resend es true, mandar token de vuelta
                    if (resend){
                        // Creo el nuevo verificationToken con el metodo de mas arriba
                        VerificationToken verificationToken = createVerificationToken(user);
                        // Guardo el verificationToken en la DB
                        verificationTokenRepository.save(verificationToken);

                        // Mando el email
                        emailService.sendVerificationEmail(verificationToken);
                    }

                    throw new UserNotVerifiedException(resend);
                }
            }
        }
        return null; // unsuccessful to verify the user
    }

    @Transactional // es transactional porque estamos CAMBIANDO data en mysql y NO haciendo queries
    public boolean verifyUser(String token){

        Optional<VerificationToken> opToken = verificationTokenRepository.findByToken(token);

        if (opToken.isPresent()){
            // El token existe
            VerificationToken verificationToken = opToken.get();
            LocalUser user = verificationToken.getLocalUser();

            if (!user.isEmailVerified()){
                user.setEmailVerified(true);
                localUserRepository.save(user); // updating
                verificationTokenRepository.deleteByLocalUser(user);
                return true;
            }

        }
        return false;
    }
}
