package ru.disdev.model.flows;

import com.google.common.collect.ImmutableSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.api.objects.Document;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.PhotoSize;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.disdev.bot.MessageConst;
import ru.disdev.entity.post.Post;
import ru.disdev.entity.post.TelegramAttachment;
import ru.disdev.model.Action;
import ru.disdev.model.StateActionMap;
import ru.disdev.service.OptionsService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static ru.disdev.bot.TelegramKeyBoards.*;

public abstract class AbstractPostFlow<T extends Post> extends Flow<T> {

    private static final String CLEAR = "Очистить";
    private static final String BACK = "Вернуться";
    private static final String ATTACH_NAME_MESSAGE = "Введите название для вложения";
    private static final List<String> INFORMATION_TYPE_TAGS = Arrays.asList("экзамен", "лаба", "семинар", "лекция");
    private static final ReplyKeyboard ATTACHMENTS_KEYBOARD = makeKeyboard(false, rows(row(MessageConst.NEXT, CLEAR, MessageConst.CANCEL)));
    private static final ReplyKeyboard ATTACH_NAME_KEYBOARD = makeKeyboard(true, rows(row(BACK, MessageConst.CANCEL)));
    private static final Comparator<PhotoSize> PHOTO_SIZE_COMPARATOR = (a, b) -> a.getFileSize() - b.getFileSize();
    private static final int MAX_ATTACHMENTS = 10;

    @Autowired
    private OptionsService optionsService;
    private String tags;
    private List<TelegramAttachment> attachments = new ArrayList<>();
    private TelegramAttachment currentAttachment;

    public AbstractPostFlow(long chatId, Runnable onDone) {
        super(chatId, onDone);
    }

    @Override
    public abstract T buildResult();

    private ReplyKeyboard getTagKeyboard() {
        ImmutableSet<String> subjectTags = optionsService.getSubjectTags();
        List<KeyboardRow> rows = new ArrayList<>();
        rows.add(row(MessageConst.NEXT, CLEAR, MessageConst.CANCEL));
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
            rows.add(row(INFORMATION_TYPE_TAGS.get(i++)));
        }
        return makeKeyboard(false, rows);
    }

    @Override
    protected StateActionMap fillStateActions(StateActionMap map) {
        return map.then(Action.of(this::getTag, "Выберите теги", getTagKeyboard()))
                .then(Action.of(this::getInformation, "Введите текст поста", hideKeyBoard()))
                .then(Action.of(this::getAttachment, "Добавьте вложения", ATTACHMENTS_KEYBOARD))
                .then(Action.of(this::getAttachmentName, ATTACH_NAME_MESSAGE, ATTACH_NAME_KEYBOARD));
    }

    @Override
    protected ReplyKeyboard getKeyboardAfterFinish() {
        return mainKeyBoard();
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
                    toNextState();
                    break;
                case CLEAR:
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
            toNextState();
        } else {
            sendMessage("Введите текст");
        }
    }

    private String getAttachmentsInfo() {
        StringBuilder builder = new StringBuilder("Вложения:\n");
        if (attachments.isEmpty()) {
            builder.append("Нет вложений");
        } else {
            attachments.forEach(attachment -> builder.append(attachment.getName())
                    .append(attachment.getFileExtension())
                    .append("\n"));
        }
        return builder.toString();
    }

    private void getAttachment(Message message) {
        if (message.hasText()) {
            switch (message.getText()) {
                case MessageConst.NEXT:
                    getResult().setAttachments(attachments);
                    finish();
                    break;
                case CLEAR:
                    attachments.clear();
                    sendMessage(getAttachmentsInfo());
                    break;
            }
        }

        if ((message.hasPhoto() || message.hasDocument())
                && attachments.size() >= MAX_ATTACHMENTS) {
            sendMessage("Максимальное число вложений: " + MAX_ATTACHMENTS);
            return;
        }

        if (message.hasPhoto()) {
            List<PhotoSize> photos = message.getPhoto();
            if (!photos.isEmpty()) {
                photos.sort(PHOTO_SIZE_COMPARATOR.reversed());
                PhotoSize photo = photos.get(0);
                currentAttachment = new TelegramAttachment(photo.getFileId(), ".jpg");
                toNextState();
            }
        } else if (message.hasDocument()) {
            Document document = message.getDocument();
            String fileName = document.getFileName();
            String extension = "";
            if (fileName != null) {
                extension = fileName.substring(fileName.lastIndexOf("."));
            }
            currentAttachment = new TelegramAttachment(document.getFileId(), extension);
            toNextState();
        }
    }

    private void getAttachmentName(Message message) {
        if (message.hasText()) {
            String text = message.getText().trim();
            switch (text) {
                case BACK:
                    currentAttachment = null;
                    toPreviousState();
                    break;
                default:
                    currentAttachment.setName(text);
                    attachments.add(currentAttachment);
                    currentAttachment = null;
                    sendMessage(getAttachmentsInfo());
                    toPreviousState();
                    break;
            }
        } else {
            sendMessage(ATTACH_NAME_MESSAGE);
        }
    }

}
