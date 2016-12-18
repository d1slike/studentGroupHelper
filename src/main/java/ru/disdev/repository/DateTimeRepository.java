package ru.disdev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.disdev.entity.mail.DateTime;

public interface DateTimeRepository extends JpaRepository<DateTime, Integer> {

}
