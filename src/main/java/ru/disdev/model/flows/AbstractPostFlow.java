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

import static ru.disdev.bot.TelegramKeyBoards.makeKeyBoard;
import static ru.disdev.bot.TelegramKeyBoards.row;

public abstract class AbstractPostFlow<T extends Post> extends Flow<T> {

    private static final String REMOVE_TAGS = "Очистить";
    private static final List<String> INFORMATION_TYPE_TAGS = Arrays.asList("экзамен", "лаба", "семинар", "лекция");

    @Autowired
    private TeacherService teacherService;
    private List<String> tags = new ArrayList<>();

    public AbstractPostFlow(long chatId, Runnable onDone) {
        super(chatId, onDone);
    }

    @Override
    public abstract T buildResult();

    @Override
    protected StateActionMap fillStateActions(StateActionMap map) {
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
        return map.next(new Action(this::getTag, "Выберите теги", makeKeyBoard(false, rows)))
                .next(new Action(this::getInformation, "Введите текст поста", TelegramKeyBoards.hideKeyBoard()));
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
                    if (tags.isEmpty()) {
                        sendMessage("Добавьте хотябы один тег!");
                        return;
                    }
                    result.setTags(String.join(",", tags));
                    nextState();
                    break;
                case REMOVE_TAGS:
                    tags.clear();
                    sendMessage("Очищено");
                    break;
                default:
                    tags.add(text);
                    sendMessage("Теги: " + String.join(",", tags));
                    break;
            }
        } else {
            sendMessage("Введите текст");
        }
    }

    private void getInformation(Message message) {
        if (message.hasText()) {
            String text = message.getText();
            result.setText(text);
            finish();
        } else {
            sendMessage("Введите текст");
        }
    }

}
