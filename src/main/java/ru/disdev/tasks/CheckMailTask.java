package ru.disdev.tasks;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.disdev.api.VkApi;
import ru.disdev.bot.TelegramBot;
import ru.disdev.entity.MailMessage;
import ru.disdev.entity.mail.DateTime;
import ru.disdev.repository.DateTimeRepository;
import ru.disdev.service.StorageService;
import ru.disdev.service.TeacherService;
import ru.disdev.util.MailUtils;

import javax.annotation.PostConstruct;
import javax.mail.*;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class CheckMailTask {

    private static final Logger LOGGER = Logger.getLogger(CheckMailTask.class);

    @Autowired
    private ScheduledExecutorService executorService;
    @Autowired
    private VkApi vkApi;
    @Autowired
    private TelegramBot groupBot;
    @Autowired
    private DateTimeRepository repository;
    @Autowired
    private TeacherService teacherService;
    @Autowired
    private StorageService storageService;

    @Value("${groupmail.login}")
    private String login;
    @Value("${groupmail.password}")
    private String password;

    private volatile Date lastCheckedDate;

    @PostConstruct
    private void init() throws IOException {
        DateTime dateTime = repository.findOne(1);
        if (dateTime == null) {
            lastCheckedDate = new Date();
            repository.save(new DateTime(lastCheckedDate));
        } else {
            lastCheckedDate = dateTime.getDate();
        }
        executorService.scheduleAtFixedRate(() -> {
            Folder folder = null;
            Store store = null;
            boolean needUpdate = false;
            try {
                Session session = getSession();
                store = session.getStore("imap");
                store.connect();
                folder = store.getFolder("INBOX");
                folder.open(Folder.READ_ONLY);
                int end = folder.getMessageCount();
                int start = Math.max(1, end - 3);
                for (Message message : folder.getMessages(start, end)) {
                    Date date = message.getReceivedDate();
                    if (date == null) {
                        continue;
                    }
                    if (date.before(lastCheckedDate) || date.equals(lastCheckedDate)) {
                        continue;
                    }

                    MailMessage mailMessage = MailUtils.handleMailMessage(message,
                            teacherService.getTeacherEmailTagLinks(),
                            throwable -> LOGGER.error("Error while getting attachments from mail", throwable));
                    List<File> attachments = mailMessage.getAttachments();
                    if (!attachments.isEmpty()) {
                        storageService.collectMailAttachments(attachments, mailMessage.getTag());
                    }
                    groupBot.announceToGroup(mailMessage.getMessage());
                    vkApi.sendMessage(mailMessage.getMessage());
                    lastCheckedDate = message.getReceivedDate();
                    needUpdate = true;
                }
            } catch (MessagingException ignore) {
                LOGGER.warn(ignore.getMessage());
            } catch (Exception e) {
                LOGGER.error("Error", e);
            } finally {
                try {
                    if (folder != null) {
                        folder.close(true);
                    }
                    if (store != null) {
                        store.close();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if (needUpdate) {
                    repository.save(new DateTime(lastCheckedDate));
                }
            }
        }, 1, 5, TimeUnit.MINUTES);

    }

    private Session getSession() {
        return Session.getDefaultInstance(getProperties(), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(login, password);
            }
        });
    }

    private Properties getProperties() {
        Properties properties = new Properties();
        properties.put("mail.imap.host", "imap.yandex.ru");
        properties.put("mail.imap.port", 993);
        properties.put("mail.imap.ssl.enable", true);
        //properties.put("mail.imap.starttls.enable", true);
        properties.put("mail.imap.auth", true);
        return properties;
    }
}
