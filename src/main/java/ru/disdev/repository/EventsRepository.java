package ru.disdev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.disdev.entity.Event;

import java.time.LocalDate;
import java.util.List;

public interface EventsRepository extends JpaRepository<Event, Integer> {
    List<Event> findByDate(LocalDate date);

    List<Event> deleteByDateLessThen(LocalDate date);
}
