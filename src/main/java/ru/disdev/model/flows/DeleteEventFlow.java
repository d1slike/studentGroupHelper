package ru.disdev.model.flows;

import org.springframework.beans.factory.annotation.Autowired;
import ru.disdev.entity.Prototype;
import ru.disdev.entity.wrappers.IntWrapper;
import ru.disdev.model.StateActionMap;
import ru.disdev.service.EventService;

@Prototype
public class DeleteEventFlow extends Flow<IntWrapper> {

    @Autowired
    private EventService eventService;

    public DeleteEventFlow(long chatId) {
        super(chatId);
    }

    @Override
    protected IntWrapper buildResult() {
        return new IntWrapper();
    }

    @Override
    protected StateActionMap fillStateActions(StateActionMap map) {
        return null;
    }
}
