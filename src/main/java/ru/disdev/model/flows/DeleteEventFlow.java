package ru.disdev.model.flows;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.disdev.bot.MessageConst;
import ru.disdev.bot.TelegramKeyBoards;
import ru.disdev.entity.Prototype;
import ru.disdev.model.Action;
import ru.disdev.model.StateActionMap;
import ru.disdev.service.EventService;

import java.util.Set;
import java.util.stream.Collectors;

@Prototype
public class DeleteEventFlow extends Flow<Integer> {

    @Autowired
    private EventService eventService;

    public DeleteEventFlow(long chatId, Runnable onDone) {
        super(chatId, onDone);
    }

    @Override
    protected Integer buildResult() {
        return -1;
    }

    private void getId(Message message) {
        if (message.hasText()) {
            String text = message.getText();
            int value = -1;
            try {
                value = Integer.parseInt(text);
            } catch (Exception ex) {
                sendMessage("Неверно указан идентификатор");
            }
            if (value > 0) {
                updateResult(value);
                finish();
            }
        }
    }

    @Override
    protected StateActionMap fillStateActions(StateActionMap map) {
        Set<String> ids = eventService.findAll().stream()
                .map(event -> event.getId() + "")
                .collect(Collectors.toSet());
        ReplyKeyboardMarkup markup = TelegramKeyBoards.makeOneColumnKeyboard(true, ids);
        TelegramKeyBoards.addLast(MessageConst.CANCEL, markup);
        return map.then(Action.of(this::getId, "Введите идентификатор события", markup));
    }

    @Override
    protected ReplyKeyboard getKeyboardAfterFinish() {
        return TelegramKeyBoards.eventKeyboard();
    }
}
