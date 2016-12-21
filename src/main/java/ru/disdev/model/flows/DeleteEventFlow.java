package ru.disdev.model.flows;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.disdev.bot.MessageConst;
import ru.disdev.bot.TelegramKeyBoards;
import ru.disdev.entity.Prototype;
import ru.disdev.entity.wrappers.IntWrapper;
import ru.disdev.model.Action;
import ru.disdev.model.StateActionMap;
import ru.disdev.service.EventService;

import java.util.Set;
import java.util.stream.Collectors;

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

    private void getId(Message message) {
        if (message.hasText()) {
            String text = message.getText();
            if (text.equals(MessageConst.CANCEL)) {
                sendKeyboard(TelegramKeyBoards.eventKeyboard());
                finish();
            } else {
                int value = -1;
                try {
                    value = Integer.parseInt(text);
                } catch (Exception ex) {
                    sendMessage("Неверно указан идентификатор");
                }
                if (value > 0) {
                    result.setValue(value);
                    finish();
                }
            }
        }
    }

    @Override
    protected StateActionMap fillStateActions(StateActionMap map) {
        Set<String> ids = eventService.findAll().stream()
                .map(event -> event.getId() + "")
                .collect(Collectors.toSet());
        ReplyKeyboardMarkup markup = TelegramKeyBoards.makeColumnKeyBoard(true, ids);
        TelegramKeyBoards.addLast(MessageConst.CANCEL, markup);
        return map.next(new Action(this::getId, "Введите идектификатор события", markup));
    }
}
