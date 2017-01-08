package ru.disdev.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.disdev.api.VkApi;
import ru.disdev.bot.TelegramBot;
import ru.disdev.entity.post.Event;
import ru.disdev.repository.EventsRepository;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class EventService {

    private static final Comparator<Event> EVENT_COMPARATOR = (a, b) -> {
        LocalDateTime first = LocalDateTime.of(a.getDate(), a.getTime());
        LocalDateTime second = LocalDateTime.of(b.getDate(), b.getTime());
        return first.compareTo(second);
    };

    private Set<Event> cache = new ConcurrentSkipListSet<>(EVENT_COMPARATOR);
    private Map<Integer, ScheduledFuture<?>> announceTask = new ConcurrentHashMap<>();

    @Autowired
    private EventsRepository repository;
    @Autowired
    private ScheduledExecutorService executorService;
    @Autowired
    private TelegramBot bot;
    @Autowired
    private VkApi vkApi;

    @PostConstruct
    private void init() {
        LocalDate now = LocalDate.now();
        repository.findAll()
                .stream()
                .filter(event -> event.getDate().isBefore(now))
                .forEach(repository::delete);
        repository.findAll().forEach(cache::add);
        cache.forEach(event -> {
            ScheduledFuture<?> task = scheduleAnnounce(event);
            if (task != null) {
                announceTask.put(event.getId(), task);
            }
        });
    }

    public List<Event> findAllByDate(LocalDate date) {
        return cache.stream()
                .filter(event -> event.getDate().isEqual(date))
                .collect(Collectors.toList());
    }

    public void addEvent(Event event) {
        repository.save(event);
        cache.add(event);
        ScheduledFuture<?> task = scheduleAnnounce(event);
        if (task != null) {
            announceTask.put(event.getId(), task);
        }
    }

    private ScheduledFuture<?> scheduleAnnounce(Event event) {
        LocalDateTime now = LocalDateTime.now();
        if (event.getNotificationDateTime() == null || now.isAfter(event.getNotificationDateTime())) {
            return null;
        }
        Timestamp timestamp = Timestamp.valueOf(event.getNotificationDateTime());
        long delayInMillis = Math.abs(timestamp.getTime() - System.currentTimeMillis());
        String message = "Напоминание:\n" + event.toString();
        return executorService.schedule(() -> {
                    bot.announceToGroup(message);
                    vkApi.sendMessage(message);
                },
                delayInMillis,
                TimeUnit.MILLISECONDS);
    }

    public boolean deleteById(int id) {
        Optional<Event> toRemove = cache.stream().filter(event -> event.getId() == id).findFirst();
        if (!toRemove.isPresent()) {
            return false;
        }
        cache.remove(toRemove.get());
        repository.delete(toRemove.get());
        ScheduledFuture<?> future = announceTask.remove(id);
        if (future != null) {
            future.cancel(true);
        }
        return true;

    }

    public List<Event> findAll() {
        return actual().collect(Collectors.toList());
    }

    private Stream<Event> actual() {
        final LocalDate date = LocalDate.now();
        return cache.stream().filter(event -> event.getDate().isEqual(date) || event.getDate().isAfter(date));
    }

}
