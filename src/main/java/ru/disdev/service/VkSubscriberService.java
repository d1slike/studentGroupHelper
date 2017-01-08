package ru.disdev.service;

import com.google.common.collect.ImmutableSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.disdev.entity.vk.VkSubscriber;
import ru.disdev.repository.VkSubscriberRepository;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

@Service
public class VkSubscriberService {

    @Autowired
    private VkSubscriberRepository vkSubscriberRepository;
    private Set<Integer> subscribers = new CopyOnWriteArraySet<>();

    @PostConstruct
    private void init() {
        List<Integer> values = vkSubscriberRepository.findAll().stream()
                .map(VkSubscriber::getVkUserId)
                .collect(Collectors.toList());
        subscribers.addAll(values);
    }

    public void addSubscriber(int vkUserId) {
        subscribers.add(vkUserId);
        vkSubscriberRepository.save(new VkSubscriber(vkUserId));
    }

    public void deleteSubscriber(int vkUserId) {
        vkSubscriberRepository.deleteByVkUserId(vkUserId);
        subscribers.remove(vkUserId);
    }

    public ImmutableSet<Integer> getSubscribers() {
        return ImmutableSet.copyOf(subscribers);
    }
}
