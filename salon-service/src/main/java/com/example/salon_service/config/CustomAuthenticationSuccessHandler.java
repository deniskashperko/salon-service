package com.example.salon_service.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("admin"))) {
            response.sendRedirect("/lkemployee");
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("master"))) {
            response.sendRedirect("/lkemployee");
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("client"))) {
            response.sendRedirect("/lkclient");
        } else {
            response.sendRedirect("/"); // fallback
        }
    }
}
