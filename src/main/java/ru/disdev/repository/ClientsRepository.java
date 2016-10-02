package ru.disdev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.disdev.entity.Client;

import java.util.List;

public interface ClientsRepository extends JpaRepository<Client, Integer> {
    Client findOneByAccessTokenAndStatus(String accessToken, int status);

    List<Client> findByStatus(int status);
}
