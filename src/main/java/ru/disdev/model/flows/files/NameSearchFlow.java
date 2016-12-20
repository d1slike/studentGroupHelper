package ru.disdev.model.flows.files;

import org.telegram.telegrambots.api.objects.Message;
import ru.disdev.bot.MessageConst;
import ru.disdev.bot.TelegramKeyBoards;
import ru.disdev.entity.FileFilter;
import ru.disdev.entity.Prototype;
import ru.disdev.model.Action;
import ru.disdev.model.StateActionMap;
import ru.disdev.model.flows.Flow;

@Prototype
public class NameSearchFlow extends Flow<FileFilter> {
    public NameSearchFlow(long chatId) {
        super(chatId);
    }

    @Override
    protected FileFilter buildResult() {
        return new FileFilter();
    }

    private void getFilter(Message message) {
        if (message.hasText()) {
            String text = message.getText();
            if (text.equals(MessageConst.CANCEL)) {
                finish();
            } else {
                result.setValue(text);
                finish();
            }
        } else {
            sendMessage("Введите имя файла");
        }
    }

    @Override
    protected StateActionMap fillStateActions(StateActionMap map) {
        return map.next(new Action(this::getFilter, "Введите имя файла", TelegramKeyBoards.cancelButton()));
    }
}
