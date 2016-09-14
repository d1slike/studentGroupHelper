package ru.disdev.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.disdev.VkGroupBot;
import ru.disdev.entity.Event;
import ru.disdev.repository.EventsRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Component
public class EventService {
    private Map<Integer, Event> cache = new ConcurrentHashMap<>();
    private Map<Integer, ScheduledFuture<?>> announceTask = new ConcurrentHashMap<>();

    @Autowired
    private EventsRepository repository;
    @Autowired
    private ScheduledExecutorService executorService;
    @Autowired
    private VkGroupBot bot;

    @PostConstruct
    private void init() {
        repository.deleteByDateLessThen(LocalDate.now());
        repository.findAll().forEach(event -> cache.put(event.getId(), event));
        cache.forEach((integer, event) -> {
            ScheduledFuture<?> task = scheduleAnnounce(event);
            if (task != null)
                announceTask.put(integer, task);
        });
    }


    public List<Event> findAllByDate(LocalDate date) {
        List<Event> result = new ArrayList<>();
        cache.forEach((integer, event) -> {
            if (event.getDate().isEqual(date))
                result.add(event);
        });

        return result;
    }

    public void addEvent(Event event) {
        repository.save(event);
        cache.put(event.getId(), event);
        ScheduledFuture<?> task = scheduleAnnounce(event);
        if (task != null)
            announceTask.put(event.getId(), task);
    }

    private ScheduledFuture<?> scheduleAnnounce(Event event) {
        if (event.getNotificationDateTime() == null)
            return null;
        Date date = Date.from(event.getNotificationDateTime().toInstant(ZoneOffset.ofHours(3)));
        long delayInMillis = Math.abs(date.getTime() - System.currentTimeMillis());
        String message = "Напоминание:\n" + event.toString();
        return executorService.schedule(() -> bot.announceToGroup(message),
                delayInMillis,
                TimeUnit.MILLISECONDS);
    }

    public boolean deleteById(int id) {
        Event removed = cache.remove(id);
        if (removed != null) {
            repository.delete(removed);
            ScheduledFuture<?> future = announceTask.get(id);
            if (future != null)
                future.cancel(true);
            return true;
        }

        return false;
    }

    public Map<Integer, Event> findAll() {
        Map<Integer, Event> result = new HashMap<>();
        actual().forEach(event -> result.put(event.getId(), event));
        return result;
    }

    private Stream<Event> actual() {
        final LocalDate date = LocalDate.now();
        return cache.values().stream().filter(event -> event.getDate().isEqual(date) || event.getDate().isAfter(date));
    }

}
