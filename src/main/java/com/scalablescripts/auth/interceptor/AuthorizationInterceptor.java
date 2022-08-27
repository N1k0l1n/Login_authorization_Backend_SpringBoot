package com.scalablescripts.auth.interceptor;

import com.scalablescripts.auth.error.NoBearerTokenError;
import com.scalablescripts.auth.service.AuthService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AuthorizationInterceptor implements HandlerInterceptor {
    private final AuthService authService;

    public AuthorizationInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader==null|| !authorizationHeader.startsWith("Bearer "))
                throw new NoBearerTokenError();

        request.setAttribute("user", authService.getUserFromToken(authorizationHeader.substring(7)));
        return true;
    }
}
