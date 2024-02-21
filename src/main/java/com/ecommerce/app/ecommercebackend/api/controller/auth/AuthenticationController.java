package com.ecommerce.app.ecommercebackend.api.controller.auth;

import com.ecommerce.app.ecommercebackend.api.dto.LoginBody;
import com.ecommerce.app.ecommercebackend.api.dto.LoginResponse;
import com.ecommerce.app.ecommercebackend.api.dto.RegistrationBody;
import com.ecommerce.app.ecommercebackend.exception.EmailFailureException;
import com.ecommerce.app.ecommercebackend.exception.UserAlreadyExistsException;
import com.ecommerce.app.ecommercebackend.exception.UserNotVerifiedException;
import com.ecommerce.app.ecommercebackend.model.LocalUser;
import com.ecommerce.app.ecommercebackend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private UserService userService;

    @Autowired
    public AuthenticationController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register") // endpoint for registration
    public ResponseEntity registerUser(@Valid @RequestBody RegistrationBody registrationBody){ // paso un JSON

        // spring transforma lo que le pasamos como JSON con postman a un objeto de tipo RegistrationBody
        // el key que mandamos en postman tiene que ser igual al atributo de la clase (RegistrationBody)
        // de esta forma puedo operar sobre el objeto tranquilamente

        try{
            userService.registerUser(registrationBody); // registro el user
            return ResponseEntity.ok().build(); // .ok() es codigo 200, build es como "mandar"
        }catch(UserAlreadyExistsException ex){
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // si existe la excepcion, tirar conflict y "mandar"
        } catch (EmailFailureException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    @PostMapping("/login") // endpoint for login
    public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginBody loginBody){
         // logueo el user, obtengo el token JWT en string (puede darme NULL si no se encuentra el user)
        String jwt = null;
        try {
            jwt = userService.loginUser(loginBody);
        } catch (UserNotVerifiedException ex) {

            // Si el user no está verificado, "response" informa la razón
            LoginResponse response = new LoginResponse();
            String reason = "USER_NOT_VERIFIED";
            if (ex.isNewEmailSent()){
                reason += "_EMAIL_RESENT";
            }

            response.setSuccess(false);
            response.setFailureReason(reason); // "USER_NOT_VERIFIED" o "USER_NOT_VERIFIED_EMAIL_RESENT"

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);

        } catch (EmailFailureException ex) {
            // Si se captura esta excepción es porque hubo un error interno del server
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        if (jwt == null){ // si es null, manda un status BAD_REQUEST
           return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
       }else {
           LoginResponse loginResponse = new LoginResponse(); // crea una instancia de LoginResponse para guardar el jwt (buena practica)
           loginResponse.setJwt(jwt); // setteo el jwt
            loginResponse.setSuccess(true);
           return ResponseEntity.ok(loginResponse); // devuelvo status OK y muestro en postman el objeto loginResponse en JSON (osea, solo muestro el jwt)
       }

    }

    @GetMapping("/me")
    public LocalUser getLoggedInUserProfile(@AuthenticationPrincipal LocalUser user){
        return user;
    }

    @PostMapping("/verify")
    public ResponseEntity verifyEmail(@RequestParam String token){
        if (userService.verifyUser(token)){
            return ResponseEntity.ok().build();
        }else{
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
