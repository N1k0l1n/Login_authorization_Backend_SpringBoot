package com.scalablescripts.auth.repositories;

import com.scalablescripts.auth.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    //select 1  from user u where u.email = : email
    Optional<User> findByEmail(String email);

    @Query      (value ="""
                select u.* from user u inner join token t on u.id = t.user
                where u.id = :id and t.refresh_token = :refreshToken and t.expired_at >= :expiredAt""",  nativeQuery = true
                )
    Optional<User> findByIdAndTokensRefreshTokenAndTokensExpiredAtGreaterThan(Long id, String refreshToken, LocalDateTime expiredAt);

    @Query      (value = """
                select u.* from user u inner join password_recovery pr on u.id = pr.user
                where pr.token = :token
                """, nativeQuery = true)

    Optional<User> findByPasswordRecoveriesToken(String token);

}