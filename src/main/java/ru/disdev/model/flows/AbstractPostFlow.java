package ru.disdev.model.flows;

import com.google.common.collect.ImmutableSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.disdev.bot.MessageConst;
import ru.disdev.bot.TelegramKeyBoards;
import ru.disdev.entity.Post;
import ru.disdev.model.Action;
import ru.disdev.model.StateActionMap;
import ru.disdev.service.TeacherService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ru.disdev.bot.TelegramKeyBoards.makeKeyboard;
import static ru.disdev.bot.TelegramKeyBoards.row;

public abstract class AbstractPostFlow<T extends Post> extends Flow<T> {

    private static final String REMOVE_TAGS = "Очистить";
    private static final List<String> INFORMATION_TYPE_TAGS = Arrays.asList("экзамен", "лаба", "семинар", "лекция");

    @Autowired
    private TeacherService teacherService;
    private String tags;

    public AbstractPostFlow(long chatId, Runnable onDone) {
        super(chatId, onDone);
    }

    @Override
    public abstract T buildResult();

    private ReplyKeyboard getTagKeyboard() {
        ImmutableSet<String> subjectTags = teacherService.getSubjectTags();
        List<KeyboardRow> rows = new ArrayList<>();
        rows.add(row(MessageConst.NEXT, REMOVE_TAGS, MessageConst.CANCEL));
        int i = 0;
        for (String subjectTag : subjectTags) {
            KeyboardRow keyboardRow = new KeyboardRow();
            keyboardRow.add(subjectTag);
            if (i < INFORMATION_TYPE_TAGS.size()) {
                keyboardRow.add(INFORMATION_TYPE_TAGS.get(i++));
            }
            rows.add(keyboardRow);
        }
        while (i < INFORMATION_TYPE_TAGS.size()) {
            rows.add(TelegramKeyBoards.row(INFORMATION_TYPE_TAGS.get(i++)));
        }
        return makeKeyboard(false, rows);
    }

    @Override
    protected StateActionMap fillStateActions(StateActionMap map) {
        return map.then(Action.of(this::getTag, "Выберите теги", getTagKeyboard()))
                .then(Action.of(this::getInformation, "Введите текст поста", TelegramKeyBoards.hideKeyBoard()));
    }

    @Override
    protected ReplyKeyboard getKeyboardAfterFinish() {
        return TelegramKeyBoards.mainKeyBoard();
    }

    private void getTag(Message message) {
        if (message.hasText()) {
            String text = message.getText();
            switch (text) {
                case MessageConst.NEXT:
                    if (tags == null || tags.isEmpty()) {
                        sendMessage("Добавьте хотябы один тег!");
                        return;
                    }
                    getResult().setTags(tags);
                    nextState();
                    break;
                case REMOVE_TAGS:
                    tags = null;
                    sendMessage("Очищено");
                    break;
                default:
                    tags = tags == null ? text : tags.concat(",").concat(text.trim());
                    sendMessage("Теги: " + tags);
                    break;
            }
        } else {
            sendMessage("Введите текст");
        }
    }

    private void getInformation(Message message) {
        if (message.hasText()) {
            String text = message.getText();
            getResult().setText(text);
            finish();
        } else {
            sendMessage("Введите текст");
        }
    }

}
