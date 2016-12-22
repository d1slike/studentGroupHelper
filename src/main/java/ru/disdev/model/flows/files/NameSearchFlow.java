package ru.disdev.model.flows.files;

import org.telegram.telegrambots.api.objects.Message;
import ru.disdev.bot.MessageConst;
import ru.disdev.entity.Prototype;
import ru.disdev.entity.wrappers.StringWrapper;
import ru.disdev.model.Action;
import ru.disdev.model.StateActionMap;
import ru.disdev.model.flows.Flow;

import static ru.disdev.bot.TelegramKeyBoards.cancelButton;
import static ru.disdev.bot.TelegramKeyBoards.storageKeyboard;

@Prototype
public class NameSearchFlow extends Flow<StringWrapper> {

    public NameSearchFlow(long chatId, Runnable onDone) {
        super(chatId, onDone);
    }

    @Override
    protected StringWrapper buildResult() {
        return new StringWrapper();
    }

    private void getFilter(Message message) {
        if (message.hasText()) {
            String text = message.getText();
            if (text.equals(MessageConst.CANCEL)) {
                cancel(storageKeyboard());
            } else {
                result.setValue(text);
                sendKeyboard(storageKeyboard());
                finish();
            }
        } else {
            sendMessage("Введите имя файла");
        }
    }

    @Override
    protected StateActionMap fillStateActions(StateActionMap map) {
        return map.next(new Action(this::getFilter, "Введите имя файла", cancelButton()));
    }
}
