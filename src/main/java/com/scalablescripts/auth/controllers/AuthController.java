package com.scalablescripts.auth.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.scalablescripts.auth.entities.User;
import com.scalablescripts.auth.service.AuthService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


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

    record RegisterResponse(Long id,
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

    record LoginRequest (String email, String password){}
    record LoginResponse(String token) {}

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest, HttpServletResponse response){
        var login=authService.login(loginRequest.email(),loginRequest.password());

        Cookie cookie = new Cookie("refresh_token" , login.getRefreshJwt().getToken());
        cookie.setMaxAge(3600);
        cookie.setHttpOnly(true);
        cookie.setPath("/api");

        response.addCookie(cookie);

        return new LoginResponse(login.getAccessJwt().getToken());
    }

    record UserResponse(Long id,
                            @JsonProperty("first_name") String firstName,
                            @JsonProperty("last_name") String lastName,
                            String email) {}

    @GetMapping("/user")
    public UserResponse user(HttpServletRequest request){
        var user = (User) request.getAttribute("user");
        return new UserResponse(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail());
    }
    record RefreshResponse(String message){}

    @PostMapping("/refresh")
    public RefreshResponse refresh(@CookieValue("refresh_token")String refreshToken){
        return new RefreshResponse(authService.refreshAccess(refreshToken).getAccessJwt().getToken());
    }

    record LogoutResponse (String message){}

    @PostMapping("/logout")
    public LogoutResponse logout(@CookieValue("refresh_token")String refreshToken, HttpServletResponse response){
        authService.logout(refreshToken);
        Cookie cookie = new Cookie("refresh_token", null);
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);

        response.addCookie(cookie);
        return new LogoutResponse("You are logged out");
    }

    record ForgotResponse (String message){}
    record ForgotRequest (String email){}
    @PostMapping("/forgot")
    public ForgotResponse forgot(@RequestBody ForgotRequest forgotRequest, HttpServletRequest request){
        var originUrl = request.getHeader("Origin");

        authService.forgot(forgotRequest.email(),originUrl);
        return new ForgotResponse("Succes");
    }
    record ResetResponse (String message){}
    record ResetRequest (String token, String password,@JsonProperty(value = "password_confirm") String passwordConfirm){}
    @PostMapping("/reset")
    public ResetResponse reset(@RequestBody ResetRequest resetRequest){
        authService.reset(resetRequest.token(), resetRequest.password(), resetRequest.passwordConfirm());
        return new ResetResponse("Success");
    }
}
