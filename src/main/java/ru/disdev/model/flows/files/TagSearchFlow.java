package ru.disdev.model.flows.files;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.disdev.bot.MessageConst;
import ru.disdev.bot.TelegramKeyBoards;
import ru.disdev.model.Action;
import ru.disdev.model.Prototype;
import ru.disdev.model.StateActionMap;
import ru.disdev.model.flows.Flow;
import ru.disdev.service.OptionsService;

import java.util.concurrent.ScheduledFuture;

@Prototype
public class TagSearchFlow extends Flow<String> {

    @Autowired
    private OptionsService optionsService;

    public TagSearchFlow(long chatId, ScheduledFuture<?> cancelTask) {
        super(chatId, cancelTask);
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
        ReplyKeyboardMarkup markup = TelegramKeyBoards.makeOneColumnKeyboard(true, optionsService.getSubjectTags());
        TelegramKeyBoards.addLast(MessageConst.CANCEL, markup);
        return map.then(Action.of(this::getFilter, "Выберите предмет", markup));
    }

    @Override
    protected ReplyKeyboard getKeyboardAfterFinish() {
        return TelegramKeyBoards.storageKeyboard();
    }
}
