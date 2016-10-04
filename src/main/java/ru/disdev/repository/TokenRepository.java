package ru.disdev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.disdev.entity.tokens.AccessToken;

public interface TokenRepository extends JpaRepository<AccessToken, Integer> {

    @Query(value = "select * from tokens where id=" + AccessToken.USER_TOKEN_ID, nativeQuery = true)
    AccessToken getUserToken();

    @Query(value = "select * from tokens where id=" + AccessToken.GROUP_TOKEN_ID, nativeQuery = true)
    AccessToken getGroupToken();

}
