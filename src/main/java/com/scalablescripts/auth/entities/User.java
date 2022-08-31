package com.scalablescripts.auth.entities;

import lombok.*;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.relational.core.mapping.MappedCollection;

import javax.persistence.*;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

@AllArgsConstructor
@RequiredArgsConstructor
@Entity
@ToString
@Table(name = "user")
public class User {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Getter @Setter
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Getter @Setter
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Getter @Setter
    @Column(name = "email", nullable = false)
    private String email;

    @Getter @Setter
    @Column(name = "password", nullable = false)
    private String password;

    @Getter @Setter
    @Column(name = "tfasecret", nullable = false)
    private String tfasecret;

    @MappedCollection @Transient private final Set<Token> tokens = new HashSet<>();
    @MappedCollection @Transient private final Set<PasswordRecovery> passwordRecoveries = new HashSet<>();

    public static User of(String firstName, String lastName, String email, String password) {
        return new User (null, firstName, lastName, email, password, null,  Collections.emptyList(), Collections.emptyList());
    }

    @PersistenceConstructor
    public User(Long id, String firstName, String lastName, String email, String password,String tfasecret,
                Collection<Token> tokens, Collection<PasswordRecovery> passwordRecoveries) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.tfasecret = tfasecret;
        this.tokens.addAll(tokens);
        this.passwordRecoveries.addAll(passwordRecoveries);
    }

    public void addToken(Token token){
        this.tokens.add(token);
    }

    public Boolean removeToken(Token token){
       return this.tokens.remove(token);
    }

    public Boolean removeTokenId(Predicate<? super Token> predicate){return this.tokens.removeIf(predicate);}

    public void addPasswordRecovery(PasswordRecovery passwordRecovery ){this.passwordRecoveries.add(passwordRecovery);}

    public Boolean removePasswordrecovery(PasswordRecovery passwordRecovery){
        return this.passwordRecoveries.remove(passwordRecovery);}

    public Boolean removePasswordrecoveryId(Predicate<? super PasswordRecovery> predicate){return this.passwordRecoveries.removeIf(predicate);}
}