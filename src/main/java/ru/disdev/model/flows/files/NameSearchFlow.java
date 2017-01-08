package ru.disdev.model.flows.files;

import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import ru.disdev.model.Action;
import ru.disdev.model.Prototype;
import ru.disdev.model.StateActionMap;
import ru.disdev.model.flows.Flow;

import java.util.concurrent.ScheduledFuture;

import static ru.disdev.bot.TelegramKeyBoards.cancelButton;
import static ru.disdev.bot.TelegramKeyBoards.storageKeyboard;

@Prototype
public class NameSearchFlow extends Flow<String> {

    public NameSearchFlow(long chatId, ScheduledFuture<?> cancelTask) {
        super(chatId, cancelTask);
    }

    @Override
    protected String buildResult() {
        return "";
    }

    private void getFilter(Message message) {
        if (message.hasText()) {
            String text = message.getText();
            updateResult(text);
            finish();
        } else {
            sendMessage("Введите имя файла");
        }
    }

    @Override
    protected StateActionMap fillStateActions(StateActionMap map) {
        return map.then(Action.of(this::getFilter, "Введите имя файла", cancelButton()));
    }

    @Override
    protected ReplyKeyboard getKeyboardAfterFinish() {
        return storageKeyboard();
    }
}
