package com.scalablescripts.auth.service;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import lombok.Getter;


public class Login {

    @Getter
    private final Jwt accessJwt;
    @Getter
    private final Jwt refreshJwt;
    @Getter
    private final String otpSecret;
    @Getter
    private final String otpUrl;


    private static final Long ACCESS_TOKEN_VALIDITY = 1L;
    private static final Long REFRESH_TOKEN_VALIDITY = 1440L;

    public Login(Jwt accessToken, Jwt refreshToken, String otpSecret, String otpUrl) {
        this.accessJwt = accessToken;
        this.refreshJwt = refreshToken;
        this.otpSecret = otpSecret;
        this.otpUrl = otpUrl;
    }

    public static Login of(Long userId, String accessSecret, String refreshSecret, Boolean generateOtp){
        String optSecret = null;
        String optUrl = null;

        if (generateOtp){
            optSecret = generatedOtpSecret();
            optUrl = getOtpUrl(optSecret);
        }


        return new Login(
                Jwt.of(userId, ACCESS_TOKEN_VALIDITY, accessSecret),
                Jwt.of(userId, REFRESH_TOKEN_VALIDITY, refreshSecret),
                optSecret,
                optUrl
        );
    }



    public static Login of(Long userId, String accessSecret, Jwt refreshToken, Boolean generateOtp){
        String optSecret = null;
        String optUrl = null;

        if (generateOtp){
            optSecret = generatedOtpSecret();
            optUrl = getOtpUrl(optSecret);
        }

        return new Login(
                Jwt.of(userId, ACCESS_TOKEN_VALIDITY, accessSecret),
                refreshToken,
                optSecret,
                optUrl
        );
    }


    private static String generatedOtpSecret() {
        return new DefaultSecretGenerator().generate();
    }


    private static String getOtpUrl(String otpSecret) {
        var appname= "My App";
        return String.format("otpauth://totp/%s:Secret?secret=%s&issuer=%s",appname, otpSecret, appname);
    }
}
