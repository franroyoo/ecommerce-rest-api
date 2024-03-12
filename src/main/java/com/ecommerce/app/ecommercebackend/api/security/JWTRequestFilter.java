package com.ecommerce.app.ecommercebackend.api.security;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.ecommerce.app.ecommercebackend.api.repository.LocalUserRepository;
import com.ecommerce.app.ecommercebackend.model.LocalUser;
import com.ecommerce.app.ecommercebackend.service.CustomUserDetailsService;
import com.ecommerce.app.ecommercebackend.service.JWTService;
import com.ecommerce.app.ecommercebackend.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

@Component
public class JWTRequestFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final LocalUserRepository localUserRepository;

    @Autowired
    public JWTRequestFilter(JWTService jwtService, LocalUserRepository localUserRepository){
        this.jwtService = jwtService;
        this.localUserRepository = localUserRepository;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String tokenHeader = request.getHeader("Authorization");
        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")){
            String token = tokenHeader.substring(7);
            try{

                String username = jwtService.getUsername(token);

                LocalUser user = localUserRepository.findByUsernameIgnoreCase(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

            }catch(JWTDecodeException ex){

            }
        }

        filterChain.doFilter(request,response);
    }
}
