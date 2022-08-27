package com.scalablescripts.auth.service;

import com.scalablescripts.auth.entities.PasswordRecovery;
import com.scalablescripts.auth.entities.Token;
import com.scalablescripts.auth.entities.User;
import com.scalablescripts.auth.error.*;
import com.scalablescripts.auth.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private  final PasswordEncoder passwordEncoder;
    private  final String accessTokenSecret;
    private  final String refreshTokenSecret;
    private final MailService mailService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       @Value("${application.security.access-token-secret}") String accessTokenSecret,
                       @Value("${application.security.refresh-token-secret}") String refreshTokenSecret, MailService mailService) {

                              this.userRepository = userRepository;
                              this.passwordEncoder = passwordEncoder;
                              this.accessTokenSecret = accessTokenSecret;
                              this.refreshTokenSecret = refreshTokenSecret;
                              this.mailService = mailService;
    }

    public User register(String firstName, String lastName, String email, String password, String passwordConfirm) {

        //Handle HTTP error
        if (! Objects.equals(password,passwordConfirm))
            throw new PasswordsdontMatchError();

        return userRepository.save(
                User.of(
                        firstName,
                        lastName,
                        email,
                        passwordEncoder.encode(password)
                )
        );
    }

    public Login login(String email, String password) {

        //find user by email
            var user = userRepository.findByEmail(email)
                    .orElseThrow(InvalidCredentialsError::new);

        //see if passwords match
            if (!passwordEncoder.matches(password, user.getPassword()))
                throw new InvalidCredentialsError();
            var login =Login.of(user.getId(), accessTokenSecret, refreshTokenSecret);
            var refreshJwt= login.getRefreshJwt();

                user.addToken(new Token(refreshJwt.getToken(), refreshJwt.getIssuedAt(), refreshJwt.getExpiration()));
                userRepository.save(user);

        return login;
    }

    public User getUserFromToken(String token) {
        return userRepository.findById(Math.toIntExact(Jwt.from(token, accessTokenSecret).getUserId()))
                .orElseThrow(UserNotFoundError::new);
    }

    public Login refreshAccess(String refreshToken) {

        var refreshJwt = Jwt.from(refreshToken, refreshTokenSecret);

        var user =
                userRepository.findByIdAndTokensRefreshTokenAndTokensExpiredAtGreaterThan(
                        refreshJwt.getUserId(),
                        refreshJwt.getToken(),
                        refreshJwt.getExpiration())
                            .orElseThrow(UnauthenticatedError::new);

        return Login.of(refreshJwt.getUserId() , accessTokenSecret, refreshJwt);
    }

    public Boolean logout(String refreshToken){
        var refreshJwt = Jwt.from(refreshToken, refreshTokenSecret);

        var user = userRepository.findById(Math.toIntExact(refreshJwt.getUserId()))
                .orElseThrow(UnauthenticatedError::new);
        var tokenIsRemoved = user.removeTokenId(token -> Objects.equals(token.refreshToken(),refreshToken));

        if (tokenIsRemoved)
            userRepository.save(user);
        return tokenIsRemoved;
    }
    public void forgot(String email, String originUrl){
        var token = UUID.randomUUID().toString().replace("-", "");
        var user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundError::new);

        user.addPasswordRecovery(new PasswordRecovery(token));
        mailService.sendForgotMessage(email, token, originUrl);

        userRepository.save(user);

    }

    public void reset(String token, String password, String passwordConfirm) {
        if (! Objects.equals(password,passwordConfirm))
            throw new PasswordsdontMatchError();
        var user = userRepository.findByPasswordRecoveriesToken(token)
                .orElseThrow(InvalidLinkError::new);
        user.setPassword(passwordEncoder.encode(password));
        user.removePasswordrecoveryId(passwordRecovery -> Objects.equals(passwordRecovery.token(), token));

        userRepository.save(user);

    }
}
