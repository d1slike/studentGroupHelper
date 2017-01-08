package ru.disdev.util;

import com.google.common.collect.ImmutableMap;
import ru.disdev.bot.Emoji;
import ru.disdev.entity.mail.MailMessage;
import ru.disdev.service.StorageService;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeUtility;
import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.function.Consumer;

public class MailUtils {

    public static MailMessage handleMailMessage(Message message,
                                                ImmutableMap<String, String> tagMap,
                                                Consumer<Throwable> mailAttachmentErrorHandler) throws MessagingException, IOException {
        String from = message.getFrom()[0].toString();
        String name = "";
        String email = "";
        String tag;
        if (from.startsWith("=?")) {
            StringTokenizer tokenizer = new StringTokenizer(from, "<");
            name = tokenizer.nextToken();
            email = tokenizer.nextToken();
            email = email.substring(0, email.length() - 1).trim();
            tag = tagMap.containsKey(email) ? tagMap.get(email) : "";
            try {
                name = MimeUtility.decodeWord(name);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            from = from.trim();
            tag = tagMap.containsKey(from) ? tagMap.get(from) : "";
        }

        String subject = message.getSubject();
        StringBuilder builder = new StringBuilder(Emoji.WARNING)
                .append("<b>Новое сообщение на почте группы:</b>\n");
        if (!tag.isEmpty()) {
            builder.append("#").append(tag).append("@idb1409group\n");
        }

        builder.append("От: ").append(from.startsWith("=?") ? name + email : from).append("\n")
                .append("Тема: ").append(subject == null || subject.isEmpty()
                ? "Без темы" : subject).append("\n")
                .append("---");
        String content = builder.toString();
        MailMessage mailMessage = new MailMessage();
        mailMessage.setMessage(content);
        mailMessage.setTag(tag.isEmpty() ? StorageService.UNDEFINED_TAG : tag);
        try {
            Object body = message.getContent();
            if (body != null && body instanceof Multipart) {
                Multipart multipart = (Multipart) body;
                for (int i = 0; i < multipart.getCount(); i++) {
                    try {
                        MimeBodyPart bodyPart = (MimeBodyPart) multipart.getBodyPart(i);
                        if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                            String fileName = bodyPart.getFileName();
                            fileName = fileName.startsWith("=?")
                                    ? MimeUtility.decodeWord(bodyPart.getFileName())
                                    : fileName;
                            File file = new File(StorageService.MAIL_TEMP_DIR + fileName);
                            bodyPart.saveFile(file);
                            mailMessage.getAttachments().add(file);
                        }
                    } catch (Exception ex) {
                        mailAttachmentErrorHandler.accept(ex);
                    }
                }
            }
        } catch (Exception ex) {
            mailAttachmentErrorHandler.accept(ex);
        }
        return mailMessage;
    }
}
