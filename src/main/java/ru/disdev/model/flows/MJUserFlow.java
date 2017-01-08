package ru.disdev.model.flows;

import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import ru.disdev.bot.TelegramKeyBoards;
import ru.disdev.entity.mj.MJUser;
import ru.disdev.model.Action;
import ru.disdev.model.Prototype;
import ru.disdev.model.StateActionMap;

@Prototype
public class MJUserFlow extends Flow<MJUser> {
    public MJUserFlow(long chatId, Runnable onDone) {
        super(chatId, onDone);
    }

    @Override
    protected MJUser buildResult() {
        return new MJUser();
    }

    @Override
    protected StateActionMap fillStateActions(StateActionMap map) {
        return map.then(Action.of(this::getLogin, "Введите номер Вашего студенческого билета", TelegramKeyBoards.cancelButton()))
                .then(Action.of(this::getPassword, "Введите Ваш пароль"));
    }

    private void getLogin(Message message) {
        if (message.hasText()) {
            getResult().setLogin(message.getText().trim());
            toNextState();
        }
    }

    private void getPassword(Message message) {
        if (message.hasText()) {
            getResult().setPassword(message.getText().trim());
            finish();
        }
    }

    @Override
    protected ReplyKeyboard getKeyboardAfterFinish() {
        return TelegramKeyBoards.modulesKeyboard();
    }
}
