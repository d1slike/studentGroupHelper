package ru.disdev.model.flows;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.disdev.bot.MessageConst;
import ru.disdev.bot.TelegramKeyBoards;
import ru.disdev.entity.Post;
import ru.disdev.model.Action;
import ru.disdev.model.StateActionMap;
import ru.disdev.service.TeacherService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractPostFlow<T extends Post> extends Flow<T> {
    public AbstractPostFlow(long chatId) {
        super(chatId);
    }

    @Autowired
    private TeacherService teacherService;
    private List<String> tags = new ArrayList<>();

    @Override
    public abstract T buildResult();

    @Override
    protected StateActionMap fillStateActions(StateActionMap map) {
        ReplyKeyboardMarkup markup =
                TelegramKeyBoards.makeColumnKeyBoard(true, teacherService.getSubjectTags());
        TelegramKeyBoards.addFirst(MessageConst.NEXT, markup);
        return map.next(new Action(getTag(), "Выберите теги", markup))
                .next(new Action(getInformation(), "Введите текст поста", TelegramKeyBoards.hideKeyBoard()));
    }

    private Consumer<Message> getTag() {
        return (message -> {
            if (message.hasText()) {
                String text = message.getText();
                if (text.equals(MessageConst.NEXT)) {
                    result.setTags(String.join(",", tags));
                    nextState();
                } else {
                    tags.add(text);
                    sendMessage("Теги: " + String.join(",", tags));
                }
            } else
                sendMessage("Введите текст");
        });
    }

    private Consumer<Message> getInformation() {
        return message -> {
            if (message.hasText()) {
                String text = message.getText();
                result.setText(text);
                finish();
            } else
                sendMessage("Введите текст");
        };
    }

}
