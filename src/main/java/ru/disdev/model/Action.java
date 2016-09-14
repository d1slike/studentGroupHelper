package ru.disdev.model;

import org.telegram.telegrambots.api.objects.Message;

import java.util.function.Consumer;

public class Action {
    private final Consumer<Message> messageConsumer;
    private final String informationForUser;

    public Action(Consumer<Message> messageConsumer, String informationForUser) {
        this.messageConsumer = messageConsumer;
        this.informationForUser = informationForUser;
    }

    public Consumer<Message> getMessageConsumer() {
        return messageConsumer;
    }

    public String getInformationForUser() {
        return informationForUser;
    }
}
