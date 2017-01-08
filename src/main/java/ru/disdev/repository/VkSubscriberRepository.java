package ru.disdev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.disdev.entity.vk.VkSubscriber;

import java.util.List;

public interface VkSubscriberRepository extends JpaRepository<VkSubscriber, Integer> {
    @Transactional
    List<VkSubscriber> deleteByVkUserId(int vkUserId);
}
