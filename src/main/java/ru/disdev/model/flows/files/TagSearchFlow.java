package ru.disdev.model.flows.files;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.disdev.bot.MessageConst;
import ru.disdev.bot.TelegramKeyBoards;
import ru.disdev.entity.Prototype;
import ru.disdev.entity.wrappers.StringWrapper;
import ru.disdev.model.Action;
import ru.disdev.model.StateActionMap;
import ru.disdev.model.flows.Flow;
import ru.disdev.service.TeacherService;

@Prototype
public class TagSearchFlow extends Flow<StringWrapper> {

    @Autowired
    private TeacherService service;

    public TagSearchFlow(long chatId, Runnable onDone) {
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
                cancel(TelegramKeyBoards.storageKeyboard());
            } else {
                result.setValue(text);
                finish();
            }
        } else {
            sendMessage("Выберите предмет");
        }
    }

    @Override
    protected StateActionMap fillStateActions(StateActionMap map) {
        ReplyKeyboardMarkup markup = TelegramKeyBoards.makeColumnKeyBoard(true, service.getSubjectTags());
        TelegramKeyBoards.addLast(MessageConst.CANCEL, markup);
        return map.next(new Action(this::getFilter, "Выберите предмет", markup));
    }
}
