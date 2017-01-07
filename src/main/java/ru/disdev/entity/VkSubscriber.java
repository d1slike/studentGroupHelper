package ru.disdev.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class VkSubscriber {

    @Id
    @GeneratedValue
    private int id;
    private int vkUserId;

    public VkSubscriber(int vkUserId) {
        this.vkUserId = vkUserId;
    }

    public VkSubscriber() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVkUserId() {
        return vkUserId;
    }

    public void setVkUserId(int vkUserId) {
        this.vkUserId = vkUserId;
    }
}
