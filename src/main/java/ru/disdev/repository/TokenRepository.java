package ru.disdev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.disdev.entity.AccessToken;

public interface TokenRepository extends JpaRepository<AccessToken, Integer> {
    AccessToken findOneByEmail(String email);
}
