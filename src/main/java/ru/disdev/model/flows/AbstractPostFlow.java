package ru.disdev.model.flows;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.api.objects.Message;
import ru.disdev.bot.TelegramKeyBoards;
import ru.disdev.entity.Post;
import ru.disdev.model.Action;
import ru.disdev.model.StateActionMap;
import ru.disdev.service.TeacherService;

import java.util.function.Consumer;

public abstract class AbstractPostFlow<T extends Post> extends Flow<T> {
    public AbstractPostFlow(long chatId) {
        super(chatId);
    }

    @Autowired
    private TeacherService teacherService;

    @Override
    public abstract T buildResult();

    @Override
    protected StateActionMap fillStateActions(StateActionMap map) {
        return map.next(new Action(getTag(), "Выберите теги", TelegramKeyBoards.tagListKeyboard(teacherService.getSubjectTags())))
                .next(new Action(getInformation(), "Введите текст поста", TelegramKeyBoards.hideKeyBoard()));
    }

    private Consumer<Message> getTag() {
        return (message -> {
            if (message.hasText()) {
                String text = message.getText();
                if (text.equals("Далее")) {
                    nextState();
                } else {
                    String tag = result.getTags();
                    if (tag == null) {
                        result.setTags(text);
                    } else {
                        result.setTags(tag.concat(",").concat(text));
                    }
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
