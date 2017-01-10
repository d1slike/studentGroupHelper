package ru.disdev.service;

import com.google.common.collect.ImmutableSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.disdev.entity.vk.VkSubscriber;
import ru.disdev.repository.VkSubscriberRepository;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class VkSubscriberService {

    @Autowired
    private VkSubscriberRepository vkSubscriberRepository;

    public void addSubscriber(int vkUserId) {
        vkSubscriberRepository.save(new VkSubscriber(vkUserId));
    }

    public void deleteSubscriber(int vkUserId) {
        vkSubscriberRepository.deleteByVkUserId(vkUserId);
    }

    public ImmutableSet<Integer> getSubscribers() {
        Set<Integer> vkIdList = vkSubscriberRepository.findAll().stream()
                .map(VkSubscriber::getVkUserId)
                .collect(Collectors.toSet());
        return ImmutableSet.copyOf(vkIdList);
    }
}
