package com.scalablescripts.auth.entities;

import java.time.LocalDateTime;


public record Token(String refreshToken, LocalDateTime issuedAt, LocalDateTime expiredAt) {}
