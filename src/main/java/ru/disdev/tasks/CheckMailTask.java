package ru.disdev.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.disdev.VkApi;
import ru.disdev.VkGroupBot;
import ru.disdev.entity.DateTime;
import ru.disdev.repository.DateTimeRepository;

import javax.annotation.PostConstruct;
import javax.mail.*;
import javax.mail.internet.MimeUtility;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class CheckMailTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckMailTask.class);

    @Autowired
    private ScheduledExecutorService executorService;
    @Autowired
    private VkApi vkApi;
    @Autowired
    private VkGroupBot groupBot;
    @Autowired
    private DateTimeRepository repository;

    private volatile Date lastCheckedDate;

    @PostConstruct
    private void init() {
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
            try {
                Session session = getSession();
                store = session.getStore("imap");
                store.connect();
                folder = store.getFolder("INBOX");
                folder.open(Folder.READ_ONLY);
                for (Message message : folder.getMessages()) {
                    Date date = message.getReceivedDate();
                    if (date == null) {
                        continue;
                    }
                    if (date.before(lastCheckedDate) || date.equals(lastCheckedDate)) {
                        continue;
                    }

                    String notification = messageNotification(message);
                    groupBot.announceToGroup(notification);
                    vkApi.announceMessage(notification);
                    lastCheckedDate = message.getReceivedDate();
                }
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

                repository.save(new DateTime(lastCheckedDate));
            }
        }, 1, 5, TimeUnit.MINUTES);
    }

    private String messageNotification(Message message) throws MessagingException {
        String from = message.getFrom()[0].toString();
        String name = "";
        String email = "";
        if (from.startsWith("=?")) {
            StringTokenizer tokenizer = new StringTokenizer(from, "<");
            name = tokenizer.nextToken();
            email = tokenizer.nextToken();
            email = email.substring(0, email.length() - 1);
            try {
                name = MimeUtility.decodeWord(name);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        String subject = message.getSubject();
        return new StringBuilder("Новое сообщение на почте группы\n\n")
                .append("От: ").append(from.startsWith("=?") ? name + email : from).append("\n")
                .append("Тема: ").append(subject == null || subject.isEmpty()
                        ? "Без темы" : subject).append("\n")
                .append("---").toString();
    }

    private Session getSession() {
        return Session.getDefaultInstance(getProperties(), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("", "");
            }
        });
    }

    private Properties getProperties() {
        Properties properties = new Properties();
        properties.put("mail.imap.host", "imap.yandex.ru");
        properties.put("mail.imap.port", 993);
        properties.put("mail.imap.ssl.enable", true);
        properties.put("mail.imap.starttls.enable", true);
        properties.put("mail.imap.auth", true);
        return properties;
    }
}
