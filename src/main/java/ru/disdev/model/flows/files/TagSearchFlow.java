package ru.disdev.model.flows.files;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.disdev.bot.MessageConst;
import ru.disdev.bot.TelegramKeyBoards;
import ru.disdev.entity.Prototype;
import ru.disdev.model.Action;
import ru.disdev.model.StateActionMap;
import ru.disdev.model.flows.Flow;
import ru.disdev.service.TeacherService;

@Prototype
public class TagSearchFlow extends Flow<String> {

    @Autowired
    private TeacherService service;

    public TagSearchFlow(long chatId, Runnable onDone) {
        super(chatId, onDone);
    }

    @Override
    protected String buildResult() {
        return "";
    }

    private void getFilter(Message message) {
        if (message.hasText()) {
            String text = message.getText();
            updateResult(text.toLowerCase());
            finish();
        } else {
            sendMessage("Выберите предмет");
        }
    }

    @Override
    protected StateActionMap fillStateActions(StateActionMap map) {
        ReplyKeyboardMarkup markup = TelegramKeyBoards.makeOneColumnKeyboard(true, service.getSubjectTags());
        TelegramKeyBoards.addLast(MessageConst.CANCEL, markup);
        return map.then(Action.of(this::getFilter, "Выберите предмет", markup));
    }

    @Override
    protected ReplyKeyboard getKeyboardAfterFinish() {
        return TelegramKeyBoards.storageKeyboard();
    }
}
