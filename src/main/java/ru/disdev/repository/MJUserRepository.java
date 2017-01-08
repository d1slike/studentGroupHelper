package ru.disdev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.disdev.entity.mj.MJUser;

public interface MJUserRepository extends JpaRepository<MJUser, Integer> {
}
