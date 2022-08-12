package com.scalablescripts.auth.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.scalablescripts.auth.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    record RegisterRequest(@JsonProperty("first_name") String firstName,
                           @JsonProperty("last_name") String lastName,
                           String email,
                           String password,
                           @JsonProperty("password_confirm") String passwordConfirm) {

    }

    record RegisterResponse(Integer id,
                            @JsonProperty("first_name") String firstName,
                            @JsonProperty("last_name") String lastName,
                            String email) {

    }


    @PostMapping("/register")

    public RegisterResponse register(@RequestBody RegisterRequest registerRequest) {

        var user = authService.register(

                registerRequest.firstName(),
                registerRequest.lastName(),
                registerRequest.email(),
                registerRequest.password(),

                registerRequest.passwordConfirm()

        );

        return new RegisterResponse(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail());
    }


}
